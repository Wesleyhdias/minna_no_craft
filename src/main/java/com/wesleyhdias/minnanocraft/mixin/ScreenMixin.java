package com.wesleyhdias.minnanocraft.mixin;

import com.wesleyhdias.minnanocraft.client.tooltip.pinnedTooltip.PinnedTooltipRenderer;
import com.wesleyhdias.minnanocraft.client.tooltip.pinnedTooltip.PinnedTooltipService;
import com.wesleyhdias.minnanocraft.client.lookup.DictionaryLookupOverlayRenderer;
import com.wesleyhdias.minnanocraft.client.syllabary_screen.SyllabaryScreen;
import com.wesleyhdias.minnanocraft.config.ModConfig;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin for the base Minecraft {@link Screen} class.
 * <p>
 * Injects custom rendering logic for the pinned tooltip system and dictionary overlay,
 * ensuring that pinned vocabulary tooltips and overlays are drawn on top of standard screen elements.
 */
@Mixin(Screen.class)
public abstract class ScreenMixin {

    /**
     * Injects custom rendering code at the TAIL (end) of the screen render state extraction method.
     * <p>
     * If a tooltip is currently pinned, delegates rendering to {@link PinnedTooltipRenderer}
     * and proceeds to render the {@link DictionaryLookupOverlayRenderer}.
     *
     * @param graphics The {@link GuiGraphicsExtractor} instance used for drawing UI elements.
     * @param mouseX   The current X coordinate of the mouse cursor.
     * @param mouseY   The current Y coordinate of the mouse cursor.
     * @param a        The partial tick delta time.
     * @param ci       The {@link CallbackInfo} provided by the Mixin framework.
     */
    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("TAIL"))
    private void renderPinnedTooltipOnTop(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        // Skips verification if the mod is disabled in configuration
        if (!ModConfig.getConfig().isEnabled()) {
            return;
        }

        if (Minecraft.getInstance().screen instanceof SyllabaryScreen) {
            return;
        }

        if (PinnedTooltipService.isPinned()) {
            PinnedTooltipService.setInternalRendering(true);
            try {
                PinnedTooltipRenderer.render(graphics);
            } finally {
                PinnedTooltipService.setInternalRendering(false); // Ensures state is reset even if an exception occurs
            }
        }

        DictionaryLookupOverlayRenderer.render(graphics);
    }
}