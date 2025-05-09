package baubles.common;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

public class Config {

	public static Configuration config;
	public static boolean renderBaubles = true;

	public static boolean useCurioGUI = true;

	public static void initialize(File file) {
		config = new Configuration(file);
		config.load();

		load();

		MinecraftForge.EVENT_BUS.register(ConfigChangeListener.class);
	}

	public static void load() {
		String renderBaublesDesc = "Set this to false to disable rendering of baubles on the player.";
		renderBaubles = config.getBoolean("baubleRender.enabled", Configuration.CATEGORY_CLIENT, renderBaubles, renderBaublesDesc);

		String useCurioGUIDesc = "Set to false to disable the Curios GUI/Backport features(AKA disables the scrollable gui that allows wearing more then 7 trinkets)";
		useCurioGUI = config.getBoolean("curiosGUI.enabled", Configuration.CATEGORY_CLIENT, useCurioGUI, useCurioGUIDesc);

		if (config.hasChanged()) config.save();
	}

	public static void save() {
		config.save();
	}

	public static class ConfigChangeListener {
		@SubscribeEvent
		public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
			if (eventArgs.getModID().equals(Baubles.MODID))
				load();
		}
	}
}
