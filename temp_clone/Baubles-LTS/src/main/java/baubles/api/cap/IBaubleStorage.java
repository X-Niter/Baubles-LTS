package baubles.api.cap;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public interface IBaubleStorage {
	void setStackInSlot(int slot, @Nonnull ItemStack stack);

	ItemStack getStackInSlot(int slot);

	void addItem(ItemStack stack);

	void removeItemFromSlot(int slot);

	ItemStack extractItem(int slot, int count, boolean simulate);

	boolean isChanged(int slot);

	void setChanged(int slot, boolean val);

	void setSizeWithoutEmpty(int size);

	void update();

	void addEmptySlot();

	int getSize();

	int getActualSize();

	boolean isItemValidForSlot(int slot, ItemStack stack, EntityLivingBase player);

	BaubleStackHandler getBaubles();
}
