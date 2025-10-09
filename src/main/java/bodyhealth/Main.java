package bodyhealth;

import bodyhealth.api.addons.AddonManager;
import bodyhealth.commands.CommandManager;
import bodyhealth.config.Lang;
import bodyhealth.data.DataManager;
import bodyhealth.depend.PlaceholderAPI;
import bodyhealth.depend.WorldGuard;
import bodyhealth.effects.EffectHandler;
import bodyhealth.listeners.*;
import bodyhealth.config.Config;
import bodyhealth.config.Debug;
import bodyhealth.migrations.Migrator;
import bodyhealth.util.FoliaUtils;
import bodyhealth.util.UpdateChecker;
import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import com.tchristofferson.configupdater.ConfigUpdater;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class Main extends JavaPlugin {

    private static Main instance;
    private static Migrator migrator;
    private static List<String> languages;
    public static PlaceholderAPI placeholderAPIexpansion;
    private static final Object MUTEX = new Object();
    private static BukkitAudiences adventure;
    private static TaskScheduler scheduler;
    public static long validationTimestamp;
    private static AddonManager addonManager;


    @Override
    public void onLoad() {
        instance = this;
        migrator = new Migrator(this);
        validationTimestamp = 0;
        languages = new ArrayList<>();
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) WorldGuard.initialize();
        addonManager = new AddonManager(this);
        addonManager.loadAddons();
        migrator.onLoad();
    }

    @Override
    public void onEnable() {

        // Load adventure
        adventure = BukkitAudiences.create(this);

        // Load scheduler
        scheduler = UniversalScheduler.getScheduler(this);

        // Migrate config
        migrator.onEnable();

        // Reload and update config
        saveDefaultConfig();
        File configFile = new File(getDataFolder(), "config.yml");
        try {
            ConfigUpdater.update(this, "config.yml", configFile);
        } catch (IOException e) {
            Debug.logErr(e);
        }
        reloadConfig();

        // Load config internally
        Config.load(getConfig());

        // Ensure language directory exists
        File languageDir = new File(getDataFolder(), "language");
        if (!languageDir.exists()) languageDir.mkdirs();

        // Filter out language files from the plugins JarFile
        try (JarFile jar = new JarFile(getFile())) {
            Enumeration<JarEntry> entries = jar.entries();
            Debug.log("Loading language files...");
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith("language/") && entry.getName().endsWith(".yml")) {
                    String langFileName = entry.getName().replace("language/", "");
                    Debug.logDev("Loading language file: " + langFileName);
                    languages.add(langFileName);

                    // Save and update language files
                    if (!new File(languageDir, langFileName).exists()) saveResource("language/" + langFileName, false);
                    ConfigUpdater.update(this, "language/" + langFileName, new File(languageDir, langFileName));
                }
            }
        } catch (IOException e) {
            Debug.logErr("Failed to update language files: " + e.getMessage());
            Debug.logErr(e);
        }

        // Get the selected language file
        File languageFile = new File(languageDir, Config.language + ".yml");
        if (!languageFile.exists()) {
            Debug.logErr("Language " + Config.language + " doesn't exist in your language folder! Defaulting to English (en).");
            languageFile = new File(languageDir, "en.yml");
            if (!languageFile.exists()) saveResource("language/en.yml", false); // Should not be necessary
        }

        // Load language configuration
        FileConfiguration languageConfig = YamlConfiguration.loadConfiguration(languageFile);

        // Load language internally
        Lang.load(languageConfig);

        Debug.log("Configuration loaded successfully");

        // Enable addons
        addonManager.enableAddons();

        // Commands and Listeners
        Bukkit.getPluginManager().registerEvents(new BodyHealthListener(), this);
        Objects.requireNonNull(getCommand("bodyhealth")).setExecutor(new CommandManager());
        Debug.log("Registered Commands and Listeners");

        // Register Placeholders
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && Bukkit.getPluginManager().getPlugin("PlaceholderAPI").isEnabled()) {
            placeholderAPIexpansion = new PlaceholderAPI();
            placeholderAPIexpansion.register();
            Debug.log("Registered PlaceholderAPI expansion");

            // BetterHud integration - Only works with PAPI installed
            if (Bukkit.getPluginManager().getPlugin("BetterHud") != null && Bukkit.getPluginManager().getPlugin("BetterHud").isEnabled()) {
                Debug.log("BetterHud detected, enabling BetterHud integration...");
                BetterHudListener bhl = new BetterHudListener();
                Bukkit.getPluginManager().registerEvents(bhl, this);

                /*
                 * BetterHud loads its pack asynchronously, meaning it might not be finished
                 * doing that by this point. We therefore wait another three seconds before
                 * simulating a reload to trigger BodyHealth's file validation process there
                 */
                scheduler.runTaskLater(() -> bhl.onBetterHudReloaded(null), 60L);
                Debug.log("BetterHud integration enabled");

                Debug.log("The BetterHud integration requires the PlaceholderAPI expansion 'Player' to be installed. Setting up a Listener to ensure it is present at all times.");
                Bukkit.getPluginManager().registerEvents(new PlaceholderAPIListener(), this);
            }

        }

        else if (Bukkit.getPluginManager().getPlugin("BetterHud") != null) Debug.logErr("BetterHud integration requires PlaceholderAPI to be installed!");

        // Set up DataManager
        DataManager.load();

        // Check for updates
        UpdateChecker updateChecker = new UpdateChecker("BodyHealth", "bodyhealth", getDescription().getVersion()).checkNow();
        if (Config.update_check_interval > 0) updateChecker.checkEveryXHours(Config.update_check_interval);
        Bukkit.getPluginManager().registerEvents(new UpdateNotifyListener(updateChecker), this);

        // Metrics
        if (Config.metrics) {
            Metrics metrics = new Metrics(this, 23538);
            Debug.log("Metrics enabled");
        }

        Debug.log("System initialized");
    }

    @Override
    public void onDisable() {
        Debug.log("Disabling System...");
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!FoliaUtils.isFolia() && Config.remove_effects_on_shutdown)
                EffectHandler.removeEffectsFromPlayer(player);
            DataManager.saveBodyHealth(player.getUniqueId());
        }
        migrator.onDisable();
        addonManager.unloadAddons();
        if (adventure != null) {
            adventure.close();
            adventure = null;
        }
        Debug.log("System disabled successfully");
    }

    public static @NotNull BukkitAudiences getAdventure() {
        if (adventure == null) throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        return adventure;
    }

    public static Main getInstance() {
        return instance;
    }

    public static TaskScheduler getScheduler() {
        return scheduler;
    }

    public static AddonManager getAddonManager() {
        return addonManager;
    }

    public static List<String> getLanguages() {
        return languages;
    }

    public static Migrator getMigrator() {
        return migrator;
    }

    public static Object getMutexObj() {
        return MUTEX;
    }

}
