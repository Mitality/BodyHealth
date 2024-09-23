package bodyhealth.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {

    public static String prefix;

    public static boolean debug_mode;
    public static boolean development_mode;
    public static boolean error_logging;
    public static boolean self_harm;
    public static boolean always_allow_eating;
    public static boolean raytracing_enabled;
    public static boolean inject_betterhud_config;
    public static boolean inject_betterhud_config_as_default;
    public static boolean disable_betterhud_default_hud;
    public static boolean disable_betterhud_compass;
    public static boolean add_betterhud_mcmeta;
    public static boolean add_betterhud_icon;

    public static double raytracing_max_distance;
    public static double raytracing_step_size;

    public static ConfigurationSection body_health;
    public static ConfigurationSection body_damage;
    public static ConfigurationSection effects;

    public static void load(FileConfiguration config) {

        prefix = ChatColor.translateAlternateColorCodes('&',
                config.getString("prefix", "&8&l[&e&lBodyHealth&8&l] "));

        debug_mode = config.getBoolean("debug-mode", false);
        development_mode = config.getBoolean("development-mode", false);
        error_logging = config.getBoolean("error-logging", true);
        self_harm = config.getBoolean("self-harm", false);
        always_allow_eating = config.getBoolean("always-allow-eating", true);
        raytracing_enabled = config.getBoolean("raytracing.enabled", true);
        inject_betterhud_config = config.getBoolean("display.betterhud.inject-config", true);
        inject_betterhud_config_as_default = config.getBoolean("display.betterhud.as-default", true);
        disable_betterhud_default_hud = config.getBoolean("display.betterhud.disable.default-hud", true);
        disable_betterhud_compass = config.getBoolean("display.betterhud.disable.compass", true);
        add_betterhud_mcmeta = config.getBoolean("display.betterhud.add.mcmeta", true);
        add_betterhud_icon = config.getBoolean("display.betterhud.add.icon", true);

        raytracing_max_distance = config.getDouble("raytracing.max-distance", 10.0);
        raytracing_step_size = config.getDouble("raytracing.step-size", 0.1);

        body_health = config.getConfigurationSection("body-health");
        body_damage = config.getConfigurationSection("body-damage");
        effects = config.getConfigurationSection("effects");

    }
}
