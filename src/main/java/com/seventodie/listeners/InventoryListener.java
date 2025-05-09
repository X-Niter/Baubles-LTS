package com.seventodie.listeners;

import com.seventodie.SevenToDie;
import com.seventodie.api.IBauble;
import com.seventodie.manager.BaubleManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for inventory-related events related to baubles
 */
public class InventoryListener implements Listener {
    private final SevenToDie plugin;
    
    public InventoryListener(SevenToDie plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handle inventory click events in the baubles inventory
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        
        // Check if this is a baubles inventory
        // This is a placeholder - in a real implementation, you'd have a way to identify
        // baubles inventories, for example by title or custom holder
        if (event.getView().getTitle().contains("Baubles")) {
            BaubleManager baubleManager = plugin.getBaubleManager();
            int slot = event.getRawSlot();
            
            // Make sure we're clicking in the top inventory (baubles)
            if (slot >= 0 && slot < 9) { // Assuming 9-slot inventory
                ItemStack currentItem = event.getCurrentItem();
                ItemStack cursorItem = event.getCursor();
                
                switch (event.getAction()) {
                    case PICKUP_ALL:
                    case PICKUP_HALF:
                    case PICKUP_ONE:
                    case PICKUP_SOME:
                        // Handle removing a bauble
                        if (currentItem != null && !currentItem.getType().isAir()) {
                            boolean success = baubleManager.setBauble(player, slot, null);
                            if (!success) {
                                event.setCancelled(true);
                                player.sendMessage(ChatColor.RED + "You cannot remove this bauble!");
                            }
                        }
                        break;
                        
                    case PLACE_ALL:
                    case PLACE_ONE:
                    case PLACE_SOME:
                        // Handle placing a bauble
                        if (cursorItem != null && !cursorItem.getType().isAir()) {
                            IBauble bauble = baubleManager.getBaubleHandler(cursorItem);
                            if (bauble != null) {
                                if (!bauble.getBaubleType(cursorItem).hasSlot(slot)) {
                                    event.setCancelled(true);
                                    player.sendMessage(ChatColor.RED + "This bauble cannot be equipped in this slot!");
                                } else if (!bauble.canEquip(cursorItem, player)) {
                                    event.setCancelled(true);
                                    player.sendMessage(ChatColor.RED + "You cannot equip this bauble!");
                                }
                            } else {
                                event.setCancelled(true);
                                player.sendMessage(ChatColor.RED + "This item is not a bauble!");
                            }
                        }
                        break;
                        
                    case SWAP_WITH_CURSOR:
                        // Handle swapping baubles
                        event.setCancelled(true); // For simplicity, cancel and handle manually
                        
                        if (cursorItem != null && !cursorItem.getType().isAir()) {
                            IBauble bauble = baubleManager.getBaubleHandler(cursorItem);
                            if (bauble != null) {
                                if (bauble.getBaubleType(cursorItem).hasSlot(slot) && bauble.canEquip(cursorItem, player)) {
                                    // Store old bauble
                                    ItemStack oldBauble = baubleManager.getBauble(player, slot);
                                    
                                    // Remove old bauble
                                    baubleManager.setBauble(player, slot, null);
                                    
                                    // Add new bauble
                                    if (baubleManager.setBauble(player, slot, cursorItem)) {
                                        // Set cursor to old bauble
                                        event.getView().setCursor(oldBauble);
                                    } else {
                                        // If equipping fails, put back the old bauble
                                        baubleManager.setBauble(player, slot, oldBauble);
                                        player.sendMessage(ChatColor.RED + "Failed to equip bauble!");
                                    }
                                } else {
                                    player.sendMessage(ChatColor.RED + "This bauble cannot be equipped in this slot!");
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "This item is not a bauble!");
                            }
                        }
                        break;
                        
                    default:
                        // For any other actions, cancel them for simplicity
                        event.setCancelled(true);
                        break;
                }
                
                // Always cancel the regular inventory click behavior
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * Prevent item dragging in the baubles inventory
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Inventory inventory = event.getInventory();
        
        // Check if this is a baubles inventory
        if (event.getView().getTitle().contains("Baubles")) {
            // Check if any of the slots are in the top inventory
            boolean affectsTopInventory = false;
            for (int slot : event.getRawSlots()) {
                if (slot < 9) { // Assuming 9-slot inventory
                    affectsTopInventory = true;
                    break;
                }
            }
            
            if (affectsTopInventory) {
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * Handle closing the baubles inventory
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Inventory inventory = event.getInventory();
        
        // Check if this is a baubles inventory
        if (event.getView().getTitle().contains("Baubles")) {
            // We might need to update something when the inventory is closed
            // For now, we're not doing anything special
        }
    }
}