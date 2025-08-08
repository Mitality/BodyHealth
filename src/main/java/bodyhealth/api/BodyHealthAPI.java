package bodyhealth.api;

import bodyhealth.Main;
import bodyhealth.core.BodyHealth;
import bodyhealth.core.BodyPart;
import bodyhealth.core.BodyPartState;
import bodyhealth.depend.VanishPlugins;
import bodyhealth.effects.BodyHealthEffect;
import bodyhealth.effects.EffectHandler;
import bodyhealth.util.BodyHealthUtils;
import bodyhealth.util.MessageUtils;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BodyHealthAPI {

    private static BodyHealthAPI instance;
    private BodyHealthAPI() {}

    /**
     * Retrieves a singleton API instance
     * @return the BodyHealthAPI instance
     */
    public static BodyHealthAPI getInstance() {
        if (instance == null) {
            instance = new BodyHealthAPI();
        }
        return instance;
    }

    /**
     * Retrieves the instance of the main plugin class
     * @return the JavaPlugin instance
     */
    public @NotNull JavaPlugin getBodyHealthPlugin() {
        return Main.getInstance();
    }

    /**
     * Reloads the plugin, momentarily removing all effects
     * @return true if the reload was successful
     */
    public boolean reloadBodyHealthPlugin() {
        return BodyHealthUtils.reloadSystem();
    }

    /**
     * Checks if BodyHealth is enabled in a given world
     * @param world The world to check for
     * @return true if enabled
     */
    public boolean isSystemEnabled(@NotNull World world) {
        return BodyHealthUtils.isSystemEnabled(world);
    }

    /**
     * Checks if BodyHealth is enabled at a players position
     * @param player The player to check for
     * @return true if enabled
     */
    public boolean isSystemEnabled(@NotNull Player player) {
        return BodyHealthUtils.isSystemEnabled(player);
    }


    /**
     * Fully heals the given player across all body parts
     * @param player the player to fully heal
     */
    public void healPlayer(@NotNull Player player) {
        BodyHealthUtils.getBodyHealth(player).regenerateHealth(Double.MAX_VALUE, false, null);
    }

    /**
     * Fully heals the given player across all body parts
     * @param player the player to fully heal
     * @param force heal even where BodyHealth is disabled
     */
    public void healPlayer(@NotNull Player player, boolean force) {
        BodyHealthUtils.getBodyHealth(player).regenerateHealth(Double.MAX_VALUE, force, null);
    }

    /**
     * Fully heals the given player across all body parts
     * @param player the player to fully heal
     * @param force heal even where BodyHealth is disabled
     * @param cause the underlying event that caused this
     */
    public void healPlayer(@NotNull Player player, boolean force, @Nullable Event cause) {
        BodyHealthUtils.getBodyHealth(player).regenerateHealth(Double.MAX_VALUE, force, cause);
    }


    /**
     * Heals the given player by a specified amount across all body parts
     * @param player the player to heal by the specified amount
     * @param amount the amount of health to restore
     */
    public void healPlayer(@NotNull Player player, int amount) {
        BodyHealthUtils.getBodyHealth(player).regenerateHealth(amount, false, null);
    }

    /**
     * Heals the given player by a specified amount across all body parts
     * @param player the player to heal by the specified amount
     * @param amount the amount of health to restore
     * @param force heal even where BodyHealth is disabled
     */
    public void healPlayer(@NotNull Player player, int amount, boolean force) {
        BodyHealthUtils.getBodyHealth(player).regenerateHealth(amount, force, null);
    }

    /**
     * Heals the given player by a specified amount across all body parts
     * @param player the player to heal by the specified amount
     * @param amount the amount of health to restore
     * @param force heal even where BodyHealth is disabled
     * @param cause the underlying event that caused this
     */
    public void healPlayer(@NotNull Player player, int amount, boolean force, @Nullable Event cause) {
        BodyHealthUtils.getBodyHealth(player).regenerateHealth(amount, force, cause);
    }


    /**
     * Fully heals a specific body part of the given player
     * @param player the player to fully heal
     * @param part the specific body part to heal
     */
    public void healPlayer(@NotNull Player player, @NotNull BodyPart part) {
        BodyHealthUtils.getBodyHealth(player).regenerateHealth(Double.MAX_VALUE, part, false, null);
    }

    /**
     * Fully heals a specific body part of the given player
     * @param player the player to fully heal
     * @param part the specific body part to heal
     * @param force heal even where BodyHealth is disabled
     */
    public void healPlayer(@NotNull Player player, @NotNull BodyPart part, boolean force) {
        BodyHealthUtils.getBodyHealth(player).regenerateHealth(Double.MAX_VALUE, part, force, null);
    }

    /**
     * Fully heals a specific body part of the given player
     * @param player the player to fully heal
     * @param part the specific body part to heal
     * @param force heal even where BodyHealth is disabled
     * @param cause the underlying event that caused this
     */
    public void healPlayer(@NotNull Player player, @NotNull BodyPart part, boolean force, @Nullable Event cause) {
        BodyHealthUtils.getBodyHealth(player).regenerateHealth(Double.MAX_VALUE, part, force, cause);
    }


    /**
     * Heals a specific body part of the given player by a specified amount
     * @param player the player to heal by the specified amount
     * @param part the body part to heal by the specified amount
     * @param amount the amount of health to restore
     */
    public void healPlayer(@NotNull Player player, @NotNull BodyPart part, int amount) {
        BodyHealthUtils.getBodyHealth(player).regenerateHealth(amount, part, false, null);
    }

    /**
     * Heals a specific body part of the given player by a specified amount
     * @param player the player to heal by the specified amount
     * @param part the body part to heal by the specified amount
     * @param amount the amount of health to restore
     * @param force heal even where BodyHealth is disabled
     */
    public void healPlayer(@NotNull Player player, @NotNull BodyPart part, int amount, boolean force) {
        BodyHealthUtils.getBodyHealth(player).regenerateHealth(amount, part, force, null);
    }

    /**
     * Heals a specific body part of the given player by a specified amount
     * @param player the player to heal by the specified amount
     * @param part the body part to heal by the specified amount
     * @param amount the amount of health to restore
     * @param force heal even where BodyHealth is disabled
     * @param cause the underlying event that caused this
     */
    public void healPlayer(@NotNull Player player, @NotNull BodyPart part, int amount, boolean force, @Nullable Event cause) {
        BodyHealthUtils.getBodyHealth(player).regenerateHealth(amount, part, force, cause);
    }


    /**
     * Damages the given player with configured settings based on the cause
     * @param player the player to damage
     * @param damageCause the cause of the damage
     * @param amount the amount of damage to apply
     */
    public void damagePlayerWithConfig(@NotNull Player player, @NotNull EntityDamageEvent.DamageCause damageCause, double amount) {
        BodyHealthUtils.applyDamageWithConfig(BodyHealthUtils.getBodyHealth(player), damageCause, amount, false, null);
    }

    /**
     * Damages the given player with configured settings based on the cause
     * @param player the player to damage
     * @param damageCause the cause of the damage
     * @param amount the amount of damage to apply
     * @param force damage even where BodyHealth is disabled
     */
    public void damagePlayerWithConfig(@NotNull Player player, @NotNull EntityDamageEvent.DamageCause damageCause, double amount, boolean force) {
        BodyHealthUtils.applyDamageWithConfig(BodyHealthUtils.getBodyHealth(player), damageCause, amount, force, null);
    }

    /**
     * Damages the given player with configured settings based on the cause
     * @param player the player to damage
     * @param damageCause the cause of the damage
     * @param amount the amount of damage to apply
     * @param force damage even where BodyHealth is disabled
     * @param cause the underlying event that caused this
     */
    public void damagePlayerWithConfig(@NotNull Player player, @NotNull EntityDamageEvent.DamageCause damageCause, double amount, boolean force, @Nullable Event cause) {
        BodyHealthUtils.applyDamageWithConfig(BodyHealthUtils.getBodyHealth(player), damageCause, amount, force, cause);
    }


    /**
     * Damages a specific body part of the given player with configured settings based on the cause
     * @param player the player to damage
     * @param damageCause the cause of the damage
     * @param amount the amount of damage to apply
     * @param part the body part to damage
     */
    public void damagePlayerWithConfig(@NotNull Player player, @NotNull EntityDamageEvent.DamageCause damageCause, double amount, @NotNull BodyPart part) {
        BodyHealthUtils.applyDamageWithConfig(BodyHealthUtils.getBodyHealth(player), damageCause, amount, part, false, null);
    }

    /**
     * Damages a specific body part of the given player with configured settings based on the cause
     * @param player the player to damage
     * @param damageCause the cause of the damage
     * @param amount the amount of damage to apply
     * @param part the body part to damage
     * @param force damage even where BodyHealth is disabled
     */
    public void damagePlayerWithConfig(@NotNull Player player, @NotNull EntityDamageEvent.DamageCause damageCause, double amount, @NotNull BodyPart part, boolean force) {
        BodyHealthUtils.applyDamageWithConfig(BodyHealthUtils.getBodyHealth(player), damageCause, amount, part, force, null);
    }

    /**
     * Damages a specific body part of the given player with configured settings based on the cause
     * @param player the player to damage
     * @param damageCause the cause of the damage
     * @param amount the amount of damage to apply
     * @param part the body part to damage
     * @param force damage even where BodyHealth is disabled
     * @param cause the underlying event that caused this
     */
    public void damagePlayerWithConfig(@NotNull Player player, @NotNull EntityDamageEvent.DamageCause damageCause, double amount, @NotNull BodyPart part, boolean force, @Nullable Event cause) {
        BodyHealthUtils.applyDamageWithConfig(BodyHealthUtils.getBodyHealth(player), damageCause, amount, part, force, cause);
    }


    /**
     * Directly damages the given player across all body parts by a specified amount, ignoring the plugins configuration
     * @param player the player to damage
     * @param amount the amount of damage to apply
     */
    public void damagePlayerDirectly(@NotNull Player player, double amount) {
        BodyHealthUtils.getBodyHealth(player).applyDamage(amount, false, null);
    }

    /**
     * Directly damages the given player across all body parts by a specified amount, ignoring the plugins configuration
     * @param player the player to damage
     * @param amount the amount of damage to apply
     * @param force damage even where BodyHealth is disabled
     */
    public void damagePlayerDirectly(@NotNull Player player, double amount, boolean force) {
        BodyHealthUtils.getBodyHealth(player).applyDamage(amount, force, null);
    }

    /**
     * Directly damages the given player across all body parts by a specified amount, ignoring the plugins configuration
     * @param player the player to damage
     * @param amount the amount of damage to apply
     * @param force damage even where BodyHealth is disabled
     * @param cause the underlying event that caused this
     */
    public void damagePlayerDirectly(@NotNull Player player, double amount, boolean force, @Nullable Event cause) {
        BodyHealthUtils.getBodyHealth(player).applyDamage(amount, force, cause);
    }


    /**
     * Directly damages a specific body part of the given player by a specified amount, ignoring the plugins configuration
     * @param player the player to damage
     * @param amount the amount of damage to apply
     * @param part the body part to damage
     */
    public void damagePlayerDirectly(@NotNull Player player, double amount, @NotNull BodyPart part) {
        BodyHealthUtils.getBodyHealth(player).applyDamage(part, amount, false, null);
    }

    /**
     * Directly damages a specific body part of the given player by a specified amount, ignoring the plugins configuration
     * @param player the player to damage
     * @param amount the amount of damage to apply
     * @param part the body part to damage
     * @param force damage even where BodyHealth is disabled
     */
    public void damagePlayerDirectly(@NotNull Player player, double amount, @NotNull BodyPart part, boolean force) {
        BodyHealthUtils.getBodyHealth(player).applyDamage(part, amount, force, null);
    }

    /**
     * Directly damages a specific body part of the given player by a specified amount, ignoring the plugins configuration
     * @param player the player to damage
     * @param amount the amount of damage to apply
     * @param part the body part to damage
     * @param force damage even where BodyHealth is disabled
     * @param cause the underlying event that caused this
     */
    public void damagePlayerDirectly(@NotNull Player player, double amount, @NotNull BodyPart part, boolean force, @Nullable Event cause) {
        BodyHealthUtils.getBodyHealth(player).applyDamage(part, amount, force, cause);
    }


    /**
     * Sets the given player's health (in percent!) across all body parts to a specified value
     * @param player the player whose health is to be set
     * @param health the new health value
     */
    public void setHealth(@NotNull Player player, double health) {
        BodyHealthUtils.getBodyHealth(player).setHealth(health, true, null);
    }

    /**
     * Sets the given player's health (in percent!) across all body parts to a specified value
     * @param player the player whose health is to be set
     * @param health the new health value
     * @param force_keep whether to force keep the new health state
     */
    public void setHealth(@NotNull Player player, double health, boolean force_keep) {
        BodyHealthUtils.getBodyHealth(player).setHealth(health, force_keep, null);
    }

    /**
     * Sets the given player's health (in percent!) across all body parts to a specified value
     * @param player the player whose health is to be set
     * @param health the new health value
     * @param force_keep whether to force keep the new health state
     * @param cause the underlying event that caused this
     */
    public void setHealth(@NotNull Player player, double health, boolean force_keep, @Nullable Event cause) {
        BodyHealthUtils.getBodyHealth(player).setHealth(health, force_keep, cause);
    }


    /**
     * Sets the health (in percent!) of a specific body part of the given player to a specified value
     * @param player the player whose health is to be set
     * @param part the body part to set the health for
     * @param health the new health value
     */
    public void setHealth(@NotNull Player player, @NotNull BodyPart part, double health) {
        BodyHealthUtils.getBodyHealth(player).setHealth(part, health, true, null);
    }

    /**
     * Sets the health (in percent!) of a specific body part of the given player to a specified value
     * @param player the player whose health is to be set
     * @param part the body part to set the health for
     * @param health the new health value
     * @param force_keep whether to force keep the new health state
     */
    public void setHealth(@NotNull Player player, @NotNull BodyPart part, double health, boolean force_keep) {
        BodyHealthUtils.getBodyHealth(player).setHealth(part, health, force_keep, null);
    }

    /**
     * Sets the health (in percent!) of a specific body part of the given player to a specified value
     * @param player the player whose health is to be set
     * @param part the body part to set the health for
     * @param health the new health value
     * @param force_keep whether to force keep the new health state
     * @param cause the underlying event that caused this
     */
    public void setHealth(@NotNull Player player, @NotNull BodyPart part, double health, boolean force_keep, @Nullable Event cause) {
        BodyHealthUtils.getBodyHealth(player).setHealth(part, health, force_keep, cause);
    }


    /**
     * Retrieves the current health (in percent!) of a specific body part of the given player
     * @param player the player whose health is to be retrieved
     * @param part the body part to retrieve the health for
     * @return the current health of the specified body part
     */
    public double getHealth(@NotNull Player player, @NotNull BodyPart part) {
        return BodyHealthUtils.getBodyHealth(player).getHealth(part);
    }

    /**
     * Retrieves the BodyHealth object of a player (careful with that!)
     * @param player the player whose BodyHealth object is to be retrieved
     * @return the BodyHealth object that belongs to the given player
     */
    public BodyHealth getBodyHealth(@NotNull Player player) {
        return BodyHealthUtils.getBodyHealth(player);
    }

    /**
     * Check what BodyPartState a given body part of a given player currently has
     * @param player The player to calculate the BodyPartState for
     * @param part The body part to calculate the BodyPartState for
     * @return The BodyPartState of the given body part of the given player
     */
    public BodyPartState getBodyHealthState(@NotNull Player player, @NotNull BodyPart part) {
        BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(player);
        return BodyHealthUtils.getBodyHealthState(bodyHealth, part);
    }

    /**
     * Calculates the maximum amount of health that a players body part should be able to have
     * @param player The player to which the body part belongs
     * @param part The body part to calculate the maximum amount of health for
     * @return The maximum amount of health for the given body part
     */
    public double getMaxPartHealth(@NotNull Player player, @NotNull BodyPart part) {
        return BodyHealthUtils.getMaxHealth(part, player);
    }


    /**
     * Registers a custom BodyHealthEffect, making it usable in the effect config
     * @param effect The custom BodyHealthEffect object to register
     * @return true if successfully registered
     */
    public boolean registerCustomEffect(BodyHealthEffect effect) {
        return EffectHandler.registerEffect(effect);
    }

    /**
     * Unregisters a custom BodyHealthEffect, making it no longer usable in the effect config
     * @param effect The custom BodyHealthEffect object to unregister
     * @return true if successfully unregistered
     */
    public boolean unregisterCustomEffect(BodyHealthEffect effect) {
        return EffectHandler.unregisterEffect(effect);
    }


    /**
     * Send a plugin message to a given CommandSender
     * @param sender The CommandSender to send the message to
     * @param message The message (may include formatting and PAPI placeholders)
     */
    public void notifySender(@NotNull CommandSender sender, String message) {
        MessageUtils.notifySender(sender, message);
    }

    /**
     * Send a plugin message to a given player
     * @param player The player to send the message to
     * @param message The message (may include formatting and PAPI placeholders)
     */
    public void notifyPlayer(@NotNull Player player, String message) {
        MessageUtils.notifyPlayer(player, message);
    }

    /**
     * Send a plugin message to the console
     * @param message The message (may include formatting and PAPI placeholders)
     */
    public void notifyConsole(String message) {
        MessageUtils.notifyConsole(message);
    }


    /**
     * Checks whether a player is currently able to interact with a given hand
     * @param player The player to check for
     * @param hand EquipmentSlot.HAND/OFF_HAND
     * @return true if able to interact
     */
    public boolean canPlayerInteract(@NotNull Player player, @NotNull EquipmentSlot hand) {
        return BodyHealthUtils.canPlayerInteract(player, hand);
    }

    /**
     * Checks whether a player is currently able to jump
     * @param player The player to check for
     * @return true if able to jump
     */
    public boolean canPlayerJump(@NotNull Player player) {
        return BodyHealthUtils.canPlayerJump(player);
    }

    /**
     * Checks whether a player is currently able to sprint
     * @param player The player to check for
     * @return true if able to sprint
     */
    public boolean canPlayerSprint(@NotNull Player player) {
        return BodyHealthUtils.canPlayerSprint(player);
    }

    /**
     * Checks whether a player is currently able to walk
     * @param player The player to check for
     * @return true if able to walk
     */
    public boolean canPlayerWalk(@NotNull Player player) {
        return BodyHealthUtils.canPlayerWalk(player);
    }


    /**
     * Evaluates a given math expression, returning -1 on failure
     * @param expression The expression to evaluate/resolve
     * @return The result or -1 on failure
     */
    public double evaluateMathExpression(String expression) {
        return BodyHealthUtils.evaluateExpression(expression);
    }

    /**
     * Checks if a player is vanished with PremiumVanish or SuperVanish
     * @param player The player to check for
     * @return true if vanished
     */
    public boolean isVanished(@NotNull Player player) {
        return VanishPlugins.isVanished(player);
    }

    /**
     * Checks for invalid leftover effects and removes them
     * @param player The player to validate effects for
     */
    public void validateEffects(@NotNull Player player) {
        BodyHealthUtils.validateEffects(player);
    }

}
