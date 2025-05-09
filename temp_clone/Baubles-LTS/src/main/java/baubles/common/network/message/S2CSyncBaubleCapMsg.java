package baubles.common.network.message;

import baubles.api.cap.BaublePlayer;
import baubles.api.cap.BaublesCapabilityManager;
import baubles.api.cap.IBaublePlayer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class S2CSyncBaubleCapMsg extends BaseMsg {
	private IBaublePlayer playerCap;
	private ByteBuf clientBuf;

	@Deprecated // pls dont use, its a must have sadly for IMessage
	public S2CSyncBaubleCapMsg() {
	}

	public S2CSyncBaubleCapMsg(IBaublePlayer playerCap) {
		this.playerCap = playerCap;
	}

	@Override
	public void write(PacketBuffer buffer) {
		BaublePlayer.Serializer.INSTANCE.writeToBuffer(playerCap, buffer);
	}

	@Override
	public void read(PacketBuffer buffer) {
		this.clientBuf = buffer.copy();
	}

	public static class Handler implements IMessageHandler<S2CSyncBaubleCapMsg, IMessage> {
		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(S2CSyncBaubleCapMsg message, MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				PacketBuffer packetBuffer = new PacketBuffer(Unpooled.wrappedBuffer(message.clientBuf));

				IBaublePlayer carpetbagPlayer = BaublesCapabilityManager.asBaublesPlayer(Minecraft.getMinecraft().player);
				BaublePlayer.Serializer.INSTANCE.readFromBuffer(carpetbagPlayer, packetBuffer);
			});

			return null;
		}
	}
}
