package com.seventodie.manager;

import com.seventodie.SevenToDie;
import com.seventodie.api.BaubleType;
import com.seventodie.api.IBauble;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages players' bauble inventories
 */
public class BaubleManager {
    private final SevenToDie plugin;
    private final Map<UUID, ItemStack[]> playerBaubles;
    private final NamespacedKey baubleTypeKey;
    
    // The total number of bauble slots
    public static final int BAUBLE_SLOTS = 7;

    public BaubleManager(SevenToDie plugin) {
        this.plugin = plugin;
        this.playerBaubles = new HashMap<>();
        this.baubleTypeKey = new NamespacedKey(plugin, "bauble_type");
    }
    
    /**
     * Get all baubles for a player
     * @param player The player
     * @return Array of baubles (may contain null values for empty slots)
     */
    public ItemStack[] getBaubles(Player player) {
        UUID uuid = player.getUniqueId();
        if (!playerBaubles.containsKey(uuid)) {
            playerBaubles.put(uuid, new ItemStack[BAUBLE_SLOTS]);
        }
        return playerBaubles.get(uuid);
    }
    
    /**
     * Get a bauble in a specific slot
     * @param player The player
     * @param slot The slot index
     * @return The ItemStack in that slot (null if empty)
     */
    public ItemStack getBauble(Player player, int slot) {
        if (slot < 0 || slot >= BAUBLE_SLOTS) {
            return null;
        }
        
        return getBaubles(player)[slot];
    }
    
    /**
     * Set a bauble in a specific slot
     * @param player The player
     * @param slot The slot index
     * @param itemStack The bauble to equip (null to unequip)
     * @return True if successful
     */
    public boolean setBauble(Player player, int slot, ItemStack itemStack) {
        if (slot < 0 || slot >= BAUBLE_SLOTS) {
            return false;
        }
        
        ItemStack[] baubles = getBaubles(player);
        
        // Handle unequipping
        if (itemStack == null && baubles[slot] != null) {
            ItemStack oldBauble = baubles[slot];
            IBauble bauble = getBaubleHandler(oldBauble);
            if (bauble != null) {
                // Check if can unequip
                if (!bauble.canUnequip(oldBauble, player)) {
                    return false;
                }
                
                // Trigger unequip event
                bauble.onUnequipped(oldBauble, player);
            }
            
            baubles[slot] = null;
            return true;
        }
        
        // Handle equipping
        if (itemStack != null) {
            IBauble bauble = getBaubleHandler(itemStack);
            if (bauble != null) {
                BaubleType type = bauble.getBaubleType(itemStack);
                
                // Check if this type can go in this slot
                if (!type.hasSlot(slot)) {
                    return false;
                }
                
                // Check if can equip
                if (!bauble.canEquip(itemStack, player)) {
                    return false;
                }
                
                // Handle unequipping previous item
                ItemStack oldBauble = baubles[slot];
                if (oldBauble != null) {
                    IBauble oldHandler = getBaubleHandler(oldBauble);
                    if (oldHandler != null) {
                        oldHandler.onUnequipped(oldBauble, player);
                    }
                }
                
                // Equip new bauble
                baubles[slot] = itemStack.clone();
                bauble.onEquipped(itemStack, player);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get the IBauble handler for an item if it's a bauble
     * @param itemStack The item to check
     * @return The IBauble handler or null if not a bauble
     */
    public IBauble getBaubleHandler(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return null;
        }
        
        // In a real implementation, we'd need some way to identify bauble items
        // For this example, we'll use persistent data
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return null;
        }
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(baubleTypeKey, PersistentDataType.STRING)) {
            // Here we would return the actual handler from somewhere
            // For simplicity, we'll return a dummy handler
            String type = container.get(baubleTypeKey, PersistentDataType.STRING);
            return createDummyHandler(type);
        }
        
        return null;
    }
    
    /**
     * Save all player bauble data
     */
    public void saveAllPlayerData() {
        // In a real implementation, this would save to a database or files
        plugin.getLogger().info("Saving bauble data for " + playerBaubles.size() + " players");
    }
    
    /**
     * Create a dummy bauble handler for a specific type
     * @param type The bauble type as a string
     * @return A bauble handler
     */
    private IBauble createDummyHandler(String type) {
        return new IBauble() {
            @Override
            public BaubleType getBaubleType(ItemStack itemStack) {
                try {
                    return BaubleType.valueOf(type.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return BaubleType.TRINKET;
                }
            }
        };
    }
}