package baubles.client.gui;

import baubles.common.Baubles;
import baubles.common.network.PacketHandler;
import baubles.common.network.PacketOpenBaublesInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

/**
 * Button for opening the baubles inventory from the player's inventory screen
 */
public class GuiBaublesButton extends GuiButton {
    
    private static final ResourceLocation TEXTURE = new ResourceLocation("baubles", "textures/gui/baubles.png");
    private final GuiInventory parentGui;
    
    public GuiBaublesButton(int buttonId, int x, int y, GuiInventory parentGui) {
        super(buttonId, x, y, 20, 20, "");
        this.parentGui = parentGui;
    }
    
    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            mc.getTextureManager().bindTexture(TEXTURE);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            
            // Is the mouse hovering over the button?
            boolean hovered = mouseX >= this.x && mouseY >= this.y && 
                  mouseX < this.x + this.width && mouseY < this.y + this.height;
            
            int textureY = 0;
            if (hovered) {
                textureY = 20; // Hovered texture position
            }
            
            // Draw the button
            drawTexturedModalRect(this.x, this.y, 0, textureY, this.width, this.height);
        }
    }
    
    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        boolean result = super.mousePressed(mc, mouseX, mouseY);
        if (result) {
            // When button is pressed, send packet to open baubles inventory
            PacketHandler.INSTANCE.sendToServer(new PacketOpenBaublesInventory());
        }
        return result;
    }
}
