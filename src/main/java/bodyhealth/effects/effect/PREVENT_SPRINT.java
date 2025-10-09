package bodyhealth.effects.effect;

import bodyhealth.config.Debug;
import bodyhealth.core.BodyPart;
import bodyhealth.effects.BodyHealthEffect;
import bodyhealth.effects.EffectType;
import org.bukkit.entity.Player;

public class PREVENT_SPRINT implements BodyHealthEffect {

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

    // This effect utilizes a special builtin system that detects this effect when listed as ongoing
    // Managing this effect here would be much less responsive, as we (un)apply it constantly

}
