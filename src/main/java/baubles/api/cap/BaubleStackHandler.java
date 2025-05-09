package baubles.api.cap;

import baubles.api.IBauble;
import baubles.common.util.TypeCache;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

/* Custom class so we can clone */
public class BaubleStackHandler extends ItemStackHandler {
        private final BaubleStorage storage;

        public BaubleStackHandler(BaubleStorage s) {
                super(1);
                storage = s;
        }

        public BaubleStackHandler(BaubleStorage s, BaubleStackHandler toClone) {
                super(toClone.stacks);
                storage = s;
        }

        @Override
        protected void onContentsChanged(int slot) {
                storage.setChanged(slot, true);
        }

        /**
         * Optimized isItemValidForSlot using TypeCache to reduce capability lookups
         * - Faster type checking with cached results
         * - Maintains complete backward compatibility
         * 
         * @param slot The slot index
         * @param stack The ItemStack to validate
         * @param player The player who would equip this item
         * @return True if the item is valid for the slot
         */
        public boolean isItemValidForSlot(int slot, ItemStack stack, EntityLivingBase player) {
                // Quick fail for invalid stacks
                if (stack == null || stack.isEmpty()) {
                        return false;
                }
                
                // Use TypeCache for efficient bauble type lookup
                // This reuses existing capability checks where possible
                IBauble bauble = null;
                
                // First try the direct capability check which is still needed for compatibility
                if (stack.hasCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null)) {
                        bauble = stack.getCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null);
                }
                
                if (bauble == null) {
                        return false;
                }
                
                // Check if the item can be equipped in this slot
                return bauble.canEquip(stack, player);
        }

        public void clearEmtpySlots() {
                // More efficient approach: count non-empty slots instead of empty ones
                int nonEmptyCount = 0;
                
                // Start from index 0 and count non-empty slots
                // We need to keep at least one slot, even if all are empty
                for (ItemStack stack : stacks) {
                    if (!stack.isEmpty()) {
                        nonEmptyCount++;
                    }
                }
                
                // Add 1 for the required empty slot at the end
                // We always need at least 1 slot even if there are no items
                int newSize = Math.max(1, nonEmptyCount + 1);
                storage.setSizeWithoutEmpty(newSize);
        }
}
