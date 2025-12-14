package bodyhealth.config;

import bodyhealth.data.StorageType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Config {

    public static String language;
    public static String prefix;

    public static boolean debug_mode;
    public static boolean development_mode;
    public static boolean error_logging;

    public static int update_check_interval;
    public static boolean releases_only;
    public static boolean metrics;

    public static boolean self_harm;
    public static boolean always_allow_eating;
    public static boolean lenient_movement_restrictions;
    public static boolean remove_effects_on_shutdown;
    public static boolean hide_potion_effects;
    public static boolean kill_with_command;

    public static int force_keep_time;
    public static boolean force_keep_relative;
    public static int confirmation_expiration;

    public static boolean wold_blacklist_whitelist_mode;
    public static List<String> world_blacklist_worlds = new ArrayList<>();

    public static boolean raytracing_enabled;
    public static boolean raytracing_fix_rotation;
    public static double  raytracing_max_distance;
    public static double  raytracing_step_size;

    public static StorageType storage_type;

    public static String storage_mysql_host;
    public static String storage_mysql_port;
    public static String storage_mysql_user;
    public static String storage_mysql_password;
    public static String storage_mysql_database;
    public static String storage_mysql_prefix;

    public static ConfigurationSection body_health;
    public static ConfigurationSection body_damage;
    public static ConfigurationSection effects;

    public static boolean display_betterhud_auto_reload;
    public static boolean display_betterhud_inject_config;
    public static boolean display_betterhud_as_default;

    public static String display_betterhud_position_anchor_point;
    public static int display_betterhud_position_horizontal_offset;
    public static int display_betterhud_position_vertical_offset;
    public static double display_betterhud_position_scale;

    public static boolean display_betterhud_disable_entity_popup;
    public static boolean display_betterhud_disable_default_hud;
    public static boolean display_betterhud_disable_compass;

    public static boolean display_betterhud_add_mcmeta;
    public static boolean display_betterhud_add_icon;

    public static boolean display_betterhud_package_compress;
    public static String  display_betterhud_package_filename;

    public static void load(FileConfiguration config) {

        language = config.getString("language", "en");
        prefix = config.getString("prefix", "&8&l[&e&lBodyHealth&8&l] &r");

        debug_mode = config.getBoolean("debug-mode", false);
        development_mode = config.getBoolean("development-mode", false);
        error_logging = config.getBoolean("error-logging", true);

        update_check_interval = config.getInt("update-check-interval", 12);
        releases_only = config.getBoolean("releases-only", true);
        metrics = config.getBoolean("metrics", true);

        self_harm = config.getBoolean("self-harm", false);
        always_allow_eating = config.getBoolean("always-allow-eating", true);
        lenient_movement_restrictions = config.getBoolean("lenient-movement-restrictions", false);
        remove_effects_on_shutdown = config.getBoolean("remove-effects-on-shutdown", true);
        hide_potion_effects = config.getBoolean("hide-potion-effects", true);
        kill_with_command = config.getBoolean("kill-with-command", true);

        force_keep_time = config.getInt("force-keep-time", 10);
        force_keep_relative = config.getBoolean("force-keep-relative", false);
        confirmation_expiration = config.getInt("confirmation-expiration", 30);

        wold_blacklist_whitelist_mode = config.getBoolean("world-blacklist.whitelist-mode", false);
        world_blacklist_worlds = config.getStringList("world-blacklist.worlds");

        raytracing_enabled = config.getBoolean("raytracing.enabled", true);
        raytracing_fix_rotation = config.getBoolean("raytracing.fix-rotation", true);
        raytracing_max_distance = config.getDouble("raytracing.max-distance", 10.0);
        raytracing_step_size = config.getDouble("raytracing.step-size", 0.1);

        storage_type = StorageType.fromString(config.getString("storage.type", "sqlite"));

        storage_mysql_host = config.getString("storage.mysql.host", "127.0.0.1");
        storage_mysql_port = config.getString("storage.mysql.port", "3306");
        storage_mysql_user = config.getString("storage.mysql.user", "bodyhealth");
        storage_mysql_password = config.getString("storage.mysql.password", "supersafe");
        storage_mysql_database = config.getString("storage.mysql.database", "bodyhealth");
        storage_mysql_prefix = config.getString("storage.mysql.prefix", "bh_");

        body_health = config.getConfigurationSection("body-health");
        body_damage = config.getConfigurationSection("body-damage");
        effects = config.getConfigurationSection("effects");

        display_betterhud_auto_reload = config.getBoolean("display.betterhud.auto-reload", true);
        display_betterhud_inject_config = config.getBoolean("display.betterhud.inject-config", true);
        display_betterhud_as_default = config.getBoolean("display.betterhud.as-default", true);

        display_betterhud_position_anchor_point = config.getString("display.betterhud.position.anchor-point", "BOTTOM_RIGHT");
        display_betterhud_position_horizontal_offset = config.getInt("display.betterhud.position.horizontal-offset", 0);
        display_betterhud_position_vertical_offset = config.getInt("display.betterhud.position.vertical-offset", 0);
        display_betterhud_position_scale = config.getDouble("display.betterhud.position.scale", 1.0);

        display_betterhud_disable_entity_popup = config.getBoolean("display.betterhud.disable.entity-popup", true);
        display_betterhud_disable_default_hud = config.getBoolean("display.betterhud.disable.default-hud", true);
        display_betterhud_disable_compass = config.getBoolean("display.betterhud.disable.compass", true);

        display_betterhud_add_mcmeta = config.getBoolean("display.betterhud.add.mcmeta", true);
        display_betterhud_add_icon = config.getBoolean("display.betterhud.add.icon", true);

        display_betterhud_package_compress = config.getBoolean("display.betterhud.package.compress", true);
        display_betterhud_package_filename = config.getString("display.betterhud.package.filename", "resource_pack");

    }

}