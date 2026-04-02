package bodyhealth.effects.effect;

import bodyhealth.Main;
import bodyhealth.config.Config;
import bodyhealth.config.Debug;
import bodyhealth.core.BodyPart;
import bodyhealth.effects.BodyHealthEffect;
import bodyhealth.effects.EffectType;
import bodyhealth.util.BodyHealthUtils;
import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class POTION_EFFECT implements BodyHealthEffect {

    private static final int REFRESH_INTERVAL_TICKS = 80;
    private static final int REFRESH_DURATION_TICKS = 340;

    private static final Map<UUID, Map<PotionEffectType, Integer>> trackedEffects = new ConcurrentHashMap<>();
    private static MyScheduledTask refreshTask = null;

    public static void startRefreshTask() {
        if (refreshTask != null) return;
        refreshTask = Main.getScheduler().runTaskTimer(() -> {
            for (Map.Entry<UUID, Map<PotionEffectType, Integer>> entry : new HashMap<>(trackedEffects).entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player == null || !player.isOnline()) {
                    trackedEffects.remove(entry.getKey());
                    continue;
                }
                Map<PotionEffectType, Integer> effects = new HashMap<>(entry.getValue());
                Main.getScheduler().runTask(player, () -> {
                    for (Map.Entry<PotionEffectType, Integer> effectEntry : effects.entrySet()) {
                        boolean ambient = !Config.hide_potion_effects, particles = !Config.hide_potion_effects, icon = !Config.hide_potion_effects;
                        player.addPotionEffect(new PotionEffect(effectEntry.getKey(), REFRESH_DURATION_TICKS, effectEntry.getValue() - 1, ambient, particles, icon));
                    }
                });
            }
        }, REFRESH_INTERVAL_TICKS, REFRESH_INTERVAL_TICKS);
    }

    public static void stopRefreshTask() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
        trackedEffects.clear();
    }

    @Override
    public EffectType getEffectType() {
        return EffectType.PERSISTENT;
    }

    @Override
    public String getIdentifier() {
        return "POTION_EFFECT";
    }

    @Override
    public String getUsage() {
        return "POTION_EFFECT / <EFFECT> / [AMPLIFIER]";
    }

    @Override
    public void onApply(Player player, BodyPart part, String[] args, boolean isRecovery) {

        if (args.length <= 1) {
            Debug.logErr("Effect \"" + args[0].trim() + "\" is missing arguments, check syntax!");
            return;
        }

        String effectName = args[1].trim().toUpperCase();
        int amplifier = (args.length > 2) ? Integer.parseInt(args[2].trim()) : 1;
        amplifier = Math.min(255, Math.max(1, amplifier)); // Ensure amplifier is between 0 and 255
        PotionEffectType effectType = PotionEffectType.getByName(effectName);

        if (effectType == null) {
            Debug.logErr("EffectType \"" + args[1].trim() + "\" is invalid, check syntax!");
            return;
        }

        if (Config.apply_potion_effects_repeatedly) {
            Map<PotionEffectType, Integer> playerEffects = trackedEffects.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());
            int trackedAmplifier = playerEffects.getOrDefault(effectType, -1);
            if (trackedAmplifier >= amplifier) return; // Already tracking an equal or stronger effect
            boolean ambient = !Config.hide_potion_effects, particles = !Config.hide_potion_effects, icon = !Config.hide_potion_effects;
            player.addPotionEffect(new PotionEffect(effectType, REFRESH_DURATION_TICKS, amplifier - 1, ambient, particles, icon));
            playerEffects.put(effectType, amplifier);
            Debug.log("(" + part.name() + ") Applied effect \"" + args[1].trim() + "\" to player " + player.getName() + " (repeating)");
        } else {
            if (player.getPotionEffect(effectType) != null
                    && Objects.requireNonNull(player.getPotionEffect(effectType)).getDuration() == PotionEffect.INFINITE_DURATION
                    && Objects.requireNonNull(player.getPotionEffect(effectType)).getAmplifier() >= amplifier)
                return; // Player should keep infinite effects with a higher or equal amplifier
            boolean ambient = !Config.hide_potion_effects, particles = !Config.hide_potion_effects, icon = !Config.hide_potion_effects;
            player.addPotionEffect(new PotionEffect(effectType, PotionEffect.INFINITE_DURATION, amplifier - 1, ambient, particles, icon));
            Debug.log("(" + part.name() + ") Applied effect \"" + args[1].trim() + "\" to player " + player.getName());
        }

    }

    @Override
    public void onRemove(Player player, BodyPart part, String[] args, boolean isRecovery) {

        if (args.length <= 1) {
            Debug.logErr("Effect \"" + args[0].trim() + "\" is missing arguments, check syntax!");
            return;
        }

        String effectName = args[1].trim().toUpperCase();
        PotionEffectType effectType = PotionEffectType.getByName(effectName);

        if (effectType == null) {
            Debug.logErr("EffectType \"" + args[1].trim() + "\" is invalid, check syntax!");
            return;
        }

        int highestAmplifier = BodyHealthUtils.getHighestPotionEffectAmplifier(player, effectType);

        if (Config.apply_potion_effects_repeatedly) {
            if (highestAmplifier >= 0) {
                boolean ambient = !Config.hide_potion_effects, particles = !Config.hide_potion_effects, icon = !Config.hide_potion_effects;
                player.addPotionEffect(new PotionEffect(effectType, REFRESH_DURATION_TICKS, highestAmplifier - 1, ambient, particles, icon));
                trackedEffects.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>()).put(effectType, highestAmplifier);
                Debug.log("(" + part.name() + ") Set PotionEffect \"" + args[1].trim() + "\" to amplifier " + highestAmplifier + " for player " + player.getName() + " (repeating)");
            } else {
                player.removePotionEffect(effectType);
                Map<PotionEffectType, Integer> playerEffects = trackedEffects.get(player.getUniqueId());
                if (playerEffects != null) {
                    playerEffects.remove(effectType);
                    if (playerEffects.isEmpty()) trackedEffects.remove(player.getUniqueId());
                }
                Debug.log("(" + part.name() + ") Removed PotionEffect \"" + args[1].trim() + "\" from player " + player.getName() + " (repeating)");
            }
        } else {
            if (highestAmplifier >= 0) {
                boolean ambient = !Config.hide_potion_effects, particles = !Config.hide_potion_effects, icon = !Config.hide_potion_effects;
                player.addPotionEffect(new PotionEffect(effectType, PotionEffect.INFINITE_DURATION, highestAmplifier - 1, ambient, particles, icon));
                Debug.log("(" + part.name() + ") Set PotionEffect \"" + args[1].trim() + "\" to amplifier " + highestAmplifier + " for player " + player.getName());
            } else {
                player.removePotionEffect(effectType);
                Debug.log("(" + part.name() + ") Removed PotionEffect \"" + args[1].trim() + "\" from player " + player.getName());
            }
        }

    }

}
