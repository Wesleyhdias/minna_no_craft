package com.wesleyhdias.minnanocraft.mixin;

import com.wesleyhdias.minnanocraft.client.hud.HudOverlayRenderer;
import com.wesleyhdias.minnanocraft.config.ModConfig;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin for Minecraft's core in-game {@link Gui} class.
 * <p>
 * Injects custom rendering logic into the heads-up display (HUD) layer,
 * drawing vocabulary overlays when players target blocks or entities in the world.
 */
@Mixin(Gui.class)
public class GuiMixin {

    /**
     * Injects custom rendering calls at the TAIL end of HUD render state extraction.
     * Ensures the custom vocabulary HUD overlay renders on top of native UI elements
     * like crosshairs and status bars.
     *
     * @param graphics     The {@link GuiGraphicsExtractor} instance used for 2D rendering.
     * @param deltaTracker The {@link DeltaTracker} providing frame delta time updates.
     * @param ci           The {@link CallbackInfo} provided by the Mixin framework.
     */
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onExtractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        // Skips overlay rendering if the mod is disabled in configuration
        if (!ModConfig.getConfig().isEnabled()) {
            return;
        }

        HudOverlayRenderer.renderOverlay(graphics);
    }
}