package bodyhealth.util;

import bodyhealth.Main;
import bodyhealth.config.Debug;
import bodyhealth.core.BodyHealth;
import bodyhealth.core.BodyPart;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.potion.PotionEffect;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DebugUtils {

    public static File createDebugDump() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        File outputDir = new File(Main.getInstance().getDataFolder(), "output");
        if (!outputDir.exists() && !outputDir.mkdirs()) return null;

        File latestLog = new File("logs/latest.log");
        File configFile = new File(Main.getInstance().getDataFolder(), "config.yml");
        File systemFile, serverFile, playerFile;
        try {
            systemFile = File.createTempFile("system_", ".yml", outputDir);
            serverFile = File.createTempFile("server_", ".yml", outputDir);
            playerFile = File.createTempFile("players_", ".yml", outputDir);
        } catch (IOException e) {
            Debug.logErr("Failed to create temp dump files: " + e.getMessage());
            return null;
        }

        YamlConfiguration systemDump = createSystemDump();
        YamlConfiguration serverDump = createServerDump();
        YamlConfiguration playerDump = createPlayerDump();
        try {
            systemDump.save(systemFile);
            serverDump.save(serverFile);
            playerDump.save(playerFile);
        } catch (IOException e) {
            Debug.logErr("Failed to save dump YAMLs: " + e.getMessage());
            return null;
        }

        File zipFile = new File(outputDir, "DebugDump_" + timestamp + ".zip");
        try (
            ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)))
        ) {
            BiConsumer<File, String> addFile = (file, entryName) -> {
                if (file != null && file.exists() && file.isFile()) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        zos.putNextEntry(new ZipEntry(entryName));
                        fis.transferTo(zos);
                        zos.closeEntry();
                    } catch (IOException ex) {
                        Debug.logErr("Failed to zip file: " + file.getName() + " -> " + ex.getMessage());
                    }
                }
            };

            addFile.accept(systemFile, "system.yml");
            addFile.accept(serverFile, "server.yml");
            addFile.accept(playerFile, "players.yml");
            addFile.accept(configFile, "config.yml");
            addFile.accept(latestLog, "latest.log");

        } catch (IOException e) {
            Debug.logErr("Failed to create debug ZIP: " + e.getMessage());
            return null;
        } finally {
            if (
                !systemFile.delete() ||
                !serverFile.delete() ||
                !playerFile.delete()
            ) {
                Debug.logErr("Failed to clean up temp files!");
            }
        }
        return zipFile;
    }

    private static YamlConfiguration createSystemDump() {
        YamlConfiguration system = new YamlConfiguration();
        system.set("version", 1);

        Properties sp = System.getProperties();
        Map<String, Object> sys = new LinkedHashMap<>();
        sys.put("java.version", sp.getProperty("java.version"));
        sys.put("java.vendor", sp.getProperty("java.vendor"));
        sys.put("java.vm.name", sp.getProperty("java.vm.name"));
        sys.put("os.name", sp.getProperty("os.name"));
        sys.put("os.arch", sp.getProperty("os.arch"));
        sys.put("os.version", sp.getProperty("os.version"));

        Runtime rt = Runtime.getRuntime();
        Map<String, Object> mem = new LinkedHashMap<>();
        mem.put("max", rt.maxMemory());
        mem.put("total", rt.totalMemory());
        mem.put("free", rt.freeMemory());
        mem.put("used", rt.totalMemory() - rt.freeMemory());
        sys.put("memory", mem);

        RuntimeMXBean mx = ManagementFactory.getRuntimeMXBean();
        sys.put("jvm.uptime.millis", mx.getUptime());
        sys.put("jvm.inputArgs", mx.getInputArguments());
        system.set("system", sys);

        return system;
    }

    private static YamlConfiguration createServerDump() {
        YamlConfiguration server = new YamlConfiguration();
        server.set("version", 1);

        // Server information
        server.set("server.name", Bukkit.getName());
        server.set("server.version", Bukkit.getVersion());
        server.set("server.bukkitVersion", Bukkit.getBukkitVersion());
        server.set("server.address", (Bukkit.getIp().isBlank() ?
                "localhost" : Bukkit.getIp()) + ":" + Bukkit.getPort());
        server.set("server.onlineMode", Bukkit.getServer().getOnlineMode());
        server.set("server.onlinePlayers", Bukkit.getOnlinePlayers().size());
        server.set("server.maxPlayers", Bukkit.getMaxPlayers());
        server.set("server.motd", Bukkit.getServer().getMotd());

        // Plugin information
        Map<String, Object> plugins = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
            PluginDescriptionFile d = p.getDescription();
            Map<String, Object> pInfo = new LinkedHashMap<>();
            pInfo.put("version", d.getVersion());
            pInfo.put("enabled", p.isEnabled());
            pInfo.put("main", d.getMain());
            if (!d.getAuthors().isEmpty()) pInfo.put("authors", d.getAuthors());
            if (d.getWebsite() != null) pInfo.put("website", d.getWebsite());
            if (!d.getDepend().isEmpty()) pInfo.put("depend", d.getDepend());
            if (!d.getSoftDepend().isEmpty()) pInfo.put("softdepend", d.getSoftDepend());
            if (!d.getLoadBefore().isEmpty()) pInfo.put("loadbefore", d.getLoadBefore());
            plugins.put(d.getName(), pInfo);
        }
        server.set("server.plugins", plugins);

        return server;
    }

    private static YamlConfiguration createPlayerDump() {
        YamlConfiguration players = new YamlConfiguration();
        players.set("version", 1);

        // Per player information
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();

            // Vanilla stats
            players.set("players." + uuid + ".name", player.getName());
            players.set("players." + uuid + ".displayName", player.getDisplayName());
            players.set("players." + uuid + ".locale", player.getLocale());
            players.set("players." + uuid + ".op", player.isOp());
            players.set("players." + uuid + ".ping", player.getPing());
            players.set("players." + uuid + ".dead", player.isDead());
            players.set("players." + uuid + ".health", player.getHealth());
            players.set("players." + uuid + ".maxHealth", player.getMaxHealth());

            // BodyHealth specific stats
            BodyHealth bodyhealth = BodyHealthUtils.getBodyHealth(player);
            for (BodyPart part : BodyPart.values()) {
                players.set("players." + uuid + ".bodyhealth.health." + part.name(), bodyhealth.getHealth(part));
            }
            Map<BodyPart, List<String[]>> ongoingEffects = bodyhealth.getOngoingEffects();
            for (Map.Entry<BodyPart, List<String[]>> entry : ongoingEffects.entrySet()) {
                List<String> serialized = new ArrayList<>();
                for (String[] array : entry.getValue()) {
                    serialized.add(String.join("/", array));
                }
                players.set("players." + uuid + ".bodyhealth.effects." + entry.getKey().name(), serialized);
            }
            List<String> permissions = new ArrayList<>();
            for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
                if (!info.getPermission().startsWith("bodyhealth.")) continue;
                if (info.getPermission().equals("bodyhealth.update-notify")) continue;
                permissions.add(info.getPermission() + ": " + info.getValue());
            }
            if (!permissions.isEmpty()) players.set("players." + uuid + ".bodyhealth.bypass-permissions", permissions);

            // Active attribute modifiers
            for (Attribute attribute : Attribute.values()) {
                AttributeInstance attributeInstance = player.getAttribute(attribute);
                if (attributeInstance == null) continue;
                for (AttributeModifier modifier : attributeInstance.getModifiers()) {
                    String key = "players." + uuid + ".attributes." + attribute.name() + ".modifiers." + modifier.getName();
                    players.set(key + ".operation", modifier.getOperation().name());
                    players.set(key + ".amount", modifier.getAmount());
                    //players.set(key + ".key", modifier.getKey());
                }
            }

            // Active potion effects
            for (PotionEffect effect : player.getActivePotionEffects()) {
                String key = "players." + uuid + ".effects." + effect.getType();
                players.set(key + ".amplifier",  effect.getAmplifier());
                players.set(key + ".duration",  effect.getDuration());
                players.set(key + ".particles",  effect.hasParticles());
                players.set(key + ".ambient",  effect.isAmbient());
                players.set(key + ".icon",  effect.hasIcon());
            }
        }
        return players;
    }

}
