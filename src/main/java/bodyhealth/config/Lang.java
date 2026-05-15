package bodyhealth.config;

import bodyhealth.core.BodyPart;
import bodyhealth.core.BodyPartState;
import org.bukkit.configuration.file.FileConfiguration;

public class Lang {

    // Body Parts
    public static String HEAD;
    public static String TORSO;
    public static String ARM_LEFT;
    public static String ARM_RIGHT;
    public static String LEG_LEFT;
    public static String LEG_RIGHT;
    public static String FOOT_LEFT;
    public static String FOOT_RIGHT;

    // Body Part States
    public static String FULL;
    public static String NEARLYFULL;
    public static String INTERMEDIATE;
    public static String DAMAGED;
    public static String BROKEN;

    // Command Manager
    public static String bodyhealth_usage;
    public static String bodyhealth_invalid;
    public static String bodyhealth_player_only;
    public static String bodyhealth_not_permitted;

    // Add Command
    public static String bodyhealth_add_usage;
    public static String bodyhealth_add_no_target;
    public static String bodyhealth_add_invalid_value;
    public static String bodyhealth_add_success_all;
    public static String bodyhealth_add_success_single;

    // Get Command
    public static String bodyhealth_get_usage;
    public static String bodyhealth_get_no_target;
    public static String bodyhealth_get_success_all;
    public static String bodyhealth_get_success_single;

    // Heal Command
    public static String bodyhealth_heal_usage;
    public static String bodyhealth_heal_no_target;
    public static String bodyhealth_heal_success_all;
    public static String bodyhealth_heal_success_single;

    // Reload Command
    public static String bodyhealth_reload_success;
    public static String bodyhealth_reload_fail;

    // Set Command
    public static String bodyhealth_set_usage;
    public static String bodyhealth_set_no_target;
    public static String bodyhealth_set_invalid_value;
    public static String bodyhealth_set_success_all;
    public static String bodyhealth_set_success_single;

    // Data Command
    public static String bodyhealth_data_usage;
    public static String bodyhealth_data_dump_usage;
    public static String bodyhealth_data_erase_usage;
    public static String bodyhealth_data_move_usage;
    public static String bodyhealth_data_erase_confirmation;
    public static String bodyhealth_data_move_confirmation;
    public static String bodyhealth_data_save_success;
    public static String bodyhealth_data_dump_success;
    public static String bodyhealth_data_erase_success;
    public static String bodyhealth_data_move_success;
    public static String bodyhealth_data_dump_fail;
    public static String bodyhealth_data_erase_fail;
    public static String bodyhealth_data_move_fail;
    public static String bodyhealth_data_dump_start;
    public static String bodyhealth_data_erase_start;
    public static String bodyhealth_data_move_start;

    // Addon Command
    public static String bodyhealth_addon_usage;
    public static String bodyhealth_addon_not_found;
    public static String bodyhealth_addon_list_header;
    public static String bodyhealth_addon_list_separator;
    public static String bodyhealth_addon_list_entry_enabled;
    public static String bodyhealth_addon_list_entry_disabled;
    public static String bodyhealth_addon_list_none;
    public static String bodyhealth_addon_info_usage;
    public static String bodyhealth_addon_info_display;
    public static String bodyhealth_addon_status_enabled;
    public static String bodyhealth_addon_status_disabled;
    public static String bodyhealth_addon_enable_usage;
    public static String bodyhealth_addon_enable_already;
    public static String bodyhealth_addon_enable_success;
    public static String bodyhealth_addon_enable_fail;
    public static String bodyhealth_addon_disable_usage;
    public static String bodyhealth_addon_disable_already;
    public static String bodyhealth_addon_disable_success;
    public static String bodyhealth_addon_disable_fail;
    public static String bodyhealth_addon_reload_usage;
    public static String bodyhealth_addon_reload_success;
    public static String bodyhealth_addon_reload_fail;
    public static String bodyhealth_addon_load_usage;
    public static String bodyhealth_addon_load_not_found;
    public static String bodyhealth_addon_load_warning;
    public static String bodyhealth_addon_load_success;
    public static String bodyhealth_addon_load_already;
    public static String bodyhealth_addon_load_fail;
    public static String bodyhealth_addon_unload_usage;
    public static String bodyhealth_addon_unload_warning;
    public static String bodyhealth_addon_unload_success;
    public static String bodyhealth_addon_unload_fail;

