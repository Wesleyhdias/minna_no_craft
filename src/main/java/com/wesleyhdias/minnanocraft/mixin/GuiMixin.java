package com.wesleyhdias.minnanocraft.mixin;

import com.wesleyhdias.minnanocraft.renderers.HudOverlayRenderer;
import com.wesleyhdias.minnanocraft.config.ModConfig;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin for the main Minecraft in-game {@link Gui} class.
 * Used to inject custom rendering logic for the in-game heads-up display (HUD),
 * specifically for showing the vocabulary overlay when looking at world targets.
 */
@Mixin(Gui.class)
public class GuiMixin {

    /**
     * Injects custom rendering code at the end (TAIL) of the GUI render state extraction.
     * This ensures the custom vocabulary HUD overlay is drawn on top of the standard
     * crosshair and other native HUD elements.
     *
     * @param graphics     The GUI graphics extractor used for 2D rendering.
     * @param deltaTracker The tracker handling frame delta timing.
     * @param ci           The callback information provided by Mixin.
     */
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onExtractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!ModConfig.isEnabled()) {
            return; // O Minecraft continua com o nome original em português
        }

        HudOverlayRenderer.renderOverlay(graphics);
    }
}