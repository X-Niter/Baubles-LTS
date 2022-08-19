package baubles.api.cap;

import baubles.api.util.WrongSideException;
import net.minecraftforge.fml.relauncher.Side;

public interface IBaublePlayer {
	BaubleStorage getBaubleStorage();

	void onTick(Side side);

	/**
	 * Syncs all capability data with client.
	 * Should be called only on server.
	 *
	 * @throws WrongSideException if called on client.
	 */
	void sendUpdates();

	@Override
	String toString();
}
