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
        
        /**
         * Cleans up resources when this message is no longer needed
         * Important to prevent ByteBuf memory leaks
         */
        public void cleanup() {
                if (clientBuf != null && clientBuf.refCnt() > 0) {
                        clientBuf.release();
                        clientBuf = null;
                }
        }

        @Override
        public void write(PacketBuffer buffer) {
                BaublePlayer.Serializer.INSTANCE.writeToBuffer(playerCap, buffer);
        }

        /**
         * Optimized read method that minimizes unnecessary buffer copying
         * 
         * @param buffer The packet buffer to read from
         */
        @Override
        public void read(PacketBuffer buffer) {
                // Retain and store a reference to the buffer
                // This is more efficient than copying the entire buffer
                // The buffer will be properly managed with retain/release in the handler
                this.clientBuf = buffer.duplicate().retain();
        }

        /**
         * Optimized handler for capability synchronization
         * - Avoids unnecessary buffer copying and allocation
         * - Uses direct buffer reference where possible
         */
        public static class Handler implements IMessageHandler<S2CSyncBaubleCapMsg, IMessage> {
                @Override
                @SideOnly(Side.CLIENT)
                public IMessage onMessage(S2CSyncBaubleCapMsg message, MessageContext ctx) {
                        Minecraft.getMinecraft().addScheduledTask(() -> {
                                // Create a packet buffer directly from the client buffer without additional wrapping
                                // This avoids an unnecessary buffer copy operation
                                PacketBuffer packetBuffer = new PacketBuffer(message.clientBuf.retain());
                                
                                try {
                                        // Get the player capability
                                        IBaublePlayer baublePlayer = BaublesCapabilityManager.asBaublesPlayer(Minecraft.getMinecraft().player);
                                        // Process the buffer data
                                        BaublePlayer.Serializer.INSTANCE.readFromBuffer(baublePlayer, packetBuffer);
                                } finally {
                                        // Ensure the buffer is released to prevent memory leaks
                                        packetBuffer.release();
                                        // Clean up the message's resources
                                        message.cleanup();
                                }
                        });

                        return null;
                }
        }
}
