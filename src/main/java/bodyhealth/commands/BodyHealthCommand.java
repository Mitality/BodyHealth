package bodyhealth.commands;

import bodyhealth.config.Lang;
import bodyhealth.core.BodyHealth;
import bodyhealth.core.BodyPart;
import bodyhealth.util.BodyHealthUtils;
import bodyhealth.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BodyHealthCommand implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (args.length < 1) {
            sender.sendMessage(Config.prefix + Lang.bodyhealth_usage);
            return true;
        }

        String action = args[0];

        if (action.equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("bodyhealth.reload")) {
                sender.sendMessage(Config.prefix + Lang.bodyhealth_reload_not_permitted);
                return true;
            }
            if (BodyHealthUtils.reloadSystem()) sender.sendMessage(Config.prefix + Lang.bodyhealth_reload_success);
            else sender.sendMessage(Config.prefix + Lang.bodyhealth_reload_fail);
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Config.prefix + Lang.bodyhealth_usage);
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(args[1]);
        if (targetPlayer == null) {
            sender.sendMessage(Config.prefix + Lang.bodyhealth_player_not_found.replace("{Player}", args[1]));
            return true;
        }

        BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(targetPlayer);

        if (action.equalsIgnoreCase("heal")) {
            if (!sender.hasPermission("bodyhealth.heal")) {
                sender.sendMessage(Config.prefix + Lang.bodyhealth_heal_not_permitted);
                return true;
            }
            for (BodyPart part : BodyPart.values()) {
                bodyHealth.setHealth(part, 100);
            }
            sender.sendMessage(Config.prefix + Lang.bodyhealth_heal_success.replace("{Player}", targetPlayer.getName()));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(Config.prefix + Lang.bodyhealth_usage);
            return true;
        }

        BodyPart part;
        try {
            part = BodyPart.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Config.prefix + Lang.bodyhealth_invalid_part);
            return true;
        }

        if (action.equalsIgnoreCase("get")) {
            if (!sender.hasPermission("bodyhealth.get")) {
                sender.sendMessage(Config.prefix + Lang.bodyhealth_get_not_permitted);
                return true;
            }
            double partHealth = bodyHealth.getHealth(part);
            sender.sendMessage(Config.prefix + Lang.bodyhealth_get_success
                    .replace("{Player}", targetPlayer.getName())
                    .replace("{Part}", part.name())
                    .replace("{Health}", String.format("%.2f", partHealth))
            );
        }

        else if (action.equalsIgnoreCase("set")) {
            if (!sender.hasPermission("bodyhealth.set")) {
                sender.sendMessage(Config.prefix + Lang.bodyhealth_set_not_permitted);
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
        }

        else {
            sender.sendMessage(Config.prefix + Lang.bodyhealth_invalid);
        }

        return true;
    }
}
