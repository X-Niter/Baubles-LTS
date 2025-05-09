package baubles.api.cap;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTBase;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.INBTSerializable;

public abstract class SerializableInnerCap<NBT extends NBTBase, T extends SerializableInnerCap<NBT, T>> implements INBTSerializable<NBT> {
        public abstract void writeToBuffer(PacketBuffer buffer);

        public abstract void readFromBuffer(PacketBuffer buffer);

        /**
         * Optimized copy method that uses NBT serialization directly when possible
         * Falls back to buffer-based copying when direct method isn't viable
         * 
         * @param to The target instance to copy to
         */
        public void copy(T to) {
                // First try to use direct NBT-based copying which is more efficient
                // than going through the buffer serialization process
                try {
                        NBT nbt = this.serializeNBT();
                        if (nbt != null) {
                                to.deserializeNBT(nbt);
                                return;
                        }
                } catch (Exception ignored) {
                        // If direct NBT copying fails for any reason, fall back to buffer method
                }
                
                // Fall back to original buffer-based method if needed
                PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
                writeToBuffer(buffer);
                to.readFromBuffer(buffer);
        }
}
