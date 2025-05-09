package baubles.client.render;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import baubles.api.render.IRenderBauble;
import baubles.common.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Event handler for rendering baubles on the player
 */
@SideOnly(Side.CLIENT)
public class BaubleRenderHandler {
    
    /**
     * Renders baubles on players
     */
    @SubscribeEvent
    public void onPlayerRender(RenderPlayerEvent.Pre event) {
        // Skip rendering if disabled in config
        if (!Config.renderBaubles) {
            return;
        }
        
        EntityPlayer player = event.getEntityPlayer();
        RenderPlayer renderer = event.getRenderer();
        
        if (player.isInvisible()) {
            return;
        }
        
        // Get the player's baubles
        IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
        
        // Save the render state
        GlStateManager.pushMatrix();
        
        // Setup transformation
        float partialTicks = event.getPartialRenderTick();
        float rotation = player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * partialTicks;
        float yaw = (float) Math.toRadians(rotation);
        float bodyRotation = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks;
        float bodyYaw = (float) Math.toRadians(bodyRotation);
        
        // Enable proper lighting
        Minecraft.getMinecraft().entityRenderer.enableLightmap();
        
        // Render each bauble that implements IRenderBauble
        for (int i = 0; i < baubles.getSlots(); i++) {
            ItemStack stack = baubles.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof IRenderBauble) {
                GlStateManager.pushMatrix();
                
                // Choose render type based on slot
                IRenderBauble.RenderType renderType;
                if (i == 4) { // Head slot
                    renderType = IRenderBauble.RenderType.HEAD;
                } else {
                    renderType = IRenderBauble.RenderType.BODY;
                }
                
                // Render the bauble
                ((IRenderBauble) stack.getItem()).onPlayerBaubleRender(stack, player, renderType, partialTicks);
                
                GlStateManager.popMatrix();
            }
        }
        
        // Restore render state
        Minecraft.getMinecraft().entityRenderer.disableLightmap();
        GlStateManager.popMatrix();
    }
}
