package com.seventodie.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Interface for items that can be equipped as baubles
 */
public interface IBauble {
    
    /**
     * Get the type of bauble this item is
     * @param itemStack The item stack to check
     * @return The bauble type
     */
    BaubleType getBaubleType(ItemStack itemStack);
    
    /**
     * Called when the bauble is equipped
     * @param itemStack The equipped item
     * @param player The player equipping the bauble
     */
    default void onEquipped(ItemStack itemStack, Player player) {
        // Default implementation does nothing
        playEquipSound(player);
    }
    
    /**
     * Called when the bauble is unequipped
     * @param itemStack The unequipped item
     * @param player The player unequipping the bauble
     */
    default void onUnequipped(ItemStack itemStack, Player player) {
        // Default implementation does nothing
    }
    
    /**
     * Called every tick while the bauble is equipped
     * @param itemStack The equipped item
     * @param player The player wearing the bauble
     */
    default void onWornTick(ItemStack itemStack, Player player) {
        // Default implementation does nothing  
    }
    
    /**
     * Check if this bauble can be equipped
     * @param itemStack The item to check
     * @param player The player trying to equip
     * @return True if can be equipped
     */
    default boolean canEquip(ItemStack itemStack, Player player) {
        return true;
    }
    
    /**
     * Check if this bauble can be unequipped
     * @param itemStack The item to check
     * @param player The player trying to unequip
     * @return True if can be unequipped
     */
    default boolean canUnequip(ItemStack itemStack, Player player) {
        return true;
    }
    
    /**
     * Play a sound when the bauble is equipped
     * @param player The player to play the sound for
     */
    default void playEquipSound(Player player) {
        player.playSound(player.getLocation(), "item.armor.equip_generic", 1.0f, 1.0f);
    }
    
    /**
     * Determines if this bauble can be equipped by right-clicking
     * @return True if can be equipped via right-click
     */
    default boolean canRightClickEquip() {
        return true;
    }
}