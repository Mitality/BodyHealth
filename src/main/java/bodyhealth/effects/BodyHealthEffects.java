package bodyhealth.effects;

import bodyhealth.object.BodyHealth;
import bodyhealth.object.BodyPart;
import bodyhealth.object.BodyPartState;
import bodyhealth.util.BodyHealthUtils;
import bodyhealth.config.Config;
import bodyhealth.debug.Debug;
import bodyhealth.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nullable;
import java.util.*;

public class BodyHealthEffects {
    public static List<Player> preventSprint = new ArrayList<>();
    public static List<Player> preventWalk = new ArrayList<>();
    public static List<Player> preventJump = new ArrayList<>();

    public static void onBodyPartStateChange(Player player, BodyPart part, @Nullable BodyPartState oldState, @Nullable BodyPartState newState) {
        if (Config.effects == null) {
            Debug.logErr("The effects section of your configuration is missing! What on earth did you do!?");
            return;
        }
        if (!Config.effects.getKeys(false).contains(part.name())) return; // Nothing configured for this BodyPart
        if (oldState != null && Objects.requireNonNull(Config.effects.getConfigurationSection(part.name())).getKeys(false).contains(oldState.name())) removeEffects(player, part, oldState);
        if (newState != null && Objects.requireNonNull(Config.effects.getConfigurationSection(part.name())).getKeys(false).contains(newState.name())) applyEffects(player, part, newState);
    }

    private static void applyEffects(Player player, BodyPart part, BodyPartState state) {
        try {
            List<String> effects = Objects.requireNonNull(Config.effects.getConfigurationSection(part.name())).getStringList(state.name());
            for (String effect : effects) {
                applyEffect(player, part, effect);
            }
        } catch (NullPointerException e) {
            Debug.logErr("Effects for state " + state.name() + " of part " + part.name() + " are not configured!");
        }
    }

    private static void removeEffects(Player player, BodyPart part, BodyPartState state) {
        try {
            List<String> effects = Objects.requireNonNull(Config.effects.getConfigurationSection(part.name())).getStringList(state.name());
            for (String effect : effects) {
                removeEffect(player, part, effect);
            }
        } catch (NullPointerException e) {
            Debug.logErr("Effects for state " + state.name() + " of part " + part.name() + " are not configured!");
        }
    }

    private static void applyEffect(Player player, BodyPart part, String effect) {
        String[] effectParts = effect.split("/");

        // POTION_EFFECT / <EFFECT> / [AMPLIFIER]
        if (effectParts[0].trim().equalsIgnoreCase("POTION_EFFECT")) {
            if (effectParts.length > 1) {

                String effectName = effectParts[1].trim().toUpperCase();
                int amplifier = (effectParts.length > 2) ? Integer.parseInt(effectParts[2].trim()) : 1;
                amplifier = Math.max(255, Math.min(1, amplifier)); // Ensure amplifier is between 0 and 255
                PotionEffectType effectType = PotionEffectType.getByName(effectName); // FIXME: Deprecated method - What else to use?

                if (effectType != null) {

                    BodyHealthUtils.getBodyHealth(player).addToOngoingEffects(part, effectParts);
                    if (player.getPotionEffect(effectType) != null
                            && Objects.requireNonNull(player.getPotionEffect(effectType)).getDuration() == PotionEffect.INFINITE_DURATION
                            && Objects.requireNonNull(player.getPotionEffect(effectType)).getAmplifier() >= amplifier)
                        return; // Player should keep infinite effects with a higher or equal amplifier
                    player.addPotionEffect(new PotionEffect(effectType, PotionEffect.INFINITE_DURATION, amplifier-1));
                    Debug.log("(" + part.name() +") Applied effect \"" + effectParts[1].trim() + "\" to player " + player.getName());

                } else {
                    Debug.logErr("EffectType \"" + effectParts[1].trim() + "\" is invalid, check syntax!");
                }

            } else {
                Debug.logErr("Effect \"" + effectParts[0].trim() + "\" is missing arguments, check syntax!");
            }

        }

        // PREVENT_INTERACT / <HAND> / [MESSAGE]
        else if (effectParts[0].trim().equalsIgnoreCase("PREVENT_INTERACT")) {
            if (effectParts.length > 1) {
                try {
                    EquipmentSlot hand = EquipmentSlot.valueOf(effectParts[1].trim().toUpperCase());
                    BodyHealthUtils.getBodyHealth(player).addToOngoingEffects(part, effectParts);
                    Debug.log("(" + part.name() +") Preventing " + hand.name() + " interaction for player " + player.getName());

                } catch (IllegalArgumentException e) {
                    Debug.logErr("EquipmentSlot (Hand) \"" + effectParts[1].trim() + "\" is invalid, check syntax!");
                }
            } else {
                Debug.logErr("Effect \"" + effectParts[0].trim() + "\" is missing arguments, check syntax!");
            }
        }

        // PREVENT_SPRINT / [MESSAGE]
        else if (effectParts[0].trim().equalsIgnoreCase("PREVENT_SPRINT")) {
            BodyHealthUtils.getBodyHealth(player).addToOngoingEffects(part, effectParts);
            Debug.log("(" + part.name() +") Preventing sprint for player " + player.getName());
        }

        // PREVENT_WALK
        else if (effectParts[0].trim().equalsIgnoreCase("PREVENT_WALK")) {
            BodyHealthUtils.getBodyHealth(player).addToOngoingEffects(part, effectParts);
            Debug.log("(" + part.name() +") Preventing walk for player " + player.getName());
            if (!preventWalk.contains(player)) {
                BodyHealthEffects.preventWalk.add(player);
                Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).addModifier(BodyHealthEffects.getWalkDenialModifier());
                Debug.log("Adding SpeedReductionModifier to player " + player.getName());
            }
        }

