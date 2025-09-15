package bodyhealth.config;

import bodyhealth.core.BodyPart;
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
    public static String bodyhealth_data_usage_dump;
    public static String bodyhealth_data_usage_erase;
    public static String bodyhealth_data_usage_move;
    public static String bodyhealth_data_confirmation_erase;
    public static String bodyhealth_data_confirmation_move;
    public static String bodyhealth_data_success_save;
    public static String bodyhealth_data_success_dump;
    public static String bodyhealth_data_success_erase;
    public static String bodyhealth_data_success_move;
    public static String bodyhealth_data_fail_dump;
    public static String bodyhealth_data_fail_erase;
    public static String bodyhealth_data_fail_move;

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
        bodyhealth_data_usage_dump = config.getString("commands.bodyhealth.data.usage-dump", "bodyhealth_data_usage_dump");
        bodyhealth_data_usage_erase = config.getString("commands.bodyhealth.data.usage-erase", "bodyhealth_data_usage_erase");
        bodyhealth_data_usage_move = config.getString("commands.bodyhealth.data.usage-move", "bodyhealth_data_usage_move");
        bodyhealth_data_confirmation_erase = config.getString("commands.bodyhealth.data.confirmation-erase", "bodyhealth_data_confirmation_erase");
        bodyhealth_data_confirmation_move = config.getString("commands.bodyhealth.data.confirmation-move", "bodyhealth_data_confirmation_move");
        bodyhealth_data_success_save = config.getString("commands.bodyhealth.data.success-save", "bodyhealth_data_success_save");
        bodyhealth_data_success_dump = config.getString("commands.bodyhealth.data.success-dump", "bodyhealth_data_success_dump");
        bodyhealth_data_success_erase = config.getString("commands.bodyhealth.data.success-erase", "bodyhealth_data_success_erase");
        bodyhealth_data_success_move = config.getString("commands.bodyhealth.data.success-move", "bodyhealth_data_success_move");
        bodyhealth_data_fail_dump = config.getString("commands.bodyhealth.data.fail-dump", "bodyhealth_data_fail_dump");
        bodyhealth_data_fail_erase = config.getString("commands.bodyhealth.data.fail-erase", "bodyhealth_data_fail_erase");
        bodyhealth_data_fail_move = config.getString("commands.bodyhealth.data.fail-move", "bodyhealth_data_fail_move");

    }

    // If someone's got a better approach, let me know
    public static String partName(BodyPart bodyPart) {
        return switch (bodyPart) {
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
}
