package baubles.api.cap;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.common.Baubles;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber
public class BaublesCapabilityManager {
	public static final ResourceLocation BAUBLE_PLAYER_CAP = new ResourceLocation(Baubles.MODID, "baubles_player");
	@CapabilityInject(IBauble.class)
	public static final Capability<IBauble> CAPABILITY_ITEM_BAUBLE = null;
	@CapabilityInject(IBaublePlayer.class)
	private static final Capability<IBaublePlayer> BAUBLE_PLAYER = null;

	public static IBaublePlayer asBaublesPlayer(EntityPlayer player) {
		return player.getCapability(getBaublePlayerCap(), null);
	}

	@Nonnull
	public static Capability<IBaublePlayer> getBaublePlayerCap() {
		return BAUBLE_PLAYER;
	}

	public static void init() {
		CapabilityManager.INSTANCE.register(IBaublePlayer.class, BaublePlayer.Serializer.INSTANCE, () -> new BaublePlayer(new BaubleStorage()));
		CapabilityManager.INSTANCE.register(IBauble.class, new CapabilityItemBaubleStorage(), () -> new BaubleItem(BaubleType.TRINKET));
	}

	@SubscribeEvent
	public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof EntityPlayer) {
			event.addCapability(BAUBLE_PLAYER_CAP, new BaublePlayerCapProvider());
		}
	}

	@SubscribeEvent
	public static void syncCapabilities(EntityJoinWorldEvent event) {
		if (!event.getWorld().isRemote && event.getEntity() instanceof EntityPlayer) {
			EntityPlayerMP player = (EntityPlayerMP) event.getEntity();
			BaublePlayer baublesPlayer = (BaublePlayer) asBaublesPlayer(player);

			baublesPlayer.bindPlayer(player);
			baublesPlayer.sendUpdates();
		}
	}

	@SubscribeEvent
	public static void onPlayerDeath(PlayerEvent.Clone event) {
		BaublePlayer.Serializer.INSTANCE.copy(asBaublesPlayer(event.getOriginal()), asBaublesPlayer(event.getEntityPlayer()));
		//We don't need to send packets here, because after this method, the method onPlayerJoin will be fired.
	}

	public static class CapabilityItemBaubleStorage implements Capability.IStorage<IBauble> {

		@Override
		public NBTBase writeNBT(Capability<IBauble> capability, IBauble instance, EnumFacing side) {
			return new NBTTagCompound();
		}

		@Override
		public void readNBT(Capability<IBauble> capability, IBauble instance, EnumFacing side, NBTBase nbt) {

		}
	}
}
