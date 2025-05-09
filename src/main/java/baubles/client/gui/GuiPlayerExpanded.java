package baubles.client.gui;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import baubles.common.network.PacketHandler;
import baubles.common.network.PacketOpenNormalInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.achievement.GuiStats;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

/**
 * GUI for the expanded player inventory with baubles slots
 */
@SideOnly(Side.CLIENT)
public class GuiPlayerExpanded extends InventoryEffectRenderer {
    
    /**
     * The ResourceLocation containing the expanded inventory GUI texture
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation("baubles", "textures/gui/expanded_inventory.png");
    
    /**
     * Button ID for normal inventory button
     */
    private static final int NORMAL_INV_BUTTON = 77;
    
    /**
     * The X size of the inventory window in pixels
     */
    private static final int xSize = 194;
    
    /**
     * The Y size of the inventory window in pixels
     */
    private static final int ySize = 196;
    
    /**
     * Old mouse X position
     */
    private float oldMouseX;
    
    /**
     * Old mouse Y position
     */
    private float oldMouseY;
    
    private boolean hasRoom = true;
    
    public GuiPlayerExpanded(EntityPlayer player, Container inventorySlotsIn) {
        super(inventorySlotsIn);
        this.allowUserInput = true;
    }
    
    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
        
        int k = guiLeft;
        int l = guiTop;
        
        // Add button to return to normal inventory
        this.buttonList.add(new GuiButton(NORMAL_INV_BUTTON, k + 64, l + 8, 20, 20, ""));
    }
    
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        
        oldMouseX = mouseX;
        oldMouseY = mouseY;
        
        super.drawScreen(mouseX, mouseY, partialTicks);
        
        this.renderHoveredToolTip(mouseX, mouseY);
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        
        // Bind the expanded inventory texture
        this.mc.getTextureManager().bindTexture(TEXTURE);
        
        int k = this.guiLeft;
        int l = this.guiTop;
        
        // Draw the expanded inventory background
        this.drawTexturedModalRect(k, l, 0, 0, xSize, ySize);
        
        // Draw the character in the inventory
        GuiInventory.drawEntityOnScreen(k + 31, l + 75, 30, (float)(k + 51) - oldMouseX, 
                (float)(l + 75 - 50) - oldMouseY, this.mc.player);
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == NORMAL_INV_BUTTON) {
            // Switch back to normal inventory
            PacketHandler.INSTANCE.sendToServer(new PacketOpenNormalInventory());
        } else {
            super.actionPerformed(button);
        }
    }
    
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // Draw the inventory text
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, 77, 4210752);
        
        // Draw the baubles text
        this.fontRenderer.drawString(I18n.format("Baubles"), 81, 8, 4210752);
    }
    
    /**
     * Draws the tooltip for a bauble slot based on its type
     */
    @Override
    public void renderHoveredToolTip(int mouseX, int mouseY) {
        super.renderHoveredToolTip(mouseX, mouseY);
        
        // If a slot is hovered but empty, show what type of bauble goes there
        Slot slot = getSlotUnderMouse();
        if (slot != null && !slot.getHasStack() && slot.slotNumber >= 5 && slot.slotNumber < 12) {
            // Determine what type of bauble goes in this slot
            int idx = slot.slotNumber - 5;
            BaubleType type = null;
            
            if (idx == 0) type = BaubleType.AMULET;
            else if (idx == 1 || idx == 2) type = BaubleType.RING;
            else if (idx == 3) type = BaubleType.BELT;
            else if (idx == 4) type = BaubleType.HEAD;
            else if (idx == 5) type = BaubleType.BODY;
            else if (idx == 6) type = BaubleType.CHARM;
            
            if (type != null) {
                // Draw tooltip showing bauble type
                this.drawHoveringText(I18n.format("baubles.slot." + type.name().toLowerCase()), mouseX, mouseY);
            }
        }
    }
}
