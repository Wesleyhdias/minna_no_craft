package com.wesleyhdias.minnanocraft.mixin;

import com.wesleyhdias.minnanocraft.config.ModConfig;
import com.wesleyhdias.minnanocraft.renderers.DictionaryLookupOverlayRenderer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import com.wesleyhdias.minnanocraft.events.PinnedTooltipInputHandler;
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

/**
 * Mixin for the base {@link AbstractContainerScreen} class.
 * Intercepts rendering and input events within inventory screens to support
 * the pinned tooltip feature, overriding default vanilla behavior when a tooltip is pinned.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

    /** Shadow reference to the vanilla field tracking the currently hovered inventory slot. */
    @Shadow protected Slot hoveredSlot;

    /**
     * Injects at the beginning (HEAD) of the vanilla tooltip extraction method.
     * 1. Hides the default floating tooltip if there is a pinned one on the screen.
     *
     * @param graphics The GUI graphics extractor used for drawing.
     * @param mouseX   The current X coordinate of the mouse.
     * @param mouseY   The current Y coordinate of the mouse.
     * @param ci       The callback information provided by Mixin.
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
     * Injects at the beginning (HEAD) of the keyboard event handling method.
     * 2. Keyboard Events (Uses the exact KeyEvent signature).
     *
     * @param event The keyboard event containing key data.
     * @param cir   The returnable callback information provided by Mixin.
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

    /**
     * Injects at the beginning (HEAD) of the mouse click handling method.
     * 3. Mouse Events (Uses the exact MouseButtonEvent signature).
     *
     * @param event       The mouse button event containing click data and coordinates.
     * @param doubleClick Whether this click was a double click.
     * @param cir         The returnable callback information provided by Mixin.
     */
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClick(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (!ModConfig.getConfig().isEnabled()) {
            return;
        }

        if (DictionaryLookupOverlayRenderer.mouseClicked()) {
            cir.setReturnValue(true);
            return;
        }

        if (PinnedTooltipInputHandler.handleMouseClick(event.x(), event.y(), event.button())) {
            cir.setReturnValue(true);
        }
    }
}