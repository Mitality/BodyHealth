package bodyhealth.depend;

import bodyhealth.config.Lang;
import bodyhealth.core.BodyHealth;
import bodyhealth.core.BodyPartState;
import bodyhealth.util.BodyHealthUtils;
import bodyhealth.core.BodyPart;
import io.github.milkdrinkers.colorparser.bukkit.ColorParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlaceholderAPI extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "bodyhealth";
    }

    @Override
    public @NotNull String getAuthor() {
        return "BodyHealth";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {

        if (params.startsWith("health_")) return getHealthPlaceholder(player, params);
        if (params.startsWith("state_")) return getStatePlaceholder(player, params);
        if (params.equals("enabled")) return getEnabledplaceholder(player);

        return null;
    }

    private static String getHealthPlaceholder(OfflinePlayer player, @NotNull String params) {

        String[] splitParams = params.split("_");
        if (splitParams.length < 2) return null;

        StringBuilder partNameBuilder = new StringBuilder();

        for (int i = 1; i < splitParams.length; i++) {
            if (splitParams[i].equalsIgnoreCase("rounded")) break;
            if (i > 1) partNameBuilder.append("_");
            partNameBuilder.append(splitParams[i].toUpperCase());
        }

        String partName = partNameBuilder.toString();
        BodyPart part;

        try {
            part = BodyPart.valueOf(partName);
        } catch (IllegalArgumentException e) {
            return "Invalid body part: " + partName;
        }

        if (!player.isOnline()) return null;
        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer != null) {
            BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(onlinePlayer);
            double partHealth = bodyHealth.getHealth(part);
            if (splitParams[splitParams.length - 1].equalsIgnoreCase("rounded"))
                return String.valueOf((int) Math.round(partHealth));
            return String.format("%.2f", partHealth);
        }

        return null;
    }

    private static String getStatePlaceholder(OfflinePlayer player, @NotNull String params) {

        String[] splitParams = params.split("_");
        if (splitParams.length < 2) return null;

        StringBuilder stateNameBuilder = new StringBuilder();

        for (int i = 1; i < splitParams.length; i++) {
            if (splitParams[i].equalsIgnoreCase("translated")) break;
            if (i > 1) stateNameBuilder.append("_");
            stateNameBuilder.append(splitParams[i].toUpperCase());
        }

        String stateName = stateNameBuilder.toString();
        BodyPart part;

        try {
            part = BodyPart.valueOf(stateName);
        } catch (IllegalArgumentException e) {
            return "Invalid body part: " + stateName;
        }

        if (!player.isOnline()) return null;
        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer != null) {
            BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(onlinePlayer);
            BodyPartState partState = BodyHealthUtils.getBodyHealthState(bodyHealth, part);
            if (splitParams[splitParams.length - 1].equalsIgnoreCase("translated"))
                return LegacyComponentSerializer.legacySection()
                        .serialize(ColorParser.of(Lang.stateName(partState))
                                .papi(player).legacy().build());
            return partState.name();
        }

        return null;
    }

    private static String getEnabledplaceholder(OfflinePlayer player) {

        if (!player.isOnline()) return null;
        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer == null) return null;

        return String.valueOf(BodyHealthUtils
                .isSystemEnabled(onlinePlayer));
    }

}
