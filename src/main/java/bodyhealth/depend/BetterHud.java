package bodyhealth.depend;

import bodyhealth.Main;
import bodyhealth.config.Config;
import bodyhealth.config.Debug;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kr.toxicity.hud.api.BetterHudAPI;
import kr.toxicity.hud.api.hud.Hud;
import kr.toxicity.hud.api.player.HudPlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class BetterHud {

    /**
     * Injects a working configuration into BetterHud to display health per BodyPart with a HUD
     * Also disables BetterHud's default stuff unless specified otherwise in BodyHealth's config
     * @throws IOException
     */
    public static void inject() throws IOException {
        final JavaPlugin instance = Main.getInstance();
        final File betterHudDataFolder = BetterHudAPI.inst().bootstrap().dataFolder();

        Debug.log("DataFolder: " + betterHudDataFolder.getAbsolutePath());

        String[] filesToCopy = { // Must be specified manually D:
                "assets/bodyhealth/bodyhealth_empty.png",
                "assets/bodyhealth/bodyhealth_broken.png",
                "assets/bodyhealth/bodyhealth_damaged.png",
                "assets/bodyhealth/bodyhealth_intermediate.png",
                "assets/bodyhealth/bodyhealth_nearlyfull.png",
                "assets/bodyhealth/bodyhealth_full.png",

                "assets/bodyhealth/bodyhealth_broken_head.png",
                "assets/bodyhealth/bodyhealth_broken_torso.png",
                "assets/bodyhealth/bodyhealth_broken_arm_left.png",
                "assets/bodyhealth/bodyhealth_broken_arm_right.png",
                "assets/bodyhealth/bodyhealth_broken_leg_left.png",
                "assets/bodyhealth/bodyhealth_broken_leg_right.png",
                "assets/bodyhealth/bodyhealth_broken_foot_left.png",
                "assets/bodyhealth/bodyhealth_broken_foot_right.png",

                "assets/bodyhealth/bodyhealth_damaged_head.png",
                "assets/bodyhealth/bodyhealth_damaged_torso.png",
                "assets/bodyhealth/bodyhealth_damaged_arm_left.png",
                "assets/bodyhealth/bodyhealth_damaged_arm_right.png",
                "assets/bodyhealth/bodyhealth_damaged_leg_left.png",
                "assets/bodyhealth/bodyhealth_damaged_leg_right.png",
                "assets/bodyhealth/bodyhealth_damaged_foot_left.png",
                "assets/bodyhealth/bodyhealth_damaged_foot_right.png",

                "assets/bodyhealth/bodyhealth_intermediate_head.png",
                "assets/bodyhealth/bodyhealth_intermediate_torso.png",
                "assets/bodyhealth/bodyhealth_intermediate_arm_left.png",
                "assets/bodyhealth/bodyhealth_intermediate_arm_right.png",
                "assets/bodyhealth/bodyhealth_intermediate_leg_left.png",
                "assets/bodyhealth/bodyhealth_intermediate_leg_right.png",
                "assets/bodyhealth/bodyhealth_intermediate_foot_left.png",
                "assets/bodyhealth/bodyhealth_intermediate_foot_right.png",

                "assets/bodyhealth/bodyhealth_nearlyfull_head.png",
                "assets/bodyhealth/bodyhealth_nearlyfull_torso.png",
                "assets/bodyhealth/bodyhealth_nearlyfull_arm_left.png",
                "assets/bodyhealth/bodyhealth_nearlyfull_arm_right.png",
                "assets/bodyhealth/bodyhealth_nearlyfull_leg_left.png",
                "assets/bodyhealth/bodyhealth_nearlyfull_leg_right.png",
                "assets/bodyhealth/bodyhealth_nearlyfull_foot_left.png",
                "assets/bodyhealth/bodyhealth_nearlyfull_foot_right.png",

                "assets/bodyhealth/bodyhealth_full_head.png",
                "assets/bodyhealth/bodyhealth_full_torso.png",
                "assets/bodyhealth/bodyhealth_full_arm_left.png",
                "assets/bodyhealth/bodyhealth_full_arm_right.png",
                "assets/bodyhealth/bodyhealth_full_leg_left.png",
                "assets/bodyhealth/bodyhealth_full_leg_right.png",
                "assets/bodyhealth/bodyhealth_full_foot_left.png",
                "assets/bodyhealth/bodyhealth_full_foot_right.png",

                "images/bodyhealth.yml",
                "layouts/bodyhealth.yml",
                "huds/bodyhealth.yml"
        };

        copySpecificFiles(filesToCopy, betterHudDataFolder, instance);
        handleDefaultCompassToggle(betterHudDataFolder);
        handleEntityPopupToggle(betterHudDataFolder);

        File configFile = new File(betterHudDataFolder, "config.yml");
        if (!configFile.exists()) {
            Debug.logErr("BetterHud config.yml not found in " + betterHudDataFolder.getAbsolutePath());
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        List<String> defaultHudList = config.getStringList("default-hud");

        if (!defaultHudList.contains("bodyhealth") && Config.display_betterhud_as_default) {
            Debug.log("Added BodyHealth's HUD as a default HUD to BetterHud");
            defaultHudList.add("bodyhealth");
        } else if (defaultHudList.contains("bodyhealth") && !Config.display_betterhud_as_default) {
            Debug.log("Removed BodyHealth's HUD from BetterHud's list of default HUDs");
            defaultHudList.remove("bodyhealth");
        }

        if (defaultHudList.contains("test_hud") && Config.display_betterhud_disable_default_hud) {
            Debug.log("Disabled BetterHud's default hud");
            defaultHudList.remove("test_hud");
        } else if (!defaultHudList.contains("test_hud") && !Config.display_betterhud_disable_default_hud) {
            Debug.log("Re-enabled BetterHud's default hud");
            defaultHudList.add("test_hud");
        }

        config.set("default-hud", defaultHudList);
        config.save(configFile);
    }

    /**
     * Adds necessary files to the BetterHud resource pack configured by BetterHud's config.yml
     * For pack-type folder: adds files into build-folder-location, then zips to BodyHealth/output/<name>.zip
     * For pack-type zip: opens the zip at build-folder-location, adds/replaces files inside it, saves it, then copies
     * it to BodyHealth/output/<name>.zip
     * @throws IOException
     */
    public static void add() throws IOException {
        BetterHudPackTarget target = readBetterHudPackTarget();

        if (target.packType == PackType.FOLDER) {
            addToPackFolder(target.buildLocation);
        } else {
            addToPackZip(target.buildLocation);
        }

        exportToBodyHealthOutputZip(target);
        Debug.log("Files added successfully");
    }

    /**
     * Enables or disables the BodyHealth HUD for a given player
     * @param player the player for whom the HUD should be enabled or disabled
     * @param enabled whether the HUD should be enabled or disabled
     * @return whether the operation was successful
     */
    public static boolean setBodyHealthHudEnabled(Player player, boolean enabled) {
        HudPlayer hudPlayer = BetterHudAPI.inst().getPlayerManager().getHudPlayer(player.getUniqueId());
        if (hudPlayer == null) return false;

        Hud hud = BetterHudAPI.inst().getHudManager().getHud("bodyhealth");
        if (hud == null) return false;

        return enabled ? hud.add(hudPlayer) : hud.remove(hudPlayer);
    }

    /**
     * Represents BetterHud's resource pack target location and type as configured in BetterHud/config.yml
     * @param buildLocation the folder or zip file path specified by build-folder-location
     * @param packType the interpreted pack type (folder or zip)
     */
    private record BetterHudPackTarget(File buildLocation, PackType packType) {}

    /**
     * Supported BetterHud pack types.
     */
    private enum PackType { FOLDER, ZIP }

    /**
     * Reads BetterHud's pack output settings from BetterHud/config.yml and resolves build-folder-location.
     * Relative paths are resolved against BetterHud's data folder.
     * @return the resolved pack target configuration
     */
    private static BetterHudPackTarget readBetterHudPackTarget() {
        File betterHudDataFolder = BetterHudAPI.inst().bootstrap().dataFolder();
        File configFile = new File(betterHudDataFolder, "config.yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(configFile);

        String locRaw = cfg.getString("build-folder-location", "build");
        String typeRaw = cfg.getString("pack-type", "folder");

        PackType packType = typeRaw.equalsIgnoreCase("zip") ? PackType.ZIP : PackType.FOLDER;
        File location = new File(locRaw);

        if (!location.isAbsolute()) {
            File pluginsDir = betterHudDataFolder.getParentFile();
            location = new File(pluginsDir, location.getPath());
        }

        if (packType == PackType.ZIP) {
            String path = location.getPath();
            if (!path.toLowerCase().endsWith(".zip")) {
                location = new File(path + ".zip");
            }
        }

        return new BetterHudPackTarget(location, packType);
    }

    /**
     * Patches a folder-based pack by adding/replacing pack.mcmeta and pack.png at the pack root
     * @param packFolder the folder configured as BetterHud's build-folder-location
     * @throws IOException
     */
    private static void addToPackFolder(File packFolder) throws IOException {
        if (!packFolder.exists()) packFolder.mkdirs();

        if (Config.display_betterhud_add_mcmeta) {
            mergePackMcmetaToFile(new File(packFolder, "pack.mcmeta"));
        }

        if (Config.display_betterhud_add_icon) {
            copyResourceToFile("BetterHudConfig/build/pack.png", new File(packFolder, "pack.png"));
        }
    }

    /**
     * Patches a zip-based pack by adding/replacing pack.mcmeta and pack.png at the zip root
     * The zip file is overwritten atomically via a temporary file
     * @param packZip the zip configured as BetterHud's build-folder-location
     * @throws IOException
     */
    private static void addToPackZip(File packZip) throws IOException {
        if (!packZip.exists()) {
            Debug.logErr("BetterHud pack zip not found: " + packZip.getAbsolutePath());
            return;
        }

        File tmp = File.createTempFile("bodyhealth-pack", ".zip");

        try (ZipFile existing = new ZipFile(packZip);
             FileOutputStream fos = new FileOutputStream(tmp);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            JsonObject mergedMcmeta = Config.display_betterhud_add_mcmeta ? mergePackMcmetaFromZip(existing) : null;

            Enumeration<? extends ZipEntry> entries = existing.entries();
            while (entries.hasMoreElements()) {
                ZipEntry e = entries.nextElement();
                String name = e.getName();

                boolean replacingMcmeta = Config.display_betterhud_add_mcmeta && "pack.mcmeta".equals(name);
                boolean replacingPng = Config.display_betterhud_add_icon && "pack.png".equals(name);
                if (replacingMcmeta || replacingPng) continue;

                ZipEntry out = new ZipEntry(name);
                out.setTime(e.getTime());
                zos.putNextEntry(out);
                try (InputStream is = existing.getInputStream(e)) {
                    is.transferTo(zos);
                }
                zos.closeEntry();
            }

            if (Config.display_betterhud_add_mcmeta && mergedMcmeta != null) {
                zos.putNextEntry(new ZipEntry("pack.mcmeta"));
                byte[] bytes = new GsonBuilder().setPrettyPrinting().create()
                        .toJson(mergedMcmeta).getBytes(StandardCharsets.UTF_8);
                zos.write(bytes);
                zos.closeEntry();
            }

            if (Config.display_betterhud_add_icon) {
                zos.putNextEntry(new ZipEntry("pack.png"));
                try (InputStream png = Main.getInstance().getResource("BetterHudConfig/build/pack.png")) {
                    if (png == null) {
                        Debug.logErr("Resource not found: BetterHudConfig/build/pack.png");
                    } else {
                        png.transferTo(zos);
                    }
                }
                zos.closeEntry();
            }
        }

        Files.move(tmp.toPath(), packZip.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Exports the patched pack to plugins/BodyHealth/output/<name>.zip
     * For folder packs, the folder is zipped. For zip packs, the zip is copied
     * @param target the pack target configuration
     * @throws IOException
     */
    private static void exportToBodyHealthOutputZip(BetterHudPackTarget target) throws IOException {
        if (!Config.display_betterhud_package_compress) return;

        String filename = Config.display_betterhud_package_filename;
        File outputDir = new File(Main.getInstance().getDataFolder(), "output");
        if (!outputDir.exists()) outputDir.mkdirs();

        File outZip = new File(outputDir, filename + ".zip");

        if (target.packType == PackType.FOLDER) {
            if (!target.buildLocation.exists()) {
                Debug.logErr("BetterHud pack folder does not exist: " + target.buildLocation.getAbsolutePath());
                return;
            }

            Debug.log("Zipping BetterHud pack folder to " + outZip.getPath());
            try (FileOutputStream fos = new FileOutputStream(outZip);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {
                zipFolder(target.buildLocation, "", zos);
            }
        } else {
            if (!target.buildLocation.exists()) {
                Debug.logErr("BetterHud pack zip does not exist: " + target.buildLocation.getAbsolutePath());
                return;
            }

            Debug.log("Copying BetterHud pack zip to " + outZip.getPath());
            Files.copy(target.buildLocation.toPath(), outZip.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Utility method to copy specific files from the plugin resources to BetterHud's data folder
     * @param filesToCopy the resource-relative files to copy
     * @param targetFolder the folder into which the files should be copied
     * @param instance an instance of the BodyHealth plugin
     * @throws IOException
     */
    private static void copySpecificFiles(String[] filesToCopy, File targetFolder, JavaPlugin instance) throws IOException {
        if (!targetFolder.exists()) targetFolder.mkdirs();

        ensureDirectoriesExist(targetFolder, "assets/bodyhealth", "huds", "images", "layouts", "build");

        for (String fileName : filesToCopy) {
            if (fileName == null) continue;

            File targetFile = new File(targetFolder, fileName.replaceAll("fix", ""));
            Debug.logDev("Validating File: " + targetFile.getAbsolutePath());

            try (InputStream resourceStream = instance.getResource("BetterHudConfig" + "/" + fileName)) {
                if (resourceStream == null) {
                    Debug.log("Resource not found: " + "BetterHudConfig" + "/" + fileName);
                    continue;
                }

                File parent = targetFile.getParentFile();
                if (!parent.exists()) parent.mkdirs();

                if ("huds/bodyhealth.yml".equals(fileName)) copyWithAnchorPointChange(resourceStream, targetFile);
                else if ("layouts/bodyhealth.yml".equals(fileName)) copyWithOffsets(resourceStream, targetFile);
                else Files.copy(resourceStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    /**
     * Copies a given layout config file and applies the correct offsets as configured in BodyHealth
     * @param resourceStream input stream for the given layout file
     * @param targetFile output file (where to copy it to)
     * @throws IOException
     */
    private static void copyWithOffsets(InputStream resourceStream, File targetFile) throws IOException {
        File tempFile = File.createTempFile("bodyhealth-layout", ".yml");
        Files.copy(resourceStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        FileConfiguration layoutConfig = YamlConfiguration.loadConfiguration(tempFile);
        ConfigurationSection images = layoutConfig.getConfigurationSection("bodyhealth.images");
        if (images == null) {
            tempFile.delete();
            return;
        }

        String anchor = Config.display_betterhud_position_anchor_point.toUpperCase();
        String vertical = "BOTTOM", horizontal = "RIGHT";

        if (anchor.contains("_")) {
            String[] parts = anchor.split("_");
            if (parts.length >= 2) {
                vertical = parts[0];
                horizontal = parts[1];
            }
        }

        int x = switch (horizontal) {
            case "LEFT" -> 38;
            case "CENTER" -> 19;
            default -> 0;
        };
        int y = switch (vertical) {
            case "TOP" -> 70;
            case "MIDDLE" -> 35;
            default -> 0;
        };

        for (String key : images.getKeys(false)) {
            ConfigurationSection image = images.getConfigurationSection(key);
            if (image == null) continue;

            image.set("scale", Config.display_betterhud_position_scale);
            image.set("x", Config.display_betterhud_position_scale * (image.getInt("x", 0) + x)
                    + Config.display_betterhud_position_horizontal_offset);
            image.set("y", Config.display_betterhud_position_scale * (image.getInt("y", 0) + y)
                    + Config.display_betterhud_position_vertical_offset);
        }

        layoutConfig.save(targetFile);
        tempFile.delete();
    }

    /**
     * Copies a given HUD config file and applies the correct anchor point as configured in BodyHealth
     * @param resourceStream input stream for the given HUD file
     * @param targetFile output file (where to copy it to)
     * @throws IOException
     */
    private static void copyWithAnchorPointChange(InputStream resourceStream, File targetFile) throws IOException {
        File tempFile = File.createTempFile("bodyhealth-hud", ".yml");
        Files.copy(resourceStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        FileConfiguration hudConfig = YamlConfiguration.loadConfiguration(tempFile);
        ConfigurationSection layouts = hudConfig.getConfigurationSection("bodyhealth.layouts");
        if (layouts == null) {
            tempFile.delete();
            return;
        }

        String anchor = Config.display_betterhud_position_anchor_point.toUpperCase();
        String vertical = "BOTTOM", horizontal = "RIGHT";

        if (anchor.contains("_")) {
            String[] parts = anchor.split("_");
            if (parts.length >= 2) {
                vertical = parts[0];
                horizontal = parts[1];
            }
        }

        int y = switch (vertical) {
            case "TOP" -> 0;
            case "MIDDLE" -> 50;
            default -> 100;
        };
        int x = switch (horizontal) {
            case "LEFT" -> 0;
            case "CENTER" -> 50;
            default -> 100;
        };

        for (String key : layouts.getKeys(false)) {
            ConfigurationSection layout = layouts.getConfigurationSection(key);
            if (layout == null) continue;
            layout.set("x", x);
            layout.set("y", y);
        }

        hudConfig.save(targetFile);
        tempFile.delete();
    }

    /**
     * Ensures that all required directories exist under a parent folder
     * @param targetFolder the parent folder in which to ensure directories exist
     * @param directories the directory paths (relative to targetFolder) to create if missing
     */
    private static void ensureDirectoriesExist(File targetFolder, String... directories) {
        for (String dir : directories) {
            File subDir = new File(targetFolder, dir);
            if (!subDir.exists()) {
                Debug.log("Creating directory: " + subDir.getAbsolutePath());
                subDir.mkdirs();
            }
        }
    }

    /**
     * Zips a folder recursively into a ZipOutputStream
     * @param folderToZip the folder whose contents should be zipped
     * @param parentFolder the relative parent path inside the zip (empty for root)
     * @param zos the ZipOutputStream to write into
     * @throws IOException
     */
    private static void zipFolder(File folderToZip, String parentFolder, ZipOutputStream zos) throws IOException {
        File[] files = folderToZip.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                zipFolder(file, parentFolder.isEmpty() ? file.getName() : parentFolder + "/" + file.getName(), zos);
                continue;
            }

            try (FileInputStream fis = new FileInputStream(file)) {
                String zipEntryName = parentFolder.isEmpty() ? file.getName() : parentFolder + "/" + file.getName();
                zos.putNextEntry(new ZipEntry(zipEntryName));

                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
            }
        }
    }

    /**
     * Handles the config toggle for BetterHud's entity popup feature by renaming the popup yml file
     * @param betterHudDataFolder BetterHud's data folder
     */
    private static void handleEntityPopupToggle(File betterHudDataFolder) {
        File popupDir = new File(betterHudDataFolder, "popups");
        File activeFile = new File(popupDir, "entity-popup.yml");
        File disabledFile = new File(popupDir, "-entity-popup.yml");

        if (Config.display_betterhud_disable_entity_popup) {
            if (activeFile.exists()) {
                boolean success = activeFile.renameTo(disabledFile);
                if (success) Debug.log("Renamed entity-popup.yml to -entity-popup.yml to disable it.");
                else Debug.logErr("Failed to rename entity-popup.yml to -entity-popup.yml.");
            }
        } else {
            if (disabledFile.exists()) {
                boolean success = disabledFile.renameTo(activeFile);
                if (success) Debug.log("Renamed -entity-popup.yml back to entity-popup.yml to re-enable it.");
                else Debug.logErr("Failed to rename -entity-popup.yml back to entity-popup.yml.");
            }
        }
    }

    /**
     * Handles the config toggle for BetterHud's default compass by editing BetterHud's compass config
     * @param betterHudDataFolder BetterHud's data folder
     */
    private static void handleDefaultCompassToggle(File betterHudDataFolder) {
        File compassFile = new File(betterHudDataFolder, "compasses/default_compass.yml");
        if (!compassFile.exists()) {
            Debug.log("Default compass config not found, skipping toggle.");
            return;
        }

        FileConfiguration compassConfig = YamlConfiguration.loadConfiguration(compassFile);
        boolean currentValue = compassConfig.getBoolean("default_compass.default", true);

        if (Config.display_betterhud_disable_compass && currentValue) {
            compassConfig.set("default_compass.default", false);
            Debug.log("Disabled BetterHud's default compass.");
        } else if (!Config.display_betterhud_disable_compass && !currentValue) {
            compassConfig.set("default_compass.default", true);
            Debug.log("Re-enabled BetterHud's default compass.");
        } else {
            return;
        }

        try {
            compassConfig.save(compassFile);
        } catch (IOException e) {
            Debug.logErr("Failed to save compass config: " + e.getMessage());
        }
    }

    /**
     * Merges BodyHealth's pack.mcmeta resource into an on-disk pack.mcmeta file
     * If the destination exists, keys from the resource overwrite/extend existing keys
     * @param destMcmeta destination pack.mcmeta file
     * @throws IOException
     */
    private static void mergePackMcmetaToFile(File destMcmeta) throws IOException {
        try (InputStream resourceStream = Main.getInstance().getResource("BetterHudConfig/build/pack.mcmeta")) {
            if (resourceStream == null) {
                Debug.logErr("Resource not found: BetterHudConfig/build/pack.mcmeta");
                return;
            }

            JsonObject newJson = JsonParser.parseReader(new InputStreamReader(resourceStream)).getAsJsonObject();
            JsonObject finalJson;

            if (destMcmeta.exists()) {
                JsonObject existingJson = JsonParser.parseReader(new FileReader(destMcmeta)).getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : newJson.entrySet()) {
                    existingJson.add(entry.getKey(), entry.getValue());
                }
                finalJson = existingJson;
            } else {
                finalJson = newJson;
            }

            File parent = destMcmeta.getParentFile();
            if (parent != null) parent.mkdirs();

            try (FileWriter writer = new FileWriter(destMcmeta)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(finalJson, writer);
            }
        }
    }

    /**
     * Merges BodyHealth's pack.mcmeta resource into an existing zip's pack.mcmeta (if present)
     * If the zip does not contain pack.mcmeta, the resource JSON is returned as-is
     * @param existingZip the zip to read an existing pack.mcmeta from
     * @return the merged pack.mcmeta JSON
     * @throws IOException
     */
    private static JsonObject mergePackMcmetaFromZip(ZipFile existingZip) throws IOException {
        JsonObject newJson;
        try (InputStream resourceStream = Main.getInstance().getResource("BetterHudConfig/build/pack.mcmeta")) {
            if (resourceStream == null) {
                Debug.logErr("Resource not found: BetterHudConfig/build/pack.mcmeta");
                return null;
            }
            newJson = JsonParser.parseReader(new InputStreamReader(resourceStream)).getAsJsonObject();
        }

        ZipEntry existingEntry = existingZip.getEntry("pack.mcmeta");
        if (existingEntry == null) return newJson;

        try (InputStream is = existingZip.getInputStream(existingEntry)) {
            JsonObject existingJson = JsonParser.parseReader(new InputStreamReader(is)).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : newJson.entrySet()) {
                existingJson.add(entry.getKey(), entry.getValue());
            }
            return existingJson;
        }
    }

    /**
     * Copies a classpath resource to an on-disk destination file, replacing if it exists
     * @param resourcePath the resource path within the plugin jar
     * @param dest destination file
     * @throws IOException
     */
    private static void copyResourceToFile(String resourcePath, File dest) throws IOException {
        try (InputStream in = Main.getInstance().getResource(resourcePath)) {
            if (in == null) {
                Debug.logErr("Resource not found: " + resourcePath);
                return;
            }
            File parent = dest.getParentFile();
            if (parent != null) parent.mkdirs();
            Files.copy(in, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
