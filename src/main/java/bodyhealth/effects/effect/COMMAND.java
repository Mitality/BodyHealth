package bodyhealth.effects.effect;

import bodyhealth.Main;
import bodyhealth.config.Debug;
import bodyhealth.core.BodyPart;
import bodyhealth.effects.BodyHealthEffect;
import bodyhealth.effects.EffectType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class COMMAND implements BodyHealthEffect {

    @Override
    public EffectType getEffectType() {
        return EffectType.ONE_TIME;
    }

    @Override
    public String getIdentifier() {
        return "COMMAND";
    }

    @Override
    public String getUsage() {
        return "COMMAND / cmd args[...]";
    }

    @Override
    public void onApply(Player player, BodyPart part, String[] args, boolean isRecovery) {

        String command = args[1].trim()
            .replace("%PlayerName%", player.getName())
            .replace("%PlayerUUID%", player.getUniqueId().toString())
            .replace("%BodyPart%", part.name());
        if (!Main.getScheduler().isGlobalThread()) {
            Main.getScheduler().execute(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
        Debug.log("(" + part.name() +") Dispatched command: /" + args[1].trim());

    }

    @Override
    public void onRemove(Player player, BodyPart part, String[] args, boolean isRecovery) {
    }

}