        // PREVENT_JUMP
        else if (effectParts[0].trim().equalsIgnoreCase("PREVENT_JUMP")) {
            BodyHealthUtils.getBodyHealth(player).addToOngoingEffects(part, effectParts);
            Debug.log("(" + part.name() +") Preventing jump for player " + player.getName());
            if (!preventJump.contains(player)) {
                BodyHealthEffects.preventJump.add(player);
                Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).addModifier(BodyHealthEffects.getJumpDenialModifier());
                Debug.log("Adding JumpDenialModifier to player " + player.getName());
            }
        }

        // KILL_PLAYER
        else if (effectParts[0].trim().equalsIgnoreCase("KILL_PLAYER")) {
            if (!player.isDead()) player.setHealth(0.0);
            Debug.log("(" + part.name() +") Killed player " + player.getName());
        }

        // COMMAND / cmd args[...]
        else if (effectParts[0].trim().equalsIgnoreCase("COMMAND")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), effectParts[1].trim()
                    .replaceAll("%PlayerName%", player.getName())
                    .replaceAll("%PlayerUUID%", player.getUniqueId().toString()));
            Debug.log("(" + part.name() +") Dispatched command: /" + effectParts[1].trim());
        }

        // COMMAND_UNDO / cmd args[...]
        else if (effectParts[0].trim().equalsIgnoreCase("COMMAND_UNDO")) {}

        // MESSAGE / <MESSAGE>
        else if (effectParts[0].trim().equalsIgnoreCase("SEND_MESSAGE")) {
            if (effectParts.length > 1) {
                MessageUtils.notifyPlayer(player, effectParts[1]);
            } else {
                Debug.logErr("Effect \"" + effectParts[0].trim() + "\" is missing arguments, check syntax!");
            }
        }

        else {
            Debug.logErr("Effect " + effectParts[0].trim() + " is invalid, check syntax!");
        }

    }

    private static void removeEffect(Player player, BodyPart part, String effect) {
        String[] effectParts = effect.split("/");

        // POTION_EFFECT / <EFFECT> / [AMPLIFIER]
        if (effectParts[0].trim().equalsIgnoreCase("POTION_EFFECT")) {
            if (effectParts.length > 1) {

                String effectName = effectParts[1].trim().toUpperCase();
                PotionEffectType effectType = PotionEffectType.getByName(effectName); // FIXME: Deprecated method - What else to use?

                if (effectType != null) {

                    BodyHealthUtils.getBodyHealth(player).removeFromOngoingEffects(part, effectParts);
                    int highestAmplifier = BodyHealthUtils.getHighestPotionEffectAmplifier(player, effectType);

                    if (highestAmplifier >= 0) {
                        player.addPotionEffect(new PotionEffect(effectType, PotionEffect.INFINITE_DURATION, highestAmplifier-1));
                        Debug.log("(" + part.name() +") Set PotionEffect \"" + effectParts[1].trim() + "\" to amplifier " + highestAmplifier + " for player " + player.getName());
                    } else {
                        player.removePotionEffect(effectType);
                        Debug.log("(" + part.name() +") Removed PotionEffect \"" + effectParts[1].trim() + "\" from player " + player.getName());
                    }

                } else {
                    Debug.logErr("EffectType \"" + effectParts[1].trim() + "\" is invalid, check syntax!");
                }

            } else {
                Debug.logErr("Effect \"" + effectParts[0].trim() + "\" is missing arguments, check syntax!");
            }

        }

        // PREVENT_INTERACT / <HAND> / [MESSAGE]
        else if (effectParts[0].trim().equalsIgnoreCase("PREVENT_INTERACT")) {
            if (effectParts.length > 1) {
                try {
                    EquipmentSlot hand = EquipmentSlot.valueOf(effectParts[1].trim().toUpperCase());
                    BodyHealthUtils.getBodyHealth(player).removeFromOngoingEffects(part, effectParts);
                    Debug.log("(" + part.name() +") No longer preventing " + hand.name() + " interaction for player " + player.getName());

                } catch (IllegalArgumentException e) {
                    Debug.logErr("EquipmentSlot (Hand) \"" + effectParts[1].trim() + "\" is invalid, check syntax!");
                }
            } else {
                Debug.logErr("Effect \"" + effectParts[0].trim() + "\" is missing arguments, check syntax!");
            }
        }

        // PREVENT_SPRINT / [MESSAGE]
        else if (effectParts[0].trim().equalsIgnoreCase("PREVENT_SPRINT")) {
            BodyHealthUtils.getBodyHealth(player).removeFromOngoingEffects(part, effectParts);
            Debug.log("(" + part.name() +") No longer preventing sprint for player " + player.getName());
        }

        // PREVENT_WALK
        else if (effectParts[0].trim().equalsIgnoreCase("PREVENT_WALK")) {
            BodyHealthUtils.getBodyHealth(player).removeFromOngoingEffects(part, effectParts);
            Debug.log("(" + part.name() +") No longer preventing walk for player " + player.getName());
            if (BodyHealthUtils.canPlayerWalk(player) && preventWalk.contains(player)) {
                BodyHealthEffects.preventWalk.remove(player);
                Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).removeModifier(BodyHealthEffects.getWalkDenialModifier());
                Debug.log("Removing WalkDenialModifier from player " + player.getName());
            }
        }

        // PREVENT_JUMP
        else if (effectParts[0].trim().equalsIgnoreCase("PREVENT_JUMP")) {
            BodyHealthUtils.getBodyHealth(player).removeFromOngoingEffects(part, effectParts);
            Debug.log("(" + part.name() +") No longer preventing jump for player " + player.getName());
            if (BodyHealthUtils.canPlayerJump(player) && preventJump.contains(player)) {
                BodyHealthEffects.preventJump.remove(player);
                Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).removeModifier(BodyHealthEffects.getJumpDenialModifier());
                Debug.log("Removing WalkDenialModifier from player " + player.getName());
            }
        }

        // COMMAND / cmd args[...]
        else if (effectParts[0].trim().equalsIgnoreCase("COMMAND")) {}

        // COMMAND_UNDO / cmd args[...]
        else if (effectParts[0].trim().equalsIgnoreCase("COMMAND_UNDO")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), effectParts[1].trim()
                    .replaceAll("%PlayerName%", player.getName())
                    .replaceAll("%PlayerUUID%", player.getUniqueId().toString()));
            Debug.log("(" + part.name() +") Dispatched command: /" + effectParts[1].trim());
        }

        // KILL_PLAYER
        else if (effectParts[0].trim().equalsIgnoreCase("KILL_PLAYER")) {}

        // MESSAGE / <MESSAGE>
        else if (effectParts[0].trim().equalsIgnoreCase("SEND_MESSAGE")) {}

        else {
            Debug.logErr("Effect " + effectParts[0].trim() + " is invalid, check syntax!");
        }

    }

    public static AttributeModifier getSpeedReductionModifier() {
        return new AttributeModifier(UUID.fromString("123e4567-e89b-12d3-a456-426614174001"), "Reduce sprint speed to walk", -0.2308, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
    }

    public static AttributeModifier getWalkDenialModifier() {
        return new AttributeModifier(UUID.fromString("123e4567-e89b-12d3-a456-426614174002"), "Reduce walk speed to zero", -1.0, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
    }

    public static AttributeModifier getJumpDenialModifier() {
        return new AttributeModifier(UUID.fromString("123e4567-e89b-12d3-a456-426614174003"), "Reduce jump strength to zero", -1.0, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
    }

    public static void removeEffectsFromPlayer(Player player) {
        // Remove effects
        BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(player);
        for (BodyPart part : BodyPart.values()) {
            BodyHealthEffects.onBodyPartStateChange(player, part, BodyHealthUtils.getBodyHealthState(bodyHealth, part), null);
        }
        // Ensure that all attribute modifiers are removed
        if (BodyHealthEffects.preventSprint.contains(player)) {
            BodyHealthEffects.preventSprint.remove(player);
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).removeModifier(BodyHealthEffects.getSpeedReductionModifier());
            Debug.log("Removing SpeedReductionModifier from player " + player.getName());
        }
        if (BodyHealthEffects.preventWalk.contains(player)) {
            BodyHealthEffects.preventWalk.remove(player);
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).removeModifier(BodyHealthEffects.getWalkDenialModifier());
            Debug.log("Removing WalkDenialModifier from player " + player.getName());
        }
        if (BodyHealthEffects.preventJump.contains(player)) {
            BodyHealthEffects.preventJump.remove(player);
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).removeModifier(BodyHealthEffects.getJumpDenialModifier());
            Debug.log("Removing JumpDenialModifier from player " + player.getName());
        }
        BodyHealthUtils.validateEffects(player);
    }

    public static void addEffectsToPlayer(Player player) {
        // Add effects
        BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(player);
        for (BodyPart part : BodyPart.values()) {
            BodyHealthEffects.onBodyPartStateChange(player, part, null, BodyHealthUtils.getBodyHealthState(bodyHealth, part));
        }
    }

}