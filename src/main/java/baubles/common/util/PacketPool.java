package baubles.common.util;

import baubles.common.network.PacketSync;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Object pooling utility for network packets to reduce GC pressure
 * By reusing packet instances, we can significantly reduce memory allocations
 * and garbage collection overhead during high-frequency network operations
 */
public class PacketPool {
    // Maximum size of the pool to prevent excessive memory usage
    private static final int MAX_POOL_SIZE = 256;
    
    // Queue of available packet instances ready for reuse
    private static final ConcurrentLinkedQueue<PacketSync> SYNC_PACKET_POOL = new ConcurrentLinkedQueue<>();
    
    // Track statistics for monitoring pool performance
    private static final AtomicInteger pooledPacketsCreated = new AtomicInteger(0);
    private static final AtomicInteger pooledPacketsReused = new AtomicInteger(0);
    private static final AtomicInteger pooledPacketsRecycled = new AtomicInteger(0);
    
    /**
     * Get a PacketSync instance from the pool, or create a new one if the pool is empty
     * 
     * @param entity The entity associated with the packet
     * @param slot The slot index
     * @param stack The ItemStack in the slot
     * @return A ready-to-use PacketSync instance
     */
    public static PacketSync getSyncPacket(EntityLivingBase entity, int slot, ItemStack stack) {
        // Try to get a packet from the pool
        PacketSync packet = SYNC_PACKET_POOL.poll();
        
        if (packet != null) {
            // Reuse the existing packet instance
            pooledPacketsReused.incrementAndGet();
            return packet.resetAndSetValues(entity, slot, stack);
        } else {
            // Create a new packet if the pool is empty
            pooledPacketsCreated.incrementAndGet();
            return new PacketSync(entity, slot, stack);
        }
    }
    
    /**
     * Return a packet to the pool for reuse
     * 
     * @param packet The packet to recycle
     */
    public static void recyclePacket(PacketSync packet) {
        // Don't add null packets to the pool
        if (packet == null) {
            return;
        }
        
        // Clear the packet data to prevent memory leaks
        packet.clear();
        
        // Only add to the pool if we haven't reached the maximum size
        if (SYNC_PACKET_POOL.size() < MAX_POOL_SIZE) {
            SYNC_PACKET_POOL.offer(packet);
            pooledPacketsRecycled.incrementAndGet();
        }
    }
    
    /**
     * Get the current number of packets in the pool
     * 
     * @return Number of available packets
     */
    public static int getPoolSize() {
        return SYNC_PACKET_POOL.size();
    }
    
    /**
     * Get statistics about pool usage
     * 
     * @return String with usage statistics
     */
    public static String getStats() {
        return String.format(
            "Packet Pool Stats - Created: %d, Reused: %d, Recycled: %d, Available: %d",
            pooledPacketsCreated.get(),
            pooledPacketsReused.get(),
            pooledPacketsRecycled.get(),
            SYNC_PACKET_POOL.size()
        );
    }
    
    /**
     * Clear all pooled packets - useful when unloading
     */
    public static void clearPool() {
        SYNC_PACKET_POOL.clear();
    }
    
    /**
     * Reset statistics counters
     */
    public static void resetStats() {
        pooledPacketsCreated.set(0);
        pooledPacketsReused.set(0);
        pooledPacketsRecycled.set(0);
    }
}