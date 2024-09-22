package bodyhealth.depend;

import bodyhealth.Main;
import bodyhealth.config.Config;
import bodyhealth.config.Debug;
import kr.toxicity.hud.api.BetterHudAPI;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class BetterHud {

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

                //(Config.add_betterhud_mcmeta) ? "build/pack.mcmeta" : null,
                //(Config.add_betterhud_icon) ? "build/pack.png" : null
        };

        copySpecificFiles(filesToCopy, betterHudDataFolder, instance);

        if (Config.disable_betterhud_compass) {
            File compassFile = new File(betterHudDataFolder, "compasses/default_compass.yml");
            if (compassFile.exists()) {
                FileConfiguration compassConfig = YamlConfiguration.loadConfiguration(compassFile);
                boolean isDefault = compassConfig.getBoolean("default_compass.default", true); // Default to true if not set
                if (isDefault) {
                    compassConfig.set("default_compass.default", false);
                    compassConfig.save(compassFile);
                    Debug.log("Disabled BetterHuds default compass");
                }
            }
        }

        if (!Config.inject_betterhud_config_as_default && !Config.disable_betterhud_default_hud) return;
        File configFile = new File(betterHudDataFolder, "config.yml");
        if (!configFile.exists()) {
            throw new IOException("BetterHud config.yml not found in " + betterHudDataFolder.getAbsolutePath());
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        List<String> defaultHudList = config.getStringList("default-hud");
        if (!defaultHudList.contains("bodyhealth") && Config.inject_betterhud_config_as_default) {
            Debug.log("Added the bodyhealth hud as a default hud to BetterHud");
            defaultHudList.add("bodyhealth");
        }
        if (defaultHudList.contains("test_hud") && Config.disable_betterhud_default_hud) {
            Debug.log("Disabled BetterHuds default hud");
            defaultHudList.remove("test_hud");
        }
        config.set("default-hud", defaultHudList);
        config.save(configFile);
    }

    public static void add() throws IOException {
        String[] filesToCopy = {
            (Config.add_betterhud_mcmeta) ? "build/pack.mcmeta" : null,
            (Config.add_betterhud_icon) ? "build/pack.png" : null
        };
        copySpecificFiles(filesToCopy, BetterHudAPI.inst().bootstrap().dataFolder(), Main.getInstance());
        Debug.log("Files added successfully");
    }

    private static void copySpecificFiles(String[] filesToCopy, File targetFolder, JavaPlugin instance) throws IOException {
        if (!targetFolder.exists()) targetFolder.mkdirs();

        ensureDirectoriesExist(targetFolder, "assets/bodyhealth", "huds", "images", "layouts", "build");

        for (String fileName : filesToCopy) {
            if (fileName == null) continue;

            File targetFile = new File(targetFolder, fileName);
            Debug.log("Validating File: " + targetFile.getAbsolutePath());

            try (InputStream resourceStream = instance.getResource("BetterHudConfig" + "/" + fileName)) {
                if (resourceStream == null) {
                    Debug.log("Resource not found: " + "BetterHudConfig" + "/" + fileName);
                    continue;
                }

                File parent = targetFile.getParentFile();
                if (!parent.exists()) parent.mkdirs();

                Files.copy(resourceStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    // Utility function to ensure subdirectories are created
    private static void ensureDirectoriesExist(File targetFolder, String... directories) {
        for (String dir : directories) {
            File subDir = new File(targetFolder, dir);
            if (!subDir.exists()) {
                Debug.log("Creating directory: " + subDir.getAbsolutePath());
                subDir.mkdirs();
            }
        }
    }
}
