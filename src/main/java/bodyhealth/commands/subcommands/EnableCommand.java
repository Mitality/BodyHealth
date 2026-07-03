package bodyhealth.commands.subcommands;

import bodyhealth.commands.SubCommand;
import bodyhealth.config.Config;
import bodyhealth.config.Lang;
import bodyhealth.depend.VanishPlugins;
import bodyhealth.util.BodyHealthUtils;
import bodyhealth.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class EnableCommand implements SubCommand {

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (args.length <= 1 && !(sender instanceof Player)) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_enable_no_target);
            return true;
        }

        Player target = args.length > 1 ? Bukkit.getPlayer(args[1]) : (Player) sender;
        if (target == null) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_enable_invalid_target.replace("{Player}", args[1]));
            return true;
        }

        if (sender instanceof Player player && player.equals(target)) {
            BodyHealthUtils.getBodyHealth(target).setEnabled(true);
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_enable_success_self);
        } else {

            if (!sender.hasPermission("bodyhealth.enable.others")) {
                MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_enable_denied_other);
                return true;
            }

            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_enable_success_other.replace("{Player}", target.getName()));
            MessageUtils.notifySender(target, Config.prefix + Lang.bodyhealth_enable_notification.replace("{Player}", sender.getName()));
        }

        BodyHealthUtils.applyBodyHealthHudVisibility(target);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {

        boolean perm = sender.hasPermission("bodyhealth.enable.others");

        if (args.length == 2) {
            if (args[1].isEmpty()) {
                return !perm ? List.of(sender.getName()) : Bukkit.getOnlinePlayers().stream()
                        .filter(player -> !VanishPlugins.isVanished(player))
                        .map(Player::getName)
                        .toList();
            } else {
                String partialInput = args[1].toUpperCase();
                List<String> result = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!perm && player.getName().equals(sender.getName())) continue;
                    if (player.getName().toUpperCase().startsWith(partialInput)
                            && !VanishPlugins.isVanished(player)
                    ) result.add(player.getName());
                }
                return result;
            }
        }

        return List.of();
    }

    @Override
    public String permission() {
        return "bodyhealth.disable";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }
}
