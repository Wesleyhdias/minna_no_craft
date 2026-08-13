package com.wesleyhdias.minnanocraft.trackers;

import com.wesleyhdias.minnanocraft.data.models.Event;
import net.minecraft.client.Minecraft;

/**
 * Tracker responsible for monitoring player item hover duration in inventory tooltips
 * to award exposure points after a sustained hover duration.
 */
public class TooltipHoverTracker {

    private static String currentKey = null;
    private static long lastUnhoverTime = 0;
    private static long lastFrameTime = 0;

    private static final ExposureTracker hoverTracker = new ExposureTracker(1000, Event.HOVER);

    /**
     * Called on every render frame when an item tooltip is drawn on screen.
     *
     * @param key The unique translation key of the item currently being hovered over.
     */
    public static void onTooltipRendered(String key) {
        Minecraft client = Minecraft.getInstance();

        // Safety Guard: If screen is null, the player is walking around in the world (not inside an inventory).
        // Completely ignore this event so it does not conflict with or override the HUD_LOOK tracker!
        if (client.screen == null) {
            currentKey = null; // Clear target as a fallback
            return;
        }
        long now = System.currentTimeMillis();

        // Protection against "ghost hovering" if rendering drops or pauses unexpectedly
        if (now - lastFrameTime > 100) {
            currentKey = null;
            lastUnhoverTime = now;
        }
        lastFrameTime = now;

        // Checks if mouse move to a different item to mark the time
        String currentTrackerKey = hoverTracker.getCurrentKey();
        if (currentTrackerKey != null && !currentTrackerKey.isEmpty() && !currentTrackerKey.equals(key)) {
            lastUnhoverTime = now;
        }

        // If at least 1 second has passed
        boolean cooldownMet = (now - lastUnhoverTime) >= 1000;

        // Update the tracker
        hoverTracker.update(key, cooldownMet);
    }

    /**
     * Called on every game tick to detect when the mouse leaves an item tooltip.
     */
    public static void tick() {
        long now = System.currentTimeMillis();

        // If more than 100 ms (~2 ticks) pass without drawing any tooltip, the mouse has left the item
        if (currentKey != null && (now - lastFrameTime) > 100) {
            currentKey = null;
            lastUnhoverTime = now; // Records exact timestamp when mouse was removed
        }
    }
}