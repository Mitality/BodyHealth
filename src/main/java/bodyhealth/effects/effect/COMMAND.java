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

        Bukkit.getScheduler().runTask(Main.getInstance(), () -> { // Dispatch command synchronously
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), args[1].trim()
                .replace("%PlayerName%", player.getName())
                .replace("%PlayerUUID%", player.getUniqueId().toString())
                .replace("%BodyPart%", part.name())
            );
            Debug.log("(" + part.name() +") Dispatched command: /" + args[1].trim());
        });

    }

    @Override
    public void onRemove(Player player, BodyPart part, String[] args, boolean isRecovery) {
    }

}
