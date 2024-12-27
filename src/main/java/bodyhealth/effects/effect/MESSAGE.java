package bodyhealth.effects.effect;

import bodyhealth.config.Debug;
import bodyhealth.core.BodyPart;
import bodyhealth.effects.BodyHealthEffect;
import bodyhealth.util.MessageUtils;
import org.bukkit.entity.Player;

public class MESSAGE implements BodyHealthEffect {

    @Override
    public String getIdentifier() {
        return "MESSAGE";
    }

    @Override
    public String getUsage() {
        return "MESSAGE / <MESSAGE>";
    }

    @Override
    public void onApply(Player player, BodyPart part, String[] args) {

        if (args.length <= 1) {
            Debug.logErr("Effect \"" + args[0].trim() + "\" is missing arguments, check syntax!");
            return;
        }

        MessageUtils.notifyPlayer(player, args[1]);

    }

    @Override
    public void onRemove(Player player, BodyPart part, String[] args) {
    }

}
