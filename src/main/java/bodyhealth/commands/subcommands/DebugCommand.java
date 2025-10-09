package bodyhealth.commands.subcommands;

import bodyhealth.Main;
import bodyhealth.commands.SubCommand;
import bodyhealth.config.Config;
import bodyhealth.config.Debug;
import bodyhealth.config.Lang;
import bodyhealth.util.DebugUtils;
import bodyhealth.util.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DebugCommand implements SubCommand {

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        // bodyhealth debug dump
        // bodyhealth debug toggle [normal/verbose] [on/off]

        if (args.length <= 1) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_debug_usage);
            return true;
        }

        switch(args[1].toLowerCase()) {
            case "dump" : {
                handleDump(sender);
                break;
            }
            case "toggle" : {
                handleToggle(sender, args);
                break;
            }
            default : {
                MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_debug_usage);
                break;
            }
        }

        return true;
    }

    private void handleDump(CommandSender sender) {
        Main.getScheduler().runTaskAsynchronously(() -> {
            File file = DebugUtils.createDebugDump();
            if (file != null) {
                MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_debug_dump_success
                        .replace("{File}", "plugins/BodyHealth/output/" + file.getName()));
            } else {
                MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_debug_dump_fail);
            }
        });
        MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_debug_dump_start);
    }

    private void handleToggle(CommandSender sender, String[] args) {
        String mode = args.length <= 2 ? "normal" : args[2];
        boolean isVerbose = mode.equalsIgnoreCase("verbose");
        boolean current = isVerbose ? Config.development_mode : Config.debug_mode;

        boolean target;
        if (args.length > 3) {
            String state = args[3];
            target = state.equalsIgnoreCase("on") || state.equalsIgnoreCase("true");
        } else {
            target = !current;
        }

        if (isVerbose) {
            Config.development_mode = target;
            MessageUtils.notifySender(sender, Config.prefix +
                    (target ? Lang.bodyhealth_debug_toggle_verbose_on
                            : Lang.bodyhealth_debug_toggle_verbose_off));
        } else {
            Config.debug_mode = target;
            MessageUtils.notifySender(sender, Config.prefix +
                    (target ? Lang.bodyhealth_debug_toggle_normal_on
                            : Lang.bodyhealth_debug_toggle_normal_off));
        }

        updateDebugSettingsInConfig();
    }

    private void updateDebugSettingsInConfig() {
        File configFile = new File(Main.getInstance().getDataFolder(), "config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        config.set("debug-mode", Config.debug_mode);
        config.set("development-mode", Config.development_mode);
        try {
            config.save(configFile);
        } catch (IOException e) {
            Debug.logErr("Could not save config: " + e.getMessage());
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 2) {
            String partial = args[1].toLowerCase();
            for (String opt : Arrays.asList("dump", "toggle")) {
                if (opt.startsWith(partial)) out.add(opt);
            }
            return out;
        }
        if (args.length == 3) {
            String op = args[1].toLowerCase();
            if (op.equals("toggle")) {
                String partial = args[2].toLowerCase();
                for (String opt : Arrays.asList("normal", "verbose")) {
                    if (opt.startsWith(partial)) out.add(opt);
                }
            }
            return out;
        }
        if (args.length == 4) {
            String op = args[1].toLowerCase();
            if (op.equals("toggle")) {
                String partial = args[3].toLowerCase();
                for (String opt : Arrays.asList("on", "off")) {
                    if (opt.startsWith(partial)) out.add(opt);
                }
            }
            return out;
        }
        return Collections.emptyList();
    }

    @Override
    public String permission() {
        return "bodyhealth.debug";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

}
