package bodyhealth.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class Lang {

    public static String bodyhealth_usage;
    public static String bodyhealth_invalid;
    public static String bodyhealth_player_not_found;
    public static String bodyhealth_invalid_part;
    public static String bodyhealth_not_permitted;
    public static String bodyhealth_player_only;

    public static String bodyhealth_reload_success;
    public static String bodyhealth_reload_fail;
    public static String bodyhealth_reload_not_permitted;

    public static String bodyhealth_get_not_permitted;
    public static String bodyhealth_get_success;

    public static String bodyhealth_set_not_permitted;
    public static String bodyhealth_set_missing_value;
    public static String bodyhealth_set_invalid_value;
    public static String bodyhealth_set_success;

    public static String bodyhealth_heal_not_permitted;
    public static String bodyhealth_heal_success;

    public static void load(FileConfiguration config) {

        bodyhealth_usage = ChatColor.translateAlternateColorCodes('&',
                config.getString("commands.bodyhealth.usage", "&bUsage: /bodyhealth <get/set/heal/reload> <player> <bodypart> [value]"));
        bodyhealth_invalid = ChatColor.translateAlternateColorCodes('&',
                config.getString("commands.bodyhealth.invalid", "&cInvalid action. Use 'get', 'set', 'heal', or 'reload'."));
        bodyhealth_player_not_found = ChatColor.translateAlternateColorCodes('&',
                config.getString("commands.bodyhealth.player-not-found", "&cPlayer {Player} not found."));
        bodyhealth_invalid_part = ChatColor.translateAlternateColorCodes('&',
                config.getString("commands.bodyhealth.invalid-part", "&cInvalid body part. Valid parts: HEAD, BODY, ARM_LEFT, ARM_RIGHT, LEG_LEFT, LEG_RIGHT, FOOT_LEFT, FOOT_RIGHT."));
        bodyhealth_not_permitted = ChatColor.translateAlternateColorCodes('&',
                config.getString("commands.bodyhealth.not-permitted", "&cYou do not have permission to use this command."));
        bodyhealth_player_only = ChatColor.translateAlternateColorCodes('&',
                config.getString("commands.bodyhealth.player-only", "&cThis subcommand may only be executed by players."));

        bodyhealth_reload_success = ChatColor.translateAlternateColorCodes('&',
                config.getString("commands.bodyhealth.reload.success", "&aConfiguration reloaded."));
        bodyhealth_reload_fail = ChatColor.translateAlternateColorCodes('&',
                config.getString("commands.bodyhealth.reload.fail", "&cConfiguration could not be reloaded."));
        bodyhealth_reload_not_permitted = ChatColor.translateAlternateColorCodes('&',
                config.getString("commands.bodyhealth.reload.not-permitted", "&cYou do not have permission to use this command."));

        bodyhealth_get_not_permitted = ChatColor.translateAlternateColorCodes('&',
                config.getString("commands.bodyhealth.get.not-permitted", "&cYou do not have permission to use this command."));
        bodyhealth_get_success = ChatColor.translateAlternateColorCodes('&',
                config.getString("commands.bodyhealth.get.success", "&a{Player}'s {Part} health: {Health}%"));

        bodyhealth_set_not_permitted = ChatColor.translateAlternateColorCodes('&',
                config.getString("commands.bodyhealth.set.not-permitted", "&cYou do not have permission to use this command."));
        bodyhealth_set_missing_value = ChatColor.translateAlternateColorCodes('&',
                config.getString("commands.bodyhealth.set.missing-value", "&ePlease specify a value."));
        bodyhealth_set_invalid_value = ChatColor.translateAlternateColorCodes('&',
                config.getString("commands.bodyhealth.set.invalid-value", "&cInvalid health value."));
        bodyhealth_set_success = ChatColor.translateAlternateColorCodes('&',
                config.getString("commands.bodyhealth.set.success", "&a{Player}'s {Part} health: {Health}%"));

        bodyhealth_heal_not_permitted = ChatColor.translateAlternateColorCodes('&',
                config.getString("commands.bodyhealth.heal.not-permitted", "&cYou do not have permission to use this command."));
        bodyhealth_heal_success = ChatColor.translateAlternateColorCodes('&',
                config.getString("commands.bodyhealth.heal.success", "&a%Player% has been fully healed."));

    }
}
