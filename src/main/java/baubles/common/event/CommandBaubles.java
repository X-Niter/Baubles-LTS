package baubles.common.event;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.cap.BaublesCapabilityManager;
import baubles.api.cap.IBaubleStorage;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;
import java.util.List;

public class CommandBaubles extends CommandBase {
	private List<String> aliases;

	public CommandBaubles() {
		this.aliases = new ArrayList<String>();
		this.aliases.add("baub");
		this.aliases.add("bau");
	}

	@Override
	public String getName() {
		return "baubles";
	}

	@Override
	public List<String> getAliases() {
		return aliases;
	}

	@Override
	public String getUsage(ICommandSender icommandsender) {
		return "/baubles <action> [<player> [<params>]]";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public boolean isUsernameIndex(String[] astring, int i) {
		return i == 1;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 2 || args[0].equalsIgnoreCase("help")) {
			sender.sendMessage(new TextComponentTranslation("\u00a73" + "help.message.one"));
			sender.sendMessage(new TextComponentTranslation("\u00a73" + "help.message.two"));
			sender.sendMessage(new TextComponentTranslation("\u00a73" + "help.message.three"));
			sender.sendMessage(new TextComponentTranslation("\u00a73" + "help.message.four"));
			sender.sendMessage(new TextComponentTranslation("\u00a73" + "help.message.five"));
		} else if (args.length >= 2) {
			EntityPlayerMP entityplayermp = getPlayer(server, sender, args[1]);

			if (entityplayermp == null) {
				sender.sendMessage(new TextComponentTranslation("\u00a7c" + args[1] + " message.not_found"));
				return;
			}

			//IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(entityplayermp);
			IBaubleStorage baubles = BaublesCapabilityManager.asBaublesPlayer(entityplayermp).getBaubleStorage();

			if (args[0].equalsIgnoreCase("view")) {
				sender.sendMessage(new TextComponentTranslation("\u00a73" + "message.showing_baubles_for " + entityplayermp.getName()));
				for (int a = 0; a < baubles.getActualSize(); a++) {
					ItemStack st = baubles.getStackInSlot(a);
					if (!st.isEmpty() && st.hasCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null)) {
						IBauble bauble = st.getCapability(BaublesCapabilityManager.CAPABILITY_ITEM_BAUBLE, null);
						BaubleType bt = bauble.getBaubleType(st);
						sender.sendMessage(new TextComponentTranslation("\u00a73 [message.slot " + a + "] " + bt + " " + st.getDisplayName()));
					}
				}
			} else if (args[0].equalsIgnoreCase("clear")) {
				if (args.length >= 3) {
					int slot = -1;
					try {
						slot = Integer.parseInt(args[2]);
					} catch (Exception e) {
					}
					if (slot < 0 || slot >= baubles.getActualSize()) {
						sender.sendMessage(new TextComponentTranslation("\u00a7c" + "message.invalid_arguments"));
						sender.sendMessage(new TextComponentTranslation("\u00a7c" + "message.use_help_command"));
					} else {
						baubles.setStackInSlot(slot, ItemStack.EMPTY);
						sender.sendMessage(new TextComponentTranslation("\u00a73Cleared baubles slot " + slot + " for " + entityplayermp.getName()));
						entityplayermp.sendMessage(new TextComponentTranslation("\u00a74Your baubles slot " + slot + " has been cleared by admin " + sender.getName()));
					}
				} else {
					for (int a = 0; a < baubles.getActualSize(); a++) {
						baubles.setStackInSlot(a, ItemStack.EMPTY);
					}
					sender.sendMessage(new TextComponentTranslation("\u00a73Cleared all baubles slots for " + entityplayermp.getName()));
					entityplayermp.sendMessage(new TextComponentTranslation("\u00a74All your baubles slots have been cleared by admin " + sender.getName()));
				}
			} else {
				sender.sendMessage(new TextComponentTranslation("\u00a7c" + "message.invalid_arguments"));
				sender.sendMessage(new TextComponentTranslation("\u00a7c" + "message.use_help_command"));
			}

		} else {
			sender.sendMessage(new TextComponentTranslation("\u00a7c" + "message.invalid_arguments"));
			sender.sendMessage(new TextComponentTranslation("\u00a7c" + "message.use_help_command"));
		}
	}
}
