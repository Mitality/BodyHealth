package bodyhealth.core;

import bodyhealth.api.events.BodyPartHealthChangeEvent;
import bodyhealth.config.Config;
import bodyhealth.config.Debug;
import bodyhealth.effects.EffectHandler;
import bodyhealth.util.BodyHealthUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BodyHealth {

    private final Map<BodyPart, Long> command_timestamps;
    private final Map<BodyPart, List<String[]>> ongoingEffects;

    private final EnumMap<BodyPart, Double> healthMap;
    private final UUID playerUUID;

    public BodyHealth(UUID uuid) {
        playerUUID = uuid;
        healthMap = new EnumMap<>(BodyPart.class);
        command_timestamps = new EnumMap<>(BodyPart.class);
        ongoingEffects = new HashMap<>();
        for (BodyPart part : BodyPart.values()) {
            healthMap.put(part, 100.0); // Initialize all parts with 100 health (100%)
        }
    }

    public BodyHealth(UUID uuid, double head, double body, double arm_left, double arm_right, double leg_left, double leg_right, double foot_left, double foot_right) {
        playerUUID = uuid;
        healthMap = new EnumMap<>(BodyPart.class);
        command_timestamps = new EnumMap<>(BodyPart.class);
        ongoingEffects = new HashMap<>();
        healthMap.put(BodyPart.HEAD, head);
        healthMap.put(BodyPart.BODY, body);
        healthMap.put(BodyPart.ARM_LEFT, arm_left);
        healthMap.put(BodyPart.ARM_RIGHT, arm_right);
        healthMap.put(BodyPart.LEG_LEFT, leg_left);
        healthMap.put(BodyPart.LEG_RIGHT, leg_right);
        healthMap.put(BodyPart.FOOT_LEFT, foot_left);
        healthMap.put(BodyPart.FOOT_RIGHT, foot_right);
    }

    /**
     * Retrieves the UUID of the player to which the BodyHealth object belongs to
     * @return The UUID of the player to which the BodyHealth object belongs to
     */
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Applies damage to all BodyParts
     * @param damage The amount of damage to apply
     * @param force If damage should be forcefully applied
     * @param cause Underlying event that caused this health change
     */
    public void applyDamage(double damage, boolean force, @Nullable Event cause) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            for (BodyPart part : BodyPart.values()) {
                applyDamage(player, part, damage, force, cause);
            }
        } else {
            Debug.logErr("Tried to modify data of a player that isn't online. This should never happen automatically and is a bug!");
        }
    }

    /**
     * Applies damage to a given BodyPart
     * @param part The BodyPart to apply damage to
     * @param damage The amount of damage to apply
     * @param force If damage should be forcefully applied
     * @param cause Underlying event that caused this health change
     */
    public void applyDamage(BodyPart part, double damage, boolean force, @Nullable Event cause) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            applyDamage(player, part, damage, force, cause);
        } else {
            Debug.logErr("Tried to modify data of a player that isn't online. This should never happen automatically and is a bug!");
        }
    }

    /**
     * Util method to apply damage to a specific BodyPart of a player that has already been retrieved
     * @param player The already retrieved and online Player
     * @param part The BodyPart to apply damage to
     * @param damage The amount of damage to apply
     * @param force If damage should be forcefully applied
     * @param cause Underlying event that caused this health change
     */
    private void applyDamage(Player player, BodyPart part, double damage, boolean force, @Nullable Event cause) {
        if (!force) {
            if (command_timestamps.containsKey(part) && ((System.currentTimeMillis() - command_timestamps.get(part)) < Config.force_keep_time * 1000L)) return;
            if (player.hasPermission("bodyhealth.bypass.damage." + part.name().toLowerCase())) return;
            if (!BodyHealthUtils.isSystemEnabled(player)) return;
        }
        double currentHealth = healthMap.get(part);
        if (currentHealth > 0) {
            BodyPartState oldState = BodyHealthUtils.getBodyHealthState(this, part);
            double damagePercent = damage / BodyHealthUtils.getMaxHealth(part, player) * 100;
            BodyPartHealthChangeEvent event = new BodyPartHealthChangeEvent(player, part, currentHealth, Math.max(0, currentHealth - damagePercent), cause);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return;
            healthMap.put(part, Math.min(100, Math.max(0, event.getNewHealth()))); // Ensure health stays between 0 and 100
            EffectHandler.onBodyPartStateChange(player, part, oldState, BodyHealthUtils.getBodyHealthState(this, part));
        }
    }

    /**
     * Regenerate health for all BodyParts
     * @param regenAmount The amount of health to regenerate
     * @param force If damage should be forcefully applied
     * @param cause Underlying event that caused this health change
     */
    public void regenerateHealth(double regenAmount, boolean force, @Nullable Event cause) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            for (BodyPart part : BodyPart.values()) {
                regenerateHealth(player, part, regenAmount, force, cause);
            }
        } else {
            Debug.logErr("Tried to modify data of a player that isn't online. This should never happen automatically and is a bug!");
        }
    }

    /**
     * Regenerate health for a specific BodyPart
     * @param regenAmount The amount of health to regenerate
     * @param part The part to regenerate health for
     * @param force If damage should be forcefully applied
     * @param cause Underlying event that caused this health change
     */
    public void regenerateHealth(double regenAmount, BodyPart part, boolean force, @Nullable Event cause) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            regenerateHealth(player, part, regenAmount, force, cause);
        } else {
            Debug.logErr("Tried to modify data of a player that isn't online. This should never happen automatically and is a bug!");
        }
    }

    /**
     * Util method to regenerate health for a specific BodyPart of a player that has already been retrieved
     * @param player The already retrieved and online Player
     * @param part The BodyPart to regenerate health for
     * @param regenAmount The amount of health to regenerate
     * @param force If damage should be forcefully applied
     * @param cause Underlying event that caused this health change
     */
    private void regenerateHealth(Player player, BodyPart part, double regenAmount, boolean force, @Nullable Event cause) {
        if (!force) {
            if (command_timestamps.containsKey(part) && ((System.currentTimeMillis() - command_timestamps.get(part)) < Config.force_keep_time * 1000L)) return;
            if (player.hasPermission("bodyhealth.bypass.regen." + part.name().toLowerCase())) return;
            if (!BodyHealthUtils.isSystemEnabled(player)) return;
        }
        double currentHealth = healthMap.get(part);
        if (currentHealth < 100) {
            BodyPartState oldState = BodyHealthUtils.getBodyHealthState(this, part);
            double regenAmountPercent = regenAmount / BodyHealthUtils.getMaxHealth(part, player) * 100;
            BodyPartHealthChangeEvent event = new BodyPartHealthChangeEvent(player, part, currentHealth, Math.min(100, currentHealth + regenAmountPercent), cause);
            Bukkit.getPluginManager().callEvent(event);
            healthMap.put(part, Math.min(100, Math.max(0, event.getNewHealth()))); // Ensure health stays between 0 and 100
            EffectHandler.onBodyPartStateChange(player, part, oldState, BodyHealthUtils.getBodyHealthState(this, part));
        }
    }

    /**
     * Retrieves the current amount of health (in percent) for a given BodyPart
     * @param part The BodyPart to retrieve the current health from
     * @return The current amount of health of the given BodyPart
     */
    public double getHealth(BodyPart part) {
        return healthMap.get(part);
    }

    /**
     * Sets the health (in percent) for all BodyParts to a given number
     * @param newHealth The amount of health to set the BodyPart to
     * @param force_keep Whether the new health state should be forcefully kept
     * @param cause Underlying event that caused this health change
     */
    public void setHealth(double newHealth, boolean force_keep, @Nullable Event cause) {
        for (BodyPart part : BodyPart.values()) {
            setHealth(part, newHealth, force_keep, cause);
        }
    }

    /**
     * Sets the health (in percent) for a given BodyPart to a given number
     * @param part The BodyPart to set the health for
     * @param newHealth The amount of health to set the BodyPart to
     * @param force_keep Whether the new health state should be forcefully kept
     * @param cause Underlying event that caused this health change
     */
    public void setHealth(BodyPart part, double newHealth, boolean force_keep, @Nullable Event cause) {
        BodyPartState oldState = BodyHealthUtils.getBodyHealthState(this, part);
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            BodyPartHealthChangeEvent event = new BodyPartHealthChangeEvent(player, part, healthMap.get(part), Math.min(100, Math.max(0, newHealth)), cause);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return;
            healthMap.put(part, Math.min(100, Math.max(0, event.getNewHealth()))); // Ensure health stays between 0 and 100
            if (force_keep) command_timestamps.put(part, System.currentTimeMillis());
            if (!BodyHealthUtils.isSystemEnabled(player)) return;
            EffectHandler.onBodyPartStateChange(player, part, oldState, BodyHealthUtils.getBodyHealthState(this, part));
        }
    }

    /**
     * Retrieves a list of all currently ongoing effects for a given BodyPart
     * @return The BodyPart to retrieve all ongoing effects for
     */
    public Map<BodyPart, List<String[]>> getOngoingEffects() {
        return ongoingEffects;
    }

    /**
     * Adds a given effect to the list of currently ongoing effects of a BodyPart
     * @param part The BodyPart to which the given effect should be added
     * @param effect The effect to add to the given BodyPart
     */
    public void addToOngoingEffects(BodyPart part, String[] effect) {
        if (ongoingEffects.containsKey(part))  ongoingEffects.get(part).add(effect);
        else {
            List<String[]> effectsList = new ArrayList<>();
            effectsList.add(effect);
            ongoingEffects.put(part, effectsList);
        }
    }

    /**
     * Removes a given effect from the list of currently ongoing effects of a BodyPart
     * @param part The BodyPart from which the given effect should be removed
     * @param effect The effect to remove from the given BodyPart
     */
    public void removeFromOngoingEffects(BodyPart part, String[] effect) {
        if (ongoingEffects.containsKey(part)) {
            List<String[]> effectsList = ongoingEffects.get(part);
            effectsList.removeIf(existingEffect -> Arrays.equals(existingEffect, effect));
            if (effectsList.isEmpty()) ongoingEffects.remove(part);
        }
    }

}
