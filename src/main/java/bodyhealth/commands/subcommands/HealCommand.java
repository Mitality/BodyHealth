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

public class HealCommand implements SubCommand {

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        // bodyhealth heal [player] [body part]

        try {

            Player target = null;
            BodyPart part = null;
            int index = 1;

            if (Bukkit.getPlayer(args[index]) != null) {
                target = Bukkit.getPlayer(args[index]);
                index++;
            } else if (sender instanceof Player) {
                target = ((Player) sender).getPlayer();
            } else {
                sender.sendMessage(Config.prefix + Lang.bodyhealth_heal_no_target);
                return true;
            }

            if (BodyHealthUtils.isValidBodyPart(args[index])) {
                part = BodyPart.valueOf(args[index]);
            }

            BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(target);

            if (part == null) {

                bodyHealth.setHealth(100, false);

                sender.sendMessage(Config.prefix + Lang.bodyhealth_heal_success_all
                        .replace("{Player}", target.getName())
                );
                return true;

            } else {

                bodyHealth.setHealth(part, 100, false);

                sender.sendMessage(Config.prefix + Lang.bodyhealth_heal_success_single
                        .replace("{Player}", target.getName())
                        .replace("{Part}", Lang.partName(part))
                );
                return true;

            }

        } catch (ArrayIndexOutOfBoundsException e) {
            sender.sendMessage(Config.prefix + Lang.bodyhealth_heal_usage);
            return true;
        }

    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {

        if (args.length == 2) {
            if (args[1].isEmpty()) {
                return List.of("player / body part");
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

            if (Bukkit.getPlayer(args[1]) == null) return List.of();

            if (args[2].isEmpty()) {
                return List.of("body part");
            } else {
                String partialInput = args[2].toUpperCase();
                List<String> result = new ArrayList<>();
                for (BodyPart part : BodyPart.values()) {
                    if (part.name().startsWith(partialInput)) result.add(part.name());
                }
                return result;
            }
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
