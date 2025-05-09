package baubles.common;

import baubles.api.BaubleType;
import baubles.api.cap.BaublesCapabilityManager;
import baubles.common.event.CommandBaubles;
import baubles.common.network.PacketHandler;
import baubles.common.util.PacketPool;
import baubles.common.util.StringPool;
import baubles.common.util.TypeCache;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(
                modid = Baubles.MODID,
                name = Baubles.MODNAME,
                version = Baubles.VERSION,
                guiFactory = "baubles.client.gui.BaublesGuiFactory",
                dependencies = "required-after:forge@[14.23.5.2860,);")
public class Baubles {

        public static final String MODID = "baubles";
        public static final String MODNAME = "Baubles LTS";
        public static final String VERSION = "1.12.2-1.0.7.0-LTS";
        public static final Logger log = LogManager.getLogger(MODID.toUpperCase());
        public static final int GUI = 0;
        @SidedProxy(clientSide = "baubles.client.ClientProxy", serverSide = "baubles.common.CommonProxy")
        public static CommonProxy proxy;
        @Instance(value = Baubles.MODID)
        public static Baubles instance;
        public File modDir;

        private static void send(String id, Object msg) {
                FMLInterModComms.sendMessage(MODID, id, msg.toString());
        }

        @EventHandler
        public void preInit(FMLPreInitializationEvent event) {
                modDir = event.getModConfigurationDirectory();

                try {
                        Config.initialize(event.getSuggestedConfigurationFile());
                } catch (Exception e) {
                        Baubles.log.fatal("Baubles has a problem loading it's configuration");
                } finally {
                        if (Config.config != null) {
                                Config.save();
                        }
                }


                // Init the capabilities
                BaublesCapabilityManager.init();
                // TODO: might delete
                //CapabilityManager.INSTANCE.register(IBaublesItemHandler.class, new CapabilityBaubles<>(), BaublesContainer::new);

                // TODO: might delete
                /*CapabilityManager.INSTANCE.register
                                (
                                                IBauble.class,
                                                new BaublesCapabilities.CapabilityItemBaubleStorage(),
                                                () -> new BaubleItem(BaubleType.TRINKET)
                                );*/


                proxy.registerEventHandlers();
                PacketHandler.init();

                Config.save();
        }

        /**
         * Initialize the mod systems including performance optimizations
         * - Initializes string pooling for network packets
         * - Prepares packet pooling system
         * - Sets up type caching for bauble validation
         */
        @EventHandler
        public void init(FMLInitializationEvent evt) {
                // Basic initialization
                NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
                proxy.init();
                
                // Initialize performance optimization systems
                initPerformanceOptimizations();
        }
        
        /**
         * Initialize all performance optimization systems
         * This method centralizes the initialization of all performance-related
         * optimizations added in the LTS version
         */
        private void initPerformanceOptimizations() {
                // Log startup information about optimizations
                log.info("Initializing Baubles LTS performance optimizations");
                
                // Initialize string interning system
                StringPool.clear();
                log.info("String interning system initialized");
                
                // Initialize packet pooling system
                PacketPool.clearPool();
                PacketPool.resetStats();
                log.info("Packet pooling system initialized");
                
                // Initialize type caching system
                TypeCache.clearCache();
                
                // Enable stats tracking in debug mode
                if (Config.debug) {
                    log.info("Debug mode enabled: performance statistics tracking activated");
                    TypeCache.setStatsTracking(true);
                }
                
                log.info("Performance optimizations initialized successfully");
        }

        @EventHandler
        public void serverLoad(FMLServerStartingEvent event) {
                event.registerServerCommand(new CommandBaubles());
        }

        private void process(FMLInterModComms.IMCEvent evt) {
                BaubleType.processBaubleTypes(evt.getMessages().stream(), evt.getMessages().stream());
        }
}
