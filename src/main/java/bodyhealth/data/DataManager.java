package bodyhealth.data;

import bodyhealth.Main;
import bodyhealth.config.Config;
import bodyhealth.config.Debug;
import bodyhealth.core.BodyHealth;
import bodyhealth.data.storage.MySQLStorage;
import bodyhealth.data.storage.SQLiteStorage;
import bodyhealth.data.storage.YAMLStorage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager {

    private static final Map<UUID, BodyHealth> bodyHealthMap = new HashMap<>();

    private static File dataFolder;
    private static Storage storage;

    /**
     * Ensures data folder exists and loads selected storage type
     */
    public static void load() {
        dataFolder = new File(Main.getInstance().getDataFolder(), "data");
        if (!dataFolder.exists()) dataFolder.mkdirs();
        storage = switch (Config.storage_type) {
            case SQLite -> new SQLiteStorage();
            case MySQL -> new MySQLStorage();
            case YAML -> new YAMLStorage();
        };
    }

    /**
     * Retrieves BodyHealth's data folder
     * @return BodyHealth's data folder
     */
    public static @NotNull File getDataFolder() {
        return dataFolder;
    }

    /**
     * Retrieves the selected Storage
     * @return The selected Storage
     */
    public static @NotNull Storage getStorage() {
        return storage;
    }

    /**
     * Retrieves a players BodyHealth object,
     * loading it from storage if necessary
     * @param uuid UUID of the player
     * @return The BodyHealth object
     */
    public static @NotNull BodyHealth getBodyHealth(@NotNull UUID uuid) {
        if (bodyHealthMap.containsKey(uuid)) return bodyHealthMap.get(uuid);
        Debug.log("Loading data for player with uuid " + uuid + "...");
        BodyHealth bodyHealth = storage.loadBodyHealth(uuid);
        bodyHealthMap.put(uuid, bodyHealth);
        return bodyHealth;
    }

    /**
     * Removes a players BodyHealth object from internal
     * storage and saves it to the selected storage type
     * @param uuid UUID of the player
     */
    public static void saveBodyHealth(@NotNull UUID uuid) {
        if (bodyHealthMap.containsKey(uuid)) {
            Debug.log("Saving data for player with uuid " + uuid + "...");
            storage.saveBodyHealth(uuid, bodyHealthMap.get(uuid));
            bodyHealthMap.remove(uuid);
        }
    }

}
