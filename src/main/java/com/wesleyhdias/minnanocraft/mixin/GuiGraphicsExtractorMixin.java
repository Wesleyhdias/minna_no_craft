package com.wesleyhdias.minnanocraft.mixin;

import com.wesleyhdias.minnanocraft.client.tooltip.pinnedTooltip.PinnedTooltipService;
import com.wesleyhdias.minnanocraft.config.ModConfig;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin targeting {@link GuiGraphicsExtractor} to intercept and manage native tooltip rendering calls.
 * <p>
 * Suppresses standard vanilla tooltips (such as recipe book or inventory hover tooltips)
 * whenever a pinned vocabulary tooltip is actively displayed, preventing visual overlap.
 */
@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorMixin {

    /**
     * Intercepts native tooltip rendering at HEAD and cancels execution if a pinned tooltip is active,
     * unless the current call originates internally from the pinned tooltip system itself.
     *
     * @param ci The {@link CallbackInfo} provided by the Mixin framework to handle cancellation.
     */
    @Inject(method = "tooltip", at = @At("HEAD"), cancellable = true, require = 0)
    private void blockDirectTooltips(CallbackInfo ci) {
        if (!ModConfig.getConfig().isEnabled() || !PinnedTooltipService.isPinned()) {
            return;
        }

        // Allows rendering if the call originates internally from PinnedTooltipRenderer
        if (PinnedTooltipService.isInternalRendering()) {
            return;
        }

        // Cancels native vanilla tooltips (e.g., recipe book) to prevent visual clutter
        ci.cancel();
    }
}
