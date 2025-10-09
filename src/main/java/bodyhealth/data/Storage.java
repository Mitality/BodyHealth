package bodyhealth.data;

import bodyhealth.core.BodyHealth;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public interface Storage {

    /**
     * Returns the StorageType of a storage object
     * @return The StorageType of this storage
     */
    StorageType getType();

    /**
     * Erase all existing data in this storage
     */
    boolean erase();

    /**
     * Saves the BodyHealth of a player to this storage type
     * @param uuid A unique id representing the player
     * @param bodyHealth The player's BodyHealth
     */
    void saveBodyHealth(UUID uuid, BodyHealth bodyHealth);

    /**
     * Loads a players BodyHealth from this storage type
     * @param uuid A unique id representing the player
     * @return The player's loaded Bodyhealth object
     */
    @NotNull BodyHealth loadBodyHealth(UUID uuid);


    /**
     * Loads all stored BodyHealth objects from this storage
     * @return All stored BodyHealth objects, mapped by UUID
     */
    @NotNull Map<UUID, BodyHealth> loadAllBodyHealth();

}
