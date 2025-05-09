package baubles.client;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.client.gui.GuiEvents;
import baubles.client.gui.GuiPlayerExpanded;
import baubles.common.Baubles;
import baubles.common.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.util.Map;

public class ClientProxy extends CommonProxy {

	public static final KeyBinding KEY_BAUBLES = new KeyBinding("keybind.baublesinventory", Keyboard.KEY_B, "key.categories.inventory");

	@SubscribeEvent
	public static void onTextureStitch(TextureStitchEvent.Pre evt) {
		TextureMap map = evt.getMap();
		BaubleType.processIcons();

		for (ResourceLocation resource : BaublesApi.getIcons().values()) {
			map.registerSprite(resource);
		}
		map.registerSprite(new ResourceLocation("curios:item/empty_generic_slot"));
	}

	@Override
	public void registerEventHandlers() {
		super.registerEventHandlers();

		ClientRegistry.registerKeyBinding(KEY_BAUBLES);

		MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
		MinecraftForge.EVENT_BUS.register(new GuiEvents());
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (world instanceof WorldClient) {
			if (ID == Baubles.GUI) {
				return new GuiPlayerExpanded(player);
			}
		}
		return null;
	}

	@Override
	public World getClientWorld() {
		return FMLClientHandler.instance().getClient().world;
	}

	@Override
	public void init() {
		Map<String, RenderPlayer> skinMap = Minecraft.getMinecraft().getRenderManager().getSkinMap();
		RenderPlayer render;
		render = skinMap.get("default");
		render.addLayer(new BaublesRenderLayer());

		render = skinMap.get("slim");
		render.addLayer(new BaublesRenderLayer());


		BaublesApi.registerIcon("amulet", new ResourceLocation(Baubles.MODID, "item/empty_amulet_slot"));
		BaublesApi.registerIcon("ring", new ResourceLocation(Baubles.MODID, "item/empty_ring_slot"));
		BaublesApi.registerIcon("belt", new ResourceLocation(Baubles.MODID, "item/empty_belt_slot"));
		BaublesApi.registerIcon("trinket", new ResourceLocation(Baubles.MODID, "item/empty_trinket_slot"));
		BaublesApi.registerIcon("head", new ResourceLocation(Baubles.MODID, "item/empty_head_slot"));
		BaublesApi.registerIcon("body", new ResourceLocation(Baubles.MODID, "item/empty_body_slot"));
		BaublesApi.registerIcon("charm", new ResourceLocation(Baubles.MODID, "item/empty_charm_slot"));

	}
}
