package bodyhealth.util;

import bodyhealth.Main;
import bodyhealth.core.BodyHealth;
import bodyhealth.core.BodyPart;
import io.github.milkdrinkers.colorparser.bukkit.ColorParser;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class MessageUtils {

    /**
     * Notifies the given player via chat, title, or actionbar
     * @param player The player that should be notified
     * @param message The message to send to the player
     */
    public static void notifyPlayer(Player player, String message) {

        Audience audience = Main.getAdventure().player(player);

        if (message.trim().toUpperCase().startsWith("ACTIONBAR:")) {
            String actionbarText = message.trim().substring(10).trim();
            Component actionbarMessage = ColorParser.of(actionbarText)
                .papi(player).legacy().build();
            audience.sendActionBar(actionbarMessage);
        }

        else if (message.trim().toUpperCase().startsWith("TITLE:")) {
            String[] parts = message.trim().substring(6).split(";", 2);
            Component title = (parts.length > 0) ? ColorParser.of(parts[0].trim()).
                    papi(player).legacy().build() : Component.empty();
            Component subtitle = (parts.length > 1) ? ColorParser.of(parts[1].trim()).
                    papi(player).legacy().build() : Component.empty();
            audience.showTitle(net.kyori.adventure.title.Title.title(title, subtitle));
        }

        else {
            Component chatMessage = ColorParser.of(message)
                .papi(player).legacy().build();
            audience.sendMessage(chatMessage);
        }
    }

    /**
     * Notifies the console, effectively logging something there
     * Applies MiniMessage and legacy text formatting, but PlaceholderAPI
     * placeholders and title/actionbar prefixes are not supported here
     * @param message The message to send to the console
     */
    public static void notifyConsole(String message) {
        Audience audience = Main.getAdventure().console();
        Component chatMessage = ColorParser.of(message).legacy().build();
        audience.sendMessage(chatMessage);
    }

    /**
     * Notifies the given CommandSender via chat, title, or actionbar
     * Messages to the console always use 'chat' ignoring prefixes
     * @param sender A CommandSender (player or console)
     * @param message The message to send to the player
     */
    public static void notifySender(CommandSender sender, String message) {
        if (sender instanceof Player) {
            notifyPlayer((Player) sender, message);
        } else {
            notifyConsole(message);
        }
    }

    /**
     * Sends optional messages that come with specific effects
     * @param player The player to send the message to
     * @param effect The effect to send the message for
     */
    public static void sendEffectMessages(Player player, String effect) {
        BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(player);
        if (bodyHealth.getOngoingEffects().isEmpty()) return;
        for (Map.Entry<BodyPart, List<String[]>> entry : bodyHealth.getOngoingEffects().entrySet()) {
            List<String[]> effectsList = entry.getValue();
            for (String[] effectParts : effectsList) {
                if (effectParts.length > 0 && effectParts[0].trim().equalsIgnoreCase(effect)) {
                    if (effect.equalsIgnoreCase("PREVENT_INTERACT") && effectParts.length > 2) notifyPlayer(player, effectParts[2]); // PREVENT_INTERACT / <HAND> / [MESSAGE]
                    else if (effectParts.length > 1) notifyPlayer(player, effectParts[1]); // PREVENT_SPRINT / [MESSAGE]    (I know this is crap code, but it works, so don't touch it)
                }
            }
        }
    }

}
