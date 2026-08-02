package com.wesleyhdias.minnanocraft.mixin;

import com.wesleyhdias.minnanocraft.renderers.HudOverlayRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onExtractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {

        HudOverlayRenderer.renderOverlay(graphics);
    }
}