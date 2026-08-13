package com.wesleyhdias.minnanocraft.trackers;

import com.wesleyhdias.minnanocraft.data.loader.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.data.models.Event;
import com.wesleyhdias.minnanocraft.services.VocabularyManager;
import net.minecraft.client.Minecraft;

import java.util.List;

/**
 * Tracker responsible for monitoring player item hover duration in inventory tooltips
 * to award exposure points after a sustained hover duration.
 */
public class TooltipHoverTracker {

    private static String currentKey = null;
    private static long hoverStartTime = 0;
    private static boolean pointAwarded = false;
    private static long lastUnhoverTime = 0;
    private static long lastFrameTime = 0;

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

        // If the mouse moved to a DIFFERENT item in the inventory
        if (!key.equals(currentKey)) {
            if (currentKey != null) {
                lastUnhoverTime = now; // Records exit timestamp from previous item
            }
            currentKey = key;
            hoverStartTime = now;
            pointAwarded = false; // Resets flag for the new item
            return;
        }

        // If still hovering over the SAME item and points haven't been awarded yet
        if (!pointAwarded) {
            long duration = now - hoverStartTime;
            long timeSinceLastUnhover = now - lastUnhoverTime;

            // RULES:
            // 1. Must stay hovered over the item for at least 1 second (1000 ms).
            // 2. At least 1 second (1000 ms) must have passed since removing mouse from another item.
            if (duration >= 1000 && timeSinceLastUnhover >= 1000) {
                List<String> structure = ItemStructureLoader.getStructures().get(key);

                if (structure != null) {
                    String targetToken = VocabularyManager.getNextTokenToUpgrade(structure);
                    if (targetToken != null) {
                        VocabularyManager.registerEvent(targetToken, Event.HOVER);
                    }
                }

                pointAwarded = true; // Marks as awarded for this session (prevents re-awarding until mouse moves)
            }
        }
    }

    /**
     * Called on every game tick to detect when the mouse leaves an item tooltip.
     */
    public static void tick() {
        long now = System.currentTimeMillis();

        // If more than 100 ms (~2 ticks) pass without drawing any tooltip, the mouse has left the item
        if (currentKey != null && (now - lastFrameTime) > 100) {
            currentKey = null;
            pointAwarded = false;
            lastUnhoverTime = now; // Records exact timestamp when mouse was removed
        }
    }
}