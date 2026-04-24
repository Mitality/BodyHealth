package bodyhealth.effects.effect;

import bodyhealth.config.Debug;
import bodyhealth.core.BodyPart;
import bodyhealth.effects.BodyHealthEffect;
import bodyhealth.core.BodyHealth;
import bodyhealth.effects.EffectHandler;
import bodyhealth.effects.EffectType;
import bodyhealth.util.BodyHealthUtils;
import bodyhealth.util.MessageUtils;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PREVENT_SPRINT implements BodyHealthEffect {

    private static List<Player> preventSprint = new ArrayList<>();

    @Override
    public EffectType getEffectType() {
        return EffectType.PERSISTENT;
    }

    @Override
    public String getIdentifier() {
        return "PREVENT_SPRINT";
    }

    @Override
    public String getUsage() {
        return "PREVENT_SPRINT / [MESSAGE]";
    }

    @Override
    public void onApply(Player player, BodyPart part, String[] args, boolean isRecovery) {
        Debug.log("(" + part.name() +") Preventing sprint for player " + player.getName());
    }

    @Override
    public void onRemove(Player player, BodyPart part, String[] args, boolean isRecovery) {
        Debug.log("(" + part.name() +") No longer preventing sprint for player " + player.getName());
    }

    public static boolean canPlayerSprint(Player player) {
        BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(player);
        if (bodyHealth.getOngoingEffects().isEmpty()) return true;
        for (Map.Entry<BodyPart, List<String[]>> entry : bodyHealth.getOngoingEffects().entrySet()) {
            for (String[] effectParts : entry.getValue()) {
                if (effectParts.length > 0 && effectParts[0].trim().equalsIgnoreCase("PREVENT_SPRINT")) return false;
            }
        }
        return true;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getPlayer().isSprinting()) {
            if (preventSprint.contains(event.getPlayer())) return;
            if (canPlayerSprint(event.getPlayer())) return;
            preventSprint.add(event.getPlayer());
            BodyHealthUtils.addAttributeModifier(Objects.requireNonNull(event.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)), EffectHandler.getSpeedReductionModifier());
            Debug.log("Adding SpeedReductionModifier to player " + event.getPlayer().getName());
            MessageUtils.sendEffectMessages(event.getPlayer(), "PREVENT_SPRINT");
        } else {
            if (!preventSprint.contains(event.getPlayer())) return;
            preventSprint.remove(event.getPlayer());
            Objects.requireNonNull(event.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).removeModifier(EffectHandler.getSpeedReductionModifier());
            Debug.log("Removing SpeedReductionModifier from player " + event.getPlayer().getName());
        }
    }

    /**
     * Clears the sprint cache for a player, but
     * DOES NOT REMOVE THE SPEED REDUCTION MODIFIER!
     * @param player the player to remove from the cache
     */
    public static void clearCacheFor(Player player) {
        preventSprint.remove(player);
    }

}
