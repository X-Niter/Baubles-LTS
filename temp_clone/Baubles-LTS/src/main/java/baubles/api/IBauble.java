package baubles.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;

/**
 * This interface should be extended by items that can be worn in bauble slots
 *
 * @author Azanor
 */

public interface IBauble {

	/**
	 * This method return the type of bauble this is.
	 * Type is used to determine the slots it can go into.
	 */
	BaubleType getBaubleType(ItemStack itemstack);

	/**
	 * This method is called once per tick if the bauble is being worn by a player
	 */
	default void onWornTick(ItemStack itemstack, EntityLivingBase player) {
	}

	/**
	 * This method is called when the bauble is equipped by a player
	 */
	default void onEquipped(ItemStack itemstack, EntityLivingBase player) {
	}

	/**
	 * This method is called when the bauble is unequipped by a player
	 */
	default void onUnequipped(ItemStack itemstack, EntityLivingBase player) {
	}

	/**
	 * Can this bauble be added to a bauble slot
	 */
	default boolean canEquip(ItemStack itemstack, EntityLivingBase player) {
		return true;
	}

	/**
	 * Can this bauble be removed from a bauble slot
	 */
	default boolean canUnequip(ItemStack itemstack, EntityLivingBase player) {
		return true;
	}

	/**
	 * Will bauble automatically sync to client if a change is detected in its NBT or damage values?
	 * Default is off, so override and set to true if you want to auto sync.
	 * This sync is not instant, but occurs every 10 ticks (.5 seconds).
	 */
	default boolean willAutoSync(ItemStack itemstack, EntityLivingBase player) {
		return false;
	}

	/**
	 * Plays a sound server-side when a bauble is equipped from right-clicking the ItemStack in hand
	 * This can be overriden to play nothing, but it is advised to always play something as an auditory feedback for players
	 *
	 * @param livingEntity The wearer of the ItemStack
	 */
	default void playEquipSound(EntityLivingBase livingEntity) {
		livingEntity.world.playSound(null, livingEntity.getPosition(), SoundEvents.ITEM_ARMOR_EQUIP_GENERIC,
				SoundCategory.NEUTRAL, 1.0f, 1.0f);
	}

	/**
	 * Determines if the Baubles or the ItemStack can be automatically equipped into the first available slot when right-clicked
	 *
	 * @return True to enable right-clicking auto-equip, false to disable
	 */
	default boolean canRightClickEquip() {
		return true;
	}

}
