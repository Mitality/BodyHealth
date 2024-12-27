package bodyhealth.effects;

import bodyhealth.core.BodyPart;
import org.bukkit.entity.Player;

public interface BodyHealthEffect {

    /**
     * Keyword to be used to set the effect (config)
     * @return The effects unique identifier
     */
    String getIdentifier();

    /**
     * Tells users how to use this effect (format)
     * @return The effects intended usage
     */
    String getUsage();

    /**
     * Applies the effect for the given player
     * @param player The player affected by this effect execution
     * @param args The effects arguments including the effect call
     */
    void onApply(Player player, BodyPart part, String[] args);

    /**
     * Removes the effect for the given player
     * @param player The player affected by this effect execution
     * @param args The effects arguments including the effect call
     */
    void onRemove(Player player, BodyPart part, String[] args);

}
