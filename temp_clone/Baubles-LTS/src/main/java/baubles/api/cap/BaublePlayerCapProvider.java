package baubles.api.cap;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;

public class BaublePlayerCapProvider implements ICapabilitySerializable<NBTBase> {
	private final IBaublePlayer capInstance = BaublesCapabilityManager.getBaublePlayerCap().getDefaultInstance();

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
		return capability == BaublesCapabilityManager.getBaublePlayerCap();
	}

	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
		Capability<IBaublePlayer> playerCap = BaublesCapabilityManager.getBaublePlayerCap();
		return capability == playerCap ? playerCap.cast(capInstance) : null;
	}

	@Override
	public NBTBase serializeNBT() {
		Capability<IBaublePlayer> playerCap = BaublesCapabilityManager.getBaublePlayerCap();

		return playerCap.getStorage().writeNBT(playerCap, capInstance, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		Capability<IBaublePlayer> playerCap = BaublesCapabilityManager.getBaublePlayerCap();
		playerCap.getStorage().readNBT(playerCap, capInstance, null, nbt);
	}
}
