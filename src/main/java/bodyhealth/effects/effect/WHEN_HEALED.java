package bodyhealth.effects.effect;

import bodyhealth.config.Debug;
import bodyhealth.core.BodyPart;
import bodyhealth.effects.BodyHealthEffect;
import bodyhealth.effects.EffectHandler;
import bodyhealth.effects.EffectType;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class WHEN_HEALED implements BodyHealthEffect {

    @Override
    public EffectType getEffectType() {
        return EffectType.META;
    }

    @Override
    public String getIdentifier() {
        return "WHEN_HEALED";
    }

    @Override
    public String getUsage() {
        return "WHEN_HEALED / <EFFECT[...]>";
    }

    @Override
    public void onApply(Player player, BodyPart part, String[] args, boolean isRecovery) {

        if (!isRecovery) return;

        if (args.length <= 1) {
            Debug.logErr("Effect \"" + args[0].trim() + "\" is missing arguments, check syntax!");
            return;
        }

        String[] effectParts = Arrays.copyOfRange(args, 1, args.length);
        if (!EffectHandler.getRegisteredEffects().containsKey(effectParts[0].trim().toUpperCase())) {
            Debug.logErr("Effect " + effectParts[0].trim() + " is invalid, check syntax!");
            return;
        }

        BodyHealthEffect effectObject = EffectHandler.getRegisteredEffects().get(effectParts[0].trim().toUpperCase());
        if (effectObject.getEffectType() != EffectType.ONE_TIME) {
            Debug.logErr("Effect " + args[0].trim() + " can only trigger one-time effects!");
            return;
        }

        effectObject.onApply(player, part, effectParts, true);
    }

    @Override
    public void onRemove(Player player, BodyPart part, String[] args, boolean isRecovery) {
    }

}