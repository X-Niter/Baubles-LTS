package baubles.api.util;

import net.minecraftforge.fml.relauncher.Side;

public class WrongSideException extends RuntimeException {
	public WrongSideException(Side forbiddenSide) {
		super("Shouldn't be called on side: " + forbiddenSide);
	}
}
