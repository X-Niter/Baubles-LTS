package baubles.api.modcom;

public class BaublesModCom {
	private final String identifier;
	private int size = 1;
	private boolean isEnabled = true;
	private boolean isHidden = false;

	public BaublesModCom(String id) {
		this.identifier = id;
	}

	public String getIdentifier() {
		return identifier;
	}

	public int getSize() {
		return size;
	}

	public BaublesModCom setSize(int size) {
		this.size = size;
		return this;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public BaublesModCom setEnabled(boolean enabled) {
		isEnabled = enabled;
		return this;
	}

	public boolean isHidden() {
		return isHidden;
	}

	public BaublesModCom setHidden(boolean hidden) {
		isHidden = hidden;
		return this;
	}
}
