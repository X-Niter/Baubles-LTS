package baubles.common.event;

import baubles.api.BaublesApi;
import baubles.api.IBauble;
import baubles.api.cap.BaubleStorage;
import baubles.api.cap.BaublesCapabilityManager;
import baubles.common.network.PacketHandler;
import baubles.common.network.PacketSync;
import baubles.common.util.PacketPool;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;

public class EventHandlerEntity {

        private HashMap<UUID, ItemStack[]> baublesSync = new HashMap<UUID, ItemStack[]>();

        @SubscribeEvent
        public void playerJoin(EntityJoinWorldEvent event) {
                Entity entity = event.getEntity();
                if (entity instanceof EntityPlayerMP) {
                        EntityPlayerMP player = (EntityPlayerMP) entity;
                        syncSlots(player, Collections.singletonList(player));
                }
        }

        @SubscribeEvent
        public void onStartTracking(PlayerEvent.StartTracking event) {
                Entity target = event.getTarget();
                if (target instanceof EntityPlayerMP) {
                        syncSlots((EntityPlayer) target, Collections.singletonList(event.getEntityPlayer()));
                }
        }

        @SubscribeEvent
        public void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
                baublesSync.remove(event.player.getUniqueID());
        }

        @SubscribeEvent
        public void playerTick(TickEvent.PlayerTickEvent event) {
                // player events
                if (event.phase == TickEvent.Phase.END) {
                        EntityPlayer player = event.player;
                        BaubleStorage baubles = BaublesCapabilityManager.asBaublesPlayer(player).getBaubleStorage();
                        for (int i = 0; i < baubles.getActualSize(); i++) {
                                ItemStack stack = baubles.getStackInSlot(i);
                                IBauble bauble = stack.getCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null);
                                if (bauble != null) {
                                        bauble.onWornTick(stack, player);
                                }
                        }
                        if (!player.world.isRemote) {
                                syncBaubles(player, baubles);
                        }
                }
        }

        /**
         * Optimized version of syncBaubles
         * - Reduces memory allocations and object creation
         * - Improves performance for the common case (no changes)
         * - Uses more efficient collection operations
         * 
         * @param player The player whose baubles need to be synced
         * @param baubles The baubles storage to sync
         */
        private void syncBaubles(EntityPlayer player, BaubleStorage baubles) {
                // Get or create the item cache for this player
                UUID playerId = player.getUniqueID();
                ItemStack[] items = baublesSync.get(playerId);
                int actualSize = baubles.getActualSize();
                
                // Initialize the cache if needed
                if (items == null) {
                        items = new ItemStack[actualSize];
                        Arrays.fill(items, ItemStack.EMPTY);
                        baublesSync.put(playerId, items);
                }
                
                // Resize the cache if needed (size mismatch)
                if (items.length != actualSize) {
                        ItemStack[] resizedItems = new ItemStack[actualSize];
                        // Fill with EMPTY to avoid NPEs
                        Arrays.fill(resizedItems, ItemStack.EMPTY);
                        // Copy existing items
                        System.arraycopy(items, 0, resizedItems, 0, Math.min(items.length, actualSize));
                        items = resizedItems;
                        baublesSync.put(playerId, items);
                }
                
                // Check if we need to sync anything - early exit if not
                boolean needsSync = false;
                for (int i = 0; i < actualSize; i++) {
                        if (baubles.isChanged(i)) {
                                needsSync = true;
                                break;
                        }
                        
                        ItemStack stack = baubles.getStackInSlot(i);
                        if (!stack.isEmpty() && stack.hasCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null)) {
                                IBauble bauble = stack.getCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null);
                                if (bauble != null && bauble.willAutoSync(stack, player) && !ItemStack.areItemStacksEqual(stack, items[i])) {
                                        needsSync = true;
                                        break;
                                }
                        }
                }
                
                // If no sync needed, return early
                if (!needsSync) {
                        return;
                }
                
                // Create receivers set only once if needed
                Set<EntityPlayer> receivers = null;
                
                // Process each slot that needs syncing
                for (int i = 0; i < actualSize; i++) {
                        ItemStack stack = baubles.getStackInSlot(i);
                        boolean shouldSync = baubles.isChanged(i);
                        
                        // Check if we need to sync this bauble
                        if (!shouldSync && !stack.isEmpty() && stack.hasCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null)) {
                                IBauble bauble = stack.getCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null);
                                shouldSync = bauble != null && bauble.willAutoSync(stack, player) && !ItemStack.areItemStacksEqual(stack, items[i]);
                        }
                        
                        if (shouldSync) {
                                // Initialize receivers only when first needed
                                if (receivers == null) {
                                        receivers = new HashSet<>(((WorldServer) player.world).getEntityTracker().getTrackingPlayers(player));
                                        receivers.add(player);
                                }
                                
                                // Sync the slot and update our cache
                                syncSlot(player, i, stack, receivers);
                                baubles.setChanged(i, false);
                                items[i] = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
                        }
                }
        }

        private void syncSlots(EntityPlayer player, Collection<? extends EntityPlayer> receivers) {
                BaubleStorage baubles = BaublesCapabilityManager.asBaublesPlayer(player).getBaubleStorage();
                for (int i = 0; i < baubles.getActualSize(); i++) {
                        syncSlot(player, i, baubles.getStackInSlot(i), receivers);
                }
        }

        /**
         * Optimized version of syncSlot
         * - Uses object pooling to reduce memory allocations
         * - Avoids creating new packet instances for each sync operation
         * 
         * @param player The player whose slot is being synced
         * @param slot The slot index to sync
         * @param stack The ItemStack in the slot
         * @param receivers The players who should receive this sync packet
         */
        private void syncSlot(EntityPlayer player, int slot, ItemStack stack, Collection<? extends EntityPlayer> receivers) {
                // Get a packet from the pool (or create a new one if the pool is empty)
                PacketSync pkt = PacketPool.getSyncPacket(player, slot, stack);
                
                // Send to all receivers
                for (EntityPlayer receiver : receivers) {
                        PacketHandler.INSTANCE.sendTo(pkt, (EntityPlayerMP) receiver);
                }
                
                // No need to explicitly recycle the packet here as it's done in the Handler when processing is complete
        }

        @SubscribeEvent
        public void playerDeath(PlayerDropsEvent event) {
                if (event.getEntity() instanceof EntityPlayer
                                && !event.getEntity().world.isRemote
                                && !event.getEntity().world.getGameRules().getBoolean("keepInventory")) {
                        dropItemsAt(event.getEntityPlayer(), event.getDrops(), event.getEntityPlayer());
                }
        }

        /**
         * Optimized version of dropItemsAt that improves performance during player death
         * - Reduces redundant calls to getStackInSlot
         * - Uses more efficient random generation
         * - Pre-calculates constants
         * 
         * @param player The player who died
         * @param drops The list to add dropped items to
         * @param e The entity location where items should drop
         */
        public void dropItemsAt(EntityPlayer player, List<EntityItem> drops, Entity e) {
                BaubleStorage baubles = BaublesCapabilityManager.asBaublesPlayer(player).getBaubleStorage();
                
                // Pre-calculate drop position values
                double posX = e.posX;
                double posY = e.posY + e.getEyeHeight();
                double posZ = e.posZ;
                
                // Standard pickup delay for all dropped items
                final int PICKUP_DELAY = 40;
                
                // Standard upward velocity for dropped items
                final double UPWARD_VELOCITY = 0.20000000298023224D;
                
                // Random generator
                Random rand = e.world.rand;
                
                // Process each slot that might contain baubles
                for (int i = baubles.getSize(); i >= 0; --i) {
                        // Get the item stack just once
                        ItemStack stack = baubles.getStackInSlot(i);
                        
                        if (!stack.isEmpty()) {
                                // Create entity item with a copy of the stack
                                EntityItem ei = new EntityItem(
                                        e.world,
                                        posX, 
                                        posY, 
                                        posZ,
                                        stack.copy()
                                );
                                
                                // Set pickup delay
                                ei.setPickupDelay(PICKUP_DELAY);
                                
                                // Calculate motion with pre-calculated constants
                                float magnitude = rand.nextFloat() * 0.5F;
                                float angle = rand.nextFloat() * (float) Math.PI * 2.0F;
                                
                                // Set motion components
                                ei.motionX = -MathHelper.sin(angle) * magnitude;
                                ei.motionZ = MathHelper.cos(angle) * magnitude;
                                ei.motionY = UPWARD_VELOCITY;
                                
                                // Add to drops list
                                drops.add(ei);
                                
                                // Remove from storage
                                baubles.removeItemFromSlot(i);
                        }
                }
        }

        /**
         * Optimized version of onBaubleRightClick that improves equip action performance
         * - More efficient capability check pattern
         * - Reduced nesting depth for better readability and performance
         * - Clear early-exit conditions to reduce unnecessary processing
         * 
         * @param event The right click event
         */
        @SubscribeEvent
        public void onBaubleRightClick(PlayerInteractEvent.RightClickItem event) {
                // Get basic event information
                EntityPlayer player = event.getEntityPlayer();
                ItemStack stack = event.getItemStack();
                
                // Skip processing for empty stacks or if the event is already canceled
                if (stack.isEmpty() || event.isCanceled()) {
                        return;
                }
                
                // Check if the item has bauble capability
                if (!stack.hasCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null)) {
                        return;
                }
                
                // Get the bauble capability
                IBauble bauble = stack.getCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null);
                if (bauble == null || !bauble.canRightClickEquip()) {
                        return;
                }
                
                // If the bauble can't be equipped, cancel the event but don't equip
                if (!bauble.canEquip(stack, player)) {
                        event.setCancellationResult(EnumActionResult.SUCCESS);
                        event.setCanceled(true);
                        return;
                }
                
                // Client-side: just cancel the event
                if (player.world.isRemote) {
                        event.setCancellationResult(EnumActionResult.SUCCESS);
                        event.setCanceled(true);
                        return;
                }
                
                // Server-side: add the item to baubles storage
                BaublesCapabilityManager.asBaublesPlayer(player).getBaubleStorage().addItem(stack);
                
                // If player is not in creative mode, shrink the stack
                if (!player.isDead && (!player.capabilities.isCreativeMode || !player.isCreative())) {
                        stack.shrink(1);
                }
                
                // Mark the event as successful and canceled
                event.setCancellationResult(EnumActionResult.SUCCESS);
                event.setCanceled(true);
        }
}
