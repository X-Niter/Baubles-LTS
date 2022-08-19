package baubles.common.network;

import baubles.common.Baubles;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketOpenBaublesInventory implements IMessage, IMessageHandler<PacketOpenBaublesInventory, IMessage> {

	private float oldMouseX;
	private float oldMouseY;

	public PacketOpenBaublesInventory() {
	}

	public PacketOpenBaublesInventory(float oldMouseX, float oldMouseY) {
		this.oldMouseX = oldMouseX;
		this.oldMouseY = oldMouseY;
	}

	public static void encode(PacketOpenBaublesInventory msg, PacketBuffer buf) {
		buf.writeFloat(msg.oldMouseX);
		buf.writeFloat(msg.oldMouseY);
	}

	public static PacketOpenBaublesInventory decode(PacketBuffer buf) {
		return new PacketOpenBaublesInventory(buf.readFloat(), buf.readFloat());
	}

	@Override
	public void toBytes(ByteBuf buffer) {
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
	}

	@Override
	public IMessage onMessage(PacketOpenBaublesInventory message, MessageContext ctx) {
		IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.world;
		mainThread.addScheduledTask(new Runnable() {
			public void run() {
				EntityPlayerMP sender = ctx.getServerHandler().player;

				if (sender != null) {
					ctx.getServerHandler().player.openContainer.onContainerClosed(ctx.getServerHandler().player);
					ctx.getServerHandler().player.openGui(Baubles.instance, Baubles.GUI, ctx.getServerHandler().player.world, 0, 0, 0);
				}
			}
		});
		return null;
	}
}
