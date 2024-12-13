package bodyhealth.commands.subcommands;

import bodyhealth.commands.SubCommand;
import bodyhealth.config.Config;
import bodyhealth.config.Lang;
import bodyhealth.core.BodyHealth;
import bodyhealth.core.BodyPart;
import bodyhealth.util.BodyHealthUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HealCommand implements SubCommand {

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (!sender.hasPermission("bodyhealth.heal")) {
            sender.sendMessage(Config.prefix + Lang.bodyhealth_heal_not_permitted);
            return true;
        }

        Player targetPlayer = args.length > 1 ? Bukkit.getPlayer(args[1]) : sender instanceof Player ? (Player) sender : null;
        if (targetPlayer == null) {
            sender.sendMessage(Config.prefix + (args.length > 1 ? Lang.bodyhealth_player_not_found.replace("{Player}", args[1]) : Lang.bodyhealth_usage));
            return true;
        }

        BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(targetPlayer);
        for (BodyPart part : BodyPart.values()) {
            bodyHealth.setHealth(part, 100, false);
        }

        sender.sendMessage(Config.prefix + Lang.bodyhealth_heal_success.replace("{Player}", targetPlayer.getName()));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {

        if (args.length == 2) {
            List<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            return playerNames;
        }

        return List.of();
    }

    @Override
    public String permission() {
        return "bodyhealth.heal";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

}
