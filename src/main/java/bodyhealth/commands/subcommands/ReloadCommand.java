package bodyhealth.commands.subcommands;

import bodyhealth.commands.SubCommand;
import bodyhealth.config.Config;
import bodyhealth.config.Lang;
import bodyhealth.util.BodyHealthUtils;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ReloadCommand implements SubCommand {

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (BodyHealthUtils.reloadSystem()) sender.sendMessage(Config.prefix + Lang.bodyhealth_reload_success);
        else sender.sendMessage(Config.prefix + Lang.bodyhealth_reload_fail);
        return true;

    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }

    @Override
    public String permission() {
        return "bodyhealth.reload";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

}
