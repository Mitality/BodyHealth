package bodyhealth.data;

import bodyhealth.core.BodyHealth;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface Storage {

    /**
     * Saves the BodyHealth of a player to this storage type
     * @param uuid A unique id representing the player
     * @param bodyHealth The players BodyHealth
     */
    void saveBodyHealth(UUID uuid, BodyHealth bodyHealth);

    /**
     * Loads a players BodyHealth from this storage type
     * @param uuid A unique id representing the player
     * @return The players loaded Bodyhealth object
     */
    @NotNull BodyHealth loadBodyHealth(UUID uuid);

}
