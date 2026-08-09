package com.wesleyhdias.minnanocraft.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import com.wesleyhdias.minnanocraft.events.PinnedTooltipInputHandler;
import com.wesleyhdias.minnanocraft.renderers.PinnedTooltipRenderer;
import com.wesleyhdias.minnanocraft.services.PinnedTooltipService;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.inventory.Slot;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

    @Shadow protected Slot hoveredSlot;

    // 1. Oculta o tooltip voador padrão se tiver um congelado na tela
    @Inject(method = "extractTooltip", at = @At("HEAD"), cancellable = true, require = 0)
    private void suppressVanillaTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (PinnedTooltipService.isPinned()) {
            ci.cancel();
        }
    }

    // 2. Eventos de Teclado (Usa a sua assinatura exata KeyEvent)
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (PinnedTooltipInputHandler.handleKeyPress(event.key(), this.hoveredSlot != null ? this.hoveredSlot.getItem() : null)) {
            cir.setReturnValue(true);
        }
    }

    // 3. Renderiza a interface
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onExtractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        PinnedTooltipRenderer.render(graphics);
    }

    // 4. Eventos de Mouse (Usa a sua assinatura exata MouseButtonEvent)
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClick(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (PinnedTooltipInputHandler.handleMouseClick(event.x(), event.y(), event.button())) {
            cir.setReturnValue(true);
        }
    }
}