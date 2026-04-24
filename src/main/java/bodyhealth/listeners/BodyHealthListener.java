package bodyhealth.listeners;

import bodyhealth.core.BodyPart;
import bodyhealth.data.DataManager;
import bodyhealth.effects.EffectHandler;
import bodyhealth.core.BodyHealth;
import bodyhealth.calculations.BodyHealthCalculator;
import bodyhealth.util.BodyHealthUtils;
import bodyhealth.Main;
import bodyhealth.config.Config;
import bodyhealth.config.Debug;
import bodyhealth.util.FoliaUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class BodyHealthListener implements Listener {

    public BodyHealthListener() {
        if (FoliaUtils.isFolia()) FoliaUtils.startWorldChangeWatcher(this::handleWorldChange);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDamage(EntityDamageEvent event) {

        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player player)) return;

        BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(player);
        EntityDamageEvent.DamageCause cause = event.getCause();
        double damage = event.getFinalDamage();

        if (event instanceof EntityDamageByEntityEvent entityDamageEvent) {
            Entity damager = entityDamageEvent.getDamager();

            if (damager instanceof Arrow arrow) {
                BodyPart hitBodyPart = BodyHealthCalculator.calculateHitByArrow(player, arrow);
                BodyHealthUtils.applyDamageWithConfig(bodyHealth, cause, damage, hitBodyPart, false, event);
                Debug.logDev("Player " + player.getName() + " was hit by an arrow on " + hitBodyPart.name() + " with " + String.format("%.2f", damage) + " damage.");
            }

            else if (damager.getType() == EntityType.CREEPER || damager.getType() == EntityType.TNT || damager.getType() == EntityType.TNT_MINECART || damager.getType() == EntityType.LIGHTNING_BOLT) {
                Debug.logDev("Player " + player.getName() + " was hit by " + damager.getType().name() + " with " + String.format("%.2f", damage) + " damage.");
                BodyHealthUtils.applyDamageWithConfig(bodyHealth, cause, damage, false, event);
            }

            else if (damager instanceof Player && damager == player) {
                if (Config.self_harm) BodyHealthUtils.applyDamageWithConfig(bodyHealth, cause, damage, false, event);
                if (Config.self_harm) Debug.logDev("Player " + player.getName() + " was hit by itself with " + String.format("%.2f", damage) + " damage.");
            }

            else {
                BodyPart hitBodyPart = BodyHealthCalculator.calculateHitByEntity(player, damager);
                if (hitBodyPart != null) {
                    BodyHealthUtils.applyDamageWithConfig(bodyHealth, cause, damage, hitBodyPart, false, event);
                    Debug.logDev("Player " + player.getName() + " was hit by an entity (" + damager.getType().name() + ") on " + hitBodyPart.name() + " with " + String.format("%.2f", damage) + " damage.");
                }
                else {
                    BodyHealthUtils.applyDamageWithConfig(bodyHealth, cause, damage, false, event);
                    Debug.logDev("Player " + player.getName() + " was hit by a non-living entity (" + damager.getType().name() + ") with " + String.format("%.2f", damage) + " damage.");
                }
            }

        }

        else if (event instanceof EntityDamageByBlockEvent blockDamageEvent) {

            // 1.21.3 & 1.21.4 PaperMC bug
            if (blockDamageEvent.getDamager() == null) {
                for (BodyPart bodyPart : BodyPart.values()) {
                    BodyHealthUtils.applyDamageWithConfig(bodyHealth, cause, damage, bodyPart, false, event);
                }
                Debug.logDev("Player " + player.getName() + " was hit by a block that couldn't be retrieved because of a bug in your server software (https://github.com/PaperMC/Paper/issues/11984) applying " + String.format("%.2f", damage) + " damage to all body parts.");
                return;
            }

            BodyPart[] hitBodyParts = BodyHealthCalculator.calculateHitByBlock(player, Objects.requireNonNull(blockDamageEvent.getDamager()));
            StringBuilder hitBodyPartsString = new StringBuilder();
            for (BodyPart bodyPart : hitBodyParts) {
                BodyHealthUtils.applyDamageWithConfig(bodyHealth, cause, damage, bodyPart, false, event);
                hitBodyPartsString.append(bodyPart.name()).append(", ");
            }
            Debug.logDev("Player " + player.getName() + " was hit by a block (" + blockDamageEvent.getDamager().getType().name() + ") on " + hitBodyPartsString.substring(0, hitBodyPartsString.length() - 2) + " with " + String.format("%.2f", damage) + "damage.");
        }

        else {
            Debug.logDev("Player " + player.getName() + " was hit by an uncategorized damage source (" + cause.name() + ") with " + String.format("%.2f", damage) + " damage.");
            BodyHealthUtils.applyDamageWithConfig(bodyHealth, cause, damage, false, event);
        }

    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerRegenerate(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        double regenAmount = event.getAmount();

        BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(player);
        bodyHealth.regenerateHealth(regenAmount, false, event);

        Debug.logDev("Player " + player.getName() + " regenerated " + String.format("%.2f", regenAmount) + " HP");

        if (Config.heal_on_full_health && player.getHealth() == player.getMaxHealth()) {
            bodyHealth.regenerateHealth(Integer.MAX_VALUE, false, event);
        }

        checkHealthDelayed(player, player.getHealth() + regenAmount, event);
    }

    // Other plugins could heal a player on item consumption
    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerEat(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (Config.heal_on_full_health && player.getHealth() == player.getMaxHealth()) {
            BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(player);
            bodyHealth.regenerateHealth(Integer.MAX_VALUE, false, event);
        }
        checkHealthDelayed(player, player.getHealth(), event);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!Config.heal_on_respawn) return;
        BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(event.getPlayer());
        Main.getScheduler().runTask(event.getPlayer(), () -> bodyHealth
                .regenerateHealth(Integer.MAX_VALUE, true, event));
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        checkHealthDelayed(event.getPlayer(), event.getPlayer().getHealth(), event); // Other plugins could heal the player here -> check delayed
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().toLowerCase();

        if (command.startsWith("/heal ")) { // Compatibility for a potential heal command
            String[] parts = command.split(" ");
            if (parts.length > 1) {
                Player target = Bukkit.getPlayer(parts[1]);
                if (target != null) checkHealthDelayed(target, target.getHealth(), event);
            }
        }
        checkHealthDelayed(event.getPlayer(), event.getPlayer().getHealth(), event);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo() != null && (event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockZ() != event.getTo().getBlockZ())) {
            BodyHealthUtils.applyBodyHealthHudVisibility(event.getPlayer()); // Refresh when moved to a new block
        }
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        FoliaUtils.updatePlayerWorld(event.getPlayer()); // Keep the watcher in sync so it doesn't double-fire
        Main.getScheduler().runTaskLater(event.getPlayer(), () ->
                handleWorldChange(event.getPlayer(), event.getFrom()), 1L);
    }

    private void handleWorldChange(Player player, World from) {
        boolean isEnabledInFrom = BodyHealthUtils.isSystemEnabled(from);
        boolean isEnabledInTo = BodyHealthUtils.isSystemEnabled(player);
        if (!isEnabledInFrom && isEnabledInTo) EffectHandler.addEffectsToPlayer(player);
        else if (isEnabledInFrom && !isEnabledInTo) {
            if (!BodyHealthUtils.getBodyHealth(player).getOngoingEffects().isEmpty())
                EffectHandler.removeEffectsFromPlayer(player);
        }
        BodyHealthUtils.applyBodyHealthHudVisibility(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        FoliaUtils.updatePlayerWorld(event.getPlayer());
        EffectHandler.removeOngoingEffects(event.getPlayer()); // Clean up known leftover effects
        if (BodyHealthUtils.isSystemEnabled(event.getPlayer())) EffectHandler.addEffectsToPlayer(event.getPlayer());
        Main.getScheduler().runTaskLater(event.getPlayer(), () -> BodyHealthUtils
                .applyBodyHealthHudVisibility(event.getPlayer()), 1L);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        FoliaUtils.removePlayerWorld(event.getPlayer());
        if (BodyHealthUtils.isSystemEnabled(event.getPlayer())) EffectHandler.removeEffectsFromPlayer(event.getPlayer());
        DataManager.saveBodyHealth(event.getPlayer().getUniqueId());
    }

    private void checkHealthDelayed(Player player, double health, @Nullable Event cause) {
        Main.getScheduler().runTaskLater(player, () -> {
            if (player.getHealth() == player.getMaxHealth()) {
                if (!Config.heal_on_full_health) return;
                BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(player);
                boolean alreadyFullyHealed = true;
                for (BodyPart part : BodyPart.values()) {
                    if (bodyHealth.getHealth(part) < 100) alreadyFullyHealed = false;
                } // We don't want to do system checks for every single interaction
                if (!alreadyFullyHealed) bodyHealth.regenerateHealth(Integer.MAX_VALUE, false, cause);
                //Debug.logDev("Healed all BodyParts for player " + player.getName() + " due to them having full health. This should prevent inconsistencies with dynamically updating maxHealth per part."); <-SPAM
            }
            else if ((player.getHealth() - health) >= 0.01) { // Significant health change
                BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(player);
                bodyHealth.regenerateHealth(player.getHealth() - health, false, cause);
                Debug.logDev("Player " + player.getName() + " regenerated " + String.format("%.2f", (player.getHealth() - health)) + " HP (custom)");
            }
        }, 2L);
    }

}