    // Debug Command
    public static String bodyhealth_debug_usage;
    public static String bodyhealth_debug_dump_start;
    public static String bodyhealth_debug_dump_success;
    public static String bodyhealth_debug_dump_fail;
    public static String bodyhealth_debug_toggle_normal_on;
    public static String bodyhealth_debug_toggle_normal_off;
    public static String bodyhealth_debug_toggle_verbose_on;
    public static String bodyhealth_debug_toggle_verbose_off;

    public static void load(FileConfiguration config) {

        // Body Parts
        HEAD = config.getString("definitions.body-parts.HEAD", "HEAD");
        TORSO = config.getString("definitions.body-parts.TORSO", "TORSO");
        ARM_LEFT = config.getString("definitions.body-parts.ARM_LEFT", "ARM_LEFT");
        ARM_RIGHT = config.getString("definitions.body-parts.ARM_RIGHT", "ARM_RIGHT");
        LEG_LEFT = config.getString("definitions.body-parts.LEG_LEFT", "LEG_LEFT");
        LEG_RIGHT = config.getString("definitions.body-parts.LEG_RIGHT", "LEG_RIGHT");
        FOOT_LEFT = config.getString("definitions.body-parts.FOOT_LEFT", "FOOT_LEFT");
        FOOT_RIGHT = config.getString("definitions.body-parts.FOOT_RIGHT", "FOOT_RIGHT");

        FULL = config.getString("definitions.body-part-states.FULL", "FULL");
        NEARLYFULL = config.getString("definitions.body-part-states.NEARLYFULL", "NEARLYFULL");
        INTERMEDIATE = config.getString("definitions.body-part-states.INTERMEDIATE", "INTERMEDIATE");
        DAMAGED = config.getString("definitions.body-part-states.DAMAGED", "DAMAGED");
        BROKEN = config.getString("definitions.body-part-states.BROKEN", "BROKEN");

        // Command Manager
        bodyhealth_usage = config.getString("commands.bodyhealth.usage", "bodyhealth_usage");
        bodyhealth_invalid = config.getString("commands.bodyhealth.invalid", "bodyhealth_invalid");
        bodyhealth_player_only = config.getString("commands.bodyhealth.player-only", "bodyhealth_player_only");
        bodyhealth_not_permitted = config.getString("commands.bodyhealth.not-permitted", "bodyhealth_not_permitted");

        // Add Command
        bodyhealth_add_usage = config.getString("commands.bodyhealth.add.usage", "bodyhealth_add_usage");
        bodyhealth_add_no_target = config.getString("commands.bodyhealth.add.no-target", "bodyhealth_add_no_target");
        bodyhealth_add_invalid_value = config.getString("commands.bodyhealth.add.invalid-value", "bodyhealth_add_invalid_value");
        bodyhealth_add_success_all = config.getString("commands.bodyhealth.add.success-all", "bodyhealth_add_success_all");
        bodyhealth_add_success_single = config.getString("commands.bodyhealth.add.success-single", "bodyhealth_add_success_single");

        // Get Command
        bodyhealth_get_usage = config.getString("commands.bodyhealth.get.usage", "bodyhealth_get_usage");
        bodyhealth_get_no_target = config.getString("commands.bodyhealth.get.no-target", "bodyhealth_get_no_target");
        bodyhealth_get_success_all = config.getString("commands.bodyhealth.get.success-all", "bodyhealth_get_success_all");
        bodyhealth_get_success_single = config.getString("commands.bodyhealth.get.success-single", "bodyhealth_get_success_single");

        // Heal Command
        bodyhealth_heal_usage = config.getString("commands.bodyhealth.heal.usage", "bodyhealth_heal_usage");
        bodyhealth_heal_no_target = config.getString("commands.bodyhealth.heal.no-target", "bodyhealth_heal_no_target");
        bodyhealth_heal_success_all = config.getString("commands.bodyhealth.heal.success-all", "bodyhealth_heal_success_all");
        bodyhealth_heal_success_single = config.getString("commands.bodyhealth.heal.success-single", "bodyhealth_heal_success_single");

        // Reload Command
        bodyhealth_reload_success = config.getString("commands.bodyhealth.reload.success", "bodyhealth_reload_success");
        bodyhealth_reload_fail = config.getString("commands.bodyhealth.reload.fail", "bodyhealth_reload_fail");

        // Set Command
        bodyhealth_set_usage = config.getString("commands.bodyhealth.set.usage", "bodyhealth_set_usage");
        bodyhealth_set_no_target = config.getString("commands.bodyhealth.set.no-target", "bodyhealth_set_no_target");
        bodyhealth_set_invalid_value = config.getString("commands.bodyhealth.set.invalid-value", "bodyhealth_set_invalid_value");
        bodyhealth_set_success_all = config.getString("commands.bodyhealth.set.success-all", "bodyhealth_set_success_all");
        bodyhealth_set_success_single = config.getString("commands.bodyhealth.set.success-single", "bodyhealth_set_success_single");

        // Data Command
        bodyhealth_data_usage = config.getString("commands.bodyhealth.data.usage", "bodyhealth_data_usage");
        bodyhealth_data_dump_usage = config.getString("commands.bodyhealth.data.dump.usage", "bodyhealth_data_dump_usage");
        bodyhealth_data_erase_usage = config.getString("commands.bodyhealth.data.erase.usage", "bodyhealth_data_erase_usage");
        bodyhealth_data_move_usage = config.getString("commands.bodyhealth.data.move.usage", "bodyhealth_data_move_usage");
        bodyhealth_data_erase_confirmation = config.getString("commands.bodyhealth.data.erase.confirmation", "bodyhealth_data_erase_confirmation");
        bodyhealth_data_move_confirmation = config.getString("commands.bodyhealth.data.move.confirmation", "bodyhealth_data_move_confirmation");
        bodyhealth_data_save_success = config.getString("commands.bodyhealth.data.save.success", "bodyhealth_data_save_success");
        bodyhealth_data_dump_success = config.getString("commands.bodyhealth.data.dump.success", "bodyhealth_data_dump_success");
        bodyhealth_data_erase_success = config.getString("commands.bodyhealth.data.erase.success", "bodyhealth_data_erase_success");
        bodyhealth_data_move_success = config.getString("commands.bodyhealth.data.move.success", "bodyhealth_data_move_success");
        bodyhealth_data_dump_fail = config.getString("commands.bodyhealth.data.dump.fail", "bodyhealth_data_dump_fail");
        bodyhealth_data_erase_fail = config.getString("commands.bodyhealth.data.erase.fail", "bodyhealth_data_erase_fail");
        bodyhealth_data_move_fail = config.getString("commands.bodyhealth.data.move.fail", "bodyhealth_data_move_fail");
        bodyhealth_data_dump_start = config.getString("commands.bodyhealth.data.dump.start", "bodyhealth_data_dump_start");
        bodyhealth_data_erase_start = config.getString("commands.bodyhealth.data.erase.start", "bodyhealth_data_erase_start");
        bodyhealth_data_move_start = config.getString("commands.bodyhealth.data.move.start", "bodyhealth_data_move_start");

        // Addon Command
        bodyhealth_addon_usage = config.getString("commands.bodyhealth.addon.usage", "bodyhealth_addon_usage");
        bodyhealth_addon_not_found = config.getString("commands.bodyhealth.addon.not-found", "bodyhealth_addon_not_found");
        bodyhealth_addon_list_header = config.getString("commands.bodyhealth.addon.list.header", "bodyhealth_addon_list_header");
        bodyhealth_addon_list_separator = config.getString("commands.bodyhealth.addon.list.separator", "bodyhealth_addon_list_separator");
        bodyhealth_addon_list_entry_enabled = config.getString("commands.bodyhealth.addon.list.entry-enabled", "bodyhealth_addon_list_entry_enabled");
        bodyhealth_addon_list_entry_disabled = config.getString("commands.bodyhealth.addon.list.entry-disabled", "bodyhealth_addon_list_entry_disabled");
        bodyhealth_addon_list_none = config.getString("commands.bodyhealth.addon.list.none", "bodyhealth_addon_list_none");
        bodyhealth_addon_info_usage = config.getString("commands.bodyhealth.addon.info.usage", "bodyhealth_addon_info_usage");
        bodyhealth_addon_info_display = config.getString("commands.bodyhealth.addon.info.display", "bodyhealth_addon_info_display");
        bodyhealth_addon_status_enabled = config.getString("commands.bodyhealth.addon.info.status-enabled", "bodyhealth_addon_status_enabled");
        bodyhealth_addon_status_disabled = config.getString("commands.bodyhealth.addon.info.status-disabled", "bodyhealth_addon_status_disabled");
        bodyhealth_addon_enable_usage = config.getString("commands.bodyhealth.addon.enable.usage", "bodyhealth_addon_enable_usage");
        bodyhealth_addon_enable_already = config.getString("commands.bodyhealth.addon.enable.already-enabled", "bodyhealth_addon_enable_already");
        bodyhealth_addon_enable_success = config.getString("commands.bodyhealth.addon.enable.success", "bodyhealth_addon_enable_success");
        bodyhealth_addon_enable_fail = config.getString("commands.bodyhealth.addon.enable.fail", "bodyhealth_addon_enable_fail");
        bodyhealth_addon_disable_usage = config.getString("commands.bodyhealth.addon.disable.usage", "bodyhealth_addon_disable_usage");
        bodyhealth_addon_disable_already = config.getString("commands.bodyhealth.addon.disable.already-disabled", "bodyhealth_addon_disable_already");
        bodyhealth_addon_disable_success = config.getString("commands.bodyhealth.addon.disable.success", "bodyhealth_addon_disable_success");
        bodyhealth_addon_disable_fail = config.getString("commands.bodyhealth.addon.disable.fail", "bodyhealth_addon_disable_fail");
        bodyhealth_addon_reload_usage = config.getString("commands.bodyhealth.addon.reload.usage", "bodyhealth_addon_reload_usage");
        bodyhealth_addon_reload_success = config.getString("commands.bodyhealth.addon.reload.success", "bodyhealth_addon_reload_success");
        bodyhealth_addon_reload_fail = config.getString("commands.bodyhealth.addon.reload.fail", "bodyhealth_addon_reload_fail");
        bodyhealth_addon_load_usage = config.getString("commands.bodyhealth.addon.load.usage", "bodyhealth_addon_load_usage");
        bodyhealth_addon_load_not_found = config.getString("commands.bodyhealth.addon.load.not-found", "bodyhealth_addon_load_not_found");
        bodyhealth_addon_load_warning = config.getString("commands.bodyhealth.addon.load.warning", "bodyhealth_addon_load_warning");
        bodyhealth_addon_load_success = config.getString("commands.bodyhealth.addon.load.success", "bodyhealth_addon_load_success");
        bodyhealth_addon_load_already = config.getString("commands.bodyhealth.addon.load.already-loaded", "bodyhealth_addon_load_already");
        bodyhealth_addon_load_fail = config.getString("commands.bodyhealth.addon.load.fail", "bodyhealth_addon_load_fail");
        bodyhealth_addon_unload_usage = config.getString("commands.bodyhealth.addon.unload.usage", "bodyhealth_addon_unload_usage");
        bodyhealth_addon_unload_warning = config.getString("commands.bodyhealth.addon.unload.warning", "bodyhealth_addon_unload_warning");
        bodyhealth_addon_unload_success = config.getString("commands.bodyhealth.addon.unload.success", "bodyhealth_addon_unload_success");
        bodyhealth_addon_unload_fail = config.getString("commands.bodyhealth.addon.unload.fail", "bodyhealth_addon_unload_fail");

        // Debug Command
        bodyhealth_debug_usage = config.getString("commands.bodyhealth.debug.usage", "bodyhealth_debug_usage");
        bodyhealth_debug_dump_start = config.getString("commands.bodyhealth.debug.dump.start", "bodyhealth_debug_dump_start");
        bodyhealth_debug_dump_success = config.getString("commands.bodyhealth.debug.dump.success", "bodyhealth_debug_dump_success");
        bodyhealth_debug_dump_fail = config.getString("commands.bodyhealth.debug.dump.fail", "bodyhealth_debug_dump_fail");
        bodyhealth_debug_toggle_normal_on = config.getString("commands.bodyhealth.debug.toggle.normal-on", "bodyhealth_debug_toggle_normal_on");
        bodyhealth_debug_toggle_normal_off = config.getString("commands.bodyhealth.debug.toggle.normal-off", "bodyhealth_debug_toggle_normal_off");
        bodyhealth_debug_toggle_verbose_on = config.getString("commands.bodyhealth.debug.toggle.verbose-on", "bodyhealth_debug_toggle_verbose_on");
        bodyhealth_debug_toggle_verbose_off = config.getString("commands.bodyhealth.debug.toggle.verbose-off", "bodyhealth_debug_toggle_verbose_off");

    }

    // If someone's got a better approach, let me know
    public static String partName(BodyPart part) {
        return switch (part) {
            case HEAD -> HEAD;
            case TORSO -> TORSO;
            case ARM_LEFT -> ARM_LEFT;
            case ARM_RIGHT -> ARM_RIGHT;
            case LEG_LEFT -> LEG_LEFT;
            case LEG_RIGHT -> LEG_RIGHT;
            case FOOT_LEFT -> FOOT_LEFT;
            case FOOT_RIGHT -> FOOT_RIGHT;
        };
    }

    public static String stateName(BodyPartState state) {
        return switch (state) {
            case FULL -> FULL;
            case NEARLYFULL -> NEARLYFULL;
            case INTERMEDIATE -> INTERMEDIATE;
            case DAMAGED -> DAMAGED;
            case BROKEN -> BROKEN;
        };
    }
}
