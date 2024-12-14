package bodyhealth.depend;

import de.myzelyam.api.vanish.VanishAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class VanishPlugins {

    public static boolean isVanished(Player player) {
        if (Bukkit.getPluginManager().isPluginEnabled("SuperVanish") || Bukkit.getPluginManager().isPluginEnabled("PremiumVanish")) {
            return VanishAPI.isInvisible(player);
        }
        return false;
    }
}
