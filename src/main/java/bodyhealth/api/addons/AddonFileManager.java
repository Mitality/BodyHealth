package bodyhealth.api.addons;

import bodyhealth.Main;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class AddonFileManager {

    private final static Main main = Main.getInstance();

    private final BodyHealthAddon addon;
    private final File addonDir;
    private final File jarFile;

    private final String addonName;
    private final AddonDebug debug;

    private final File configFile;
    private YamlConfiguration config;

    public AddonFileManager(BodyHealthAddon addon, File jarFile) {
        this.addon = addon;
        this.addonName = extractAddonName(jarFile.getName());
        this.addonDir = new File(main.getDataFolder().getAbsolutePath() + File.separator + "addons" + File.separator + addonName);
        this.jarFile = jarFile;
        this.debug = addon.getAddonDebug();
        this.configFile = new File(addonDir, addonName + ".yml");
        this.config = configFile.exists() ? YamlConfiguration.loadConfiguration(configFile) : null;
    }

    /**
     * Generate a file from a given fileName
     * @param fileName The name of the file to create
     */
    public void generateFileFromFileName(String fileName) {
        generateFile(new File(addonDir, fileName));
    }

    /**
     * Generate a file from a given absolute path
     * @param absolutePath The absolute path
     */
    public void generateFileFromAbsolutePath(String absolutePath) {
        generateFile(new File(absolutePath));
    }

    /**
     * Generate a file from a given fileName in a given parent directory
     * @param parent The directory to create the file in
     * @param fileName The name of the file to create
     */
    public void generateFileInParentDirectory(File parent, String fileName) {
        generateFile(new File(parent, fileName));
    }

    /**
     * Generate a file for a given File object
     * @param file The File object
     */
    public void generateFile(File file) {
        createAddonFolder();
        try {
            if (!file.exists()) {
                file.createNewFile();
                try (JarInputStream jarInputStream = new JarInputStream(new FileInputStream(jarFile))) {
                    JarEntry jarEntry;
                    while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
                        if (jarEntry.isDirectory() || !jarEntry.getName().equals(file.getName())) {
                            continue;
                        }
                        OutputStream outputStream = Files.newOutputStream(file.toPath());
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = jarInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        outputStream.flush();
                        outputStream.close();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            debug.logErr("Failed to generate addon file: " + e.getMessage());
        }
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
     * Retrieves the addon folder as a File object
     * @return The addon folder / directory
     */
    public File getAddonFolder() {
        return addonDir;
    }

    /**
     * Loads a YamlConfiguration from the addons primary config file
     * @return The loaded YamlConfiguration object
     */
    public YamlConfiguration getAddonConfig() {
        generateAddonConfig();
        return config;
    }

    /**
     * Saves the addons primary config
     */
    public void saveAddonConfig() {
        generateAddonConfig();
        try {
            config.save(configFile);
        } catch (IOException e) {
            debug.logErr("Failed to save addon config: " + e.getMessage());
        }
    }

    /**
     * Generates the addons primary config file
     */
    private void generateAddonConfig() {
        if (config == null) {
            generateFile(configFile);
            config = YamlConfiguration.loadConfiguration(configFile);
        }
    }

    /**
     * Ensures that the addon folder exists
     */
    private void createAddonFolder() {
        if (!addonDir.exists()) addonDir.mkdirs();
    }

    /**
     * Extracts the addon name from the jar file name, removing any version information.
     * Assumes that the version is appended with a '-' and contains numbers, dots, and possibly other characters.
     */
    private String extractAddonName(String jarFileName) {
        // Remove ".jar" at the end if present
        if (jarFileName.endsWith(".jar")) {
            jarFileName = jarFileName.substring(0, jarFileName.length() - 4);
        }
        // Split at the first hyphen "-" to remove version information if present
        int hyphenIndex = jarFileName.indexOf('-');
        if (hyphenIndex != -1) {
            return jarFileName.substring(0, hyphenIndex);
        }
        return jarFileName; // Return the full name if no version pattern is found
    }

}
