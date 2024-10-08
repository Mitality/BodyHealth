package bodyhealth;

import bodyhealth.commands.BodyHealthCommand;
import bodyhealth.commands.BodyHealthTAB;
import bodyhealth.config.Lang;
import bodyhealth.data.DataManager;
import bodyhealth.data.HealthStorage;
import bodyhealth.depend.PlaceholderAPI;
import bodyhealth.depend.WorldGuard;
import bodyhealth.effects.BodyHealthEffects;
import bodyhealth.listeners.BetterHudListener;
import bodyhealth.listeners.BodyHealthListener;
import bodyhealth.core.BodyHealth;
import bodyhealth.config.Config;
import bodyhealth.config.Debug;
import bodyhealth.listeners.PlaceholderAPIListener;
import com.jeff_media.updatechecker.UpdateCheckSource;
import com.jeff_media.updatechecker.UpdateChecker;
import com.jeff_media.updatechecker.UserAgentBuilder;
import com.tchristofferson.configupdater.ConfigUpdater;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class Main extends JavaPlugin {
    private static Main instance;
    private static String SPIGOT_RESOURCE_ID;
    public static Map<UUID, BodyHealth> playerBodyHealthMap;
    public static PlaceholderAPI placeholderAPIexpansion;
    public static long validationTimestamp;

    @Override
    public void onLoad() {
        instance = this;
        validationTimestamp = 0;
        SPIGOT_RESOURCE_ID = "119966";
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null)  WorldGuard.initialize();
    }

    @Override
    public void onEnable() {

        Debug.log("Initializing...");

        // Reload and update config and language
        saveDefaultConfig();
        File configFile = new File(getDataFolder(), "config.yml");
        File languageFile = new File(getDataFolder(), "language.yml");
        if (!languageFile.exists()) saveResource("language.yml", false);
        try {
            ConfigUpdater.update(this, "config.yml", configFile);
            ConfigUpdater.update(this, "language.yml", languageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        reloadConfig();

        FileConfiguration languageConfig = YamlConfiguration.loadConfiguration(languageFile);

        // Load config and language internally
        Config.load(getConfig());
        Lang.load(languageConfig);
        Debug.log("Configuration loaded successfully");

        // Initialize BodyHealthMap
        playerBodyHealthMap = new HashMap<>();

        // Commands and Listeners
        Bukkit.getPluginManager().registerEvents(new BodyHealthListener(), this);
        Objects.requireNonNull(Main.getPlugin(Main.class).getCommand("bodyhealth")).setExecutor(new BodyHealthCommand());
        Objects.requireNonNull(Main.getPlugin(Main.class).getCommand("bodyhealth")).setTabCompleter(new BodyHealthTAB());
        Debug.log("Registered Commands and Listeners");

        // Register Placeholders
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && Bukkit.getPluginManager().getPlugin("PlaceholderAPI").isEnabled()) {
            placeholderAPIexpansion = new PlaceholderAPI();
            placeholderAPIexpansion.register();
            Debug.log("Registered PlaceholderAPI expansion");

            // BetterHud integration - Only works with PAPI installed
            if (Bukkit.getPluginManager().getPlugin("BetterHud") != null && Bukkit.getPluginManager().getPlugin("BetterHud").isEnabled()) {
                Debug.log("BetterHud detected, enabling BetterHud integration...");
                Bukkit.getPluginManager().registerEvents(new BetterHudListener(), this);
                Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> { // FIXME: This can lead to double reloads
                    ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                    Bukkit.dispatchCommand(console, "betterhud reload");
                }, 60L); // Ensure BetterHud reloads its pack after the integration is enabled
                Debug.log("BetterHud integration enabled");

                Debug.log("The BetterHud integration requires the PlaceholderAPI expansion 'Player' to be installed. Setting up a Listener to ensure it is present at all times.");
                Bukkit.getPluginManager().registerEvents(new PlaceholderAPIListener(), this);

            }

        }

        else if (Bukkit.getPluginManager().getPlugin("BetterHud") != null) Debug.logErr("BetterHud integration requires PlaceholderAPI to be installed!");

        // Set up DataManager
        DataManager.setup();

        Debug.log("Scanning for existing data...");

        // Load data from storage if available
        int count = HealthStorage.loadPlayerHealthData();
        if (count > 0) Debug.log(count == 1 ? "Loaded data for 1 existing player" : "Loaded data for " + count + " existing players");
        else Debug.log("No existing data was found");

        // Check for updates
        new UpdateChecker(this, UpdateCheckSource.SPIGOT, SPIGOT_RESOURCE_ID)
                .setDownloadLink("https://www.spigotmc.org/resources/bodyhealth.119966/")
                .setDonationLink("https://paypal.me/mitality")
                .setChangelogLink("https://www.spigotmc.org/resources/bodyhealth.119966/updates")
                .setNotifyByPermissionOnJoin("bodyhealth.update-notify")
                .setUserAgent(new UserAgentBuilder().addPluginNameAndVersion())
                .checkEveryXHours(1).checkNow();

        // Metrics
        if (Config.metrics) {
            Metrics metrics = new Metrics(this, 23538);
            Debug.log("Metrics enabled");
            // TODO: Custom charts
        }

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
