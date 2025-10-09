package bodyhealth.commands;

import bodyhealth.commands.subcommands.*;
import bodyhealth.config.Config;
import bodyhealth.config.Lang;
import bodyhealth.util.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandManager implements TabExecutor {

    private static final Map<String, SubCommand> subCommands = new HashMap<>();

    public CommandManager() {
        subCommands.put("reload", new ReloadCommand());
        subCommands.put("heal", new HealCommand());
        subCommands.put("get", new GetCommand());
        subCommands.put("set", new SetCommand());
        subCommands.put("add", new AddCommand());
        subCommands.put("data", new DataCommand());
        subCommands.put("debug", new DebugCommand());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (args.length < 1) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_usage);
            return true;
        }

        SubCommand subCommand = subCommands.get(args[0]);
        if (subCommand == null) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_invalid);
            return true;
        }

        boolean playerOnly = subCommand.playerOnly();
        String permission = subCommand.permission();

        if (playerOnly && !(sender instanceof Player)) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_player_only);
            return true;
        } else if (permission != null && !sender.hasPermission(permission)) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_not_permitted);
            return true;
        }

        return subCommand.execute(sender, args);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 1) {
            List<String> commands = new ArrayList<>();
            for (Map.Entry<String, SubCommand> entry : subCommands.entrySet()) {
                String perm = entry.getValue().permission();
                if (perm != null && commandSender.hasPermission(perm)) {
                    commands.add(entry.getKey());
                }
            }
            return commands;
        }

        SubCommand subCommand = subCommands.get(strings[0].toLowerCase());
        if (subCommand != null) {
            return subCommand.tabComplete(commandSender, strings);
        }
        return null;
    }

    public static void addSubCommand(String name, SubCommand subCommand) {
        subCommands.put(name, subCommand);
    }

    public static void removeSubCommand(String name) {
        subCommands.remove(name);
    }

}
