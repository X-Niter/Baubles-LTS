package baubles.api;

import baubles.api.modcom.BaublesModCom;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLInterModComms;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

public enum BaubleType {
	AMULET(0),
	RING(1, 2),
	BELT(3),
	TRINKET(0, 1, 2, 3, 4, 5, 6),
	HEAD(4),
	BODY(5),
	CHARM(6);


	public static Map<String, BaubleType> idToType = new HashMap<>();
	static ConcurrentMap<String, ConcurrentSet<ResourceLocation>> iconQueues = new ConcurrentHashMap<>();
	static Map<String, ResourceLocation> icons = new HashMap<>();
	int[] validSlots;
	private String identifier;
	/**
	 * The default number of slots
	 */
	private int size;
	/**
	 * Enabled slots will be given to holders by default
	 */
	private boolean isEnabled;
	/**
	 * Hidden slots will not show up in the default Curios GUI, but will still exist
	 */
	private boolean isHidden;

	BaubleType(int... validSlots) {
		this.validSlots = validSlots;
	}

	BaubleType(String identifier) {
		this.identifier = identifier;
		this.size = 1;
		this.isEnabled = true;
		this.isHidden = false;
	}

	public static void processIcons() {

		if (!icons.isEmpty()) {
			icons = new HashMap<>();
		}
		iconQueues.forEach((k, v) -> {

			if (!icons.containsKey(k)) {
				List<ResourceLocation> sortedList = new ArrayList<>(v);
				Collections.sort(sortedList);
				icons.put(k, sortedList.get(sortedList.size() - 1));
			}
		});
	}

	public static void processBaubleTypes(Stream<FMLInterModComms.IMCMessage> register, Stream<FMLInterModComms.IMCMessage> modify) {
		register
				.filter(msg -> msg.getSender() != null)
				.map(msg -> (BaublesModCom) msg.getMessageType().getAnnotatedSuperclass())
				.forEach(msg -> processType(msg, true));

		modify
				.filter(msg -> msg.getSender() != null)
				.map(msg -> (BaublesModCom) msg.getMessageType().getAnnotatedSuperclass())
				.forEach(msg -> processType(msg, false));
	}

	private static void processType(BaublesModCom message, boolean create) {
		String identifier = message.getIdentifier();

		if (idToType.containsKey(identifier)) {
			BaubleType presentType = idToType.get(identifier);

			if (message.getSize() > presentType.getSize()) {
				presentType.defaultSize(message.getSize());
			}

			if (!message.isEnabled() && presentType.isEnabled()) {
				presentType.enabled(false);
			}

			if (message.isHidden() && !presentType.isHidden()) {
				presentType.hide(true);
			}

		} else if (create) {
			idToType.put(identifier, BaubleType.valueOf(identifier)
					.defaultSize(message.getSize())
					.enabled(message.isEnabled())
					.hide(message.isHidden()));
		}
	}

	public String getIdentifier() {
		return identifier;
	}

	public int getSize() {
		return this.size;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public boolean isHidden() {
		return isHidden;
	}

	public final BaubleType defaultSize(int size) {
		this.size = Math.max(size, this.size);
		return this;
	}

	public final BaubleType enabled(boolean enabled) {
		this.isEnabled = enabled;
		return this;
	}

	public final BaubleType hide(boolean hide) {
		this.isHidden = hide;
		return this;
	}

	public boolean hasSlot(int slot) {
		for (int s : validSlots) {
			if (s == slot) return true;
		}
		return false;
	}

	public int[] getValidSlots() {
		return validSlots;
	}
}
