package bodyhealth.effects.effect;

import bodyhealth.Main;
import bodyhealth.config.Debug;
import bodyhealth.core.BodyPart;
import bodyhealth.effects.BodyHealthEffect;
import bodyhealth.effects.EffectHandler;
import bodyhealth.effects.EffectType;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class CHANCE implements BodyHealthEffect {

    private static final ConcurrentHashMap<String, Boolean> groupRolls = new ConcurrentHashMap<>();

    @Override
    public EffectType getEffectType() {
        return EffectType.META;
    }

    @Override
    public String getIdentifier() {
        return "CHANCE";
    }

    @Override
    public String getUsage() {
        return "CHANCE / <CHANCE> / [KEY] / <EFFECT[...]>";
    }

    @Override
    public void onApply(Player player, BodyPart part, String[] args, boolean isRecovery) {

        if (args.length <= 2) {
            Debug.logErr("Effect \"" + args[0].trim() + "\" is missing arguments, check syntax!");
            return;
        }

        double chance;
        try {
            chance = Double.parseDouble(args[1].trim());
        } catch (NumberFormatException e) {
            Debug.logErr("Effect \"" + args[0].trim() + "\" has an invalid chance value \"" + args[1].trim() + "\", expected a number between 0.0 and 1.0!");
            return;
        }

        if (chance < 0.0 || chance > 1.0) {
            Debug.logErr("Effect \"" + args[0].trim() + "\" has an out-of-range chance value \"" + args[1].trim() + "\", expected a number between 0.0 and 1.0!");
            return;
        }

        int effectStart;
        String groupKey = null;
        if (EffectHandler.getRegisteredEffects().containsKey(args[2].trim().toUpperCase())) {
            effectStart = 2;
        } else {
            if (args.length == 3) {
                Debug.logErr("Effect \"" + args[0].trim() + "\" is missing arguments after key, check syntax!");
                return;
            }
            if (!EffectHandler.getRegisteredEffects().containsKey(args[3].trim().toUpperCase())) {
                Debug.logErr("Effect " + args[3].trim() + " is invalid, check syntax!");
                return;
            }
            groupKey = player.getUniqueId() + ":" + args[2].trim();
            effectStart = 3;
        }

        boolean roll;
        if (groupKey != null) {
            String finalGroupKey = groupKey;
            AtomicBoolean isNew = new AtomicBoolean(false);
            roll = groupRolls.computeIfAbsent(groupKey, k -> {
                isNew.set(true);
                return ThreadLocalRandom.current().nextDouble() < chance;
            });
            if (isNew.get()) {
                Main.getScheduler().runTaskLater(player, () -> groupRolls.remove(finalGroupKey), 1L);
            }
        } else {
            roll = ThreadLocalRandom.current().nextDouble() < chance;
        }

        if (!roll) return;

        String[] effectParts = Arrays.copyOfRange(args, effectStart, args.length);
        if (!EffectHandler.getRegisteredEffects().containsKey(effectParts[0].trim().toUpperCase())) {
            Debug.logErr("Effect " + effectParts[0].trim() + " is invalid, check syntax!");
            return;
        }

        BodyHealthEffect effectObject = EffectHandler.getRegisteredEffects().get(effectParts[0].trim().toUpperCase());
        if (effectObject.getEffectType() == EffectType.PERSISTENT) {
            Debug.logErr("Effect " + args[0].trim() + " cannot trigger persistent effects!");
            return;
        }

        effectObject.onApply(player, part, effectParts, isRecovery);
    }

    @Override
    public void onRemove(Player player, BodyPart part, String[] args, boolean isRecovery) {
    }

}
