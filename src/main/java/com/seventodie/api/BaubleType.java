package com.seventodie.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines the different types of baubles that can be equipped
 */
public enum BaubleType {
    /**
     * Amulet - worn around the neck
     */
    AMULET(0),
    
    /**
     * Ring - worn on fingers (2 slots available)
     */
    RING(1, 2),
    
    /**
     * Belt - worn around waist
     */
    BELT(3),
    
    /**
     * Trinket - can be equipped in any slot
     */
    TRINKET(0, 1, 2, 3, 4, 5, 6),
    
    /**
     * Head - worn on the head
     */
    HEAD(4),
    
    /**
     * Body - worn on the torso/body
     */
    BODY(5),
    
    /**
     * Charm - general accessory
     */
    CHARM(6);
    
    private final int[] validSlots;
    
    /**
     * Creates a bauble type with specified valid slot indices
     * @param validSlots The slot indices where this bauble can be equipped
     */
    BaubleType(int... validSlots) {
        this.validSlots = validSlots;
    }
    
    /**
     * Check if this bauble type can be equipped in the specified slot
     * @param slot The slot index to check
     * @return True if this bauble can be equipped in that slot
     */
    public boolean hasSlot(int slot) {
        for (int s : validSlots) {
            if (s == slot) return true;
        }
        return false;
    }
    
    /**
     * Get all valid slots for this bauble type
     * @return Array of valid slot indices
     */
    public int[] getValidSlots() {
        return validSlots;
    }
}