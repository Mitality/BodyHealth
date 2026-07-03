package bodyhealth.commands.subcommands;

import bodyhealth.commands.SubCommand;
import bodyhealth.config.Config;
import bodyhealth.config.Lang;
import bodyhealth.depend.VanishPlugins;
import bodyhealth.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DisableCommand implements SubCommand {

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (args.length <= 1 && !(sender instanceof Player)) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_disable_no_target);
            return true;
        }

        Player target;
        if (args.length > 1) {
            if (Bukkit.getPlayer(args[1]) == null) {
                MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_disable_invalid_target.replace("{Player}", args[1]));
                return true;
            } else {
                target = Bukkit.getPlayer(args[1]);
            }
        } else {
            target = (Player) sender;
        }



        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {

        if (args.length == 2) {
            if (args[1].isEmpty()) {
                return Bukkit.getOnlinePlayers().stream()
                        .filter(player -> !VanishPlugins.isVanished(player))
                        .map(Player::getName)
                        .toList();
            } else {
                String partialInput = args[1].toUpperCase();
                List<String> result = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
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
