package com.wesleyhdias.minnanocraft.srs;

import com.wesleyhdias.minnanocraft.language.TranslationCacheManager;
import com.wesleyhdias.minnanocraft.language.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.srs.models.ExpEvents;

import java.util.List;

/**
 * Generic tracker that monitors continuous exposure to a target element (e.g., HUD or Tooltip Hover).
 * <p>
 * Handles focus timing, detects item transitions or timeouts when the mouse leaves a tooltip,
 * triggers translation cache invalidation when required, and delegates vocabulary event registration
 * to {@link PlayerVocabularyManager} once the required focus duration is reached.
 */
public class ExposureTracker {

    /** Required continuous focus duration in milliseconds to award exposure points. */
    private final long requiredFocusTimeMs;

    /** Specific event type to register upon completing focus (e.g., HOVER, SEEN). */
    private final ExpEvents expEventsType;

    /** Unique key identifier of the target currently being tracked. */
    private String currentKey = "";

    /** Timestamp in milliseconds when focus on the current key began. */
    private long startTime = 0;

    /** Flag indicating whether exposure experience has already been awarded for the current focus cycle. */
    private boolean expAwarded = false;

    /** Timestamp in milliseconds of the last heartbeat update. */
    private long lastUpdateTime = 0;

    /** Inactivity timeout threshold in milliseconds to detect target loss. */
    private static final long TIMEOUT_MS = 150;

    private boolean delayAwardUntilUnfocus = false;
    private boolean pendingAward = false;

    /**
     * Constructs a new {@link ExposureTracker}.
     *
     * @param requiredFocusTimeMs The continuous focus duration in milliseconds needed to trigger exposure.
     * @param expEventsType       The type of event to register when focus duration is met.
     */
    public ExposureTracker(long requiredFocusTimeMs, ExpEvents expEventsType, boolean delayAwardUntilUnfocus) {
        this.requiredFocusTimeMs = requiredFocusTimeMs;
        this.expEventsType = expEventsType;
        this.delayAwardUntilUnfocus = delayAwardUntilUnfocus;
    }

    public ExposureTracker(long requiredFocusTimeMs, ExpEvents expEventsType) {
        this.requiredFocusTimeMs = requiredFocusTimeMs;
        this.expEventsType = expEventsType;
    }

    /**
     * Updates the tracker with the active target key assuming default conditions.
     *
     * @param targetKey The unique identifier of the target being observed.
     */
    public void update(String targetKey) {
        update(targetKey, true);
    }

    /**
     * Updates the tracker state, handling timeouts, target switching, and focus duration evaluation.
     *
     * @param targetKey      The unique identifier of the target being observed.
     * @param extraCondition Additional prerequisite condition required to award exposure progress.
     */
    public void update(String targetKey, boolean extraCondition) {
        long now = System.currentTimeMillis();

        // 1. TIMEOUT VERIFICATION (Mouse left the item/tooltip area)
        // If more than 150ms passed since the last heartbeat, the target was unhovered.
        if (now - lastUpdateTime > TIMEOUT_MS) {
            handleTargetLost();
        }
        lastUpdateTime = now; // Refresh the heartbeat timestamp

        if (targetKey == null || targetKey.isBlank()) {
            handleTargetLost();
            return;
        }

        // 2. TARGET TRANSITION (Switched from one item/target to another)
        if (!targetKey.equals(currentKey)) {
            handleTargetLost();
            currentKey = targetKey;
            startTime = now;
            expAwarded = false;
            pendingAward = false;
        }

        // 3. FOCUS DURATION & REQUIREMENT CHECK
        if (!expAwarded && (now - startTime) >= requiredFocusTimeMs && extraCondition) {
            if (delayAwardUntilUnfocus) {
                pendingAward = true; // Raise flag, but don't give the EXP yet
            } else {
                awardExp(currentKey); // Give the exp immediately
                expAwarded = true;
            }
        }
    }

    /**
     * Helper method to centralize the EXP rewarding logic.
     */
    private void awardExp(String key) {
        List<String> structure = ItemStructureLoader.getStructures().get(key);
        if (structure != null && !structure.isEmpty()) {
            String targetToken = TokenUpgradeSelector.getNextTokenToUpgrade(structure, PlayerVocabularyManager.getInstance());
            if (targetToken != null) {
                PlayerVocabularyManager.getInstance().registerEvent(targetToken, expEventsType);
            }
        }
    }

    /**
     * Handles resolving any pending EXP and resetting state when focus is lost.
     */
    private void handleTargetLost() {
        if (pendingAward) {
            awardExp(currentKey); // Give the EXP delayed
            pendingAward = false; // lower the flag after give the EXP
            expAwarded = true;
        }

        if (TranslationCacheManager.pendingClear) {
            TranslationCacheManager.clearAll();
        }
        reset();
    }

    /**
     * Resets internal tracking state and active target references.
     */
    public void reset() {
        currentKey = "";
        expAwarded = false;
    }

    /**
     * Retrieves the key of the target currently being tracked.
     *
     * @return The active target key string, or empty string if no target is active.
     */
    public String getCurrentKey() {
        return currentKey;
    }

    /**
     * Ticks periodically to check whether the active target was lost unexpectedly.
     * Ensures pending cache invalidations are cleared promptly even if UI screens close.
     */
    public void tick() {
        // If an item is tracked, but more than the timeout threshold has passed since the last update
        if (!currentKey.isEmpty() && (System.currentTimeMillis() - lastUpdateTime > TIMEOUT_MS)) {
            handleTargetLost();
        }
    }
}