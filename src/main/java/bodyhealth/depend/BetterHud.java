package bodyhealth.depend;

import bodyhealth.Main;
import bodyhealth.config.Config;
import bodyhealth.config.Debug;
import kr.toxicity.hud.api.BetterHudAPI;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BetterHud {

    /**
     * Injects a working configuration into BetterHud to display health per BodyPart with a HUD
     * Also disables BetterHuds default stuff unless specified otherwise in BodyHealth's config
     * @throws IOException
     */
    public static void inject() throws IOException {
        final JavaPlugin instance = Main.getInstance();
        final File betterHudDataFolder = BetterHudAPI.inst().bootstrap().dataFolder();

        Debug.log("DataFolder: " + betterHudDataFolder.getAbsolutePath());

        // Copy BetterHudConfig from BodyHealth resources to BetterHud's data folder
        String[] filesToCopy = { // Must be specified manually
                "assets/bodyhealth/bodyhealth_empty.png",
                "assets/bodyhealth/bodyhealth_broken.png",
                "assets/bodyhealth/bodyhealth_damaged.png",
                "assets/bodyhealth/bodyhealth_intermediate.png",
                "assets/bodyhealth/bodyhealth_nearlyfull.png",
                "assets/bodyhealth/bodyhealth_full.png",

                "assets/bodyhealth/bodyhealth_broken_head.png",
                "assets/bodyhealth/bodyhealth_broken_body.png",
                "assets/bodyhealth/bodyhealth_broken_arm_left.png",
                "assets/bodyhealth/bodyhealth_broken_arm_right.png",
                "assets/bodyhealth/bodyhealth_broken_leg_left.png",
                "assets/bodyhealth/bodyhealth_broken_leg_right.png",
                "assets/bodyhealth/bodyhealth_broken_foot_left.png",
                "assets/bodyhealth/bodyhealth_broken_foot_right.png",

                "assets/bodyhealth/bodyhealth_damaged_head.png",
                "assets/bodyhealth/bodyhealth_damaged_body.png",
                "assets/bodyhealth/bodyhealth_damaged_arm_left.png",
                "assets/bodyhealth/bodyhealth_damaged_arm_right.png",
                "assets/bodyhealth/bodyhealth_damaged_leg_left.png",
                "assets/bodyhealth/bodyhealth_damaged_leg_right.png",
                "assets/bodyhealth/bodyhealth_damaged_foot_left.png",
                "assets/bodyhealth/bodyhealth_damaged_foot_right.png",

                "assets/bodyhealth/bodyhealth_intermediate_head.png",
                "assets/bodyhealth/bodyhealth_intermediate_body.png",
                "assets/bodyhealth/bodyhealth_intermediate_arm_left.png",
                "assets/bodyhealth/bodyhealth_intermediate_arm_right.png",
                "assets/bodyhealth/bodyhealth_intermediate_leg_left.png",
                "assets/bodyhealth/bodyhealth_intermediate_leg_right.png",
                "assets/bodyhealth/bodyhealth_intermediate_foot_left.png",
                "assets/bodyhealth/bodyhealth_intermediate_foot_right.png",

                "assets/bodyhealth/bodyhealth_nearlyfull_head.png",
                "assets/bodyhealth/bodyhealth_nearlyfull_body.png",
                "assets/bodyhealth/bodyhealth_nearlyfull_arm_left.png",
                "assets/bodyhealth/bodyhealth_nearlyfull_arm_right.png",
                "assets/bodyhealth/bodyhealth_nearlyfull_leg_left.png",
                "assets/bodyhealth/bodyhealth_nearlyfull_leg_right.png",
                "assets/bodyhealth/bodyhealth_nearlyfull_foot_left.png",
                "assets/bodyhealth/bodyhealth_nearlyfull_foot_right.png",

                "assets/bodyhealth/bodyhealth_full_head.png",
                "assets/bodyhealth/bodyhealth_full_body.png",
                "assets/bodyhealth/bodyhealth_full_arm_left.png",
                "assets/bodyhealth/bodyhealth_full_arm_right.png",
                "assets/bodyhealth/bodyhealth_full_leg_left.png",
                "assets/bodyhealth/bodyhealth_full_leg_right.png",
                "assets/bodyhealth/bodyhealth_full_foot_left.png",
                "assets/bodyhealth/bodyhealth_full_foot_right.png",

                "assets/bodyhealth/bodyhealth_damaged.png",
                "images/bodyhealth.yml",
                "layouts/bodyhealth.yml",
                "huds/bodyhealth.yml",

                //(Config.display_betterhud_add_mcmeta) ? "build/pack.mcmeta" : null,
                //(Config.display_betterhud_add_icon) ? "build/pack.png" : null
        };

        copySpecificFiles(filesToCopy, betterHudDataFolder, instance);
        handleDefaultCompassToggle(betterHudDataFolder);
        handleEntityPopupToggle(betterHudDataFolder);

        File configFile = new File(betterHudDataFolder, "config.yml");
        if (!configFile.exists()) {
            throw new IOException("BetterHud config.yml not found in " + betterHudDataFolder.getAbsolutePath());
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        List<String> defaultHudList = config.getStringList("default-hud");

        // Handle as-default display option
        if (!defaultHudList.contains("bodyhealth") && Config.display_betterhud_as_default) {
            Debug.log("Added BodyHealth's HUD as a default HUD to BetterHud");
            defaultHudList.add("bodyhealth");
        }
        else if (defaultHudList.contains("bodyhealth") && !Config.display_betterhud_as_default) {
            Debug.log("Removed BodyHealth's HUD from BetterHud's list of default HUDs");
            defaultHudList.add("bodyhealth");
        }

        // Handle default HUD toggle
        if (defaultHudList.contains("test_hud") && Config.display_betterhud_disable_default_hud) {
            Debug.log("Disabled BetterHud's default hud");
            defaultHudList.remove("test_hud");
        }
        else if (!defaultHudList.contains("test_hud") && !Config.display_betterhud_disable_default_hud) {
            Debug.log("Re-enabled BetterHud's default hud");
            defaultHudList.add("test_hud");
        }

        config.set("default-hud", defaultHudList);
        config.save(configFile);

    }

    /**
     * Adds necessary files to BetterHuds resource pack output folder to make it usable
     * (Adds and icon and mcmeta file to specify the pack version)
     * @throws IOException
     */
    public static void add() throws IOException {
        String[] filesToCopy = {
            (Config.display_betterhud_add_mcmeta) ? "build/pack.mcmeta" : null,
            (Config.display_betterhud_add_icon) ? "build/pack.png" : null
        };
        copySpecificFiles(filesToCopy, BetterHudAPI.inst().bootstrap().dataFolder(), Main.getInstance());
        Debug.log("Files added successfully");
        zip(); // Zip after adding required files
    }

    /**
     * Zips the contents of BetterHuds resource pack folder to BodyHealth/resource_pack.zip
     */
    private static void zip() {
        if (!Config.display_betterhud_package_compress) return;

        String filename = Config.display_betterhud_package_filename;
        Debug.log("Zipping BetterHud/build to plugins/BodyHealth/output/" + filename + ".zip");

        File betterHudBuildFolder = new File(BetterHudAPI.inst().bootstrap().dataFolder(), "build");
        if (!betterHudBuildFolder.exists()) {
            Debug.logErr("BetterHud build folder does not exist!");
            return;
        }

        File outputDir = new File(Main.getInstance().getDataFolder(), "output");
        if (!outputDir.exists()) outputDir.mkdirs();

        File zipFile = new File(outputDir, filename + ".zip");
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            zipFolder(betterHudBuildFolder, "", zos);
            Debug.log("Zipping complete! resource_pack.zip created at " + zipFile.getPath());
        } catch (IOException e) {
            Debug.log("Failed to create the zip file: " + e.getMessage());
        }
    }

    /**
     * Utility method to copy specific files from one folder to another
     * @param filesToCopy The files to copy
     * @param targetFolder The folder into which the files should be copied to
     * @param instance An instance of the BodyHealth plugin
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

                if (fileName.equals("huds/bodyhealth.yml")) copyWithAnchorPointChange(resourceStream, targetFile);
                else if (fileName.equals("layouts/bodyhealth.yml")) copyWithOffsets(resourceStream, targetFile);
                else Files.copy(resourceStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    /**
     * Copies a given HUD config file and applies the correct offsets
     * @param resourceStream InputStream for the given HUD file
     * @param targetFile Output file (where to copy it to)
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

            image.set("x", image.getInt("x", 0) + x + Config.display_betterhud_position_horizontal_offset);
            image.set("y", image.getInt("y", 0) + y + Config.display_betterhud_position_vertical_offset);
        }

        layoutConfig.save(targetFile);
        tempFile.delete();
    }

    /**
     * Copies a given HUD config file and applies the correct anchor point
     * @param resourceStream InputStream for the given HUD file
     * @param targetFile Output file (where to copy it to)
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
     * Utility method to ensure that all used directories actually exist
     * @param targetFolder The parent folder in which this should be done
     * @param directories The directories that should be in said folder
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
     * Utility method to zip the contents of a folder to another folder
     * @param folderToZip The folder of which the contents should be zipped
     * @param parentFolder If this is "", the zip directly contains the files
     * @param zos The ZipOutputStream to use
     * @throws IOException
     */
    private static void zipFolder(File folderToZip, String parentFolder, ZipOutputStream zos) throws IOException {
        File[] files = folderToZip.listFiles();
        if (files == null) {
            return;
        }

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
     * Handles the config toggle for BetterHud's entity popup feature
     * @param betterHudDataFolder BetterHud's data folder as File object
     */
    private static void handleEntityPopupToggle(File betterHudDataFolder) {
        File popupDir = new File(betterHudDataFolder, "popups");
        File activeFile = new File(popupDir, "entity-popup.yml");
        File disabledFile = new File(popupDir, "-entity-popup.yml");

        if (Config.display_betterhud_disable_entity_popup) {
            if (activeFile.exists()) {
                boolean success = activeFile.renameTo(disabledFile);
                if (success) {
                    Debug.log("Renamed entity-popup.yml to -entity-popup.yml to disable it.");
                } else {
                    Debug.logErr("Failed to rename entity-popup.yml to -entity-popup.yml.");
                }
            }
        } else {
            if (disabledFile.exists()) {
                boolean success = disabledFile.renameTo(activeFile);
                if (success) {
                    Debug.log("Renamed -entity-popup.yml back to entity-popup.yml to re-enable it.");
                } else {
                    Debug.logErr("Failed to rename -entity-popup.yml back to entity-popup.yml.");
                }
            }
        }
    }

    /**
     * Handles the config toggle for BetterHud's default compass
     * @param betterHudDataFolder BetterHud's data folder as File object
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
            return; // No change needed
        }

        try {
            compassConfig.save(compassFile);
        } catch (IOException e) {
            Debug.logErr("Failed to save compass config: " + e.getMessage());
        }
    }


}
