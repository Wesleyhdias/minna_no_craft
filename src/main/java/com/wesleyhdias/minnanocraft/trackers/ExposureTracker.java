package com.wesleyhdias.minnanocraft.trackers;

import com.wesleyhdias.minnanocraft.data.loader.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.services.VocabularyManager;
import com.wesleyhdias.minnanocraft.data.models.Event;

import java.util.List;

/**
 * A generic tracker that monitors continuous exposure to a target (HUD or Hover).
 * Handles timers and delegates event registration to the VocabularyManager.
 */
public class ExposureTracker {

    private final long requiredFocusTimeMs;
    private final Event eventType;

    private String currentKey = "";
    private long startTime = 0;
    private boolean expAwarded = false;

    public ExposureTracker(long requiredFocusTimeMs, Event eventType) {
        this.requiredFocusTimeMs = requiredFocusTimeMs;
        this.eventType = eventType;
    }

    /**
     * Standard update call for when there are no extra cooldown rules.
     */
    public void update(String targetKey) {
        update(targetKey, true);
    }

    /**
     * Updates the exposure state.
     *
     * @param targetKey      The translation key being looked at or hovered.
     * @param extraCondition Any additional condition that must be true to award points (e.g., unhover cooldown).
     */
    public void update(String targetKey, boolean extraCondition) {
        if (targetKey == null || targetKey.isBlank()) {
            reset();
            return;
        }

        long now = System.currentTimeMillis();

        // If changed item, reset time and xp flag
        if (!targetKey.equals(currentKey)) {
            currentKey = targetKey;
            startTime = now;
            expAwarded = false;
        }

        // Verify minimum time and extra condition
        if (!expAwarded && (now - startTime) >= requiredFocusTimeMs && extraCondition) {
            List<String> structure = ItemStructureLoader.getStructures().get(targetKey);

            if (structure != null && !structure.isEmpty()) {
                String targetToken = VocabularyManager.getNextTokenToUpgrade(structure);
                if (targetToken != null) {
                    VocabularyManager.registerEvent(targetToken, eventType);
                }
            }

            expAwarded = true; // Don't give xp to the same item again
        }
    }

    public void reset() {
        currentKey = "";
        expAwarded = false;
    }

    public String getCurrentKey() {
        return currentKey;
    }
}