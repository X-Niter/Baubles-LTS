package baubles.common.network;

import baubles.common.Baubles;
import baubles.common.network.message.S2CSyncBaubleCapMsg;
import baubles.common.network.server.SPacketBaubleScroll;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Baubles.MODID.toLowerCase());

	private static int id = 0;

	public static void init() {
		registerMessage(PacketOpenBaublesInventory.class, PacketOpenBaublesInventory.class, Side.SERVER);
		registerMessage(PacketOpenNormalInventory.class, PacketOpenNormalInventory.class, Side.SERVER);
		registerMessage(PacketSync.class, PacketSync.Handler.class, Side.CLIENT);


		registerMessage(SPacketBaubleScroll.class, SPacketBaubleScroll.class, Side.SERVER);

		registerMessage(S2CSyncBaubleCapMsg.class, S2CSyncBaubleCapMsg.Handler.class, Side.CLIENT);
	}

	private static <REQ extends IMessage, REPLY extends IMessage> void registerMessage(Class<REQ> packetClass, Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Side side) {
		INSTANCE.registerMessage(messageHandler, packetClass, id++, side);
	}

	private static <REQ extends IMessage, REPLY extends IMessage> void registerMessage(Class<REQ> packetClass, IMessageHandler<? super REQ, ? extends REPLY> messageHandler, Side side) {
		INSTANCE.registerMessage(messageHandler, packetClass, id++, side);
	}
}
