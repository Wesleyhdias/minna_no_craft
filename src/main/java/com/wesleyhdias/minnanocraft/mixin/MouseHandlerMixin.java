package com.wesleyhdias.minnanocraft.mixin;

import com.wesleyhdias.minnanocraft.client.tooltip.lookup.DictionaryLookupService;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    // Bloqueia cliques
    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void blockMouseClicks(long handle, MouseButtonInfo rawButtonInfo, int action, CallbackInfo ci) {
        if (DictionaryLookupService.isOpen()) {
            ci.cancel();
        }
    }

    // Bloqueia o scroll
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void blockMouseScroll(long handle, double xoffset, double yoffset, CallbackInfo ci) {
        if (DictionaryLookupService.isOpen()) {
            ci.cancel();
        }
    }

    // Bloqueia a atualização de hover/posição do mouse para o inventário de trás
    @Inject(method = "onMove", at = @At("HEAD"), cancellable = true)
    private void blockMouseMove(long handle, double xpos, double ypos, CallbackInfo ci) {
        if (DictionaryLookupService.isOpen()) {
            // Se o lookup está aberto, o jogo ignora o movimento do mouse para o fundo,
            // matando qualquer tooltip indesejado de itens ou do livro de receitas.
            ci.cancel();
        }
    }
}