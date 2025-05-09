package baubles.common.network;

import baubles.common.Baubles;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Packet sent when player clicks the baubles button to open the baubles inventory
 */
public class PacketOpenBaublesInventory implements IMessage {
    
    public PacketOpenBaublesInventory() {}
    
    @Override
    public void toBytes(ByteBuf buf) {}
    
    @Override
    public void fromBytes(ByteBuf buf) {}
    
    public static class Handler implements IMessageHandler<PacketOpenBaublesInventory, IMessage> {
        
        @Override
        public IMessage onMessage(final PacketOpenBaublesInventory message, final MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            
            player.getServerWorld().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    // Open the player's baubles inventory
                    player.openContainer.onContainerClosed(player);
                    player.openGui(Baubles.instance, 0, player.world, (int)player.posX, (int)player.posY, (int)player.posZ);
                }
            });
            
            return null;
        }
    }
}
