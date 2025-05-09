package baubles.common.util;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.cap.BaublesCapabilityManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.EnumMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A cache system for BaubleType lookups to avoid repeated capability queries
 * Significantly improves performance by eliminating redundant capability lookups
 * and reducing object allocations during inventory operations
 */
public class TypeCache {
    // Cache mapping item types to their BaubleType
    // Using WeakHashMap to avoid memory leaks when items are unloaded
    private static final Map<Item, BaubleType> ITEM_TYPE_CACHE = new WeakHashMap<>(128);
    
    // Cache for checking which slots a bauble type can go into
    // Using EnumMap for efficient enum-based lookups
    private static final Map<BaubleType, boolean[]> SLOT_VALIDITY_CACHE = new EnumMap<>(BaubleType.class);
    
    // Cache for non-baubles to avoid repeated capability lookups that will always fail
    // Using WeakHashMap to avoid memory leaks
    private static final Map<Item, Boolean> NON_BAUBLE_CACHE = new WeakHashMap<>(128);
    
    // Track cache statistics if enabled
    private static boolean TRACK_STATS = false;
    private static int cacheHits = 0;
    private static int cacheMisses = 0;
    
    static {
        // Pre-populate the slot validity cache for all bauble types
        for (BaubleType type : BaubleType.values()) {
            SLOT_VALIDITY_CACHE.put(type, createValidityArray(type));
        }
    }
    
    /**
     * Get the bauble type for an item stack, using cache when possible
     * 
     * @param stack The item stack to check
     * @return The BaubleType or null if not a bauble
     */
    public static BaubleType getBaubleType(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        
        Item item = stack.getItem();
        
        // Check if we know this is not a bauble
        if (NON_BAUBLE_CACHE.containsKey(item)) {
            if (TRACK_STATS) cacheHits++;
            return null;
        }
        
        // Check for cached type
        BaubleType cachedType = ITEM_TYPE_CACHE.get(item);
        if (cachedType != null) {
            if (TRACK_STATS) cacheHits++;
            return cachedType;
        }
        
        if (TRACK_STATS) cacheMisses++;
        
        // Not in cache, do the capability lookup
        if (!stack.hasCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null)) {
            // Remember this is not a bauble
            NON_BAUBLE_CACHE.put(item, Boolean.TRUE);
            return null;
        }
        
        IBauble bauble = stack.getCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null);
        if (bauble == null) {
            // Remember this is not a bauble
            NON_BAUBLE_CACHE.put(item, Boolean.TRUE);
            return null;
        }
        
        // Cache the type and return it
        BaubleType type = bauble.getBaubleType(stack);
        if (type != null) {
            ITEM_TYPE_CACHE.put(item, type);
        }
        return type;
    }
    
    /**
     * Check if a bauble type is valid for a specific slot
     * Uses pre-computed validity arrays for improved performance
     * 
     * @param type The bauble type
     * @param slot The slot index
     * @return True if the type is valid for the slot
     */
    public static boolean isValidTypeForSlot(BaubleType type, int slot) {
        if (type == null) {
            return false;
        }
        
        boolean[] validityArray = SLOT_VALIDITY_CACHE.get(type);
        return slot >= 0 && slot < validityArray.length && validityArray[slot];
    }
    
    /**
     * Create a pre-computed validity array for a bauble type
     * 
     * @param type The bauble type
     * @return Array where index=slot and value=valid
     */
    private static boolean[] createValidityArray(BaubleType type) {
        // Find max slot to determine array size
        int maxSlot = 0;
        for (int slot : type.getValidSlots()) {
            maxSlot = Math.max(maxSlot, slot);
        }
        
        // Create and populate the array
        boolean[] validityArray = new boolean[maxSlot + 1];
        for (int slot : type.getValidSlots()) {
            validityArray[slot] = true;
        }
        
        return validityArray;
    }
    
    /**
     * Clear the cache, useful for testing or when mods are loaded/unloaded
     */
    public static void clearCache() {
        ITEM_TYPE_CACHE.clear();
        NON_BAUBLE_CACHE.clear();
    }
    
    /**
     * Enable or disable tracking of cache statistics
     * 
     * @param enabled True to enable tracking
     */
    public static void setStatsTracking(boolean enabled) {
        TRACK_STATS = enabled;
        if (!enabled) {
            cacheHits = 0;
            cacheMisses = 0;
        }
    }
    
    /**
     * Get the current cache hit/miss statistics
     * 
     * @return Stats string in format "hits/total (percentage%)"
     */
    public static String getStats() {
        int total = cacheHits + cacheMisses;
        double percentage = total > 0 ? (cacheHits * 100.0 / total) : 0;
        return String.format("%d/%d (%.1f%%)", cacheHits, total, percentage);
    }
}