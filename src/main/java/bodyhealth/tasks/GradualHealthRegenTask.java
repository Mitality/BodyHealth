package bodyhealth.tasks;

import bodyhealth.Main;
import bodyhealth.config.Config;
import bodyhealth.config.Debug;
import bodyhealth.core.BodyHealth;
import bodyhealth.core.BodyPart;
import bodyhealth.util.BodyHealthUtils;
import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class GradualHealthRegenTask {

    private static MyScheduledTask task = null;

    public static void start() {
        if (task != null || Config.gradual_heath_regen_interval <= 0) return;
        task = Main.getScheduler().runTaskTimer(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Main.getScheduler().runTask(player, () -> tick(player));
            }
        }, Config.gradual_heath_regen_interval, Config.gradual_heath_regen_interval);
    }

    public static void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private static void tick(Player player) {
        if (!BodyHealthUtils.isSystemEnabled(player)) return;
        if (player.getHealth() < Config.gradual_heath_regen_min_vanilla_health) return;
        if (player.getFoodLevel() < Config.gradual_heath_regen_min_vanilla_hunger) return;

        BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(player);
        double totalDrain = 0;

        for (BodyPart part : BodyPart.values()) {
            if (bodyHealth.getHealth(part) >= 100) continue;

            double regenHp = Config.gradual_heath_regen_amount_is_percent
                ? Config.gradual_heath_regen_amount / 100.0 * BodyHealthUtils.getMaxHealth(part, player)
                : Config.gradual_heath_regen_amount;

            double actualRegen = bodyHealth.regenerateHealth(regenHp, part, false, null);
            if (actualRegen > 0) {
                double expectedRegen = regenHp / BodyHealthUtils.getMaxHealth(part, player) * 100;
                Debug.logDev("(" + part.name() + ") expectedRegen: " + expectedRegen + ",  actualRegen: " + actualRegen);
                totalDrain += Config.gradual_heath_regen_hunger_drain * (actualRegen / expectedRegen);
            }
        }

        if (totalDrain > 0) {
            Debug.logDev("Draining " + totalDrain + " hunger from " + player.getName());
            drainHunger(player, totalDrain);
        }
    }

    private static void drainHunger(Player player, double amount) {
        float newSaturation = (float) (player.getSaturation() - amount);
        if (newSaturation < 0) {
            player.setFoodLevel(Math.max(0, player.getFoodLevel() - 1));
            newSaturation = 0;
        }
        player.setSaturation(newSaturation);
    }

}
