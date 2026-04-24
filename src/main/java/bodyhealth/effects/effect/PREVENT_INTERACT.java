package bodyhealth.effects.effect;

import bodyhealth.config.Config;
import bodyhealth.config.Debug;
import bodyhealth.core.BodyHealth;
import bodyhealth.core.BodyPart;
import bodyhealth.effects.BodyHealthEffect;
import bodyhealth.effects.EffectType;
import bodyhealth.util.BodyHealthUtils;
import bodyhealth.util.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.List;
import java.util.Map;

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
            Debug.log("(" + part.name() + ") Preventing " + hand.name() + " interaction for player " + player.getName());
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
            Debug.log("(" + part.name() + ") No longer preventing " + hand.name() + " interaction for player " + player.getName());
        } catch (IllegalArgumentException e) {
            Debug.logErr("EquipmentSlot (Hand) \"" + args[1].trim() + "\" is invalid, check syntax!");
        }

    }

    public static boolean canPlayerInteract(Player player, EquipmentSlot hand) {
        BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(player);
        if (bodyHealth.getOngoingEffects().isEmpty()) return true;
        for (Map.Entry<BodyPart, List<String[]>> entry : bodyHealth.getOngoingEffects().entrySet()) {
            for (String[] effectParts : entry.getValue()) {
                if (effectParts.length > 1 && effectParts[0].trim().equalsIgnoreCase("PREVENT_INTERACT")
                        && effectParts[1].trim().equalsIgnoreCase(hand.name())) return false;
            }
        }
        return true;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() == null) return;
        if (!Config.always_allow_eating || event.getItem() == null || event.getItem().getItemMeta() == null || !event.getItem().getItemMeta().hasFood()) {
            if (canPlayerInteract(event.getPlayer(), event.getHand())) return;
            MessageUtils.sendEffectMessages(event.getPlayer(), "PREVENT_INTERACT");
            event.setCancelled(true);
        }
    }

}
