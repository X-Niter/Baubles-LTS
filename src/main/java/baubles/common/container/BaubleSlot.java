package baubles.common.container;

import baubles.api.IBauble;
import baubles.api.cap.BaubleStackHandler;
import baubles.api.cap.BaublesCapabilityManager;
import baubles.common.event.BaubleEquipmentChangeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class BaubleSlot extends SlotItemHandler {
	private final int baubleSlot;
	private final EntityPlayer player;

	public BaubleSlot(EntityPlayer player, BaubleStackHandler itemHandler, int slot, int xPosition, int yPosition) {
		super(itemHandler, slot, xPosition, yPosition);
		this.baubleSlot = slot;
		this.player = player;
	}

	/**
	 * Check if the stack is a valid item for this slot.
	 */
	@Override
	public boolean isItemValid(@Nonnull ItemStack stack) {
		return ((BaubleStackHandler) getItemHandler()).isItemValidForSlot(baubleSlot, stack, player);
	}

	@Override
	public boolean canTakeStack(EntityPlayer player) {
		ItemStack stack = getStack();
		if (stack.isEmpty())
			return false;

		IBauble bauble = stack.getCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null);
		return bauble.canUnequip(stack, player);
	}

	@Nonnull
	@Override
	public ItemStack onTake(@Nonnull EntityPlayer playerIn, @Nonnull ItemStack stack) {
		/*if (!getHasStack() && !((IBaublesItemHandler) getItemHandler()).isEventBlocked() &&
				stack.hasCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null)) {
			stack.getCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null).onUnequipped(stack, playerIn);
		}*/
		if (!getHasStack() && stack.hasCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null))
			stack.getCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null).onUnequipped(stack, playerIn);
		super.onTake(playerIn, stack);
		MinecraftForge.EVENT_BUS.post(new BaubleEquipmentChangeEvent(playerIn, baubleSlot, stack, ItemStack.EMPTY));
		return stack;
	}

	@Override
	public void putStack(@Nonnull ItemStack stack) {
		ItemStack oldstack = getStack().copy();

		/*if (getHasStack() && !ItemStack.areItemStacksEqual(stack, getStack()) &&
				!((IBaublesItemHandler) getItemHandler()).isEventBlocked() &&
				getStack().hasCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null)) {
			getStack().getCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null).onUnequipped(getStack(), player);
		}

		if (getHasStack() && !ItemStack.areItemStacksEqual(oldstack, getStack())
				&& !((IBaublesItemHandler) getItemHandler()).isEventBlocked() &&
				getStack().hasCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null)) {
			getStack().getCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null).onEquipped(getStack(), player);
		}*/

		if (getHasStack() && !ItemStack.areItemStacksEqual(stack, getStack()) &&
				getStack().hasCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null))
			getStack().getCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null).onUnequipped(getStack(), player);

		if (getHasStack() && !ItemStack.areItemStacksEqual(oldstack, getStack()) &&
				getStack().hasCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null))
			getStack().getCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null).onEquipped(getStack(), player);

		if (!ItemStack.areItemStacksEqual(oldstack, getStack())) {
			MinecraftForge.EVENT_BUS.post(new BaubleEquipmentChangeEvent(player, baubleSlot, oldstack, getStack()));
		}

		super.putStack(stack);
	}

	@Override
	public int getSlotStackLimit() {
		return 1;
	}
}
