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

public class SetCommand implements SubCommand {
    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (!sender.hasPermission("bodyhealth.set")) {
            sender.sendMessage(Config.prefix + Lang.bodyhealth_set_not_permitted);
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(Config.prefix + Lang.bodyhealth_usage);
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(args[1]);
        if (targetPlayer == null) {
            sender.sendMessage(Config.prefix + Lang.bodyhealth_player_not_found.replace("{Player}", args[1]));
            return true;
        }

        BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(targetPlayer);

        BodyPart part;
        try {
            part = BodyPart.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Config.prefix + Lang.bodyhealth_invalid_part);
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage(Config.prefix + Lang.bodyhealth_set_missing_value);
            return true;
        }

        try {
            double newHealth = Math.min(100, Math.max(0, Double.parseDouble(args[3]))); // Keep health between 0 and 100
            bodyHealth.setHealth(part, newHealth);
            sender.sendMessage(Config.prefix + Lang.bodyhealth_set_success
                    .replace("{Player}", targetPlayer.getName())
                    .replace("{Part}", part.name())
                    .replace("{Health}", String.format("%.2f", newHealth))
            );
        } catch (NumberFormatException e) {
            sender.sendMessage(Config.prefix + Lang.bodyhealth_set_invalid_value);
        }

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

        if (args.length == 3) {
            String partialInput = args[2].toUpperCase();
            List<String> bodyParts = new ArrayList<>();
            for (BodyPart part : BodyPart.values()) {
                if (part.name().startsWith(partialInput)) {
                    bodyParts.add(part.name());
                }
            }
            return bodyParts;
        }

        return List.of();
    }

    @Override
    public String permission() {
        return "bodyhealth.set";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

}
