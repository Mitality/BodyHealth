package bodyhealth.util;

import bodyhealth.Main;
import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public final class FoliaUtils {

    private static final ConcurrentHashMap<UUID, World> playerWorlds = new ConcurrentHashMap<>();
    private static MyScheduledTask worldPollerTask = null;

    public static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void updatePlayerWorld(Player player) {
        playerWorlds.put(player.getUniqueId(), player.getWorld());
    }

    public static void removePlayerWorld(Player player) {
        playerWorlds.remove(player.getUniqueId());
    }

    /**
     * Queries all online players every 5 ticks on their region thread,
     * detecting world changes that Folia fails to fire PlayerChangedWorldEvent for
     */
    public static void startWorldChangeWatcher(BiConsumer<Player, World> onWorldChange) {
        if (worldPollerTask != null) return;
        worldPollerTask = Main.getScheduler().runTaskTimer(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Main.getScheduler().runTask(player, () -> {
                    World current = player.getWorld();
                    World previous = playerWorlds.put(player.getUniqueId(), current);
                    if (previous != null && !previous.equals(current)) {
                        onWorldChange.accept(player, previous);
                    }
                });
            }
        }, 5L, 5L);
    }

    public static void stopWorldChangeWatcher() {
        if (worldPollerTask != null) {
            worldPollerTask.cancel();
            worldPollerTask = null;
        }
        playerWorlds.clear();
    }

}
