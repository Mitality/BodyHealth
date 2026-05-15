package bodyhealth.listeners;

import bodyhealth.config.Config;
import bodyhealth.config.Debug;
import bodyhealth.core.BodyHealth;
import bodyhealth.core.BodyPart;
import bodyhealth.util.BodyHealthUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SleepListener implements Listener {

    private record BedEntry(long worldTime, long irlTime) {}
    private final Map<UUID, BedEntry> bedEntryData = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if (event.getBedEnterResult() == PlayerBedEnterEvent.BedEnterResult.OK) {
            bedEntryData.put(event.getPlayer().getUniqueId(),
                new BedEntry(event.getPlayer().getWorld().getTime(), System.currentTimeMillis()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        BedEntry entry = bedEntryData.remove(player.getUniqueId());
        if (entry == null) return;
        long elapsedMs = System.currentTimeMillis() - entry.irlTime();
        long expectedWorldTime = (entry.worldTime() + elapsedMs / 50L) % 24000L;
        long currentWorldTime = player.getWorld().getTime() % 24000L;
        if (expectedWorldTime >= 12000L && currentWorldTime < 1000L) {
            applySleepRegen(player, event);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        bedEntryData.remove(event.getPlayer().getUniqueId());
    }

    private void applySleepRegen(Player player, org.bukkit.event.Event cause) {
        if (!Config.regenerate_on_sleep_enabled) return;
        if (!BodyHealthUtils.isSystemEnabled(player)) return;
        BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(player);
        for (BodyPart part : BodyPart.values()) {
            double regenHp = Config.regenerate_on_sleep_is_percent
                ? Config.regenerate_on_sleep_amount / 100.0 * BodyHealthUtils.getMaxHealth(part, player)
                : Config.regenerate_on_sleep_amount;
            bodyHealth.regenerateHealth(regenHp, part, false, cause);
        }
        Debug.logDev("Applied sleep regen (" + Config.regenerate_on_sleep_amount + (Config.regenerate_on_sleep_is_percent ? "%" : " HP") + ") to " + player.getName());
    }

}
