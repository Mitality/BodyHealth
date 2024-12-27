package bodyhealth.effects.effect;

import bodyhealth.config.Debug;
import bodyhealth.core.BodyPart;
import bodyhealth.effects.BodyHealthEffect;
import bodyhealth.effects.EffectHandler;
import bodyhealth.util.BodyHealthUtils;
import org.bukkit.attribute.Attribute;
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

        if (!EffectHandler.preventWalk.contains(player)) {
            EffectHandler.preventWalk.add(player);
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).addModifier(EffectHandler.getWalkDenialModifier());
            Debug.log("Adding SpeedReductionModifier to player " + player.getName());
        }

    }

    @Override
    public void onRemove(Player player, BodyPart part, String[] args) {

        BodyHealthUtils.getBodyHealth(player).removeFromOngoingEffects(part, args);
        Debug.log("(" + part.name() +") No longer preventing walk for player " + player.getName());

        if (BodyHealthUtils.canPlayerWalk(player) && EffectHandler.preventWalk.contains(player)) {
            EffectHandler.preventWalk.remove(player);
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).removeModifier(EffectHandler.getWalkDenialModifier());
            Debug.log("Removing WalkDenialModifier from player " + player.getName());
        }

    }

}
