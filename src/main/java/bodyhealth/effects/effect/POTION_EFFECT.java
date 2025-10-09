package bodyhealth.effects.effect;

import bodyhealth.config.Debug;
import bodyhealth.core.BodyPart;
import bodyhealth.effects.BodyHealthEffect;
import bodyhealth.effects.EffectType;
import bodyhealth.util.BodyHealthUtils;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

public class POTION_EFFECT implements BodyHealthEffect {

    @Override
    public EffectType getEffectType() {
        return EffectType.PERSISTENT;
    }

    @Override
    public String getIdentifier() {
        return "POTION_EFFECT";
    }

    @Override
    public String getUsage() {
        return "POTION_EFFECT / <EFFECT> / [AMPLIFIER]";
    }

    @Override
    public void onApply(Player player, BodyPart part, String[] args, boolean isRecovery) {

        if (args.length <= 1) {
            Debug.logErr("Effect \"" + args[0].trim() + "\" is missing arguments, check syntax!");
            return;
        }

        String effectName = args[1].trim().toUpperCase();
        int amplifier = (args.length > 2) ? Integer.parseInt(args[2].trim()) : 1;
        amplifier = Math.min(255, Math.max(1, amplifier)); // Ensure amplifier is between 0 and 255
        PotionEffectType effectType = PotionEffectType.getByName(effectName);

        if (effectType == null) {
            Debug.logErr("EffectType \"" + args[1].trim() + "\" is invalid, check syntax!");
            return;
        }

        if (player.getPotionEffect(effectType) != null
                && Objects.requireNonNull(player.getPotionEffect(effectType)).getDuration() == PotionEffect.INFINITE_DURATION
                && Objects.requireNonNull(player.getPotionEffect(effectType)).getAmplifier() >= amplifier)
            return; // Player should keep infinite effects with a higher or equal amplifier
        player.addPotionEffect(new PotionEffect(effectType, PotionEffect.INFINITE_DURATION, amplifier-1));
        Debug.log("(" + part.name() +") Applied effect \"" + args[1].trim() + "\" to player " + player.getName());

    }

    @Override
    public void onRemove(Player player, BodyPart part, String[] args, boolean isRecovery) {

        if (args.length <= 1) {
            Debug.logErr("Effect \"" + args[0].trim() + "\" is missing arguments, check syntax!");
            return;
        }

        String effectName = args[1].trim().toUpperCase();
        PotionEffectType effectType = PotionEffectType.getByName(effectName);

        if (effectType == null) {
            Debug.logErr("EffectType \"" + args[1].trim() + "\" is invalid, check syntax!");
            return;
        }

        int highestAmplifier = BodyHealthUtils.getHighestPotionEffectAmplifier(player, effectType);

        if (highestAmplifier >= 0) {
            player.addPotionEffect(new PotionEffect(effectType, PotionEffect.INFINITE_DURATION, highestAmplifier-1));
            Debug.log("(" + part.name() +") Set PotionEffect \"" + args[1].trim() + "\" to amplifier " + highestAmplifier + " for player " + player.getName());
        } else {
            player.removePotionEffect(effectType);
            Debug.log("(" + part.name() +") Removed PotionEffect \"" + args[1].trim() + "\" from player " + player.getName());
        }

    }

}
