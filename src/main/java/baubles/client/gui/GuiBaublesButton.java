package baubles.client.gui;

import baubles.common.Config;
import baubles.common.network.PacketHandler;
import baubles.common.network.PacketOpenBaublesInventory;
import baubles.common.network.PacketOpenNormalInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.lwjgl.opengl.GL11;

public class GuiBaublesButton extends GuiButton {

	private final GuiContainer parentGui;

	public GuiBaublesButton(int buttonId, GuiContainer parentGui, int x, int y, int width, int height, String buttonText) {
		super(buttonId, x, parentGui.getGuiTop() + y, width, height, buttonText);
		this.parentGui = parentGui;
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		boolean pressed = super.mousePressed(mc, mouseX - this.parentGui.getGuiLeft(), mouseY);
		if (pressed) {
			if (parentGui instanceof GuiInventory) {
				if (!Config.useCurioGUI) {
					PacketHandler.INSTANCE.sendToServer(new PacketOpenBaublesInventory());
				} else {
					float oldMouseX = ObfuscationReflectionHelper.getPrivateValue(GuiInventory.class, (GuiInventory) parentGui, "field_147048_u");
					float oldMouseY = ObfuscationReflectionHelper.getPrivateValue(GuiInventory.class, (GuiInventory) parentGui, "field_147047_v");
					PacketHandler.INSTANCE.sendToServer(new PacketOpenBaublesInventory(oldMouseX, oldMouseY));
				}
			} else {
				((GuiPlayerExpanded) parentGui).displayNormalInventory();
				PacketHandler.INSTANCE.sendToServer(new PacketOpenNormalInventory());
			}
		}
		return pressed;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			int x = this.x + this.parentGui.getGuiLeft();

			FontRenderer fontrenderer = mc.fontRenderer;
			if (!Config.useCurioGUI) {
				mc.getTextureManager().bindTexture(GuiPlayerExpanded.background);
			} else {
				mc.getTextureManager().bindTexture(GuiPlayerExpanded.CURIO_INVENTORY);
			}
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.hovered = mouseX >= x && mouseY >= this.y && mouseX < x + this.width && mouseY < this.y + this.height;
			int k = this.getHoverState(this.hovered);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, 200);
			if (k == 1) {
				if (!Config.useCurioGUI) {
					this.drawTexturedModalRect(x, this.y, 200, 48, 10, 10);
				} else {
					this.drawTexturedModalRect(x, this.y, 52, 2, 10, 10);
				}
			} else {
				if (!Config.useCurioGUI) {
					this.drawTexturedModalRect(x, this.y, 210, 48, 10, 10);
				} else {
					this.drawTexturedModalRect(x, this.y, 52, 16, 10, 10);
				}
				this.drawCenteredString(fontrenderer, I18n.format(this.displayString), x + 5, this.y + this.height, 0xffffff);
			}
			GlStateManager.popMatrix();

			this.mouseDragged(mc, mouseX, mouseY);
		}
	}
}
