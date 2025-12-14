package bodyhealth.effects;

import bodyhealth.api.events.BodyPartStateChangeEvent;
import bodyhealth.core.BodyHealth;
import bodyhealth.core.BodyPart;
import bodyhealth.core.BodyPartState;
import bodyhealth.effects.effect.*;
import bodyhealth.util.BodyHealthUtils;
import bodyhealth.config.Config;
import bodyhealth.config.Debug;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;

import javax.annotation.Nullable;
import java.util.*;

public class EffectHandler {

    private static Map<String, BodyHealthEffect> effects = Map.ofEntries(
        Map.entry("POTION_EFFECT", new POTION_EFFECT()),
        Map.entry("PREVENT_INTERACT", new PREVENT_INTERACT()),
        Map.entry("PREVENT_SPRINT", new PREVENT_SPRINT()),
        Map.entry("PREVENT_WALK", new PREVENT_WALK()),
        Map.entry("PREVENT_JUMP", new PREVENT_JUMP()),
        Map.entry("KILL_PLAYER", new KILL_PLAYER()),
        Map.entry("COMMAND", new COMMAND()),
        Map.entry("COMMAND_UNDO", new COMMAND_UNDO()),
        Map.entry("MESSAGE", new MESSAGE()),
        Map.entry("SOUND", new SOUND()),
        Map.entry("REPEATING", new REPEATING()),
        Map.entry("DELAYED", new DELAYED()),
        Map.entry("WHEN_DAMAGED", new WHEN_DAMAGED()),
        Map.entry("WHEN_HEALED", new WHEN_HEALED())
    );

    /**
     * Registers a BodyHealthEffect, making it usable in the effect config
     * @param effect The BodyHealthEffect object to register
     * @return true if successfully registered
     */
    public static boolean registerEffect(BodyHealthEffect effect) {
        if (effects.containsKey(effect.getIdentifier())) return false;
        effects.put(effect.getIdentifier(), effect);
        return true;
    }

    /**
     * Unregisters a BodyHealthEffect, making it no longer usable in the effect config
     * @param effect The BodyHealthEffect object to unregister
     * @return true if successfully unregistered
     */
    public static boolean unregisterEffect(BodyHealthEffect effect) {
        if (!effects.containsKey(effect.getIdentifier())) return false;
        effects.remove(effect.getIdentifier());
        return true;
    }

    /**
     * Retrieves all currently registered effects, mapped by their identifier
     * @return A Map of all currently registered effects with their identifier
     */
    public static Map<String, BodyHealthEffect> getRegisteredEffects() {
        return effects;
    }

    public static List<Player> preventSprint = new ArrayList<>();

    /**
     * Calculate what should happen in terms of effects when the BodyPart of a player changes its state
     * @param player The player to which the BodyPart that has changed its state belongs to
     * @param part The BodyPart that has changed its state
     * @param oldState The previous state of the BodyPart
     * @param newState The new state of the BodyPart
     */
    public static void onBodyPartStateChange(Player player, BodyPart part, @Nullable BodyPartState oldState, @Nullable BodyPartState newState) {
        if (oldState == newState) return; // Only calculate effects when necessary
        Bukkit.getPluginManager().callEvent(new BodyPartStateChangeEvent(player, part, oldState, newState));
        if (Config.effects == null) {
            Debug.logErr("The effects section of your configuration is missing! What on earth did you do!?");
            return; // Should not be possible to accomplish, but let's leave this here just in case
        }
        if (!Config.effects.getKeys(false).contains(part.name())) return; // Nothing configured for this BodyPart
        boolean isRecovery = oldState != null && newState != null && newState.ordinal() < oldState.ordinal();
        if (oldState != null && Objects.requireNonNull(Config.effects.getConfigurationSection(part.name())).getKeys(false).contains(oldState.name())) removeEffects(player, part, oldState, isRecovery);
        if (newState != null && Objects.requireNonNull(Config.effects.getConfigurationSection(part.name())).getKeys(false).contains(newState.name())) applyEffects(player, part, newState, isRecovery);
    }

    /**
     * Applies all configured effects for a BodyPart and BodyPartState
     * @param player The player to which the BodyPart belongs to
     * @param part The BodyPart to calculate the effects for
     * @param state The BodyPartState to calculate the effects for
     * @param isRecovery If the BodyPartState was reached due to recovery
     */
    private static void applyEffects(Player player, BodyPart part, BodyPartState state, boolean isRecovery) {
        try {
            List<String> effects = Objects.requireNonNull(Config.effects.getConfigurationSection(part.name())).getStringList(state.name());
            for (String effect : effects) {
                applyEffect(player, part, effect, isRecovery);
            }
        } catch (NullPointerException e) {
            Debug.logErr("Effects for state " + state.name() + " of part " + part.name() + " are not configured!");
        }
    }

    /**
     * Removes all configured effects for a BodyPart and BodyPartState
     * @param player The player to which the BodyPart belongs to
     * @param part The BodyPart to remove the effects from
     * @param state The BodyPartState to remove the effects from
     * @param isRecovery If the BodyPartState was reached due to recovery
     */
    private static void removeEffects(Player player, BodyPart part, BodyPartState state, boolean isRecovery) {
        try {
            List<String> effects = Objects.requireNonNull(Config.effects.getConfigurationSection(part.name())).getStringList(state.name());
            for (String effect : effects) {
                removeEffect(player, part, effect, isRecovery);
            }
        } catch (NullPointerException e) {
            Debug.logErr("Effects for state " + state.name() + " of part " + part.name() + " are not configured!");
        }
    }

