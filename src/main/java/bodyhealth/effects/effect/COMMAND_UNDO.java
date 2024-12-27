package bodyhealth.effects.effect;

import bodyhealth.config.Debug;
import bodyhealth.core.BodyPart;
import bodyhealth.effects.BodyHealthEffect;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class COMMAND_UNDO implements BodyHealthEffect {

    @Override
    public String getIdentifier() {
        return "COMMAND_UNDO";
    }

    @Override
    public String getUsage() {
        return "COMMAND_UNDO / cmd args[...]";
    }

    @Override
    public void onApply(Player player, BodyPart part, String[] args) {
    }

    @Override
    public void onRemove(Player player, BodyPart part, String[] args) {

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), args[1].trim()
                .replaceAll("%PlayerName%", player.getName())
                .replaceAll("%PlayerUUID%", player.getUniqueId().toString()));
        Debug.log("(" + part.name() +") Dispatched command: /" + args[1].trim());

    }

}
