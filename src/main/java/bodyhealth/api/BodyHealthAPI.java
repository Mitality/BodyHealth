package bodyhealth.api;

import bodyhealth.Main;
import bodyhealth.core.BodyPart;
import bodyhealth.util.BodyHealthUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class BodyHealthAPI {

    /**
     * Retrieves the instance of the main plugin class
     * @return the JavaPlugin instance
     */
    public static @NotNull JavaPlugin getInstance() {
        return Main.getInstance();
    }

    /**
     * Reloads the health system configuration
     * @return A boolean representing if the system reloads successfully
     */
    public static boolean reloadSystem() {
        return BodyHealthUtils.reloadSystem();
    }

    /**
     * Fully heals the given player across all body parts
     * @param player the player to heal
     */
    public static void healPlayer(@NotNull Player player) {
        BodyHealthUtils.getBodyHealth(player).regenerateHealth(Double.MAX_VALUE);
    }

    /**
     * Heals the given player by a specified amount across all body parts
     * @param player the player to heal
     * @param amount the amount of health to restore
     */
    public static void healPlayer(@NotNull Player player, int amount) {
        BodyHealthUtils.getBodyHealth(player).regenerateHealth(amount);
    }

    /**
     * Fully heals a specific body part of the given player
     * @param player the player to heal
     * @param part the specific body part to heal
     */
    public static void healPlayer(@NotNull Player player, @NotNull BodyPart part) {
        BodyHealthUtils.getBodyHealth(player).regenerateHealth(Double.MAX_VALUE, part);
    }

    /**
     * Heals a specific body part of the given player by a specified amount
     * @param player the player to heal
     * @param part the body part to heal
     * @param amount the amount of health to restore
     */
    public static void healPlayer(@NotNull Player player, @NotNull BodyPart part, int amount) {
        BodyHealthUtils.getBodyHealth(player).regenerateHealth(amount, part);
    }

    /**
     * Damages the given player with configured settings based on the cause
     * @param player the player to damage
     * @param cause the cause of the damage
     * @param amount the amount of damage to apply
     */
    public static void damagePlayerWithConfig(@NotNull Player player, @NotNull EntityDamageEvent.DamageCause cause, double amount) {
        BodyHealthUtils.applyDamageWithConfig(BodyHealthUtils.getBodyHealth(player), cause, amount);
    }

    /**
     * Damages a specific body part of the given player with configured settings based on the cause
     * @param player the player to damage
     * @param cause the cause of the damage
     * @param amount the amount of damage to apply
     * @param part the body part to damage
     */
    public static void damagePlayerWithConfig(@NotNull Player player, @NotNull EntityDamageEvent.DamageCause cause, double amount, @NotNull BodyPart part) {
        BodyHealthUtils.applyDamageWithConfig(BodyHealthUtils.getBodyHealth(player), cause, amount, part);
    }

    /**
     * Directly damages the given player across all body parts by a specified amount, ignoring the plugins configuration
     * @param player the player to damage
     * @param amount the amount of damage to apply
     */
    public static void damagePlayerDirectly(@NotNull Player player, double amount) {
        BodyHealthUtils.getBodyHealth(player).applyDamage(amount);
    }

    /**
     * Directly damages a specific body part of the given player by a specified amount, ignoring the plugins configuration
     * @param player the player to damage
     * @param amount the amount of damage to apply
     * @param part the body part to damage
     */
    public static void damagePlayerDirectly(@NotNull Player player, double amount, @NotNull BodyPart part) {
        BodyHealthUtils.getBodyHealth(player).applyDamage(part, amount);
    }

    /**
     * Sets the given player's health (in percent!) across all body parts to a specified value
     * @param player the player whose health is to be set
     * @param health the new health value
     */
    public static void setHealth(@NotNull Player player, double health) {
        BodyHealthUtils.getBodyHealth(player).setHealth(health, true);
    }

    /**
     * Sets the health (in percent!) of a specific body part of the given player to a specified value
     * @param player the player whose health is to be set
     * @param health the new health value
     * @param part the body part to set the health for
     */
    public static void setHealth(@NotNull Player player, double health, @NotNull BodyPart part) {
        BodyHealthUtils.getBodyHealth(player).setHealth(part, health, true);
    }

    /**
     * Retrieves the current health (in percent!) of a specific body part of the given player
     * @param player the player whose health is to be retrieved
     * @param part the body part to retrieve the health for
     * @return the current health of the specified body part
     */
    public static double getHealth(@NotNull Player player, @NotNull BodyPart part) {
        return BodyHealthUtils.getBodyHealth(player).getHealth(part);
    }

}
