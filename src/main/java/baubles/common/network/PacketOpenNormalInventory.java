package baubles.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Packet sent when player clicks the button to switch back to normal inventory
 */
public class PacketOpenNormalInventory implements IMessage {
    
    public PacketOpenNormalInventory() {}
    
    @Override
    public void toBytes(ByteBuf buf) {}
    
    @Override
    public void fromBytes(ByteBuf buf) {}
    
    public static class Handler implements IMessageHandler<PacketOpenNormalInventory, IMessage> {
        
        @Override
        public IMessage onMessage(final PacketOpenNormalInventory message, final MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            
            player.getServerWorld().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    // Open normal inventory
                    player.openContainer.onContainerClosed(player);
                    player.openContainer = player.inventoryContainer;
                }
            });
            
            return null;
        }
    }
}
