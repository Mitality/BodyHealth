package bodyhealth.effects.effect;

import bodyhealth.config.Debug;
import bodyhealth.core.BodyPart;
import bodyhealth.effects.BodyHealthEffect;
import bodyhealth.effects.EffectHandler;
import bodyhealth.util.BodyHealthUtils;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

import java.util.Objects;

public class PREVENT_WALK implements BodyHealthEffect {

    @Override
    public String getIdentifier() {
        return "PREVENT_WALK";
    }

    @Override
    public String getUsage() {
        return "PREVENT_WALK";
    }

    @Override
    public void onApply(Player player, BodyPart part, String[] args) {

        BodyHealthUtils.getBodyHealth(player).addToOngoingEffects(part, args);
        Debug.log("(" + part.name() +") Preventing walk for player " + player.getName());

        AttributeInstance walkAttribute = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (walkAttribute != null && walkAttribute.getModifiers().stream().noneMatch(mod -> mod.getUniqueId().equals(EffectHandler.getWalkDenialModifier().getUniqueId()))) {
            Debug.log("Adding WalkDenialModifier to player " + player.getName());
            walkAttribute.addModifier(EffectHandler.getWalkDenialModifier());
        }

    }

    @Override
    public void onRemove(Player player, BodyPart part, String[] args) {

        BodyHealthUtils.getBodyHealth(player).removeFromOngoingEffects(part, args);
        Debug.log("(" + part.name() +") No longer preventing walk for player " + player.getName());

        AttributeInstance walkAttribute = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (walkAttribute != null && BodyHealthUtils.canPlayerWalk(player)) {
            if (walkAttribute.getModifiers().stream().anyMatch(mod -> mod.getUniqueId().equals(EffectHandler.getWalkDenialModifier().getUniqueId()))) {
                Debug.log("Removing WalkDenialModifier from player " + player.getName());
                walkAttribute.removeModifier(EffectHandler.getWalkDenialModifier());
            }
        }

    }

}
