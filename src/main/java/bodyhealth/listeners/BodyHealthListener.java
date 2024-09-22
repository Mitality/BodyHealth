package bodyhealth.listeners;

import bodyhealth.core.BodyPart;
import bodyhealth.effects.BodyHealthEffects;
import bodyhealth.core.BodyHealth;
import bodyhealth.calculations.BodyHealthCalculator;
import bodyhealth.util.BodyHealthUtils;
import bodyhealth.Main;
import bodyhealth.config.Config;
import bodyhealth.config.Debug;
import bodyhealth.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;

public class BodyHealthListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(player);
        EntityDamageEvent.DamageCause cause = event.getCause();
        double damage = event.getFinalDamage();

        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent entityDamageEvent = (EntityDamageByEntityEvent) event;
            Entity damager = entityDamageEvent.getDamager();

            if (damager instanceof Arrow) {
                Arrow arrow = (Arrow) damager;
                BodyPart hitBodyPart = BodyHealthCalculator.calculateHitByArrow(player, arrow);
                BodyHealthUtils.applyDamageWithConfig(bodyHealth, cause, damage, hitBodyPart);
                Debug.log("Player " + player.getName() + " was hit by an arrow on " + hitBodyPart.name() + " with " + damage + " damage.");
            }

            else if (damager.getType() == EntityType.CREEPER || damager.getType() == EntityType.TNT || damager.getType() == EntityType.TNT_MINECART || damager.getType() == EntityType.LIGHTNING_BOLT) {
                Debug.log("Player " + player.getName() + " was hit by " + damager.getType().name() + " with " + damage + " damage.");
                BodyHealthUtils.applyDamageWithConfig(bodyHealth, cause, damage);
            }

            else if (damager instanceof Player && damager == player) {
                if (Config.self_harm) BodyHealthUtils.applyDamageWithConfig(bodyHealth, cause, damage);
                if (Config.self_harm) Debug.log("Player " + player.getName() + " was hit by itself with " + damage + " damage.");
            }

            else {
                BodyPart hitBodyPart = BodyHealthCalculator.calculateHitByEntity(player, damager);
                BodyHealthUtils.applyDamageWithConfig(bodyHealth, cause, damage, hitBodyPart);
                Debug.log("Player " + player.getName() + " was hit by an entity (" + damager.getType().name() + ") on " + hitBodyPart.name() + " with " + damage + " damage.");
            }

        }

        else if (event instanceof EntityDamageByBlockEvent) {
            EntityDamageByBlockEvent blockDamageEvent = (EntityDamageByBlockEvent) event;
            //if (blockDamageEvent.getDamager().getType() == Material.SWEET_BERRY_BUSH) blockLocation.subtract(0.0, 0.5, 0.0); // TODO: Why tf is this needed!?
            BodyPart[] hitBodyParts = BodyHealthCalculator.calculateHitByBlock(player, Objects.requireNonNull(blockDamageEvent.getDamager()));
            StringBuilder hitBodyPartsString = new StringBuilder();
            for (BodyPart bodyPart : hitBodyParts) {
                BodyHealthUtils.applyDamageWithConfig(bodyHealth, cause, damage, bodyPart);
                hitBodyPartsString.append(bodyPart.name()).append(", ");
            }
            Debug.log("Player " + player.getName() + " was hit by a block (" + blockDamageEvent.getDamager().getType().name() + ") on " + hitBodyPartsString.substring(0, hitBodyPartsString.length() - 1) + "with " + damage + "damage.");
        }

        else {
            Debug.log("Player " + player.getName() + " was hit by an uncategorized damage source (" + cause.name() + ") with " + damage + " damage.");
            BodyHealthUtils.applyDamageWithConfig(bodyHealth, cause, damage);
        }

    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerRegenerate(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        double regenAmount = event.getAmount();

        BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(player);
        bodyHealth.regenerateHealth(regenAmount);

        Debug.log("Player " + player.getName() + " regenerated " + regenAmount + " HP");

        if (player.getHealth() == player.getMaxHealth()) {
            bodyHealth.regenerateHealth(Integer.MAX_VALUE);
        }

        checkHealthDelayed(player, player.getHealth() + regenAmount);
    }

    // Other plugins could heal a player on item consumption
    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerEat(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (player.getHealth() == player.getMaxHealth()) {
            BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(player);
            bodyHealth.regenerateHealth(Integer.MAX_VALUE);
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(event.getPlayer());
        bodyHealth.regenerateHealth(Integer.MAX_VALUE);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        checkHealthDelayed(event.getPlayer(), event.getPlayer().getHealth()); // Other plugins could heal the player here -> check delayed
        if (!Config.always_allow_eating || event.getItem() == null || event.getItem().getItemMeta() == null || !event.getItem().getItemMeta().hasFood()) {
            if (BodyHealthUtils.canPlayerInteract(event.getPlayer(), event.getHand())) return;
            MessageUtils.sendEffectMessages(event.getPlayer(), "PREVENT_INTERACT");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().toLowerCase();

        if (command.startsWith("/heal")) { // Compatibility for a potential heal command
            Player player = event.getPlayer();
            if (command.equals("/heal")) {
                BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(player);
                bodyHealth.regenerateHealth(Integer.MAX_VALUE);
            } else if (command.startsWith("/heal ")) {
                String[] parts = command.split(" ");
                if (parts.length == 2) {
                    Player target = Bukkit.getPlayer(parts[1]);
                    if (target != null) {
                        BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(target);
                        bodyHealth.regenerateHealth(Integer.MAX_VALUE);
                    }
                }
            }
        }
        checkHealthDelayed(event.getPlayer(), event.getPlayer().getHealth());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getPlayer().isSprinting()) {
            if (BodyHealthEffects.preventSprint.contains(event.getPlayer())) return; // Player is already slowed down
            //if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() != event.getTo().getBlockZ()) return; // Player hasn't moved a block yet (possible optimization for the line below)
            if (BodyHealthUtils.canPlayerSprint(event.getPlayer())) return; // Player is allowed to sprint
            BodyHealthEffects.preventSprint.add(event.getPlayer());
            Objects.requireNonNull(event.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).addModifier(BodyHealthEffects.getSpeedReductionModifier());
            Debug.log("Adding SpeedReductionModifier to player " + event.getPlayer().getName());
            MessageUtils.sendEffectMessages(event.getPlayer(), "PREVENT_SPRINT");
        } else {
            if (!BodyHealthEffects.preventSprint.contains(event.getPlayer())) return; // Player isn't slowed down
            BodyHealthEffects.preventSprint.remove(event.getPlayer());
            Objects.requireNonNull(event.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).removeModifier(BodyHealthEffects.getSpeedReductionModifier());
            Debug.log("Removing SpeedReductionModifier from player " + event.getPlayer().getName());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        BodyHealthEffects.addEffectsToPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        BodyHealthEffects.removeEffectsFromPlayer(event.getPlayer());
    }

    private void checkHealthDelayed(Player player, double health) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (player.getHealth() == player.getMaxHealth()) {
                    BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(player);
                    bodyHealth.regenerateHealth(Integer.MAX_VALUE);
                    //Debug.log("Healed all BodyParts for player " + player.getName() + " due to them having full health. This should prevent inconsistencies with dynamically updating maxHealth per part.");
                }
                else if (player.getHealth() > health) {
                    BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(player);
                    bodyHealth.regenerateHealth(player.getHealth() - health);
                    Debug.log("Player " + player.getName() + " regenerated " + (player.getHealth() - health) + " HP");
                }
            }
        }.runTaskLaterAsynchronously(Main.getPlugin(Main.class), 2);
    }

}
