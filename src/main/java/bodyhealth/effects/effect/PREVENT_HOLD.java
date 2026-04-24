package bodyhealth.effects.effect;

import bodyhealth.Main;
import bodyhealth.config.Debug;
import bodyhealth.core.BodyHealth;
import bodyhealth.core.BodyPart;
import bodyhealth.effects.BodyHealthEffect;
import bodyhealth.effects.EffectType;
import bodyhealth.util.BodyHealthUtils;
import bodyhealth.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
import java.util.Map;

public class PREVENT_HOLD implements BodyHealthEffect {

    @Override
    public EffectType getEffectType() {
        return EffectType.PERSISTENT;
    }

    @Override
    public String getIdentifier() {
        return "PREVENT_HOLD";
    }

    @Override
    public String getUsage() {
        return "PREVENT_HOLD / <HAND> / [MESSAGE]";
    }

    @Override
    public void onApply(Player player, BodyPart part, String[] args, boolean isRecovery) {
        if (args.length <= 1) {
            Debug.logErr("Effect \"" + args[0].trim() + "\" is missing arguments, check syntax!");
            return;
        }
        try {
            EquipmentSlot hand = EquipmentSlot.valueOf(args[1].trim().toUpperCase());
            Debug.log("(" + part.name() + ") Preventing " + hand.name() + " hold for player " + player.getName());
            ItemStack held = hand == EquipmentSlot.HAND
                    ? player.getInventory().getItemInMainHand()
                    : player.getInventory().getItemInOffHand();
            if (held.getType() != Material.AIR) {
                if (hand == EquipmentSlot.HAND) player.getInventory().setItemInMainHand(null);
                else player.getInventory().setItemInOffHand(null);
                player.getWorld().dropItemNaturally(player.getLocation(), held);
                if (args.length > 2) MessageUtils.notifyPlayer(player, args[2].trim());
            }
        } catch (IllegalArgumentException e) {
            Debug.logErr("EquipmentSlot (Hand) \"" + args[1].trim() + "\" is invalid, check syntax!");
        }
    }

    @Override
    public void onRemove(Player player, BodyPart part, String[] args, boolean isRecovery) {
        if (args.length <= 1) {
            Debug.logErr("Effect \"" + args[0].trim() + "\" is missing arguments, check syntax!");
            return;
        }
        try {
            EquipmentSlot hand = EquipmentSlot.valueOf(args[1].trim().toUpperCase());
            Debug.log("(" + part.name() + ") No longer preventing " + hand.name() + " hold for player " + player.getName());
        } catch (IllegalArgumentException e) {
            Debug.logErr("EquipmentSlot (Hand) \"" + args[1].trim() + "\" is invalid, check syntax!");
        }
    }

    public static boolean isHandPrevented(Player player, EquipmentSlot hand) {
        BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(player);
        if (bodyHealth.getOngoingEffects().isEmpty()) return false;
        for (Map.Entry<BodyPart, List<String[]>> entry : bodyHealth.getOngoingEffects().entrySet()) {
            for (String[] effectParts : entry.getValue()) {
                if (effectParts.length > 1 && effectParts[0].trim().equalsIgnoreCase("PREVENT_HOLD")
                        && effectParts[1].trim().equalsIgnoreCase(hand.name())) return true;
            }
        }
        return false;
    }

    private static void sendHandMessage(Player player, EquipmentSlot hand) {
        BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(player);
        if (bodyHealth.getOngoingEffects().isEmpty()) return;
        for (Map.Entry<BodyPart, List<String[]>> entry : bodyHealth.getOngoingEffects().entrySet()) {
            for (String[] effectParts : entry.getValue()) {
                if (effectParts.length > 2 && effectParts[0].trim().equalsIgnoreCase("PREVENT_HOLD")
                        && effectParts[1].trim().equalsIgnoreCase(hand.name())) {
                    MessageUtils.notifyPlayer(player, effectParts[2].trim());
                }
            }
        }
    }

    private static void dropHeldSlotAndNotify(Player player, int slot) {
        ItemStack item = player.getInventory().getItem(slot);
        if (item == null || item.getType() == Material.AIR) return;
        player.getInventory().setItem(slot, null);
        player.getWorld().dropItemNaturally(player.getLocation(), item);
        sendHandMessage(player, EquipmentSlot.HAND);
    }

