package bodyhealth.core;

import bodyhealth.config.Debug;
import bodyhealth.effects.BodyHealthEffects;
import bodyhealth.util.BodyHealthUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class BodyHealth {

    private final Map<BodyPart, List<String[]>> ongoingEffects;

    private final EnumMap<BodyPart, Double> healthMap;
    private final UUID playerUUID;

    public BodyHealth(UUID uuid) {
        playerUUID = uuid;
        healthMap = new EnumMap<>(BodyPart.class);
        ongoingEffects = new HashMap<>();
        for (BodyPart part : BodyPart.values()) {
            healthMap.put(part, 100.0); // Initialize all parts with 100 health (100%)
        }
    }


    public void applyDamage(BodyPart part, double damage) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            double currentHealth = healthMap.get(part);
            BodyPartState oldState = BodyHealthUtils.getBodyHealthState(this, part);
            double damagePercent = damage / BodyHealthUtils.getMaxHealth(part, player) * 100;
            healthMap.put(part, Math.max(0, currentHealth - damagePercent)); // Ensure health never goes below 0
            BodyHealthEffects.onBodyPartStateChange(player, part, oldState, BodyHealthUtils.getBodyHealthState(this, part));
        } else {
            Debug.logErr("Tried to modify data of a player that isn't online. This should never happen automatically and is a bug!");
        }
    }

    public void regenerateHealth(double regenAmount) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            for (BodyPart part : BodyPart.values()) {
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


    public double getHealth(BodyPart part) {
        return healthMap.get(part);
    }

    public void setHealth(BodyPart part, double newHealth) {
        BodyPartState oldState = BodyHealthUtils.getBodyHealthState(this, part);
        healthMap.put(part, Math.min(100, Math.max(0, newHealth))); // Keep health between 0 and 100
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            BodyHealthEffects.onBodyPartStateChange(player, part, oldState, BodyHealthUtils.getBodyHealthState(this, part));
        }
    }

    public Map<BodyPart, List<String[]>> getOngoingEffects() {
        return ongoingEffects;
    }

    public void addToOngoingEffects(BodyPart part, String[] effect) {
        if (ongoingEffects.containsKey(part))  ongoingEffects.get(part).add(effect);
        else {
            List<String[]> effectsList = new ArrayList<>();
            effectsList.add(effect);
            ongoingEffects.put(part, effectsList);
        }
    }

    public void removeFromOngoingEffects(BodyPart part, String[] effect) {
        if (ongoingEffects.containsKey(part)) {
            List<String[]> effectsList = ongoingEffects.get(part);
            effectsList.removeIf(existingEffect -> Arrays.equals(existingEffect, effect));
            if (effectsList.isEmpty()) ongoingEffects.remove(part);
        }
    }

}
