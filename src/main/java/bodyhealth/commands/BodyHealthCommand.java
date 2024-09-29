package bodyhealth.commands;

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
            sender.sendMessage(Config.prefix + "§bUsage: /bodyhealth <get/set/heal/reload> <player> <bodypart> [value]");
            return true;
        }

        String action = args[0];

        if (action.equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("bodyhealth.reload")) {
                sender.sendMessage(Config.prefix + "§cYou do not have permission to use this command");
                return true;
            }
            if (BodyHealthUtils.reloadSystem()) sender.sendMessage(Config.prefix + "§aConfiguration reloaded.");
            else sender.sendMessage(Config.prefix + "§cConfiguration could not be reloaded.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Config.prefix + "§bUsage: /bodyhealth <get/set/heal/reload> <player> <bodypart> [value]");
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(args[1]);
        if (targetPlayer == null) {
            sender.sendMessage(Config.prefix + "§cPlayer not found.");
            return true;
        }

        BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(targetPlayer);

        if (action.equalsIgnoreCase("heal")) {
            if (!sender.hasPermission("bodyhealth.heal")) {
                sender.sendMessage(Config.prefix + "§cYou do not have permission to use this command");
                return true;
            }
            for (BodyPart part : BodyPart.values()) {
                bodyHealth.setHealth(part, 100);
            }
            sender.sendMessage(Config.prefix + "§a" + targetPlayer.getName() + " has been fully healed.");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(Config.prefix + "§bUsage: /bodyhealth <get/set> <player> <bodypart> [value]");
            return true;
        }

        BodyPart part;
        try {
            part = BodyPart.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Config.prefix + "§cInvalid body part. Valid parts: HEAD, BODY, ARM_LEFT, ARM_RIGHT, LEG_LEFT, LEG_RIGHT, FOOT_LEFT, FOOT_RIGHT.");
            return true;
        }

        if (action.equalsIgnoreCase("get")) {
            if (!sender.hasPermission("bodyhealth.get")) {
                sender.sendMessage(Config.prefix + "§cYou do not have permission to use this command");
                return true;
            }
            double partHealth = bodyHealth.getHealth(part);
            sender.sendMessage(Config.prefix + "§a" + targetPlayer.getName() + "'s " + part.name() + " health: " + partHealth + "%");
        }

        else if (action.equalsIgnoreCase("set")) {
            if (!sender.hasPermission("bodyhealth.set")) {
                sender.sendMessage(Config.prefix + "§cYou do not have permission to use this command");
                return true;
            }
            if (args.length < 4) {
                sender.sendMessage(Config.prefix + "§ePlease specify a value.");
                return true;
            }

            try {
                double newHealth = Math.min(100, Math.max(0, Double.parseDouble(args[3]))); // Keep health between 0 and 100
                bodyHealth.setHealth(part, newHealth);
                sender.sendMessage(Config.prefix + "§a" + targetPlayer.getName() + "'s " + part.name() + " health set to " + newHealth + "%");
            } catch (NumberFormatException e) {
                sender.sendMessage(Config.prefix + "§cInvalid health value.");
            }
        }

        else {
            sender.sendMessage(Config.prefix + "§cInvalid action. Use 'get', 'set', 'heal', or 'reload'.");
        }

        return true;
    }
}
