package baubles.common.network;

import baubles.api.cap.BaublesCapabilityManager;
import baubles.common.Baubles;
import baubles.common.Config;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;

public class PacketSync implements IMessage {

	int playerId;
	byte slot = 0;
	ItemStack bauble;
	private int entityId;
	private int slotId;
	private ItemStack stack;
	private String baubleId;

	public PacketSync() {
	}

	public PacketSync(EntityLivingBase p, int slot, ItemStack bauble) {
		this.slot = (byte) slot;
		this.bauble = bauble;
		this.playerId = p.getEntityId();
	}

	public PacketSync(int entityId, String baubleId, int slotId, ItemStack stack) {
		this.entityId = entityId;
		this.slotId = slotId;
		this.stack = stack.copy();
		this.baubleId = baubleId;
	}

	public static void encode(PacketSync msg, PacketBuffer buf) {
		if (Config.useCurioGUI) {
			buf.writeInt(msg.entityId);
			buf.writeString(msg.baubleId);
			buf.writeInt(msg.slotId);
			buf.writeItemStack(msg.stack);
		}
	}

	public static PacketSync decode(PacketBuffer buf) throws IOException {
		return new PacketSync(buf.readInt(), buf.readString(25), buf.readInt(), buf.readItemStack());
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeInt(playerId);
		buffer.writeByte(slot);
		ByteBufUtils.writeItemStack(buffer, bauble);
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		playerId = buffer.readInt();
		slot = buffer.readByte();
		bauble = ByteBufUtils.readItemStack(buffer);
	}

	public static class Handler implements IMessageHandler<PacketSync, IMessage> {
		@Override
		public IMessage onMessage(PacketSync message, MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(new Runnable() {
				public void run() {
					World world = Baubles.proxy.getClientWorld();
					if (world == null) {
						return;
					}
					Entity p = world.getEntityByID(message.playerId);
					if (p instanceof EntityPlayer) {
						BaublesCapabilityManager.asBaublesPlayer((EntityPlayer) p).getBaubleStorage().setStackInSlot(message.slot, message.bauble);
					}
				/*else if (p instanceof EntityLivingBase) {
					// TODO: might not work cuz old code
					//BaublesApi.getOBaublesHandler((EntityLivingBase) p).ifPresent(handler -> handler.setStackInSlot(message.baubleId, message.slotId, message.stack));
				}*/
				}
			});
			return null;
		}
	}
}
