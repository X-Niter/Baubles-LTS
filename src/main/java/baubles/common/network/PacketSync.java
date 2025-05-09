package baubles.common.network;

import baubles.api.cap.BaublesCapabilityManager;
import baubles.common.Baubles;
import baubles.common.Config;
import baubles.common.util.PacketPool;
import baubles.common.util.StringPool;
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

/**
 * Optimized packet implementation for bauble synchronization.
 * Uses object pooling to reduce GC pressure.
 */
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

        /**
         * Constructor with string interning for baubleId to reduce memory usage
         * 
         * @param entityId The entity ID
         * @param baubleId The bauble ID string (will be interned)
         * @param slotId The slot ID
         * @param stack The item stack
         */
        public PacketSync(int entityId, String baubleId, int slotId, ItemStack stack) {
                this.entityId = entityId;
                this.slotId = slotId;
                this.stack = stack.copy();
                // Use string interning to reduce memory usage
                this.baubleId = StringPool.intern(baubleId);
        }
        
        /**
         * Reset and update this packet's values, allowing for object reuse
         * 
         * @param p The entity
         * @param slot The bauble slot
         * @param bauble The bauble stack
         * @return This packet instance for chaining
         */
        public PacketSync resetAndSetValues(EntityLivingBase p, int slot, ItemStack bauble) {
                this.slot = (byte) slot;
                this.bauble = bauble;
                this.playerId = p.getEntityId();
                return this;
        }
        
        /**
         * Clear the packet data for pooling
         */
        public void clear() {
                this.playerId = 0;
                this.slot = 0;
                this.bauble = ItemStack.EMPTY;
                this.entityId = 0;
                this.slotId = 0;
                this.stack = ItemStack.EMPTY;
                this.baubleId = null;
        }

        /**
         * Encode method with string interning optimization
         * 
         * @param msg The message to encode
         * @param buf The buffer to write to
         */
        public static void encode(PacketSync msg, PacketBuffer buf) {
                if (Config.useCurioGUI) {
                        buf.writeInt(msg.entityId);
                        // msg.baubleId is already interned in the constructor
                        buf.writeString(msg.baubleId);
                        buf.writeInt(msg.slotId);
                        buf.writeItemStack(msg.stack);
                }
        }

        /**
         * Decode method that automatically uses string interning
         * String interning happens in the PacketSync constructor
         * 
         * @param buf The buffer to read from
         * @return The decoded packet
         * @throws IOException If reading fails
         */
        public static PacketSync decode(PacketBuffer buf) throws IOException {
                int entityId = buf.readInt();
                // String interning happens in the constructor
                String baubleId = buf.readString(25);
                int slotId = buf.readInt();
                ItemStack stack = buf.readItemStack();
                
                return new PacketSync(entityId, baubleId, slotId, stack);
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
                        Minecraft.getMinecraft().addScheduledTask(() -> {
                                try {
                                        World world = Baubles.proxy.getClientWorld();
                                        if (world == null) {
                                                return;
                                        }
                                        Entity p = world.getEntityByID(message.playerId);
                                        if (p instanceof EntityPlayer) {
                                                BaublesCapabilityManager.asBaublesPlayer((EntityPlayer) p)
                                                        .getBaubleStorage()
                                                        .setStackInSlot(message.slot, message.bauble);
                                        }
                                        /*else if (p instanceof EntityLivingBase) {
                                                // TODO: might not work cuz old code
                                                //BaublesApi.getOBaublesHandler((EntityLivingBase) p).ifPresent(handler -> handler.setStackInSlot(message.baubleId, message.slotId, message.stack));
                                        }*/
                                } finally {
                                        // Return the packet to the pool when processing is complete
                                        PacketPool.recyclePacket(message);
                                }
                        });
                        return null;
                }
        }
}
