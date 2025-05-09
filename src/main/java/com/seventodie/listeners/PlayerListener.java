package com.seventodie.listeners;

import com.seventodie.SevenToDie;
import com.seventodie.api.IBauble;
import com.seventodie.manager.BaubleManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for player-related events
 */
public class PlayerListener implements Listener {
    private final SevenToDie plugin;
    
    public PlayerListener(SevenToDie plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handle player join events - load bauble data
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // In a real implementation, you would load the player's bauble data from storage
        plugin.getLogger().info("Loading bauble data for " + player.getName());
        
        // Temporary for testing - we're not actually loading any data here
    }
    
    /**
     * Handle player quit events - save bauble data if configured
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getConfigHandler().shouldSaveDataOnLogout()) {
            // In a real implementation, you would save the player's bauble data to storage
            plugin.getLogger().info("Saving bauble data for " + player.getName());
            
            // Temporary for testing - we're not actually saving any data here
        }
    }
    
    /**
     * Handle player interact events - right-click equipping of baubles
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only handle right-click actions
        if (!event.getAction().name().contains("RIGHT_CLICK")) {
            return;
        }
        
        // Only proceed if right-click equipping is enabled
        if (!plugin.getConfigHandler().isRightClickEquipEnabled()) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        
        if (itemInHand == null || itemInHand.getType().isAir()) {
            return;
        }
        
        BaubleManager baubleManager = plugin.getBaubleManager();
        IBauble bauble = baubleManager.getBaubleHandler(itemInHand);
        
        if (bauble != null && bauble.canRightClickEquip()) {
            // Try to equip in first valid slot
            boolean equipped = tryEquipInFirstValidSlot(player, itemInHand, bauble);
            
            if (equipped) {
                // Consume one item if successful
                if (itemInHand.getAmount() > 1) {
                    itemInHand.setAmount(itemInHand.getAmount() - 1);
                } else {
                    player.getInventory().setItemInMainHand(null);
                }
                
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * Try to equip a bauble in the first valid slot
     * @param player The player
     * @param itemStack The bauble to equip
     * @param bauble The bauble handler
     * @return True if the bauble was equipped successfully
     */
    private boolean tryEquipInFirstValidSlot(Player player, ItemStack itemStack, IBauble bauble) {
        BaubleManager baubleManager = plugin.getBaubleManager();
        int[] validSlots = bauble.getBaubleType(itemStack).getValidSlots();
        
        for (int slot : validSlots) {
            // Check if the slot is empty
            if (baubleManager.getBauble(player, slot) == null) {
                return baubleManager.setBauble(player, slot, itemStack);
            }
        }
        
        return false;
    }
}