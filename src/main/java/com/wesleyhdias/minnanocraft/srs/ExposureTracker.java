package com.wesleyhdias.minnanocraft.srs;

import com.wesleyhdias.minnanocraft.language.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.language.TranslationCacheManager;
import com.wesleyhdias.minnanocraft.srs.models.ExpEvents;

import java.util.List;

/**
 * A generic tracker that monitors continuous exposure to a target (HUD or Hover).
 * <p>
 * Handles focus timers, detects item transitions or timeouts (mouse leaving a tooltip area),
 * triggers cache invalidations when necessary, and delegates vocabulary event registration
 * to the {@link VocabularyManager} once the required focus duration is met.
 */
public class ExposureTracker {

    private final long requiredFocusTimeMs;
    private final ExpEvents expEventsType;

    private String currentKey = "";
    private long startTime = 0;
    private boolean expAwarded = false;

    /** Timeout threshold to detect when the game tooltip stops rendering or the mouse leaves the target. */
    private long lastUpdateTime = 0;
    private static final long TIMEOUT_MS = 150;

    /**
     * Constructs a new ExposureTracker.
     *
     * @param requiredFocusTimeMs The continuous time in milliseconds required to trigger an exposure event.
     * @param expEventsType           The specific type of vocabulary event to register (e.g., HOVER, SEEN).
     */
    public ExposureTracker(long requiredFocusTimeMs, ExpEvents expEventsType) {
        this.requiredFocusTimeMs = requiredFocusTimeMs;
        this.expEventsType = expEventsType;
    }

    /**
     * Updates the tracker with the current target key, assuming standard conditions are met.
     *
     * @param targetKey The unique identifier of the target being observed.
     */
    public void update(String targetKey) {
        update(targetKey, true);
    }

    /**
     * Updates the tracker state, handling timeouts, item switching, and focus duration evaluation.
     *
     * @param targetKey      The unique identifier of the target being observed.
     * @param extraCondition Additional prerequisite condition required to award exposure progress.
     */
    public void update(String targetKey, boolean extraCondition) {
        long now = System.currentTimeMillis();

        // 1. TIMEOUT VERIFICATION (Mouse left the item)
        // If more than 150ms have passed since the last update, the player has moved away from the target.
        if (now - lastUpdateTime > TIMEOUT_MS) {
            if (TranslationCacheManager.pendingClear) {
                TranslationCacheManager.clearAll();
            }
            reset();
        }
        lastUpdateTime = now; // Refresh the "heartbeat" timestamp of the active tracker

        if (targetKey == null || targetKey.isBlank()) {
            reset();
            return;
        }

        // 2. TARGET TRANSITION (Switched from one item/target to another)
        if (!targetKey.equals(currentKey)) {
            if (TranslationCacheManager.pendingClear) {
                TranslationCacheManager.clearAll();
            }
            currentKey = targetKey;
            startTime = now;
            expAwarded = false;
        }

        // 3. FOCUS DURATION & REQUIREMENT CHECK
        if (!expAwarded && (now - startTime) >= requiredFocusTimeMs && extraCondition) {
            List<String> structure = ItemStructureLoader.getStructures().get(targetKey);

            if (structure != null && !structure.isEmpty()) {
                String targetToken = VocabularyManager.getNextTokenToUpgrade(structure);
                if (targetToken != null) {
                    VocabularyManager.registerEvent(targetToken, expEventsType);
                }
            }

            expAwarded = true;
        }
    }

    /**
     * Resets the internal state and tracking timers of the tracker.
     */
    public void reset() {
        currentKey = "";
        expAwarded = false;
    }

    /**
     * Retrieves the key of the current target being tracked.
     *
     * @return The active target key string, or empty if none.
     */
    public String getCurrentKey() {
        return currentKey;
    }

    /**
     * Called every game tick to proactively check if the target has lost focus (e.g., mouse left the item).
     * Ensures cached translations are cleared immediately even if the player closes the inventory interface.
     */
    public void tick() {
        // If an item is tracked, but more than the timeout threshold has passed since the last update...
        if (!currentKey.isEmpty() && (System.currentTimeMillis() - lastUpdateTime > TIMEOUT_MS)) {
            if (TranslationCacheManager.pendingClear) {
                TranslationCacheManager.clearAll();
            }
            reset(); // Target lost! Proactively clear and reset state.
        }
    }
}