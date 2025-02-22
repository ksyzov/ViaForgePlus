package net.aspw.viaforgeplus.injection.forge.mixins.gui;

import net.aspw.viaforgeplus.ProtocolBase;
import net.aspw.viaforgeplus.api.ProtocolSelector;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiMultiplayer.class)
public abstract class MixinGuiMultiplayer extends GuiScreen {

    @Inject(method = "initGui", at = @At("RETURN"))
    private void initGui(CallbackInfo callbackInfo) {
        buttonList.add(new GuiButton(1151, 4, height - 24, 68, 20, "Protocol"));
    }

    @Inject(method = "drawScreen", at = @At("RETURN"))
    private void drawScreen(CallbackInfo callbackInfo) {
        GlStateManager.disableLighting();
        fontRendererObj.drawString(
            String.format("§7Version: §d%s§r", ProtocolBase.getManager().getTargetVersion().getName()),
            6, height - 35, -1, true
        );
        GlStateManager.enableLighting();
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"))
    private void actionPerformed(GuiButton button, CallbackInfo callbackInfo) {
        if (button.id == 1151) {
            mc.displayGuiScreen(new ProtocolSelector(this));
        }
    }
}