package bodyhealth.effects.effect;

import bodyhealth.config.Debug;
import bodyhealth.core.BodyPart;
import bodyhealth.effects.BodyHealthEffect;
import bodyhealth.effects.EffectHandler;
import bodyhealth.util.BodyHealthUtils;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.Objects;

public class PREVENT_JUMP implements BodyHealthEffect {

    @Override
    public String getIdentifier() {
        return "PREVENT_JUMP";
    }

    @Override
    public String getUsage() {
        return "PREVENT_JUMP";
    }

    @Override
    public void onApply(Player player, BodyPart part, String[] args) {

        BodyHealthUtils.getBodyHealth(player).addToOngoingEffects(part, args);
        Debug.log("(" + part.name() +") Preventing jump for player " + player.getName());

        if (!EffectHandler.preventJump.contains(player)) {
            EffectHandler.preventJump.add(player);
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).addModifier(EffectHandler.getJumpDenialModifier());
            Debug.log("Adding JumpDenialModifier to player " + player.getName());
        }

    }

    @Override
    public void onRemove(Player player, BodyPart part, String[] args) {

        BodyHealthUtils.getBodyHealth(player).removeFromOngoingEffects(part, args);
        Debug.log("(" + part.name() +") No longer preventing jump for player " + player.getName());

        if (BodyHealthUtils.canPlayerJump(player) && EffectHandler.preventJump.contains(player)) {
            EffectHandler.preventJump.remove(player);
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).removeModifier(EffectHandler.getJumpDenialModifier());
            Debug.log("Removing WalkDenialModifier from player " + player.getName());
        }

    }

}
