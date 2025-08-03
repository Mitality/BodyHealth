package bodyhealth.listeners;

import bodyhealth.config.Config;
import bodyhealth.util.MessageUtils;
import bodyhealth.util.UpdateChecker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateNotifyListener implements Listener {

    private final UpdateChecker updateChecker;

    public UpdateNotifyListener(UpdateChecker updateChecker) {
        this.updateChecker = updateChecker;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("bodyhealth.update-notify") && updateChecker.isUpdateAvailable()) {
            MessageUtils.notifyPlayer(player, Config.prefix + "&aA new version of &6"
            + updateChecker.getResourceName() + "&a is available: &6v" + updateChecker.getLatestVersion()
            + "\n&7" + updateChecker.getUpdateLink());
        }
    }
}
