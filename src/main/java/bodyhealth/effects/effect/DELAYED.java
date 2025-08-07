package bodyhealth.effects.effect;

import bodyhealth.Main;
import bodyhealth.config.Debug;
import bodyhealth.core.BodyPart;
import bodyhealth.effects.BodyHealthEffect;
import bodyhealth.effects.EffectHandler;
import bodyhealth.effects.EffectType;
import bodyhealth.util.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DELAYED implements BodyHealthEffect {

    private final Map<String, List<Integer>> scheduledTasks = new HashMap<>();

    @Override
    public EffectType getEffectType() {
        return EffectType.META;
    }

    @Override
    public String getIdentifier() {
        return "DELAYED";
    }

    @Override
    public String getUsage() {
        return "DELAYED / <delay> / <EFFECT[...]>";
    }

    @Override
    public void onApply(Player player, BodyPart part, String[] args, boolean isRecovery) {

        if (args.length <= 2) {
            Debug.logErr("Effect \"" + args[0].trim() + "\" is missing arguments, check syntax!");
            return;
        }

        long ticks = TimeUtils.convertToTicks(args[1].trim());

        String[] effectParts = Arrays.copyOfRange(args, 2, args.length);
        if (!EffectHandler.getRegisteredEffects().containsKey(effectParts[0].trim().toUpperCase())) {
            Debug.logErr("Effect " + effectParts[0].trim() + " is invalid, check syntax!");
            return;
        }

        BodyHealthEffect effectObject = EffectHandler.getRegisteredEffects().get(effectParts[0].trim().toUpperCase());
        if (effectObject.getEffectType() == EffectType.PERSISTENT) {
            Debug.logErr("Effect " + args[0].trim() + " cannot trigger persistent effects!");
            return;
        }

        if (ticks > 0) {
            AtomicInteger taskIdHolder = new AtomicInteger();
            int taskId = Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                effectObject.onApply(player, part, effectParts, isRecovery);
                removeTaskId(player, part, taskIdHolder.get()); // Was it worth it?
            }, ticks).getTaskId();
            taskIdHolder.set(taskId);
            storeTaskId(player, part, taskId);
        } else {
            effectObject.onApply(player, part, effectParts, isRecovery);
        }
    }

    @Override
    public void onRemove(Player player, BodyPart part, String[] args, boolean isRecovery) {
        synchronized (scheduledTasks) {
            String key = player.getUniqueId() + ":" + part.name().toLowerCase();
            List<Integer> tasks = scheduledTasks.remove(key);
            if (tasks == null) return;
            for (int id : tasks) Bukkit.getScheduler().cancelTask(id);
        }
    }

    private void storeTaskId(Player player, BodyPart part, int taskId) {
        synchronized (scheduledTasks) {
            String key = player.getUniqueId() + ":" + part.name().toLowerCase();
            scheduledTasks.computeIfAbsent(key, k -> new ArrayList<>()).add(taskId);
        }
    }

    private void removeTaskId(Player player, BodyPart part, int taskId) {
        synchronized (scheduledTasks) {
            String key = player.getUniqueId() + ":" + part.name().toLowerCase();
            List<Integer> tasks = scheduledTasks.get(key);
            if (tasks == null) return;
            tasks.remove(Integer.valueOf(taskId));
            if (tasks.isEmpty()) scheduledTasks.remove(key);
        }
    }

}