    /**
     * Utility method to apply a specific effect to a BodyPart
     * @param player The player to which the BodyPart belongs to
     * @param part The BodyPart because of which the effect should be applied
     * @param effect The effect to apply to the given player
     */
    private static void applyEffect(Player player, BodyPart part, String effect, boolean isRecovery) {

        String[] effectParts = effect.split("/");
        String effectIdentifier = effectParts[0].trim();

        if (!effects.containsKey(effectIdentifier.toUpperCase())) {
            Debug.logErr("Effect " + effectParts[0].trim() + " is invalid, check syntax!");
            return;
        }
        BodyHealthEffect effectObject = effects.get(effectIdentifier.toUpperCase());

        // ORDER IMPORTANT:
        effectObject.onApply(player, part, effectParts, isRecovery);
        BodyHealthUtils.getBodyHealth(player).addToOngoingEffects(part, effectParts);
    }

    /**
     * Utility method to remove a specific effect from a BodyPart
     * @param player The player to which the BodyPart belongs to
     * @param part The BodyPart because of which the effect should be removed
     * @param effect The effect to remove from the given player
     */
    private static void removeEffect(Player player, BodyPart part, String effect, boolean isRecovery) {

        String[] effectParts = effect.split("/");
        String effectIdentifier = effectParts[0].trim();

        if (!effects.containsKey(effectIdentifier.toUpperCase())) {
            Debug.logErr("Effect " + effectParts[0].trim() + " is invalid, check syntax!");
            return;
        }
        BodyHealthEffect effectObject = effects.get(effectIdentifier.toUpperCase());

        // ORDER IMPORTANT:
        BodyHealthUtils.getBodyHealth(player).removeFromOngoingEffects(part, effectParts);
        effectObject.onRemove(player, part, effectParts, isRecovery);
    }

    /**
     * Utility method to remove all currently active effects from a given player
     * @param player The player from which to remove all ongoing effects
     */
    public static void removeOngoingEffects(Player player) {
        BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(player);
        Map<BodyPart, List<String[]>> ongoingEffects = bodyHealth.getOngoingEffects();

        for (
            Iterator<Map.Entry<BodyPart, List<String[]>>> it = ongoingEffects.entrySet().iterator();
            it.hasNext();
        ) {
            Map.Entry<BodyPart, List<String[]>> entry = it.next();

            for (
                ListIterator<String[]> lit = entry.getValue().listIterator();
                lit.hasNext();
            ) {
                String[] effectParts = lit.next();

                String effectIdentifier = effectParts[0].trim().toUpperCase();
                BodyHealthEffect effectObject = effects.get(effectIdentifier);

                lit.remove(); // Remove from ongoingEffects
                if (effectObject == null) continue;
                effectObject.onRemove(player, entry.getKey(), effectParts, true);
            }

            if (entry.getValue().isEmpty()) it.remove();
        }
        bodyHealth.clearOngoingEffects(); // Just to be sure
    }

    /**
     * Utility method to get an attribute modifier that reduces sprint speed to walk speed
     * @return An attribute modifier that reduces sprint speed to walk speed
     */
    public static AttributeModifier getSpeedReductionModifier() {
        return new AttributeModifier(new NamespacedKey("bodyhealth", "sprint_denial"), -0.2308, AttributeModifier.Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.ANY);
    }

    /**
     * Utility method to get an attribute modifier that reduces walk speed to zero (or 15% if lenient)
     * @return An attribute modifier that reduces walk speed to zero
     */
    public static AttributeModifier getWalkDenialModifier() {
        double amount = Config.lenient_movement_restrictions ? -0.85 : -1.0;
        return new AttributeModifier(new NamespacedKey("bodyhealth", "walk_denial"), amount, AttributeModifier.Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.ANY);
    }

    /**
     * Utility method to get an attribute modifier that reduces jump strength to zero (or 15% if lenient)
     * @return An attribute modifier that reduces jump strength to zero
     */
    public static AttributeModifier getJumpDenialModifier() {
        double amount = Config.lenient_movement_restrictions ? -0.85 : -1.0;
        return new AttributeModifier(new NamespacedKey("bodyhealth", "jump_denial"), amount, AttributeModifier.Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.ANY);
    }

    /**
     * Removes all ongoing effects from a given player
     * @param player The player to remove all effects from
     */
    public static void removeEffectsFromPlayer(Player player) {
        // Remove effects
        BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(player);
        for (BodyPart part : BodyPart.values()) {
            EffectHandler.onBodyPartStateChange(player, part, BodyHealthUtils.getBodyHealthState(bodyHealth, part), null);
        }
        // Ensure that all attribute modifiers are removed
        EffectHandler.preventSprint.remove(player);
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).removeModifier(EffectHandler.getSpeedReductionModifier());
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).removeModifier(EffectHandler.getWalkDenialModifier());
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).removeModifier(EffectHandler.getJumpDenialModifier());
        // Remove invalid effects if present
        BodyHealthUtils.validateEffects(player);
    }

    /**
     * Adds all effects to a player that should currently be applied
     * @param player The player to add all effects to
     */
    public static void addEffectsToPlayer(Player player) {
        // Add effects
        BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(player);
        for (BodyPart part : BodyPart.values()) {
            EffectHandler.onBodyPartStateChange(player, part, null, BodyHealthUtils.getBodyHealthState(bodyHealth, part));
        }
    }

}
