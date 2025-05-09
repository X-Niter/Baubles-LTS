package baubles.api.cap;

import baubles.common.Baubles;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nonnull;
import java.util.HashMap;
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

	// TODO: can make it faster? (rn is O(n))
	@Override
	public void addItem(@Nonnull ItemStack stack) {
		// Deep copy the baubles list (it's kind of a workaround I guess)
		baublesold = new BaubleStackHandler(this, baubles);
		baubles.setSize(baubles.getSlots() + 1);
		// Add old items
		for (int i = 0; i < baublesold.getSlots(); ++i)
			baubles.setStackInSlot(i, baublesold.getStackInSlot(i).copy());
		baubles.setStackInSlot(baubles.getSlots() - 2, stack.copy()); // Add new item
	}

	@Override
	public void removeItemFromSlot(int slot) {
		changed.remove(baubles.getSlots());
		// Deep copy the baubles list (it's kind of a workaround I guess)
		baublesold = new BaubleStackHandler(this, baubles);
		baubles.setSize(baubles.getSlots() - 1);
		// Add old items, except slot we are removing
		for (int i = 0; i < baublesold.getSlots(); ++i) {
			if (i == slot) continue;
			baubles.setStackInSlot(i, baublesold.getStackInSlot(i).copy());
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
		// Deep copy the baubles list (it's kind of a workaround I guess)
		baublesold = new BaubleStackHandler(this, baubles);
		baubles.setSize(size);
		// Add old items
		for (int i = 0, j = 0; i < baubles.getSlots() && j < baublesold.getSlots(); ++i, ++j) {
			while (j < baublesold.getSlots() - 1 && baublesold.getStackInSlot(j).isEmpty()) ++j;
			baubles.setStackInSlot(i, baublesold.getStackInSlot(j).copy());
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
		// Deep copy the baubles list (it's kind of a workaround I guess)
		baublesold = new BaubleStackHandler(this, baubles);
		baubles.setSize(baubles.getSlots() + 1);
		// Add old items
		for (int i = 0; i < baublesold.getSlots(); ++i)
			baubles.setStackInSlot(i, baublesold.getStackInSlot(i).copy());
	}

	@Override
	public int getSize() {
		return baubles.getSlots() - 1;
	}

	@Override
	public int getActualSize() {
		return baubles.getSlots();
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack, EntityLivingBase player) {
		if (stack == null || stack.isEmpty() || !stack.hasCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null)) {
			return false;
		}
		return stack.getCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null).canEquip(stack, player);
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
