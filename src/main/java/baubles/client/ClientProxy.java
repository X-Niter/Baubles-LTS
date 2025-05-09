package baubles.client;

import baubles.client.gui.GuiEvents;
import baubles.client.render.BaubleRenderHandler;
import baubles.common.CommonProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Client-side proxy for registering client-only events and rendering
 */
public class ClientProxy extends CommonProxy {
    
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
    }
    
    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        
        // Register client GUI events
        MinecraftForge.EVENT_BUS.register(new GuiEvents());
        
        // Register bauble rendering handler
        MinecraftForge.EVENT_BUS.register(new BaubleRenderHandler());
    }
}
