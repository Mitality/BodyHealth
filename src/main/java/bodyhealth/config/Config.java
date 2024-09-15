package bodyhealth.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {

    public static String prefix;
    public static boolean debug_mode;
    public static boolean error_logging;
    public static boolean self_harm;
    public static boolean always_allow_eating;
    public static ConfigurationSection body_health;
    public static ConfigurationSection body_damage;
    public static ConfigurationSection effects;

    public static void load(FileConfiguration config) {

        prefix = ChatColor.translateAlternateColorCodes('&',
                config.getString("prefix", "&8&l[&e&lBodyHealth&8&l] "));
        debug_mode = config.getBoolean("debug-mode", false);
        error_logging = config.getBoolean("error-logging", true);
        self_harm = config.getBoolean("self-harm", false);
        always_allow_eating = config.getBoolean("always-allow-eating", true);
        body_health = config.getConfigurationSection("body-health");
        body_damage = config.getConfigurationSection("body-damage");
        effects = config.getConfigurationSection("effects");

    }
}
