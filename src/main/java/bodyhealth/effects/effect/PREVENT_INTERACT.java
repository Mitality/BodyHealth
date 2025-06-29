package bodyhealth.effects.effect;

import bodyhealth.config.Debug;
import bodyhealth.core.BodyPart;
import bodyhealth.effects.BodyHealthEffect;
import bodyhealth.effects.EffectType;
import bodyhealth.util.BodyHealthUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

public class PREVENT_INTERACT implements BodyHealthEffect {

    @Override
    public EffectType getEffectType() {
        return EffectType.PERSISTENT;
    }

    @Override
    public String getIdentifier() {
        return "PREVENT_INTERACT";
    }

    @Override
    public String getUsage() {
        return "PREVENT_INTERACT / <HAND> / [MESSAGE]";
    }

    @Override
    public void onApply(Player player, BodyPart part, String[] args, boolean isRecovery) {

        if (args.length <= 1) {
            Debug.logErr("Effect \"" + args[0].trim() + "\" is missing arguments, check syntax!");
            return;
        }

        try {
            EquipmentSlot hand = EquipmentSlot.valueOf(args[1].trim().toUpperCase());
            BodyHealthUtils.getBodyHealth(player).addToOngoingEffects(part, args);
            Debug.log("(" + part.name() +") Preventing " + hand.name() + " interaction for player " + player.getName());

        } catch (IllegalArgumentException e) {
            Debug.logErr("EquipmentSlot (Hand) \"" + args[1].trim() + "\" is invalid, check syntax!");
        }

    }

    @Override
    public void onRemove(Player player, BodyPart part, String[] args, boolean isRecovery) {

        if (args.length <= 1) {
            Debug.logErr("Effect \"" + args[0].trim() + "\" is missing arguments, check syntax!");
            return;
        }

        try {
            EquipmentSlot hand = EquipmentSlot.valueOf(args[1].trim().toUpperCase());
            BodyHealthUtils.getBodyHealth(player).removeFromOngoingEffects(part, args);
            Debug.log("(" + part.name() +") No longer preventing " + hand.name() + " interaction for player " + player.getName());

        } catch (IllegalArgumentException e) {
            Debug.logErr("EquipmentSlot (Hand) \"" + args[1].trim() + "\" is invalid, check syntax!");
        }

    }

}
