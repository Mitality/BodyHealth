package bodyhealth.effects.effect;

import bodyhealth.config.Config;
import bodyhealth.config.Debug;
import bodyhealth.core.BodyHealth;
import bodyhealth.core.BodyPart;
import bodyhealth.effects.BodyHealthEffect;
import bodyhealth.effects.EffectHandler;
import bodyhealth.effects.EffectType;
import bodyhealth.util.BodyHealthUtils;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class PREVENT_WALK implements BodyHealthEffect {

    @Override
    public EffectType getEffectType() {
        return EffectType.PERSISTENT;
    }

    @Override
    public String getIdentifier() {
        return "PREVENT_WALK";
    }

    @Override
    public String getUsage() {
        return "PREVENT_WALK";
    }

    @Override
    public void onApply(Player player, BodyPart part, String[] args, boolean isRecovery) {

        boolean lenient = Config.lenient_movement_restrictions;
        Debug.log("(" + part.name() +") Preventing walk for player " + player.getName());
        AttributeInstance walkAttribute = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (walkAttribute != null && walkAttribute.getModifiers().stream().noneMatch(mod -> mod.getKey().equals(EffectHandler.getWalkDenialModifier(lenient).getKey()))) {
            Debug.log("Adding WalkDenialModifier to player " + player.getName());
            BodyHealthUtils.addAttributeModifier(walkAttribute, EffectHandler.getWalkDenialModifier(lenient));
        }

    }

    @Override
    public void onRemove(Player player, BodyPart part, String[] args, boolean isRecovery) {

        boolean lenient = Config.lenient_movement_restrictions;
        Debug.log("(" + part.name() +") No longer preventing walk for player " + player.getName());
        AttributeInstance walkAttribute = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (walkAttribute != null && canPlayerWalk(player)) {
            if (walkAttribute.getModifiers().stream().anyMatch(mod -> mod.getKey().equals(EffectHandler.getWalkDenialModifier(lenient).getKey()))) {
                Debug.log("Removing WalkDenialModifier from player " + player.getName());
                walkAttribute.removeModifier(EffectHandler.getWalkDenialModifier(lenient));
            } else if (walkAttribute.getModifiers().stream().anyMatch(mod -> mod.getKey().equals(EffectHandler.getWalkDenialModifier(!lenient).getKey()))) {
                Debug.log("Removing WalkDenialModifier from player " + player.getName());
                walkAttribute.removeModifier(EffectHandler.getWalkDenialModifier(!lenient));
            }
        }

    }

    public static boolean canPlayerWalk(Player player) {
        BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(player);
        if (bodyHealth.getOngoingEffects().isEmpty()) return true;
        for (Map.Entry<BodyPart, List<String[]>> entry : bodyHealth.getOngoingEffects().entrySet()) {
            for (String[] effectParts : entry.getValue()) {
                if (effectParts.length > 0 && effectParts[0].trim().equalsIgnoreCase("PREVENT_WALK")) return false;
            }
        }
        return true;
    }

}
