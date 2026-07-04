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

public class HealCommand implements SubCommand {

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        // bodyhealth heal [player] [body part]

        OfflinePlayer target = null;
        try {

            BodyPart part = null;
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
                    MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_heal_no_target);
                    return true;
                }
            }

            if (args.length > index && BodyHealthUtils.isValidBodyPart(args[index].toUpperCase())) {
                part = BodyPart.valueOf(args[index].toUpperCase());
            }

            BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(target);

            if (part == null) {

                bodyHealth.setHealth(100, false, null);

                MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_heal_success_all
                        .replace("{Player}", Objects.requireNonNull(target.getName()))
                );

            } else {

                bodyHealth.setHealth(part, 100, false, null);

                MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_heal_success_single
                        .replace("{Player}", Objects.requireNonNull(target.getName()))
                        .replace("{Part}", part.name().toUpperCase())
                );

            }
            return true;

        } catch (ArrayIndexOutOfBoundsException e) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_heal_usage);
            return true;
        } finally {
            if (target != null) BodyHealthUtils.unloadIfOffline(target);
        }

    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {

        if (args.length == 2) {
            if (args[1].isEmpty()) {
                return List.of("player / body part");
            } else {
                String partialInput = args[1].toUpperCase();
                List<String> result = new ArrayList<>(BodyHealthUtils.matchingPlayerNames(args[1]));
                for (BodyPart part : BodyPart.values()) {
                    if (part.name().startsWith(partialInput)) result.add(part.name());
                }
                return result;
            }
        }

        if (args.length == 3) {

            if (BodyHealthUtils.resolveTarget(args[1]) == null) return List.of();

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
