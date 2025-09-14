package bodyhealth.migrations.migration;

import bodyhealth.Main;
import bodyhealth.migrations.Migration;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.logging.Level;

public class BodyToTorsoMigration extends Migration {

    @Override
    public void onEnable(Main main) {
        try {

            File pluginFolder = main.getDataFolder();
            if (!pluginFolder.exists()) return;

            File configFile = new File(pluginFolder, "config.yml");
            if (configFile.exists()) {
                migrateYamlFile(configFile);
            } else {
                main.saveDefaultConfig();
            }

            File languageDir = new File(main.getDataFolder(), "language");
            if (languageDir.exists()) {
                for (File langFile : Arrays.stream(languageDir.listFiles()).filter(file ->
                        file.getName().endsWith(".yml")).toList()) {
                    migrateYamlFile(langFile);
                }
            }

        } catch (Exception e) {
            StackTraceElement caller = Thread.currentThread().getStackTrace()[2]; // Debug#logErr not loaded yet
            String className = caller.getClassName().substring(caller.getClassName().lastIndexOf('.') + 1);
            String prefix = "[BodyHealthError - " + className + ":" + caller.getLineNumber() + "] ";
            Bukkit.getLogger().log(Level.SEVERE, prefix + e.getMessage(), e);
        }
    }

    private static void migrateYamlFile(File file) throws IOException {
        String content = Files.readString(file.toPath());
        content = content.replaceAll("(?m)^(\\s*)BODY(\\s*:)", "$1TORSO$2");
        content = content.replace("{Health_BODY}", "{Health_TORSO}");
        Files.writeString(file.toPath(), content);
    }

}
