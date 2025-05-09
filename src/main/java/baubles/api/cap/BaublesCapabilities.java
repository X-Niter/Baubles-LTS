package baubles.api.cap;

import baubles.api.IBauble;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

/**
 * Capability registration for Baubles
 */
public class BaublesCapabilities {
    
    /**
     * Capability for the baubles inventory handler
     */
    @CapabilityInject(IBaublesItemHandler.class)
    public static final Capability<IBaublesItemHandler> CAPABILITY_BAUBLES = null;
    
    /**
     * Capability for items that can be equipped as baubles
     */
    @CapabilityInject(IBauble.class)
    public static final Capability<IBauble> CAPABILITY_ITEM_BAUBLE = null;
    
    /**
     * Register all capabilities for baubles
     */
    public static void register() {
        // Register the baubles inventory capability
        CapabilityManager.INSTANCE.register(IBaublesItemHandler.class, 
                new Capability.IStorage<IBaublesItemHandler>() {
                    @Override
                    public NBTBase writeNBT(Capability<IBaublesItemHandler> capability, 
                            IBaublesItemHandler instance, EnumFacing side) {
                        throw new UnsupportedOperationException("Not implemented");
                    }

                    @Override
                    public void readNBT(Capability<IBaublesItemHandler> capability, 
                            IBaublesItemHandler instance, EnumFacing side, NBTBase nbt) {
                        throw new UnsupportedOperationException("Not implemented");
                    }
                }, 
                () -> { throw new UnsupportedOperationException("Not implemented"); });
        
        // Register the IBauble capability for items
        CapabilityManager.INSTANCE.register(IBauble.class, 
                new Capability.IStorage<IBauble>() {
                    @Override
                    public NBTBase writeNBT(Capability<IBauble> capability, 
                            IBauble instance, EnumFacing side) {
                        return null;
                    }

                    @Override
                    public void readNBT(Capability<IBauble> capability, 
                            IBauble instance, EnumFacing side, NBTBase nbt) {
                    }
                }, 
                () -> { throw new UnsupportedOperationException("Not implemented"); });
    }
}