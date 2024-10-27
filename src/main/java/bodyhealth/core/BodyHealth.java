package bodyhealth.core;

import bodyhealth.config.Config;
import bodyhealth.config.Debug;
import bodyhealth.effects.BodyHealthEffects;
import bodyhealth.util.BodyHealthUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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

    /**
     * Applies damage to a given BodyPart
     * @param part The BodyPart to apply damage to
     * @param damage The amount of damage to apply
     */
    public void applyDamage(BodyPart part, double damage) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            if (command_timestamps.containsKey(part) && ((System.currentTimeMillis() - command_timestamps.get(part)) < Config.force_keep_time * 1000L)) return;
            if (!BodyHealthUtils.isSystemEnabled(player)) return;
            if (player.hasPermission("bodyhealth.bypass.damage." + part.name().toLowerCase())) return;
            double currentHealth = healthMap.get(part);
            BodyPartState oldState = BodyHealthUtils.getBodyHealthState(this, part);
            double damagePercent = damage / BodyHealthUtils.getMaxHealth(part, player) * 100;
            healthMap.put(part, Math.max(0, currentHealth - damagePercent)); // Ensure health never goes below 0
            BodyHealthEffects.onBodyPartStateChange(player, part, oldState, BodyHealthUtils.getBodyHealthState(this, part));
        } else {
            Debug.logErr("Tried to modify data of a player that isn't online. This should never happen automatically and is a bug!");
        }
    }

    /**
     * Regenerate health for all BodyParts
     * @param regenAmount The amount of health to regenerate
     */
    public void regenerateHealth(double regenAmount) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            if (!BodyHealthUtils.isSystemEnabled(player)) return;
            for (BodyPart part : BodyPart.values()) {
                if (command_timestamps.containsKey(part) && ((System.currentTimeMillis() - command_timestamps.get(part)) < Config.force_keep_time * 1000L)) return;
                if (player.hasPermission("bodyhealth.bypass.regen." + part.name().toLowerCase())) return;
                double currentHealth = healthMap.get(part);
                if (currentHealth < 100) {
                    BodyPartState oldState = BodyHealthUtils.getBodyHealthState(this, part);
                    double regenAmountPercent = regenAmount / BodyHealthUtils.getMaxHealth(part, player) * 100;
                    healthMap.put(part, Math.min(100, currentHealth + regenAmountPercent)); // Ensure health does not exceed max health
                    BodyHealthEffects.onBodyPartStateChange(player, part, oldState, BodyHealthUtils.getBodyHealthState(this, part));
                }
            }
        } else {
            Debug.logErr("Tried to modify data of a player that isn't online. This should never happen automatically and is a bug!");
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
     * Sets the health (in percent) for a given BodyPart to a given number
     * @param part The BodyPart to set the health for
     * @param newHealth The amount of health to set the BodyPart to
     */
    public void setHealth(BodyPart part, double newHealth) {
        BodyPartState oldState = BodyHealthUtils.getBodyHealthState(this, part);
        healthMap.put(part, Math.min(100, Math.max(0, newHealth))); // Keep health between 0 and 100
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            if (!BodyHealthUtils.isSystemEnabled(player)) return;
            BodyHealthEffects.onBodyPartStateChange(player, part, oldState, BodyHealthUtils.getBodyHealthState(this, part));
            command_timestamps.put(part, System.currentTimeMillis());
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
