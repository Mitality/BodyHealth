package bodyhealth.commands.subcommands;

import bodyhealth.commands.SubCommand;
import bodyhealth.config.Config;
import bodyhealth.config.Lang;
import bodyhealth.core.BodyHealth;
import bodyhealth.core.BodyPart;
import bodyhealth.util.BodyHealthUtils;
import bodyhealth.util.MessageUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddCommand implements SubCommand {

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        // bodyhealth add [player] [body part] <value>

        OfflinePlayer target = null;
        try {

            boolean percent = false;
            BodyPart part = null;
            double value;
            int index = 1;

            if (args.length > index) {
                OfflinePlayer resolved = BodyHealthUtils.resolveTarget(args[index]);
                if (resolved != null) {
                    target = resolved;
                    index++;
                }
            }
            if (target == null) {
                if (sender instanceof Player) {
                    target = (Player) sender;
                } else {
                    MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_add_no_target);
                    return true;
                }
            }

            if (args.length > index && BodyHealthUtils.isValidBodyPart(args[index].toUpperCase())) {
                part = BodyPart.valueOf(args[index].toUpperCase());
                index++;
            }

            if (args[index].endsWith("%")) {
                args[index] = args[index].substring(0, args[index].length() - 1);
                percent = true;
            }

            try {
                value = Double.parseDouble(args[index]);
            } catch (NumberFormatException e) {
                MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_add_invalid_value
                        .replace("{Value}", args[index])
                );
                return true;
            }

            BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(target);

            if (part == null) {

                for (BodyPart p : BodyPart.values()) {
                    double addValue = percent ? value : value / BodyHealthUtils.getMaxHealth(p, target) * 100;
                    bodyHealth.setHealth(p, bodyHealth.getHealth(p) + addValue, Config.force_keep_relative, null);
                }

                MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_add_success_all
                        .replace("{Player}", Objects.requireNonNull(target.getName()))
                        .replace("{Value}", args[index] + (percent ? "%" : " HP"))
                );

            } else {

                double addValue = percent ? value : value / BodyHealthUtils.getMaxHealth(part, target) * 100;
                bodyHealth.setHealth(part, bodyHealth.getHealth(part) + addValue, Config.force_keep_relative, null);

                MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_add_success_single
                        .replace("{Player}", Objects.requireNonNull(target.getName()))
                        .replace("{Part}", part.name().toUpperCase())
                        .replace("{Value}", args[index] + (percent ? "%" : " HP"))
                );

            }
            return true;

        } catch (ArrayIndexOutOfBoundsException e) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_add_usage);
            return true;
        } finally {
            if (target != null) BodyHealthUtils.unloadIfOffline(target);
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
                if (partialInput.matches("\\d+")) result.add(partialInput + "%");
                result.addAll(BodyHealthUtils.matchingPlayerNames(args[1]));
                for (BodyPart part : BodyPart.values()) {
                    if (part.name().startsWith(partialInput)) result.add(part.name());
                }
                return result;
            }
        }

        if (args.length == 3) {

            if (args[1].matches("\\d+%?")) return List.of();
            if (BodyHealthUtils.resolveTarget(args[1]) == null && args[2].isEmpty()) return List.of("value");

            if (args[2].isEmpty()) {
                return List.of("body part / value");
            } else {
                String partialInput = args[2].toUpperCase();
                List<String> result = new ArrayList<>();
                if (partialInput.matches("\\d+")) result.add(partialInput + "%");
                for (BodyPart part : BodyPart.values()) {
                    if (part.name().startsWith(partialInput)) result.add(part.name());
                }
                return result;
            }
        }

        if (args.length == 4) {
            if (args[1].matches("\\d+%?") || args[2].matches("\\d+%?")) return List.of();
            if (args[3].isEmpty()) return List.of("value");
            if (args[3].matches("\\d+")) return List.of(args[3] + "%");
        }
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
