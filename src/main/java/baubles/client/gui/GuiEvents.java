package baubles.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Event handler for GUI-related events
 */
@SideOnly(Side.CLIENT)
public class GuiEvents {
    
    /**
     * Button ID for the baubles inventory button
     */
    private static final int BAUBLES_BUTTON_ID = 55;
    
    /**
     * Adds the baubles button to the player inventory screen
     */
    @SubscribeEvent
    public void guiPostInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof GuiInventory) {
            GuiInventory gui = (GuiInventory) event.getGui();
            
            // Calculate button position - right side of inventory screen
            int xSize = ((GuiInventory) event.getGui()).getXSize();
            int ySize = ((GuiInventory) event.getGui()).getYSize();
            int guiLeft = (event.getGui().width - xSize) / 2;
            int guiTop = (event.getGui().height - ySize) / 2;
            
            // Add the baubles button to the GUI
            event.getButtonList().add(new GuiBaublesButton(BAUBLES_BUTTON_ID, 
                    guiLeft + xSize - 25, guiTop + 8, gui));
        }
    }
    
    /**
     * Handles click events for the baubles button
     */
    @SubscribeEvent
    public void guiButtonClick(GuiScreenEvent.ActionPerformedEvent.Post event) {
        // The button click handling is done in GuiBaublesButton class
    }
}
