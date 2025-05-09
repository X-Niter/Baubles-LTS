package baubles.client.gui;

import baubles.api.cap.BaublesCapabilityManager;
import baubles.client.ClientProxy;
import baubles.common.Baubles;
import baubles.common.Config;
import baubles.common.container.ContainerPlayerExpanded;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.achievement.GuiStats;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiPlayerExpanded extends InventoryEffectRenderer {

	public static final ResourceLocation background = new ResourceLocation(Baubles.MODID, "textures/gui/expanded_inventory.png");

	static final ResourceLocation CURIO_INVENTORY = new ResourceLocation(Baubles.MODID, "textures/gui/inventory.png");

	private static final ResourceLocation CREATIVE_INVENTORY_TABS = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");

	/**
	 * The old x position of the mouse pointer
	 */
	private float oldMouseX;

	/**
	 * The old y position of the mouse pointer
	 */
	private float oldMouseY;

	private boolean widthTooNarrow;

	private float currentScroll;

	private boolean isScrolling;

	private boolean buttonClicked;

	public GuiPlayerExpanded(EntityPlayer player) {
		super(new ContainerPlayerExpanded(player.inventory, !player.getEntityWorld().isRemote, player));
		this.allowUserInput = true;
	}

	private void resetGuiLeft() {
		this.guiLeft = (this.width - this.xSize) / 2;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@Override
	public void initGui() {
		super.initGui();
		this.buttonList.clear();
		resetGuiLeft();
		if (Config.useCurioGUI) {
			this.widthTooNarrow = this.width < 379;
			this.guiLeft = (this.width - this.xSize) / 2;
		}
	}

	private boolean inScrollBar(double mouseX, double mouseY) {
		int i = this.guiLeft;
		int j = this.guiTop;
		int k = i - 34;
		int l = j + 12;
		int i1 = k + 14;
		int j1 = l + 139;
		return mouseX >= (double) k && mouseY >= (double) l && mouseX < (double) i1 && mouseY < (double) j1;
	}


	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items)
	 */
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.fontRenderer.drawString(I18n.format("container.crafting"), 97, 8, 4210752);

			/*if (this.mc.player.inventory.getItemStack().isEmpty() && this.getSlotUnderMouse() != null) {
				Slot slot = this.getSlotUnderMouse();
				if (slot instanceof SlotBauble && !slot.getHasStack()) {
					SlotBauble slotBauble = (SlotBauble)slot;
					this.drawHoveringText(slotBauble.getSlotName(), mouseX - this.guiLeft, mouseY - this.guiTop);
				}
			}*/

	}

	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.oldMouseX = (float) mouseX;
		this.oldMouseY = (float) mouseY;
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.hasActivePotionEffects = false;
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		this.mc.getTextureManager().bindTexture(GuiInventory.INVENTORY_BACKGROUND);
		int k = this.guiLeft;
		int l = this.guiTop;
		this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);

		/*if (!Config.useCurioGUI) {
			for (int i1 = 0; i1 < this.inventorySlots.inventorySlots.size(); ++i1) {
				Slot slot = (Slot) this.inventorySlots.inventorySlots.get(i1);
				if (slot.getHasStack() && slot.getSlotStackLimit() == 1) {
					this.drawTexturedModalRect(k + slot.xPos, l + slot.yPos, 200, 0, 16, 16);
				}
			}

		} else {*/
			/*BaublesApi.getOBaublesHandler(this.mc.player).ifPresent(handler -> {
				int slotCount = handler.getSlots();
				int upperHeight = 7 + slotCount * 18;
				this.mc.getTextureManager().bindTexture(CURIO_INVENTORY);
				this.drawTexturedModalRect(k - 26, l + 4, 0, 0, 27, upperHeight);

				if (slotCount <= 8) {
					this.drawTexturedModalRect(k - 26, l + 4 + upperHeight, 0, 151, 27, 7);
				} else {
					this.drawTexturedModalRect(k - 42, l + 4, 27, 0, 23, 158);
					this.mc.getTextureManager().bindTexture(CREATIVE_INVENTORY_TABS);
					this.drawTexturedModalRect(k - 34, l + 12 + (int)(127f * this.currentScroll), 232, 0, 12, 15);
				}
			});*/
		//}

		int slotCount = BaublesCapabilityManager.asBaublesPlayer(mc.player).getBaubleStorage().getActualSize();
		int upperHeight = 7 + slotCount * 18;
		this.mc.getTextureManager().bindTexture(CURIO_INVENTORY);
		this.drawTexturedModalRect(k - 26, l + 4, 0, 0, 27, upperHeight);

		if (slotCount <= 8) {
			this.drawTexturedModalRect(k - 26, l + 4 + upperHeight, 0, 151, 27, 7);
		} else {
			this.drawTexturedModalRect(k - 42, l + 4, 27, 0, 23, 158);
			this.mc.getTextureManager().bindTexture(CREATIVE_INVENTORY_TABS);
			this.drawTexturedModalRect(k - 34, l + 12 + (int) (127f * this.currentScroll), 232, 0, 12, 15);
		}

		GuiInventory.drawEntityOnScreen(k + 51, l + 75, 30, (float) (k + 51) - this.oldMouseX, (float) (l + 75 - 50) - this.oldMouseY, this.mc.player);

	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 1) {
			this.mc.displayGuiScreen(new GuiStats(this, this.mc.player.getStatFileWriter()));
		}
	}

	@Override
	protected void keyTyped(char par1, int par2) throws IOException {
		if (par2 == ClientProxy.KEY_BAUBLES.getKeyCode()) {
			this.mc.player.closeScreen();
		} else
			super.keyTyped(par1, par2);
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (this.inScrollBar(mouseX, mouseY)) {
			this.isScrolling = this.needsScrollBars();

		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	private boolean needsScrollBars() {
		return ((ContainerPlayerExpanded) this.inventorySlots).canScroll();
	}

	public void displayNormalInventory() {
		GuiInventory gui = new GuiInventory(this.mc.player);
		ObfuscationReflectionHelper.setPrivateValue(GuiInventory.class, gui, this.oldMouseX, "field_147048_u");
		ObfuscationReflectionHelper.setPrivateValue(GuiInventory.class, gui, this.oldMouseY, "field_147047_v");
		this.mc.displayGuiScreen(gui);
	}
}
