package baubles.api.cap;

import baubles.api.BaubleType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * Interface for the capability handler for baubles inventory
 */
public interface IBaublesItemHandler extends IItemHandlerModifiable {
    
    /**
     * Returns true if the bauble can be placed in the given slot
     */
    public boolean isItemValidForSlot(int slot, ItemStack stack, EntityLivingBase player);
    
    /**
     * Returns the number of bauble inventory slots available
     */
    @Override
    public int getSlots();
    
    /**
     * Returns the stack in the given slot
     */
    @Override
    public ItemStack getStackInSlot(int slot);
    
    /**
     * Set the stack in the given slot
     */
    @Override
    public void setStackInSlot(int slot, ItemStack stack);
    
    /**
     * Attempt to insert the stack into the given slot. Returns any leftover stack.
     */
    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate);
    
    /**
     * Attempt to extract items from the given slot. Returns the extracted stack.
     */
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate);
    
    /**
     * Returns the maximum stack size allowed in the given slot
     */
    @Override
    public int getSlotLimit(int slot);
    
    /**
     * Returns the type of bauble that fits in the given slot
     */
    public BaubleType getBaubleType(int slot);
    
    /**
     * Adds a bauble to the first available slot
     * @param stack the bauble to add
     * @param player the player
     * @return null if fully inserted, or a remainder stack
     */
    public ItemStack addItem(ItemStack stack, EntityLivingBase player);
}