package bodyhealth.effects.effect;

import bodyhealth.config.Debug;
import bodyhealth.core.BodyPart;
import bodyhealth.effects.BodyHealthEffect;
import bodyhealth.effects.EffectType;
import org.bukkit.entity.Player;

public class SOUND implements BodyHealthEffect {

    @Override
    public EffectType getEffectType() {
        return EffectType.ONE_TIME;
    }

    @Override
    public String getIdentifier() {
        return "SOUND";
    }

    @Override
    public String getUsage() {
        return "SOUND / <ID> / [VOLUME] / [PITCH]";
    }

    @Override
    public void onApply(Player player, BodyPart part, String[] args, boolean isRecovery) {

        if (args.length <= 1) {
            Debug.logErr("Effect \"" + args[0].trim() + "\" is missing arguments, check syntax!");
            return;
        }
        String sound = args[1].trim().toLowerCase();
        float volume = args.length >= 3 ? parseOrDefault(args[2]) : 1.0f;
        float pitch  = args.length >= 4 ? parseOrDefault(args[3]) : 1.0f;
        player.playSound(player, sound, volume, pitch);

    }

    @Override
    public void onRemove(Player player, BodyPart part, String[] args, boolean isRecovery) {
    }

    private float parseOrDefault(String input) {
        try {
            return Float.parseFloat(input.trim());
        } catch (Exception e) {
            return (float) 1.0;
        }
    }

}
