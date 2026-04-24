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

        boolean lenient = Config.lenient_movement_restrictions;
        Debug.log("(" + part.name() + ") Preventing jump for player " + player.getName());
        AttributeInstance jumpAttribute = player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH);
        if (jumpAttribute != null && jumpAttribute.getModifiers().stream().noneMatch(mod -> mod.getKey().equals(EffectHandler.getJumpDenialModifier(lenient).getKey()))) {
            Debug.log("Adding JumpDenialModifier to player " + player.getName());
            BodyHealthUtils.addAttributeModifier(jumpAttribute, EffectHandler.getJumpDenialModifier(lenient));
        }

    }

    @Override
    public void onRemove(Player player, BodyPart part, String[] args, boolean isRecovery) {

        boolean lenient = Config.lenient_movement_restrictions;
        Debug.log("(" + part.name() + ") No longer preventing jump for player " + player.getName());
        AttributeInstance jumpAttribute = player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH);
        if (jumpAttribute != null && canPlayerJump(player)) {
            if (jumpAttribute.getModifiers().stream().anyMatch(mod -> mod.getKey().equals(EffectHandler.getJumpDenialModifier(lenient).getKey()))) {
                Debug.log("Removing JumpDenialModifier from player " + player.getName());
                jumpAttribute.removeModifier(EffectHandler.getJumpDenialModifier(lenient));
            } else if (jumpAttribute.getModifiers().stream().anyMatch(mod -> mod.getKey().equals(EffectHandler.getJumpDenialModifier(!lenient).getKey()))) {
                Debug.log("Removing JumpDenialModifier from player " + player.getName());
                jumpAttribute.removeModifier(EffectHandler.getJumpDenialModifier(!lenient));
            }
        }

    }

    public static boolean canPlayerJump(Player player) {
        BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(player);
        if (bodyHealth.getOngoingEffects().isEmpty()) return true;
        for (Map.Entry<BodyPart, List<String[]>> entry : bodyHealth.getOngoingEffects().entrySet()) {
            for (String[] effectParts : entry.getValue()) {
                if (effectParts.length > 0 && effectParts[0].trim().equalsIgnoreCase("PREVENT_JUMP")) return false;
            }
        }
        return true;
    }

}
