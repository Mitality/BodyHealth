package bodyhealth.effects.effect;

import bodyhealth.Main;
import bodyhealth.config.Debug;
import bodyhealth.core.BodyPart;
import bodyhealth.effects.BodyHealthEffect;
import bodyhealth.effects.EffectHandler;
import bodyhealth.effects.EffectType;
import bodyhealth.util.TimeUtils;
import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import org.bukkit.entity.Player;

import java.util.*;

public class REPEATING implements BodyHealthEffect {

    private final Map<String, List<MyScheduledTask>> scheduledTasks = new HashMap<>();

    @Override
    public EffectType getEffectType() {
        return EffectType.META;
    }

    @Override
    public String getIdentifier() {
        return "REPEATING";
    }

    @Override
    public String getUsage() {
        return "REPEATING / <interval> / <EFFECT[...]>";
    }

    @Override
    public void onApply(Player player, BodyPart part, String[] args, boolean isRecovery) {

        if (args.length <= 3) {
            Debug.logErr("Effect \"" + args[0].trim() + "\" is missing arguments, check syntax!");
            return;
        }

        long delay = TimeUtils.convertToTicks(args[1].trim());
        long interval = TimeUtils.convertToTicks(args[2].trim());

        String[] effectParts = Arrays.copyOfRange(args, 3, args.length);
        if (!EffectHandler.getRegisteredEffects().containsKey(effectParts[0].trim().toUpperCase())) {
            Debug.logErr("Effect " + effectParts[0].trim() + " is invalid, check syntax!");
            return;
        }

        BodyHealthEffect effectObject = EffectHandler.getRegisteredEffects().get(effectParts[0].trim().toUpperCase());
        if (effectObject.getEffectType() == EffectType.PERSISTENT) {
            Debug.logErr("Effect " + args[0].trim() + " cannot trigger persistent effects!");
            return;
        }

        MyScheduledTask task = Main.getScheduler().runTaskTimer(() -> effectObject.onApply(player, part, effectParts, isRecovery), delay, interval);
        storeTask(player, part, task);
    }

    @Override
    public void onRemove(Player player, BodyPart part, String[] args, boolean isRecovery) {
        synchronized (scheduledTasks) {
            String key = player.getUniqueId() + ":" + part.name().toLowerCase();
            List<MyScheduledTask> tasks = scheduledTasks.remove(key);
            if (tasks == null) return;
            for (MyScheduledTask task : tasks) task.cancel();
        }
    }

    private void storeTask(Player player, BodyPart part, MyScheduledTask task) {
        synchronized (scheduledTasks) {
            String key = player.getUniqueId() + ":" + part.name().toLowerCase();
            scheduledTasks.computeIfAbsent(key, k -> new ArrayList<>()).add(task);
        }
    }

}
