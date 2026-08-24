package com.wesleyhdias.minnanocraft.client.tooltip;

import com.wesleyhdias.minnanocraft.srs.models.ExpEvents;
import com.wesleyhdias.minnanocraft.srs.ExposureTracker;

import net.minecraft.client.Minecraft;

/**
 * Tracker responsible for monitoring the duration of the player's item hover in inventory tooltips.
 * It manages the accumulation of exposure time and triggers vocabulary event registration
 * once a sustained hover duration is met.
 */
public class TooltipHoverTracker {

    /**
     * The exposure tracker instance configured for hovering interactions.
     * Handles timing, threshold validation, and event dispatching.
     */
    private static final ExposureTracker hoverTracker = new ExposureTracker(1000, ExpEvents.HOVER);

    /**
     * Called on every render frame when an item tooltip is being drawn on the screen.
     *
     * @param key The unique translation key of the item currently being hovered over.
     */
    public static void onTooltipRendered(String key) {
        Minecraft client = Minecraft.getInstance();

        // Safety Guard: Ignore if the player is not currently viewing an inventory screen.
        // This prevents conflicts with global HUD tracking.
        if (client.screen == null) {
            return;
        }

        // Delegate updates to the exposure tracker.
        hoverTracker.update(key);
    }

    /**
     * Called on every game tick to perform maintenance tasks, such as detecting
     * when the mouse has left an item tooltip area or triggering proactive cache clearing.
     */
    public static void tick() {
        hoverTracker.tick();
    }
}