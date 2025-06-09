package bodyhealth.commands.subcommands;

import bodyhealth.commands.SubCommand;
import bodyhealth.config.Config;
import bodyhealth.config.Lang;
import bodyhealth.util.BodyHealthUtils;
import bodyhealth.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.List;

public class ReloadCommand implements SubCommand {

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (BodyHealthUtils.reloadSystem()) MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_reload_success);
        else MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_reload_fail);
        if (Config.display_betterhud_auto_reload) {
            if (Bukkit.getPluginManager().getPlugin("BetterHud") == null) return true;
            if (!Bukkit.getPluginManager().getPlugin("BetterHud").isEnabled()) return true;
            ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
            Bukkit.dispatchCommand(console, "betterhud reload");
            // ^ BetterHud reload via API is unfortunately not reliable
        }
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
