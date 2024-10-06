package bodyhealth.listeners;

import bodyhealth.Main;
import bodyhealth.config.Config;
import bodyhealth.config.Debug;
import bodyhealth.depend.BetterHud;
import kr.toxicity.hud.api.bukkit.event.PluginReloadedEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;

public class BetterHudListener implements Listener {

    @EventHandler
    public void onBetterHudReloaded(PluginReloadedEvent event) {
        if (System.currentTimeMillis() - Main.validationTimestamp < 10000) return; // Don't validate over and over again
        if (Config.inject_betterhud_config) {
            Debug.log("BetterHud (re)loaded, validating configuration...");
            Main.validationTimestamp = System.currentTimeMillis();
            try {
                BetterHud.inject();
                Debug.log("BetterHud Configuration successfully validated");
                Debug.log("Reloading BetterHud to apply potential changes");
                Bukkit.getScheduler().runTask(Main.getInstance(), () -> { // Dispatch command synchronously
                    ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                    Bukkit.dispatchCommand(console, "betterhud reload");
                });
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            Debug.log("Adding files to BetterHud...");
                            BetterHud.add(); // Add mcmeta and icon
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }.runTaskLater(Main.getInstance(), 60); // Wait for BetterHud to clean the directory

            } catch (IOException e) {
                Debug.logErr("Couldn't validate BetterHud config: " + e.getMessage());
            }
        } else {
            Debug.log("BetterHud was reloaded, but you have disabled validation in BodyHealth's config.yml");
        }
    }

}
