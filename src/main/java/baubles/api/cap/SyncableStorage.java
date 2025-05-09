package baubles.api.cap;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.capabilities.Capability;

public abstract class SyncableStorage<T, IMPL> implements Capability.IStorage<T> {
        private final Class<IMPL> implClass;

        public SyncableStorage(Class<IMPL> implClass) {
                this.implClass = implClass;
        }

        /**
         * Optimized implementation validation
         * - Uses direct class comparison first for better performance
         * - Falls back to instanceof check if needed
         * 
         * @param instance The instance to validate
         * @return The validated implementation cast to the correct type
         * @throws IllegalArgumentException if the instance is not of the expected implementation type
         */
        protected IMPL validateDefaultImpl(T instance) {
                // Fast path - direct class equality check is much faster than instanceof
                if (instance != null && instance.getClass() == implClass) {
                        return (IMPL) instance;
                }
                
                // Slower path - use instanceof for compatibility with subclasses
                if (instance != null && implClass.isInstance(instance)) {
                        return (IMPL) instance;
                }
                
                // Failed validation
                throw new IllegalArgumentException("Can't work with instance that isn't the default implementation");
        }

        public abstract void writeToBuffer(T instance, PacketBuffer buffer);

        public abstract void readFromBuffer(T instance, PacketBuffer buffer);

        public abstract void copy(T from, T to);
}