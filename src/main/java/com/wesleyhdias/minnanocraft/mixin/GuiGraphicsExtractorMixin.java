package com.wesleyhdias.minnanocraft.mixin;

import com.wesleyhdias.minnanocraft.client.tooltip.pinnedTooltip.PinnedTooltipService;
import com.wesleyhdias.minnanocraft.config.ModConfig;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorMixin {

    @Inject(method = "tooltip", at = @At("HEAD"), cancellable = true, require = 0)
    private void blockDirectTooltips(CallbackInfo ci) {
        if (!ModConfig.getConfig().isEnabled() || !PinnedTooltipService.isPinned()) {
            return;
        }

        // Se for o próprio PinnedTooltip sendo desenhado, deixa desenhar
        if (PinnedTooltipService.isInternalRendering()) {
            return;
        }

        // Caso contrário (livro de receitas, etc), cancela o tooltip do vanilla!
        ci.cancel();
    }
}
