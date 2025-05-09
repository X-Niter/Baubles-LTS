package baubles.api;

import baubles.api.cap.BaublesCapabilityManager;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author Azanor
 */
public class BaublesApi {


	/**
	 * Retrieves the baubles inventory capability handler for the supplied player
	 */
	/*public static IBaublesItemHandler getBaublesHandler(EntityPlayer player)
	{
		//IBaublesItemHandler handler = player.getCapability(BaublesCapabilityManager.CAPABILITY_BAUBLES, null);
		//handler.setPlayer(player);
		//return handler;
		return null;
	}*/

	/**
	 * @param livingEntity  The ItemStack to get the bauble inventory capability from
	 * @return Optional of the bauble inventory capability attached to the entity
	 */
	/*public static Optional<IBaublesItemHandler> getOBaublesHandler(@Nonnull final EntityLivingBase livingEntity) {
		//return Optional.ofNullable(livingEntity.getCapability(BaublesCapabilityManager.CAPABILITY_BAUBLES, null));
		return null;
	}*/

	private static Map<Item, Set<String>> itemToTypes = new HashMap<>();

	/**
	 * @param stack The ItemStack to get the bauble capability from
	 * @return Optional of the bauble capability attached to the ItemStack
	 */
	public static Optional<IBauble> getOBaubles(ItemStack stack) {
		return Optional.ofNullable(stack.getCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null));
	}

	/**
	 * Retrieves the baubles capability handler wrapped as a IInventory for the supplied player
	 */
	public static IInventory getBaubles(EntityPlayer player) {
		/*IBaublesItemHandler handler = player.getCapability(BaublesCapabilityManager.getBaublePlayerCap(), null);
		handler.setPlayer(player);
		return new BaublesInventoryWrapper(handler, player);*/
		return null;
	}

	/**
	 * Returns if the passed in item is equipped in a bauble slot. Will return the first slot found
	 *
	 * @return -1 if not found and slot number if it is found
	 */
	public static int isBaubleEquipped(EntityPlayer player, Item bauble) {
		/*IBaublesItemHandler handler = getBaublesHandler(player);
		for (int a=0;a<handler.getSlots();a++) {
			if (!handler.getStackInSlot(a).isEmpty() && handler.getStackInSlot(a).getItem()==bauble) return a;
		}*/
		return -1;
	}

	/**
	 * Registers a resource location to be used as the slot overlay icon in the GUI
	 *
	 * @param id               The identifier of the type of bauble to be associated with the icon
	 * @param resourceLocation The resource location of the icon
	 */
	public static void registerIcon(String id, @Nonnull ResourceLocation resourceLocation) {
		BaubleType.iconQueues.computeIfAbsent(id, k -> new ConcurrentSet<>()).add(resourceLocation);
	}

	/**
	 * @return A map of identifiers and their registered icons
	 */
	public static Map<String, ResourceLocation> getIcons() {
		return ImmutableMap.copyOf(BaubleType.icons);
	}

	/**
	 * @param identifier The unique identifier for the {@link BaubleType}
	 * @return The {@link BaubleType} from the given identifier
	 */
	@Nullable
	public static BaubleType getType(String identifier) {
		return BaubleType.idToType.get(identifier);
	}

	/**
	 * @return An unmodifiable list of all unique registered identifiers
	 */
	public static ImmutableSet<String> getTypeIdentifiers() {
		return ImmutableSet.copyOf(BaubleType.idToType.keySet());
	}

	/**
	 * Holder class for IMC message identifiers
	 */
	public final static class IMC {

		public static final String REGISTER_TYPE = "register_type";
		public static final String MODIFY_TYPE = "modify_type";
	}

	public final static class FinderData {

		String identifier;
		int index;
		ItemStack stack;

		public FinderData(String identifier, int index, ItemStack stack) {
			this.identifier = identifier;
			this.index = index;
			this.stack = stack;
		}

		public String getIdentifier() {
			return identifier;
		}

		public int getIndex() {
			return index;
		}

		public ItemStack getStack() {
			return stack;
		}
	}

}
