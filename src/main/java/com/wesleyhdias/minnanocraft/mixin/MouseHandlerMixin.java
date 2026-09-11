package com.wesleyhdias.minnanocraft.mixin;

import com.wesleyhdias.minnanocraft.client.tooltip.lookup.DictionaryLookupService;

import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.MouseHandler;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin for Minecraft's {@link MouseHandler}.
 * <p>
 * Intercepts and cancels mouse input events (clicks, scrolling, and cursor movements)
 * whenever the dictionary lookup modal interface is active, preventing unwanted
 * interactions with background inventory screens or tooltips.
 */
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    /**
     * Prevents mouse clicks from passing through to underlying UI elements when dictionary lookup is open.
     *
     * @param handle        The window handle.
     * @param rawButtonInfo The mouse button information.
     * @param action        The mouse action (e.g., press, release).
     * @param ci            The {@link CallbackInfo} provided by Mixin to manage cancellation.
     */
    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void blockMouseClicks(long handle, MouseButtonInfo rawButtonInfo, int action, CallbackInfo ci) {
        if (DictionaryLookupService.isOpen()) {
            ci.cancel();
        }
    }

    /**
     * Prevents mouse scroll wheel input from affecting background screens when dictionary lookup is open.
     *
     * @param handle  The window handle.
     * @param xoffset The horizontal scroll offset.
     * @param yoffset The vertical scroll offset.
     * @param ci      The {@link CallbackInfo} provided by Mixin to manage cancellation.
     */
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void blockMouseScroll(long handle, double xoffset, double yoffset, CallbackInfo ci) {
        if (DictionaryLookupService.isOpen()) {
            ci.cancel();
        }
    }

    /**
     * Prevents mouse position updates from updating hover states or triggering item tooltips
     * on background screens while the dictionary lookup interface is open.
     *
     * @param handle The window handle.
     * @param xpos   The target X coordinate position.
     * @param ypos   The target Y coordinate position.
     * @param ci     The {@link CallbackInfo} provided by Mixin to manage cancellation.
     */
    @Inject(method = "onMove", at = @At("HEAD"), cancellable = true)
    private void blockMouseMove(long handle, double xpos, double ypos, CallbackInfo ci) {
        if (DictionaryLookupService.isOpen()) {
            // Cancels background mouse movements to suppress unintended hover tooltips or recipe book updates
            ci.cancel();
        }
    }
}