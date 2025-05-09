package baubles.api.cap;

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

	public boolean isItemValidForSlot(int slot, ItemStack stack, EntityLivingBase player) {
		if (stack == null || stack.isEmpty() || !stack.hasCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null)) {
			return false;
		}
		return stack.getCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null).canEquip(stack, player);
	}

	public void clearEmtpySlots() {
		int toRemove = -1; // Last index is always empty, but we dont want to remove that or we crash
		for (ItemStack stack : stacks) {
			if (stack.isEmpty()) ++toRemove;
		}
		storage.setSizeWithoutEmpty(getSlots() - toRemove);
	}
}
