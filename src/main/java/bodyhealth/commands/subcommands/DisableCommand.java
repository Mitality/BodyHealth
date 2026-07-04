package bodyhealth.commands.subcommands;

import bodyhealth.commands.SubCommand;
import bodyhealth.config.Config;
import bodyhealth.config.Lang;
import bodyhealth.util.BodyHealthUtils;
import bodyhealth.util.MessageUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class DisableCommand implements SubCommand {

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (args.length <= 1 && !(sender instanceof Player)) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_disable_no_target);
            return true;
        }

        OfflinePlayer target = args.length > 1 ? BodyHealthUtils.resolveTarget(args[1]) : (Player) sender;
        if (target == null) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_disable_invalid_target.replace("{Player}", args[1]));
            return true;
        }

        try {
            boolean isSelf = sender instanceof Player player && player.getUniqueId().equals(target.getUniqueId());
            if (isSelf) {
                BodyHealthUtils.getBodyHealth(target).setEnabled(false);
                MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_disable_success_self);
            } else {

                if (!sender.hasPermission("bodyhealth.disable.others")) {
                    MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_disable_denied_other);
                    return true;
                }

                BodyHealthUtils.getBodyHealth(target).setEnabled(false);
                MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_disable_success_other.replace("{Player}", Objects.requireNonNull(target.getName())));
                if (target.isOnline()) MessageUtils.notifyPlayer(target.getPlayer(), Config.prefix + Lang.bodyhealth_disable_notification.replace("{Player}", sender.getName()));
            }

            if (target.isOnline()) BodyHealthUtils.applyBodyHealthHudVisibility(target.getPlayer());
            return true;
        } finally {
            BodyHealthUtils.unloadIfOffline(target);
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {

        boolean perm = sender.hasPermission("bodyhealth.disable.others");

        if (args.length == 2) {
            if (args[1].isEmpty()) {
                return !perm ? List.of(sender.getName()) : BodyHealthUtils.matchingPlayerNames("");
            } else {
                if (!perm) {
                    return sender.getName().toUpperCase().startsWith(args[1].toUpperCase())
                            ? List.of(sender.getName()) : List.of();
                }
                return BodyHealthUtils.matchingPlayerNames(args[1]);
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
