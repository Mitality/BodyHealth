package bodyhealth.data.storage;

import bodyhealth.Main;
import bodyhealth.config.Debug;
import bodyhealth.core.BodyHealth;
import bodyhealth.core.BodyPart;
import bodyhealth.data.DataManager;
import bodyhealth.data.Storage;
import bodyhealth.data.StorageType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class YAMLStorage implements Storage {

    @Override
    public StorageType getType() {
        return StorageType.YAML;
    }

    private final File file;

    public YAMLStorage() {
        file = new File(DataManager.getDataFolder(), "bodyHealth.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Debug.logErr(e);
            }
        } else {
            try { // Migrate YAML storage
                String content = Files.readString(file.toPath());
                content = content.replaceAll("(?m)^(\\s*)BODY(\\s*:)", "$1TORSO$2");
                Files.writeString(file.toPath(), content);
            } catch (IOException e) {
                Debug.logErr(e);
            }
        }
    }

    @Override
    public boolean erase() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("players", new ArrayList<>());
        try {
            yaml.save(file);
            return true;
        } catch (IOException e) {
            Debug.logErr(e);
            return false;
        }
    }

    @Override
    public void saveBodyHealth(UUID uuid, BodyHealth bodyHealth) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        for (BodyPart part : BodyPart.values()) {
            double health = bodyHealth.getHealth(part);
            yaml.set("players." + uuid + ".health." + part.name(), health);
        }

        try {
            yaml.save(file);
        } catch (IOException e) {
            Debug.logErr(e);
        }
    }

    @Override
    public @NotNull BodyHealth loadBodyHealth(UUID uuid) {

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection players = yaml.getConfigurationSection("players");
        if (players == null) return new BodyHealth(uuid);

        ConfigurationSection player = players.getConfigurationSection(uuid.toString());
        if (player == null) return new BodyHealth(uuid);

        ConfigurationSection health = player.getConfigurationSection("health");
        if (health == null) return new BodyHealth(uuid);

        return getBodyHealth(uuid, health);
    }

    @Override
    public @NotNull Map<UUID, BodyHealth> loadAllBodyHealth() {

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection players = yaml.getConfigurationSection("players");
        if (players == null) return Map.of();

        Map<UUID, BodyHealth> map = new HashMap<>();
        for (String key : players.getKeys(false)) {

            ConfigurationSection player = players.getConfigurationSection(key);
            if (player == null) continue;

            ConfigurationSection health = player.getConfigurationSection("health");
            if (health == null) continue;

            try {
                UUID uuid = UUID.fromString(key);
                map.put(uuid, getBodyHealth(uuid, health));
            } catch (Exception ignored) {
            }
        }

        return map;
    }

    private BodyHealth getBodyHealth(UUID uuid, ConfigurationSection health) {

        double head = health.getDouble(BodyPart.HEAD.name(), 100.0);
        double torso = health.getDouble(BodyPart.TORSO.name(), 100.0);
        double arm_left = health.getDouble(BodyPart.ARM_LEFT.name(), 100.0);
        double arm_right = health.getDouble(BodyPart.ARM_RIGHT.name(), 100.0);
        double leg_left = health.getDouble(BodyPart.LEG_LEFT.name(), 100.0);
        double leg_right = health.getDouble(BodyPart.LEG_RIGHT.name(), 100.0);
        double foot_left = health.getDouble(BodyPart.FOOT_LEFT.name(), 100.0);
        double foot_right = health.getDouble(BodyPart.FOOT_RIGHT.name(), 100.0);

        return new BodyHealth(uuid, head, torso, arm_left, arm_right, leg_left, leg_right, foot_left, foot_right);
    }

    public static String dump(Map<UUID, BodyHealth> map, StorageType origin) {

        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<UUID, BodyHealth> entry : map.entrySet()) {
            for (BodyPart part : BodyPart.values()) {
                yaml.set("players." + entry.getKey() + ".health." + part.name(), entry.getValue().getHealth(part));
            }
        }

        File outDir = new File(Main.getInstance().getDataFolder(), "output");
        if (!outDir.exists() && !outDir.mkdirs()) {
            Debug.logErr(new IOException("Could not create output directory: " + outDir.getAbsolutePath()));
            return "";
        }

        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        File outFile = new File(outDir, origin.name() + "Dump_" + timestamp + ".yml");

        try {
            yaml.save(outFile);
        } catch (IOException e) {
            Debug.logErr(e);
            return "";
        }

        return "plugins/BodyHealth/output/" + outFile.getName();
    }

}
