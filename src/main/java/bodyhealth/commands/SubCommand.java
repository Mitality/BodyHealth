package bodyhealth.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface SubCommand {

    /**
     * Executes the subcommands code
     * @param sender The CommandSender that executed the command
     * @param args The command arguments
     */
    boolean execute(CommandSender sender, String[] args);

    /**
     * Returns a list of tab completions for the subcommand
     * @param sender The CommandSender that executed the command
     * @param args The command arguments
     * @return A list of tab completions for the subcommand
     */
    List<String> tabComplete(CommandSender sender, String[] args);

    /**
     * @return the permission node required to use the command
     */
    String permission();

    /**
     * @return A boolean representing if the command can only be executed by a player
     */
    boolean playerOnly();

}
