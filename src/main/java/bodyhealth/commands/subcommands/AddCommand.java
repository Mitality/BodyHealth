package bodyhealth.commands.subcommands;

import bodyhealth.commands.SubCommand;
import bodyhealth.config.Config;
import bodyhealth.config.Lang;
import bodyhealth.core.BodyHealth;
import bodyhealth.core.BodyPart;
import bodyhealth.depend.VanishPlugins;
import bodyhealth.util.BodyHealthUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AddCommand implements SubCommand {

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        // bodyhealth add [player] [body part] <value>

        try {

            boolean percent = false;
            Player target = null;
            BodyPart part = null;
            double value = 0;
            int index = 1;

            if (args.length > index && Bukkit.getPlayer(args[index]) != null) {
                target = Bukkit.getPlayer(args[index]);
                index++;
            } else if (sender instanceof Player) {
                target = ((Player) sender).getPlayer();
            } else {
                sender.sendMessage(Config.prefix + Lang.bodyhealth_add_no_target);
                return true;
            }

            if (args.length > index && BodyHealthUtils.isValidBodyPart(args[index])) {
                part = BodyPart.valueOf(args[index]);
                index++;
            }

            if (args[index].endsWith("%")) {
                args[index] = args[index].substring(0, args[index].length() - 1);
                percent = true;
            }

            try {
                value = Double.parseDouble(args[index]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Config.prefix + Lang.bodyhealth_add_invalid_value
                        .replace("{Value}", args[index])
                );
                return true;
            }

            BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(target);

            if (part == null) {

                for (BodyPart p : BodyPart.values()) {
                    double addValue = percent ? value : value / BodyHealthUtils.getMaxHealth(p, target) * 100;
                    bodyHealth.setHealth(p, bodyHealth.getHealth(p) + addValue, Config.force_keep_relative);
                }

                sender.sendMessage(Config.prefix + Lang.bodyhealth_add_success_all
                        .replace("{Player}", target.getName())
                        .replace("{Value}", args[index] + (percent ? "%" : " HP"))
                );
                return true;

            } else {

                double addValue = percent ? value : value / BodyHealthUtils.getMaxHealth(part, target) * 100;
                bodyHealth.setHealth(part, bodyHealth.getHealth(part) + addValue, Config.force_keep_relative);

                sender.sendMessage(Config.prefix + Lang.bodyhealth_add_success_single
                        .replace("{Player}", target.getName())
                        .replace("{Part}", Lang.partName(part))
                        .replace("{Value}", args[index] + (percent ? "%" : " HP"))
                );
                return true;

            }

        } catch (ArrayIndexOutOfBoundsException e) {
            sender.sendMessage(Config.prefix + Lang.bodyhealth_add_usage);
            return true;
        }

    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {

        if (args.length == 2) {
            if (args[1].isEmpty()) {
                return List.of("player / body part / value");
            } else {
                String partialInput = args[1].toUpperCase();
                List<String> result = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toUpperCase().startsWith(partialInput)
                            && !VanishPlugins.isVanished(player)
                    ) result.add(player.getName());
                }
                for (BodyPart part : BodyPart.values()) {
                    if (part.name().startsWith(partialInput)) result.add(part.name());
                }
                return result;
            }
        }

        if (args.length == 3) {

            if (Bukkit.getPlayer(args[1]) == null) return List.of("value");

            if (args[2].isEmpty()) {
                return List.of("body part / value");
            } else {
                String partialInput = args[2].toUpperCase();
                List<String> result = new ArrayList<>();
                for (BodyPart part : BodyPart.values()) {
                    if (part.name().startsWith(partialInput)) result.add(part.name());
                }
                return result;
            }
        }

        if (args.length == 4 && args[3].isEmpty()) return List.of("value");
        return List.of();
    }

    @Override
    public String permission() {
        return "bodyhealth.add";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

}
