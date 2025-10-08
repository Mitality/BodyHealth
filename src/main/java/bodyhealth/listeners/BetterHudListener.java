package bodyhealth.listeners;

import bodyhealth.Main;
import bodyhealth.config.Config;
import bodyhealth.config.Debug;
import bodyhealth.depend.BetterHud;
import kr.toxicity.hud.api.bukkit.event.PluginReloadedEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.IOException;

public class BetterHudListener implements Listener {

    @EventHandler
    public void onBetterHudReloaded(PluginReloadedEvent event) {
        if (System.currentTimeMillis() - Main.validationTimestamp < 10000) return; // Don't validate over and over again
        if (Config.display_betterhud_inject_config) {
            Debug.log("BetterHud (re)loaded, validating configuration...");
            Main.validationTimestamp = System.currentTimeMillis();
            try {
                BetterHud.inject();
                Debug.log("BetterHud Configuration successfully validated");
                Debug.log("Reloading BetterHud to apply potential changes");
                Main.getScheduler().runTask(() -> Bukkit
                        .dispatchCommand(Bukkit.getConsoleSender(), "betterhud reload"));
                Main.getScheduler().runTaskLaterAsynchronously(() -> {
                    try {
                        Debug.log("Adding files to BetterHud...");
                        BetterHud.add(); // Add mcmeta and icon
                    } catch (IOException e) {
                        Debug.logErr(e);
                    }
                }, 60L);

            } catch (IOException e) {
                Debug.logErr("Couldn't validate BetterHud config: " + e.getMessage());
            }
        } else {
            Debug.log("BetterHud was reloaded, but you have disabled injection & validation in BodyHealth's config.yml");
        }
    }

}
