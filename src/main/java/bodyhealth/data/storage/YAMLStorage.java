package bodyhealth.data.storage;

import bodyhealth.config.Debug;
import bodyhealth.core.BodyHealth;
import bodyhealth.core.BodyPart;
import bodyhealth.data.DataManager;
import bodyhealth.data.Storage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class YAMLStorage implements Storage {

    private final File file;

    public YAMLStorage() {
        file = new File(DataManager.getDataFolder(), "bodyHealth.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Debug.logErr(e);
            }
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

        double head = health.getDouble(BodyPart.HEAD.name(), 100.0);
        double body = health.getDouble(BodyPart.TORSO.name(), 100.0);
        double arm_left = health.getDouble(BodyPart.ARM_LEFT.name(), 100.0);
        double arm_right = health.getDouble(BodyPart.ARM_RIGHT.name(), 100.0);
        double leg_left = health.getDouble(BodyPart.LEG_LEFT.name(), 100.0);
        double leg_right = health.getDouble(BodyPart.LEG_RIGHT.name(), 100.0);
        double foot_left = health.getDouble(BodyPart.FOOT_LEFT.name(), 100.0);
        double foot_right = health.getDouble(BodyPart.FOOT_RIGHT.name(), 100.0);

        return new BodyHealth(uuid, head, body, arm_left, arm_right, leg_left, leg_right, foot_left, foot_right);
    }

}
