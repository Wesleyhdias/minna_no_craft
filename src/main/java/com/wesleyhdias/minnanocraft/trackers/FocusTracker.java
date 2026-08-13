package com.wesleyhdias.minnanocraft.trackers;

import com.wesleyhdias.minnanocraft.data.loader.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.services.VocabularyManager;
import com.wesleyhdias.minnanocraft.data.models.Event;

import java.util.List;

/**
 * Tracker responsible for monitoring how long the player continuously looks at
 * a specific block or entity in the world (via the crosshair/HUD).
 * Awards vocabulary exposure points after a sustained focus duration.
 */
public class FocusTracker {

    /** The translation key of the target currently being looked at. */
    private static String currentLookKey = "";

    /** Timestamp of when the player first started looking at the current target. */
    private static long lookStartTime = 0;

    /** Flag to ensure experience is only awarded once per continuous gaze. */
    private static boolean expAwarded = false;

    /** The required continuous time (in milliseconds) the player must focus to earn a point. */
    private static final long REQUIRED_FOCUS_TIME_MS = 2000;

    /**
     * Updates the focus state based on what the player is currently looking at.
     * Called frequently (e.g., on every render frame or game tick) while in-world.
     *
     * @param targetKey The translation key of the block or entity in the crosshair,
     *                  or null/blank if looking at nothing.
     */
    public static void update(String targetKey) {
        // If the player looks away into the air, reset the tracker immediately
        if (targetKey == null || targetKey.isBlank()) {
            reset();
            return;
        }

        long now = System.currentTimeMillis();

        // If the player starts looking at a new/different target, reset the timer
        if (!targetKey.equals(currentLookKey)) {
            currentLookKey = targetKey;
            lookStartTime = now;
            expAwarded = false;
        }

        // If the required time has passed and points haven't been awarded yet for this gaze
        if (!expAwarded && (now - lookStartTime) >= REQUIRED_FOCUS_TIME_MS) {
            List<String> structure = ItemStructureLoader.getStructures().get(targetKey);

            if (structure != null && !structure.isEmpty()) {
                // Find a token that needs upgrading and register a HUD_LOOK event for it
                String targetToken = VocabularyManager.getNextTokenToUpgrade(structure);
                if (targetToken != null) {
                    VocabularyManager.registerEvent(targetToken, Event.HUD_LOOK);
                }
            }

            // Mark as awarded so we don't spam events while the player keeps looking
            expAwarded = true;
        }
    }

    /**
     * Resets the internal state of the tracker, clearing the current target and timer.
     */
    public static void reset() {
        currentLookKey = "";
        expAwarded = false;
    }
}