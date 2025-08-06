package bodyhealth.effects.effect;

import bodyhealth.config.Debug;
import bodyhealth.core.BodyPart;
import bodyhealth.effects.BodyHealthEffect;
import bodyhealth.effects.EffectHandler;
import bodyhealth.effects.EffectType;
import bodyhealth.util.BodyHealthUtils;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

public class PREVENT_JUMP implements BodyHealthEffect {

    @Override
    public EffectType getEffectType() {
        return EffectType.PERSISTENT;
    }

    @Override
    public String getIdentifier() {
        return "PREVENT_JUMP";
    }

    @Override
    public String getUsage() {
        return "PREVENT_JUMP";
    }

    @Override
    public void onApply(Player player, BodyPart part, String[] args, boolean isRecovery) {

        BodyHealthUtils.getBodyHealth(player).addToOngoingEffects(part, args);
        Debug.log("(" + part.name() +") Preventing jump for player " + player.getName());

        AttributeInstance jumpAttribute = player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH);
        if (jumpAttribute != null && jumpAttribute.getModifiers().stream().noneMatch(mod -> mod.getKey().equals(EffectHandler.getJumpDenialModifier().getKey()))) {
            Debug.log("Adding JumpDenialModifier to player " + player.getName());
            jumpAttribute.addModifier(EffectHandler.getJumpDenialModifier());
        }

    }

    @Override
    public void onRemove(Player player, BodyPart part, String[] args, boolean isRecovery) {

        BodyHealthUtils.getBodyHealth(player).removeFromOngoingEffects(part, args);
        Debug.log("(" + part.name() +") No longer preventing jump for player " + player.getName());

        AttributeInstance jumpAttribute = player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH);
        if (jumpAttribute != null && BodyHealthUtils.canPlayerJump(player)) {
            if (jumpAttribute.getModifiers().stream().anyMatch(mod -> mod.getKey().equals(EffectHandler.getJumpDenialModifier().getKey()))) {
                Debug.log("Removing JumpDenialModifier from player " + player.getName());
                jumpAttribute.removeModifier(EffectHandler.getJumpDenialModifier());
            }
        }

    }

}