    private static void dropOffHandAndNotify(Player player) {
        ItemStack item = player.getInventory().getItemInOffHand();
        if (item.getType() == Material.AIR) return;
        player.getInventory().setItemInOffHand(null);
        player.getWorld().dropItemNaturally(player.getLocation(), item);
        sendHandMessage(player, EquipmentSlot.OFF_HAND);
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        if (!isHandPrevented(event.getPlayer(), EquipmentSlot.HAND)) return;
        ItemStack item = event.getPlayer().getInventory().getItem(event.getNewSlot());
        if (item == null || item.getType() == Material.AIR) return;
        event.getPlayer().getInventory().setItem(event.getNewSlot(), null);
        event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), item);
        sendHandMessage(event.getPlayer(), EquipmentSlot.HAND);
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        boolean preventOff = isHandPrevented(player, EquipmentSlot.OFF_HAND);
        boolean preventMain = isHandPrevented(player, EquipmentSlot.HAND);
        if (!preventOff && !preventMain) return;

        event.setCancelled(true);
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            if (preventOff) {
                ItemStack mainItem = player.getInventory().getItemInMainHand();
                if (mainItem.getType() != Material.AIR) {
                    player.getInventory().setItemInMainHand(null);
                    player.getWorld().dropItemNaturally(player.getLocation(), mainItem);
                    sendHandMessage(player, EquipmentSlot.OFF_HAND);
                }
            }
            if (preventMain) {
                ItemStack offItem = player.getInventory().getItemInOffHand();
                if (offItem.getType() != Material.AIR) {
                    player.getInventory().setItemInOffHand(null);
                    player.getWorld().dropItemNaturally(player.getLocation(), offItem);
                    sendHandMessage(player, EquipmentSlot.HAND);
                }
            }
        });
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        boolean checkHand = isHandPrevented(player, EquipmentSlot.HAND);
        boolean checkOffHand = isHandPrevented(player, EquipmentSlot.OFF_HAND);
        if (!checkHand && !checkOffHand) return;

        int heldSlot = player.getInventory().getHeldItemSlot();
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            if (checkHand) dropHeldSlotAndNotify(player, heldSlot);
            if (checkOffHand) dropOffHandAndNotify(player);
        });
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        InventoryAction action = event.getAction();
        if (action == InventoryAction.NOTHING) return;

        boolean checkHand = isHandPrevented(player, EquipmentSlot.HAND);
        boolean checkOffHand = isHandPrevented(player, EquipmentSlot.OFF_HAND);
        if (!checkHand && !checkOffHand) return;

        int heldSlot = player.getInventory().getHeldItemSlot();

        // Hotbar number key pressed while hovering, item moves into the hotbar slot
        if (action == InventoryAction.HOTBAR_SWAP || action == InventoryAction.HOTBAR_MOVE_AND_READD) {
            int hotbarButton = event.getHotbarButton();
            if (hotbarButton == -1) {
                // F pressed while hovering in inventory => swap with off-hand
                if (checkOffHand) Bukkit.getScheduler().runTask(Main.getInstance(), () -> dropOffHandAndNotify(player));
                return;
            }
            if (checkHand && hotbarButton == heldSlot) {
                ItemStack moving = event.getCurrentItem();
                if (moving != null && moving.getType() != Material.AIR) {
                    Bukkit.getScheduler().runTask(Main.getInstance(), () ->
                            dropHeldSlotAndNotify(player, heldSlot));
                }
            }
            return;
        }

        // Shift-click moving an item to the other inventory, may land in a protected slot
        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                if (checkHand) dropHeldSlotAndNotify(player, heldSlot);
                if (checkOffHand) dropOffHandAndNotify(player);
            });
            return;
        }

        // Direct cursor placement into a slot, only within PlayerInventory
        if (action != InventoryAction.PLACE_ALL && action != InventoryAction.PLACE_ONE
                && action != InventoryAction.PLACE_SOME && action != InventoryAction.SWAP_WITH_CURSOR) return;
        if (!(event.getClickedInventory() instanceof PlayerInventory)) return;

        ItemStack cursorItem = event.getCursor();
        if (cursorItem == null || cursorItem.getType() == Material.AIR) return;
        int slot = event.getSlot();

        if (checkHand && slot == heldSlot) {
            Bukkit.getScheduler().runTask(Main.getInstance(), () ->
                    dropHeldSlotAndNotify(player, heldSlot));
        } else if (checkOffHand && slot == 40) { // slot 40 in PlayerInventory = off-hand
            Bukkit.getScheduler().runTask(Main.getInstance(), () ->
                    dropOffHandAndNotify(player));
        }
    }

}
