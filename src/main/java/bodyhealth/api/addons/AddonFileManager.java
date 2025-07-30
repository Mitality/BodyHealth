package bodyhealth.api.addons;

import bodyhealth.Main;
import bodyhealth.config.Debug;
import com.tchristofferson.configupdater.ConfigUpdater;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddonFileManager {

    private final static Main instance = Main.getInstance();

    private final BodyHealthAddon addon;
    private final File addonDir;
    private final File jarFile;

    private final String addonName;
    private final AddonDebug debug;

    public AddonFileManager(BodyHealthAddon addon, File jarFile) {
        this.addon = addon;
        this.addonName = extractAddonName(jarFile.getName());
        this.addonDir = new File(instance.getDataFolder().getAbsolutePath() + File.separator + "addons" + File.separator + addonName);
        this.jarFile = jarFile;
        this.debug = addon.getAddonDebug();
    }

    /**
     * Ensures that the addon folder exists
     */
    private void createAddonFolder() {
        if (!addonDir.exists()) addonDir.mkdirs();
    }

    /**
     * Saves a resource from the addons JAR to the addon's data folder
     * @param fileName the name of the resource/file to save
     * @param replace whether to replace existing files
     */
    public void saveResource(String fileName, boolean replace) {
        saveResource(new File(addonDir, fileName), replace);
    }

    /**
     * Saves a resource from the addons JAR to a given parent directory
     * @param parentDirectory the directory to create the file in
     * @param fileName the name of the file to create/save
     * @param replace whether to replace existing files
     */
    public void saveResource(File parentDirectory, String fileName, boolean replace) {
        saveResource(new File(parentDirectory, fileName), replace);
    }

    /**
     * Saves a resource from the addons JAR to the given path
     * @param path the absolute path to save the resource to
     * @param replace whether to replace existing files
     */
    public void saveResource(Path path, boolean replace) {
        saveResource(path.toFile(), replace);
    }

    /**
     * Saves a resource from the addons JAR to the addon's data folder
     * @param file the File object to create/save
     * @param replace whether to replace existing files
     */
    public void saveResource(File file, boolean replace) {
        createAddonFolder();

        if (file.exists() && !replace) return;

        try (JarInputStream jarInputStream = new JarInputStream(new FileInputStream(jarFile))) {
            JarEntry jarEntry;

            while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
                if (jarEntry.isDirectory()) continue;
                if (!jarEntry.getName().equals(file.getName())) continue;

                // Ensure parent directories exist
                File parent = file.getParentFile();
                if (parent != null && !parent.exists()) parent.mkdirs();

                try (OutputStream outputStream = Files.newOutputStream(file.toPath())) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = jarInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }

                return; // Successfully written
            }

            debug.logErr("Resource not found in JAR: " + file.getName());

        } catch (IOException e) {
            debug.logErr("Failed to save resource: " + file.getName() + " (" + e.getMessage() + ")");
        }
    }

    /**
     * Updates a given yaml configuration file, adding missing config options,
     * removing ones that are no longer needed, updating comments, e.t.c.
     * @param resourceName Name of the resource within the addon jar
     * @param yamlFile The file to update to the latest version
     * @return true if successfully updated, false if not
     */
    public boolean updateYamlFile(String resourceName, File yamlFile) {
        try {
            Plugin dummy = new DummyPluginWrapper(jarFile);
            ConfigUpdater.update(dummy, resourceName, yamlFile);
            return true;
        } catch (IOException e) {
            Debug.logErr(e);
            return false;
        }
    }

    /**
     * Retrieves the addon folder as a File object
     * @return The addon folder / directory
     */
    public File getAddonFolder() {
        return addonDir;
    }

    /**
     * Retrieves a file by its name
     * @param fileName The files name
     * @return The File object
     */
    public File getFile(String fileName) {
        createAddonFolder();
        return new File(addonDir, fileName);
    }

    /**
     * Loads a YamlConfiguration from a given fileName
     * @param fileName The name of the config file
     * @return A YamlConfiguration object
     */
    public YamlConfiguration getYamlConfiguration(String fileName) {
        createAddonFolder();
        return YamlConfiguration.loadConfiguration(new File(addonDir, fileName));
    }

    /**
     * Extracts the addon name from the jar file name, removing any version information.
     * Assumes that the version is appended with a '-' and contains numbers, dots, and possibly other characters.
     */
    private String extractAddonName(String jarFileName) {
        if (jarFileName.endsWith(".jar")) {
            jarFileName = jarFileName.substring(0, jarFileName.length() - 4);
        }

        // matches a hyphen followed by something resembling a version at the end of the string
        Pattern versionPattern = Pattern.compile("(.+)-v?\\d[\\w.\\-]*$");
        Matcher matcher = versionPattern.matcher(jarFileName);

        if (matcher.matches()) {
            return matcher.group(1);
        }

        return jarFileName;
    }

}
