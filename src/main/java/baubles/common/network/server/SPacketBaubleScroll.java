package baubles.common.network.server;

import baubles.common.Config;
import baubles.common.container.ContainerPlayerExpanded;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SPacketBaubleScroll implements IMessage, IMessageHandler<SPacketBaubleScroll, IMessage> {

	private int windowId;
	private int index;

	public SPacketBaubleScroll() {
		//this.windowId = windowId;
		//this.index = index;
	}

	public SPacketBaubleScroll(int windowId, int index) {
		this.windowId = windowId;
		this.index = index;
	}

	public static void encode(SPacketBaubleScroll msg, PacketBuffer buf) {
		buf.writeInt(msg.windowId);
		buf.writeInt(msg.index);
	}

	public static SPacketBaubleScroll decode(PacketBuffer buf) {
		return new SPacketBaubleScroll(buf.readInt(), buf.readInt());
	}

	@Override
	public void toBytes(ByteBuf buffer) {
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
	}

	public IMessage onMessage(SPacketBaubleScroll message, MessageContext ctx) {
		IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.world;
		EntityPlayer packetPlayer = Minecraft.getMinecraft().player;
		Container container = packetPlayer.openContainer;

		mainThread.addScheduledTask((new Runnable() {
			public void run() {

				if (container instanceof ContainerPlayerExpanded && container.windowId == message.windowId) {
					if (Config.useCurioGUI) {
						((ContainerPlayerExpanded) container).scrollToIndex(message.index);
					}
				}

			}
		}));
		return null;
	}
}
