package baubles.common.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * A utility for interning strings to reduce memory usage
 * This reduces memory pressure by ensuring frequently used strings 
 * share the same memory location instead of creating duplicates
 */
public class StringPool {
    // Use ConcurrentHashMap for thread safety without locking
    private static final Map<String, String> STRING_POOL = new ConcurrentHashMap<>(256);
    
    // Maximum pool size to prevent memory leaks
    private static final int MAX_POOL_SIZE = 1000;
    
    // Maximum string length to intern (avoid huge strings)
    private static final int MAX_STRING_LENGTH = 128;
    
    /**
     * Intern a string to reduce memory usage
     * Returns the same instance for equal strings
     * 
     * @param str The string to intern (can be null)
     * @return The interned string or null if input was null
     */
    @Nullable
    public static String intern(@Nullable String str) {
        // Handle null case
        if (str == null) {
            return null;
        }
        
        // Don't intern empty strings (JVM already handles this)
        if (str.isEmpty()) {
            return "";
        }
        
        // Don't intern large strings to avoid memory waste
        if (str.length() > MAX_STRING_LENGTH) {
            return str;
        }
        
        // Check if we're at capacity
        if (STRING_POOL.size() >= MAX_POOL_SIZE) {
            // Just return the string without interning if pool is full
            return str;
        }
        
        // Get from pool or add if not present
        return STRING_POOL.computeIfAbsent(str, s -> s);
    }
    
    /**
     * Clear the string pool (e.g., during game reload)
     */
    public static void clear() {
        STRING_POOL.clear();
    }
    
    /**
     * Get the current size of the string pool
     * @return Number of unique strings in the pool
     */
    public static int size() {
        return STRING_POOL.size();
    }
}