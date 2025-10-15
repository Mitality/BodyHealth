package bodyhealth.effects.effect;

import bodyhealth.Main;
import bodyhealth.config.Config;
import bodyhealth.config.Debug;
import bodyhealth.core.BodyPart;
import bodyhealth.effects.BodyHealthEffect;
import bodyhealth.effects.EffectType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class KILL_PLAYER implements BodyHealthEffect {

    @Override
    public EffectType getEffectType() {
        return EffectType.ONE_TIME;
    }

    @Override
    public String getIdentifier() {
        return "KILL_PLAYER";
    }

    @Override
    public String getUsage() {
        return "KILL_PLAYER";
    }

    @Override
    public void onApply(Player player, BodyPart part, String[] args, boolean isRecovery) {

        if (Config.kill_with_command) {
            if (!Main.getScheduler().isGlobalThread()) {
                Main.getScheduler().execute(() ->
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "kill " + player.getName()));
            } else {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "kill " + player.getName());
            }
        } else {
            if (player.getHealth() > 0) player.setHealth(0);
        }
        Debug.log("(" + part.name() +") Killed player " + player.getName());

    }

    @Override
    public void onRemove(Player player, BodyPart part, String[] args, boolean isRecovery) {
    }

}
