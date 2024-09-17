package bodyhealth.util;

import bodyhealth.core.BodyHealth;
import bodyhealth.core.BodyPart;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class MessageUtils {
    public static void notifyPlayer(Player player, String message) {

        if (message.trim().toUpperCase().startsWith("ACTIONBAR:")) {
            String actionBarMessage = ChatColor.translateAlternateColorCodes('&', message.trim().substring(10).trim());
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionBarMessage));
        }

        else if (message.trim().toUpperCase().startsWith("TITLE:")) {
            String[] parts = message.trim().substring(6).split(";", 2);
            String title = (parts.length > 0) ? ChatColor.translateAlternateColorCodes('&', parts[0].trim()) : "";
            String subtitle = (parts.length > 1) ? ChatColor.translateAlternateColorCodes('&', parts[1].trim()) : "";
            player.sendTitle(title, subtitle, 10, 70, 20);
        }

        else {
            String chatMessage = ChatColor.translateAlternateColorCodes('&', message);
            player.sendMessage(chatMessage);
        }
    }

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
