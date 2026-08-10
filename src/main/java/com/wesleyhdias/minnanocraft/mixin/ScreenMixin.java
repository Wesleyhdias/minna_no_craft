package com.wesleyhdias.minnanocraft.mixin;

import com.wesleyhdias.minnanocraft.renderers.PinnedTooltipRenderer;
import com.wesleyhdias.minnanocraft.services.PinnedTooltipService;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    // Injetamos no TAIL (final) do método mestre que junta a tela e os tooltips nativos.
    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("TAIL"))
    private void renderPinnedTooltipOnTop(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (PinnedTooltipService.isPinned()) {
            PinnedTooltipRenderer.render(graphics);
        }
    }
}