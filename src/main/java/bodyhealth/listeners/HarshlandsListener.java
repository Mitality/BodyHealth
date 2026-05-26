package bodyhealth.listeners;

import bodyhealth.util.BodyHealthUtils;
import cz.hashiri.harshlands.api.bukkit.event.PluginReloadedEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Harshlands clears its per-player "shown" set on /hl reload, so when Harshlands
 * reloads we re-issue Hud.add for every online player. The Harshlands equivalent of
 * {@link BetterHudListener}, but trivial - Harshlands ships its own glyphs, so there
 * is no config/asset re-injection to do.
 */
public class HarshlandsListener implements Listener {

    @EventHandler
    public void onHarshlandsReloaded(PluginReloadedEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            BodyHealthUtils.applyBodyHealthHudVisibility(player);
        }
    }
}
