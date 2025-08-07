package bodyhealth.listeners;

import bodyhealth.Main;
import bodyhealth.config.Config;
import bodyhealth.util.MessageUtils;
import bodyhealth.util.UpdateChecker;
import io.github.milkdrinkers.colorparser.ColorParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
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
            Component prefix = ColorParser.of(Config.prefix).parsePAPIPlaceholders(player).parseLegacy().build();
            Component message = Component.text()
                .append(prefix)
                .append(Component.text("A new version of ", NamedTextColor.GREEN))
                .append(Component.text(updateChecker.getResourceName(), NamedTextColor.GOLD))
                .append(Component.text(" is available: ", NamedTextColor.GREEN))
                .append(Component.text("v" + updateChecker.getLatestVersion(), NamedTextColor.GOLD))
                .appendNewline()
                .append(Component.text(updateChecker.getUpdateLink(), NamedTextColor.GRAY)
                    .clickEvent(ClickEvent.openUrl(updateChecker.getUpdateLink()))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to open this link in your browser", NamedTextColor.GRAY))))
                .build();
            Main.getAdventure().player(player).sendMessage(message);
        }
    }
}
