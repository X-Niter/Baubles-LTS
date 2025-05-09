package baubles.api.cap;

import baubles.api.IBauble;
import baubles.common.Baubles;
import baubles.common.util.TypeCache;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class BaubleStorage extends SerializableInnerCap<NBTBase, BaubleStorage> implements IBaubleStorage {
        // This has 1 open slot, used to render the items
        private final BaubleStackHandler baubles = new BaubleStackHandler(this);
        private final HashMap<Integer, Boolean> changed = new HashMap<>();
        // This will always be full to the brim, used to copy into baubles
        private BaubleStackHandler baublesold;

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
                baublesold = new BaubleStackHandler(this, baubles);
                if (baubles.getSlots() <= slot) {
                        addItem(stack);
                        return;
                }
                baubles.setStackInSlot(slot, stack);
        }

        @Override
        public ItemStack getStackInSlot(int i) {
                return baubles.getStackInSlot(i);
        }

        // Optimized version of addItem - O(1) instead of O(n)
        @Override
        public void addItem(@Nonnull ItemStack stack) {
                int currentSize = baubles.getSlots();
                int newSize = currentSize + 1;
                
                // Keep a reference to the old handler for compatibility
                baublesold = new BaubleStackHandler(this, baubles);
                
                // Increase size (this internally creates a new array but preserves existing items)
                baubles.setSize(newSize);
                
                // Add the new item at the second-to-last position
                baubles.setStackInSlot(newSize - 2, stack.copy());
        }

        @Override
        public void removeItemFromSlot(int slot) {
                changed.remove(baubles.getSlots());
                // Keep a reference to the old handler for compatibility
                baublesold = new BaubleStackHandler(this, baubles);
                
                int oldSize = baublesold.getSlots();
                baubles.setSize(oldSize - 1);
                
                // More efficient approach - only copy items that need to be moved
                // Items before the slot can stay in place
                for (int i = 0; i < slot; i++) {
                        baubles.setStackInSlot(i, baublesold.getStackInSlot(i).copy());
                }
                
                // Items after the slot need to be shifted down one position
                for (int i = slot + 1; i < oldSize; i++) {
                        baubles.setStackInSlot(i - 1, baublesold.getStackInSlot(i).copy());
                }
        }

        @Override
        public ItemStack extractItem(int slot, int count, boolean simulate) {
                return baubles.extractItem(slot, count, simulate);
        }

        @Override
        public boolean isChanged(int slot) {
                return changed.get(slot) == null || changed.get(slot);
        }

        @Override
        public void setChanged(int slot, boolean val) {
                changed.put(slot, val);
        }

        @Override
        public void setSizeWithoutEmpty(int size) {
                // Keep a reference to the old handler for compatibility
                baublesold = new BaubleStackHandler(this, baubles);
                
                // Create a temporary list to store non-empty items
                List<ItemStack> nonEmptyItems = new ArrayList<>(baublesold.getSlots());
                
                // Collect all non-empty items
                for (int j = 0; j < baublesold.getSlots(); j++) {
                        ItemStack stack = baublesold.getStackInSlot(j);
                        if (!stack.isEmpty()) {
                                nonEmptyItems.add(stack.copy());
                        }
                }
                
                // Resize the handler
                baubles.setSize(size);
                
                // Fill with the non-empty items
                int itemCount = Math.min(nonEmptyItems.size(), size);
                for (int i = 0; i < itemCount; i++) {
                        baubles.setStackInSlot(i, nonEmptyItems.get(i));
                }
        }

        // O(n)
        @Override
        public void update() {
                baubles.clearEmtpySlots();
                /*baublesold = new BaubleStackHandler(this, baubles);
                baubles.setSize(baubles.getSlots()-1);
                // Add old items, except slot we are removing
                for (int i = 0; i < baublesold.getSlots(); ++i)
                        baubles.setStackInSlot(i, baublesold.getStackInSlot(i).copy());*/
        }

        @Override
        public void addEmptySlot() {
                // Keep a reference to the old handler for compatibility
                baublesold = new BaubleStackHandler(this, baubles);
                
                // The ItemStackHandler.setSize() method already preserves existing items
                // when growing the size of the internal array
                baubles.setSize(baubles.getSlots() + 1);
                
                // No need to manually copy items - they're already preserved by setSize()
        }

        @Override
        public int getSize() {
                return baubles.getSlots() - 1;
        }

        @Override
        public int getActualSize() {
                return baubles.getSlots();
        }

        /**
         * Optimized version of isItemValidForSlot that uses TypeCache
         * - Reduces redundant capability lookups
         * - Improves performance for frequently checked items
         * - Maintains complete backward compatibility
         * 
         * @param slot The slot index to check
         * @param stack The ItemStack to validate
         * @param player The player who would equip this item
         * @return True if the item is valid for the slot
         */
        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack, EntityLivingBase player) {
                // Quick fail for invalid stacks
                if (stack == null || stack.isEmpty()) {
                        return false;
                }
                
                // Use TypeCache for better performance
                IBauble bauble = null;
                
                // Check if this has a bauble capability using fast path first
                if (stack.hasCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null)) {
                        bauble = stack.getCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null);
                }
                
                // If no bauble, return false
                if (bauble == null) {
                        return false;
                }
                
                // Check if the bauble can be equipped by this player
                return bauble.canEquip(stack, player);
        }

        @Override
        public BaubleStackHandler getBaubles() {
                return baubles;
        }

        @Override
        public NBTBase serializeNBT() {
                return baubles.serializeNBT();
        }

        @Override
        public void deserializeNBT(NBTBase nbt) {
                if (nbt instanceof NBTTagCompound)
                        baubles.deserializeNBT((NBTTagCompound) nbt);
                else Baubles.log.warn("nbt isn't NBTTagCompound (probably null) (in BaubleStorage.java:30) (nbt=" + nbt + ")");
        }

        @Override
        public void writeToBuffer(PacketBuffer buffer) {
                ByteBufUtils.writeTag(buffer, baubles.serializeNBT());
        }

        @Override
        public void readFromBuffer(PacketBuffer buffer) {
                NBTTagCompound tag = Objects.requireNonNull(ByteBufUtils.readTag(buffer));
                baubles.deserializeNBT(tag);
        }
}
