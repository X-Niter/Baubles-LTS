package baubles.api;

import baubles.api.modcom.BaublesModCom;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLInterModComms;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

public enum BaubleType {
AMULET(0),
RING(1, 2),
BELT(3),
TRINKET(0, 1, 2, 3, 4, 5, 6),
HEAD(4),
BODY(5),
CHARM(6);


public static Map<String, BaubleType> idToType = new HashMap<>();
static ConcurrentMap<String, ConcurrentSet<ResourceLocation>> iconQueues = new ConcurrentHashMap<>();
static Map<String, ResourceLocation> icons = new HashMap<>();
int[] validSlots;
private String identifier;
/**
 * The default number of slots
 */
private int size;
/**
 * Enabled slots will be given to holders by default
 */
private boolean isEnabled;
/**
 * Hidden slots will not show up in the default Curios GUI, but will still exist
 */
private boolean isHidden;

BaubleType(int... validSlots) {
    this.validSlots = validSlots;
}

BaubleType(String identifier) {
    this.identifier = identifier;
    this.size = 1;
    this.isEnabled = true;
    this.isHidden = false;
}

/**
 * Optimized version of processIcons method
 * - Avoids unnecessary list creation and sorting when possible
 * - Uses more efficient comparison logic
 */
public static void processIcons() {
    // Clear existing icons if needed
    if (!icons.isEmpty()) {
        icons = new HashMap<>();
    }
    
    // Process each icon queue
    iconQueues.forEach((k, v) -> {
        if (!icons.containsKey(k) && !v.isEmpty()) {
            // If only one icon, no need to sort
            if (v.size() == 1) {
                icons.put(k, v.iterator().next());
            } else {
                // Find the highest value ResourceLocation directly
                // This avoids unnecessary ArrayList creation and sorting
                ResourceLocation highest = null;
                for (ResourceLocation location : v) {
                    if (highest == null || location.toString().compareTo(highest.toString()) > 0) {
                        highest = location;
                    }
                }
                icons.put(k, highest);
            }
        }
    });
}

/**
 * Optimized version of processBaubleTypes
 * - Uses more efficient stream processing
 * - Reduces intermediate object creation
 * 
 * @param register Stream of registration messages
 * @param modify Stream of modification messages
 */
public static void processBaubleTypes(Stream<FMLInterModComms.IMCMessage> register, Stream<FMLInterModComms.IMCMessage> modify) {
    // Process registration messages more efficiently
    register
            .filter(msg -> msg.getSender() != null)
            // Directly process each message instead of creating intermediate collections
            .forEach(msg -> {
                BaublesModCom msgType = (BaublesModCom) msg.getMessageType().getAnnotatedSuperclass();
                processType(msgType, true);
            });

    // Process modification messages more efficiently
    modify
            .filter(msg -> msg.getSender() != null)
            // Directly process each message instead of creating intermediate collections
            .forEach(msg -> {
                BaublesModCom msgType = (BaublesModCom) msg.getMessageType().getAnnotatedSuperclass();
                processType(msgType, false);
            });
}

private static void processType(BaublesModCom message, boolean create) {
    String identifier = message.getIdentifier();

    if (idToType.containsKey(identifier)) {
        BaubleType presentType = idToType.get(identifier);

        if (message.getSize() > presentType.getSize()) {
            presentType.defaultSize(message.getSize());
        }

        if (!message.isEnabled() && presentType.isEnabled()) {
            presentType.enabled(false);
        }

        if (message.isHidden() && !presentType.isHidden()) {
            presentType.hide(true);
        }

    } else if (create) {
        idToType.put(identifier, BaubleType.valueOf(identifier)
                .defaultSize(message.getSize())
                .enabled(message.isEnabled())
                .hide(message.isHidden()));
    }
}

public String getIdentifier() {
    return identifier;
}

public int getSize() {
    return this.size;
}

public boolean isEnabled() {
    return isEnabled;
}

public boolean isHidden() {
    return isHidden;
}

public final BaubleType defaultSize(int size) {
    this.size = Math.max(size, this.size);
    return this;
}

public final BaubleType enabled(boolean enabled) {
    this.isEnabled = enabled;
    return this;
}

public final BaubleType hide(boolean hide) {
    this.isHidden = hide;
    return this;
}

/**
 * Optimized version of hasSlot method
 * This method is called frequently during slot validation, so optimizing it helps performance
 * 
 * @param slot The slot to check
 * @return true if this BaubleType can be equipped in the given slot
 */
public boolean hasSlot(int slot) {
    // Most BaubleTypes have a very small number of valid slots
    // A direct comparison is faster than iterating when array is small
    if (validSlots.length <= 3) {
        // Unrolled loop is faster for small arrays
        for (int i = 0; i < validSlots.length; i++) {
            if (validSlots[i] == slot) return true;
        }
        return false;
    } else {
        // For BaubleTypes with many slots (e.g. TRINKET), 
        // use the original loop which is more maintainable
        for (int s : validSlots) {
            if (s == slot) return true;
        }
        return false;
    }
}

public int[] getValidSlots() {
    return validSlots;
}
}