package bodyhealth.effects.effect;

import bodyhealth.config.Debug;
import bodyhealth.core.BodyPart;
import bodyhealth.effects.BodyHealthEffect;
import org.bukkit.entity.Player;

public class KILL_PLAYER implements BodyHealthEffect {

    @Override
    public String getIdentifier() {
        return "KILL_PLAYER";
    }

    @Override
    public String getUsage() {
        return "KILL_PLAYER";
    }

    @Override
    public void onApply(Player player, BodyPart part, String[] args) {

        if (!player.isDead()) player.damage(Double.MAX_VALUE, player); // player.setHealth(0.0) could cause issues
        Debug.log("(" + part.name() +") Killed player " + player.getName());

    }

    @Override
    public void onRemove(Player player, BodyPart part, String[] args) {
    }

}
