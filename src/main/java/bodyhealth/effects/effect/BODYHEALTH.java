package bodyhealth.effects.effect;

import bodyhealth.config.Debug;
import bodyhealth.core.BodyHealth;
import bodyhealth.core.BodyPart;
import bodyhealth.effects.BodyHealthEffect;
import bodyhealth.effects.EffectType;
import bodyhealth.util.BodyHealthUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BODYHEALTH implements BodyHealthEffect {

    @Override
    public EffectType getEffectType() {
        return EffectType.ONE_TIME;
    }

    @Override
    public String getIdentifier() {
        return "BODYHEALTH";
    }

    @Override
    public String getUsage() {
        return "BODYHEALTH / <ADD/SET> / [PLAYER/ALL] / [PART] / <VALUE>";
    }

    @Override
    public void onApply(Player player, BodyPart part, String[] args, boolean isRecovery) {

        int index = 1;
        if (args.length <= index) {
            Debug.logErr("Effect \"" + args[0].trim() + "\" is missing arguments, check syntax!");
            return;
        }
        String operation = args[index].trim().toUpperCase();
        if (!operation.equals("ADD") && !operation.equals("SET")) {
            Debug.logErr("Effect \"" + args[0].trim() + "\" has an invalid operation specified: \"" + operation + "\", expected ADD or SET!");
            return;
        }
        index++;

        List<Player> targets = new ArrayList<>();
        if (args.length > index + 1) {
            String playerArg = args[index].trim();
            if (playerArg.equalsIgnoreCase("ALL")) {
                targets.addAll(Bukkit.getOnlinePlayers());
                index++;
            } else {
                Player found = Bukkit.getPlayer(playerArg);
                if (found != null) {
                    targets.add(found);
                    index++;
                }
            }
        }
        if (targets.isEmpty()) targets.add(player);

        BodyPart targetPart = null;
        if (args.length > index + 1) {
            String partArg = args[index].trim().toUpperCase();
            if (BodyHealthUtils.isValidBodyPart(partArg)) {
                targetPart = BodyPart.valueOf(partArg);
                index++;
            }
        }
        List<BodyPart> parts = targetPart != null ? List.of(targetPart) : List.of(BodyPart.values());

        if (args.length <= index) {
            Debug.logErr("Effect \"" + args[0].trim() + "\" is missing arguments, check syntax!");
            return;
        }
        String valueStr = args[index].trim();
        boolean percent = false;
        if (valueStr.endsWith("%")) {
            valueStr = valueStr.substring(0, valueStr.length() - 1);
            percent = true;
        }

        double value;
        try {
            value = Double.parseDouble(valueStr);
        } catch (NumberFormatException e) {
            Debug.logErr("Effect \"" + args[0].trim() + "\" has an invalid value: \"" + args[1].trim() + "\"!");
            return;
        }

        for (Player target : targets) {
            BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(target);
            for (BodyPart p : parts) {
                double resolved = percent ? value : value / BodyHealthUtils.getMaxHealth(p, target) * 100;
                if (operation.equals("SET")) {
                    bodyHealth.setHealth(p, resolved, false, null);
                } else {
                    bodyHealth.setHealth(p, bodyHealth.getHealth(p) + resolved, false, null);
                }
            }
        }

    }

    @Override
    public void onRemove(Player player, BodyPart part, String[] args, boolean isRecovery) {
    }

}
