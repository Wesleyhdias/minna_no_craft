package com.wesleyhdias.minnanocraft.mixin;

import com.wesleyhdias.minnanocraft.client.tooltip.lookup.DictionaryLookupOverlayRenderer;
import com.wesleyhdias.minnanocraft.client.tooltip.pinnedTooltip.PinnedTooltipRenderer;
import com.wesleyhdias.minnanocraft.client.tooltip.pinnedTooltip.PinnedTooltipService;
import com.wesleyhdias.minnanocraft.config.ModConfig;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin for the base Minecraft {@link Screen} class.
 * Used to inject custom rendering logic for the pinned tooltip system,
 * ensuring that the pinned vocabulary tooltip is drawn on top of all other screen elements.
 */
@Mixin(Screen.class)
public abstract class ScreenMixin {

    /**
     * Injects custom rendering code at the TAIL (end) of the method that combines the screen and native tooltips.
     * If a tooltip is currently pinned, it delegates the rendering to the {@link PinnedTooltipRenderer}.
     *
     * @param graphics The GUI graphics extractor used for drawing.
     * @param mouseX   The current X coordinate of the mouse cursor.
     * @param mouseY   The current Y coordinate of the mouse cursor.
     * @param a        The partial tick time (delta).
     * @param ci       The callback information provided by Mixin.
     */
    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("TAIL"))
    private void renderPinnedTooltipOnTop(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (!ModConfig.getConfig().isEnabled()) {
            return;
        }

        if (PinnedTooltipService.isPinned()) {
            PinnedTooltipService.setInternalRendering(true);
            try {
                PinnedTooltipRenderer.render(graphics);
            } finally {
                PinnedTooltipService.setInternalRendering(false); // Garante que reseta mesmo se der erro
            }
        }

        DictionaryLookupOverlayRenderer.render(graphics);
    }
}