package bodyhealth.depend;

import cz.hashiri.harshlands.api.HarshlandsAPI;
import cz.hashiri.harshlands.api.hud.Hud;
import cz.hashiri.harshlands.api.player.HudPlayer;
import org.bukkit.entity.Player;

/**
 * Thin bridge to the Harshlands HUD API. The Harshlands equivalent of
 * {@link bodyhealth.depend.BetterHud#setBodyHealthHudEnabled(Player, boolean)} -
 * Harshlands ships its own glyphs and render task, so unlike {@link BetterHud}
 * there is no config/asset injection to do here.
 */
public class Harshlands {

    /**
     * Enables or disables the BodyHealth HUD for a given player via the Harshlands API.
     * @param player  the player for whom the HUD should be enabled or disabled
     * @param enabled whether the HUD should be enabled or disabled
     * @return whether the operation changed the player's shown-state
     */
    public static boolean setBodyHealthHudEnabled(Player player, boolean enabled) {
        // Harshlands' BodyHealth module may be disabled in Harshlands' own config, in which case
        // HarshlandsAPI.inst() is null even though the Harshlands plugin itself is enabled.
        if (HarshlandsAPI.inst() == null) return false;

        HudPlayer hudPlayer = HarshlandsAPI.inst().getPlayerManager().getHudPlayer(player.getUniqueId());
        if (hudPlayer == null) return false;

        Hud hud = HarshlandsAPI.inst().getHudManager().getHud("bodyhealth");
        if (hud == null) return false;

        return enabled ? hud.add(hudPlayer) : hud.remove(hudPlayer);
    }
}
