package bodyhealth.commands.subcommands;

import bodyhealth.commands.SubCommand;
import bodyhealth.config.Config;
import bodyhealth.config.Lang;
import bodyhealth.core.BodyHealth;
import bodyhealth.core.BodyPart;
import bodyhealth.depend.VanishPlugins;
import bodyhealth.util.BodyHealthUtils;
import bodyhealth.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GetCommand implements SubCommand {

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        // bodyhealth get [player] [body part]

        try {

            Player target = null;
            BodyPart part = null;
            int index = 1;

            if (args.length > index && Bukkit.getPlayer(args[index]) != null) {
                target = Bukkit.getPlayer(args[index]);
                index++;
            } else if (sender instanceof Player) {
                target = ((Player) sender).getPlayer();
            } else {
                MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_get_no_target);
                return true;
            }

            if (args.length > index && BodyHealthUtils.isValidBodyPart(args[index].toUpperCase())) {
                part = BodyPart.valueOf(args[index].toUpperCase());
            }

            BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(target);

            if (part == null) {

                MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_get_success_all
                        .replace("{Player}", target.getName())
                        .replace("{Health_HEAD}",  String.format("%.2f", bodyHealth.getHealth(BodyPart.HEAD)))
                        .replace("{Health_TORSO}",  String.format("%.2f", bodyHealth.getHealth(BodyPart.TORSO)))
                        .replace("{Health_ARM_LEFT}",  String.format("%.2f", bodyHealth.getHealth(BodyPart.ARM_LEFT)))
                        .replace("{Health_ARM_RIGHT}",  String.format("%.2f", bodyHealth.getHealth(BodyPart.ARM_RIGHT)))
                        .replace("{Health_LEG_LEFT}",  String.format("%.2f", bodyHealth.getHealth(BodyPart.LEG_LEFT)))
                        .replace("{Health_LEG_RIGHT}",  String.format("%.2f", bodyHealth.getHealth(BodyPart.LEG_RIGHT)))
                        .replace("{Health_FOOT_LEFT}",  String.format("%.2f", bodyHealth.getHealth(BodyPart.FOOT_LEFT)))
                        .replace("{Health_FOOT_RIGHT}",  String.format("%.2f", bodyHealth.getHealth(BodyPart.FOOT_RIGHT)))
                );
                return true;

            } else {

                MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_get_success_single
                        .replace("{Player}", target.getName())
                        .replace("{Part}", Lang.partName(part))
                        .replace("{Health}", String.format("%.2f", bodyHealth.getHealth(part)))
                );
                return true;

            }

        } catch (ArrayIndexOutOfBoundsException e) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_get_usage);
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
        return "bodyhealth.get";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

}
