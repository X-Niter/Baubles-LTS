package baubles.api.cap;

import baubles.api.util.WrongSideException;
import baubles.common.network.PacketHandler;
import baubles.common.network.message.S2CSyncBaubleCapMsg;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import java.util.UUID;

public class BaublePlayer implements IBaublePlayer {
	private final BaubleStorage baubleStorage;

	/**
	 * If equals null, then it is on client, otherwise - on server.
	 */
	private UUID playerId = null;

	public BaublePlayer(BaubleStorage bs) {
		baubleStorage = bs;
	}

	void bindPlayer(EntityPlayerMP player) {
		playerId = player.getPersistentID();
	}


	@Override
	public BaubleStorage getBaubleStorage() {
		return baubleStorage;
	}

	@Override
	public void onTick(Side side) {

	}

	@Override
	public void sendUpdates() {
		if (playerId == null) {
			throw new WrongSideException(Side.CLIENT);
		}

		EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(playerId);
		PacketHandler.INSTANCE.sendTo(new S2CSyncBaubleCapMsg(this), player);
	}

	@Override
	public String toString() {
		return TextFormatting.YELLOW + "Baubles: " + baubleStorage.serializeNBT();
	}

	public static class Serializer extends SyncableStorage<IBaublePlayer, BaublePlayer> {
		public static final Serializer INSTANCE = new Serializer();

		public Serializer() {
			super(BaublePlayer.class);
		}

		@Nullable
		@Override
		public NBTBase writeNBT(Capability<IBaublePlayer> capability, IBaublePlayer instance, EnumFacing side) {
			BaublePlayer playerCap = validateDefaultImpl(instance);

			NBTTagCompound compound = new NBTTagCompound();
			compound.setTag("bauble_storage", playerCap.baubleStorage.serializeNBT());

			return compound;
		}

		@Override
		public void readNBT(Capability<IBaublePlayer> capability, IBaublePlayer instance, EnumFacing side, NBTBase nbt) {
			BaublePlayer playerCap = validateDefaultImpl(instance);

			NBTTagCompound compound = ((NBTTagCompound) nbt);

			if (compound.hasKey("bauble_storage"))
				playerCap.baubleStorage.deserializeNBT(compound.getTag("mana_storage"));
		}

		@Override
		public void writeToBuffer(IBaublePlayer instance, PacketBuffer buffer) {
			BaublePlayer playerCap = validateDefaultImpl(instance);

			playerCap.baubleStorage.writeToBuffer(buffer);
		}

		@Override
		public void readFromBuffer(IBaublePlayer instance, PacketBuffer buffer) {
			BaublePlayer playerCap = validateDefaultImpl(instance);

			playerCap.baubleStorage.readFromBuffer(buffer);
		}

		@Override
		public void copy(IBaublePlayer from, IBaublePlayer to) {
			PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());

			writeToBuffer(from, buffer);
			readFromBuffer(to, buffer);
		}
	}
}
