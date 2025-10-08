package bodyhealth.listeners;

import bodyhealth.Main;
import bodyhealth.config.Debug;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.events.ExpansionsLoadedEvent;
import me.clip.placeholderapi.expansion.cloud.CloudExpansion;
import me.clip.placeholderapi.expansion.manager.CloudExpansionManager;
import me.clip.placeholderapi.expansion.manager.LocalExpansionManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Optional;

public class PlaceholderAPIListener implements Listener {

    @EventHandler
    public void onExpansionsLoaded(ExpansionsLoadedEvent event) {
        Debug.log("PlaceholderAPI (re)loaded, ensuring presence of the 'Player' expansion...");
        Main.getScheduler().runTaskLater(PlaceholderAPIListener::ensurePlayerExpansion, 20L);
    }

    /**
     * Ensures that the "Player" expansion of PlaceholderAPI is installed at all times (needed for HUD conditions)
     */
    public static void ensurePlayerExpansion() {
        PlaceholderAPIPlugin placeholderAPIPlugin = (PlaceholderAPIPlugin) Bukkit.getPluginManager().getPlugin("PlaceholderAPI");

        if (placeholderAPIPlugin == null) {
            Debug.logErr("PlaceholderAPI is not available. Cannot ensure that the Player expansion is being used.");
            return;
        }

        CloudExpansionManager cloudExpansionManager = placeholderAPIPlugin.getCloudExpansionManager();
        LocalExpansionManager localExpansionManager = placeholderAPIPlugin.getLocalExpansionManager();

        boolean isPlayerExpansionLoaded = localExpansionManager.getExpansions().stream()
                .anyMatch(expansion -> expansion.getIdentifier().equalsIgnoreCase("player"));

        if (isPlayerExpansionLoaded) {
            Debug.log("Player expansion is already installed.");
            return;
        }

        Optional<CloudExpansion> optionalPlayerExpansion = cloudExpansionManager.findCloudExpansionByName("Player");

        if (optionalPlayerExpansion.isPresent()) {
            CloudExpansion playerExpansion = optionalPlayerExpansion.get();
            Debug.log("Downloading the Player expansion from PlaceholderAPIs eCloud...");

            // Download the expansion asynchronously (File is returned)
            cloudExpansionManager.downloadExpansion(playerExpansion, playerExpansion.getVersion(playerExpansion.getLatestVersion())).thenAccept(downloadedFile -> {
                if (downloadedFile != null) {
                    Debug.log("Successfully downloaded the Player expansion from PlaceholderAPIs eCloud.");

                    Debug.log("Reloading PlaceholderAPI to register the expansion...");
                    Main.getScheduler().runTask(() -> Bukkit // Dispatch command synchronously
                            .dispatchCommand(Bukkit.getConsoleSender(), "papi reload"));
                } else {
                    Debug.logErr("Failed to download the Player expansion. Please download it manually with '/papi ecloud download Player'");
                }
            }).exceptionally(ex -> {
                Debug.logErr("An error occurred while downloading the Player expansion: " + ex.getMessage());
                return null;
            });
        } else {
            Debug.logErr("Player expansion not found on eCloud. Couldn't verify its existence! If BetterHud only shows a gray display, download it manually with '/papi ecloud download Player'");
        }
    }

}
