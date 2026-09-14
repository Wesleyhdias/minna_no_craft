package com.wesleyhdias.minnanocraft.mixin;

import com.wesleyhdias.minnanocraft.client.tooltip.lookup.DictionaryLookupOverlayRenderer;
import com.wesleyhdias.minnanocraft.client.tooltip.pinnedTooltip.PinnedTooltipInputHandler;
import com.wesleyhdias.minnanocraft.client.tooltip.pinnedTooltip.PinnedTooltipService;
import com.wesleyhdias.minnanocraft.config.ModConfig;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
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

    /**
     * Intercepts mouse click events in container screens to prioritize modal overlay interactions
     * and handle clicking inside or outside pinned tooltips.
     *
     * @param event       The {@link MouseButtonEvent} containing click details and coordinates.
     * @param doubleClick Indicates whether the click event is a double click.
     * @param cir         The {@link CallbackInfoReturnable} provided by Mixin to manage return values.
     */
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClick(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (!ModConfig.getConfig().isEnabled()) {
            return;
        }

        // Handles clicks within active dictionary lookup overlay
        if (DictionaryLookupOverlayRenderer.mouseClicked(event.x(), event.y(), event.button())) {
            cir.setReturnValue(true);
            return;
        }

        boolean isPinned = PinnedTooltipService.isPinned();

        // Handles click inside pinned tooltip bounds (e.g., selecting a translated word)
        if (PinnedTooltipInputHandler.handleMouseClick(event.x(), event.y(), event.button())) {
            cir.setReturnValue(true);
            return;
        }

        // If clicked outside an active pinned tooltip, unpins it and suppresses background clicks
        if (isPinned) {
            PinnedTooltipService.unpin();
            cir.setReturnValue(true);
        }
    }

    /**
     * Prevents mouse drag actions (such as scrollbar dragging) when a pinned tooltip is open.
     *
     * @param event The {@link MouseButtonEvent} containing drag event data.
     * @param dx    The change in X position.
     * @param dy    The change in Y position.
     * @param cir   The {@link CallbackInfoReturnable} provided by Mixin to handle cancellation.
     */
    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void blockBackgroundDrag(MouseButtonEvent event, double dx, double dy, CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.getConfig().isEnabled() && PinnedTooltipService.isPinned()) {
            cir.setReturnValue(true);
        }
    }

    /**
     * Prevents mouse scroll wheel input from scrolling background container inventories when a pinned tooltip is open.
     *
     * @param x  The current X coordinate of the mouse.
     * @param y  The current Y coordinate of the mouse.
     * @param scrollX The horizontal scroll delta.
     * @param scrollY The vertical scroll delta.
     * @param cir     The {@link CallbackInfoReturnable} provided by Mixin to handle cancellation.
     */
    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void blockBackgroundScroll(double x, double y, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.getConfig().isEnabled() && PinnedTooltipService.isPinned()) {
            cir.setReturnValue(true);
        }
    }
}