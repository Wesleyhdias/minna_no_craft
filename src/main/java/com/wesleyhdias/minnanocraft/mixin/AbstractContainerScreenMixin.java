package com.wesleyhdias.minnanocraft.mixin;

import com.wesleyhdias.minnanocraft.client.tooltip.pinnedTooltip.PinnedTooltipInputHandler;
import com.wesleyhdias.minnanocraft.client.tooltip.pinnedTooltip.PinnedTooltipService;
import com.wesleyhdias.minnanocraft.client.lookup.DictionaryLookupOverlayRenderer;
import com.wesleyhdias.minnanocraft.config.ModConfig;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.inventory.Slot;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin targeting {@link AbstractContainerScreen} to intercept rendering and user inputs in container screens.
 * <p>
 * Suppresses native tooltips, mouse interactions, key events, and scrolling whenever a pinned
 * vocabulary tooltip or dictionary lookup overlay is actively open.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

    /** Reference to the vanilla field tracking the currently hovered container slot. */
    @Shadow
    protected Slot hoveredSlot;

    /**
     * Cancels native vanilla tooltip rendering if a pinned vocabulary tooltip is active on screen.
     *
     * @param graphics The {@link GuiGraphicsExtractor} instance used for drawing UI elements.
     * @param mouseX   The current X coordinate of the mouse.
     * @param mouseY   The current Y coordinate of the mouse.
     * @param ci       The {@link CallbackInfo} provided by the Mixin framework.
     */
    @Inject(method = "extractTooltip", at = @At("HEAD"), cancellable = true, require = 0)
    private void suppressVanillaTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (!ModConfig.getConfig().isEnabled()) {
            return;
        }

        if (PinnedTooltipService.isPinned()) {
            ci.cancel();
        }
    }

    /**
     * Intercepts keyboard input to handle dictionary lookup navigation or tooltip pinning actions.
     *
     * @param event The {@link KeyEvent} containing key state and keycode data.
     * @param cir   The {@link CallbackInfoReturnable} provided by Mixin to manage return values.
     */
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (!ModConfig.getConfig().isEnabled()) {
            return;
        }

        if (DictionaryLookupOverlayRenderer.keyPressed(event.key())) {
            cir.setReturnValue(true);
            return;
        }

        if (PinnedTooltipInputHandler.handleKeyPress(event.key(), this.hoveredSlot != null ? this.hoveredSlot.getItem() : null)) {
            cir.setReturnValue(true);
        }
    }
}