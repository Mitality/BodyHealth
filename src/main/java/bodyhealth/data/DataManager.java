package bodyhealth.data;

import bodyhealth.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class DataManager {

    private static File file;
    private static FileConfiguration data;

    /**
     * Sets up the plugin's yaml data storage system
     */
    public static void setup() {

        // Create the data folder if it doesn't exist yet
        File dataFolder = new File(Main.getInstance().getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        // Create the data.yml file in the data folder or get the existing one
        file = new File(dataFolder, "bodyHealth.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Saves the current data to the data.yml file
     */
    public static void saveData() {
        try {
            data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the currently stored data
     * @return A FileConfiguration representing the currently stored data
     */
    public static FileConfiguration getData() {
        return data;
    }

}
