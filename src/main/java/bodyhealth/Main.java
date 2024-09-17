package bodyhealth;

import bodyhealth.commands.BodyHealthCommand;
import bodyhealth.commands.BodyHealthTAB;
import bodyhealth.data.DataManager;
import bodyhealth.data.HealthStorage;
import bodyhealth.depend.BetterHud;
import bodyhealth.depend.PlaceholderAPI;
import bodyhealth.effects.BodyHealthEffects;
import bodyhealth.listeners.BodyHealthListener;
import bodyhealth.core.BodyHealth;
import bodyhealth.config.Config;
import bodyhealth.config.Debug;
import com.tchristofferson.configupdater.ConfigUpdater;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class Main extends JavaPlugin {
    private static Main instance;
    public static Map<UUID, BodyHealth> playerBodyHealthMap;

    @Override
    public void onEnable() {
        instance = this;

        Debug.log("Initializing...");

        // Reload and update config.yml
        saveDefaultConfig();
        File configFile = new File(getDataFolder(), "config.yml");
        try {
            ConfigUpdater.update(this, "config.yml", configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        reloadConfig();

        // Load configuration internally
        Config.load(getConfig());
        Debug.log("Configuration loaded successfully");

        // Initialize BodyHealthMap
        playerBodyHealthMap = new HashMap<>();

        // Commands and Listeners
        Bukkit.getPluginManager().registerEvents(new BodyHealthListener(), Main.getPlugin(Main.class));
        Objects.requireNonNull(Main.getPlugin(Main.class).getCommand("bodyhealth")).setExecutor(new BodyHealthCommand());
        Objects.requireNonNull(Main.getPlugin(Main.class).getCommand("bodyhealth")).setTabCompleter(new BodyHealthTAB());
        Debug.log("Registered Commands and Listeners");

        // Register Placeholders
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && Bukkit.getPluginManager().getPlugin("PlaceholderAPI").isEnabled()) {
            new PlaceholderAPI().register();
            Debug.log("Registered PlaceholderAPI extension");
        }

        // Register Placeholders
        if (Bukkit.getPluginManager().getPlugin("BetterHud") != null && Bukkit.getPluginManager().getPlugin("BetterHud").isEnabled()) {
            BetterHud.initialize();
            Debug.log("BetterHud integration enabled");
        }

        // Set up DataManager
        DataManager.setup();

        Debug.log("Scanning for existing data...");

        // Load data from storage if available
        int count = HealthStorage.loadPlayerHealthData();
        if (count > 0) Debug.log(count == 1 ? "Loaded data for 1 existing player" : "Loaded data for " + count + " existing players");
        else Debug.log("No existing data was found");

        Debug.log("System initialized");
    }

    @Override
    public void onDisable() {
        Debug.log("Disabling System...");
        for (Player player : Bukkit.getOnlinePlayers()) {
            BodyHealthEffects.removeEffectsFromPlayer(player); // Ensure that all effects are removed
        }
        Debug.log("Saving data...");
        HealthStorage.savePlayerHealthData(); // Save data to storage
        Debug.log("System disabled successfully");
    }

    public static Main getInstance() {
        return instance;
    }

}
