package com.wesleyhdias.minnanocraft.srs;

import com.wesleyhdias.minnanocraft.language.TranslationCacheManager;
import com.wesleyhdias.minnanocraft.srs.models.LearningState;
import com.wesleyhdias.minnanocraft.srs.models.WordProgress;
import com.wesleyhdias.minnanocraft.srs.models.ExpEvents;
import com.wesleyhdias.minnanocraft.config.ModConfig;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Core engine managing the mod's Spaced Repetition System (SRS).
 * <p>
 * Responsible for handling exposure score updates, state transitions
 * ({@code WAITING} &rarr; {@code ACTIVE} &rarr; {@code MASTERED}), real-time
 * inactivity decay, and queue management for active vocabulary learning.
 */
public class ProgressionSystem {

    private static ProgressionSystem instance;

    /** Total exposure points required for a word to achieve the MASTERED state. */
    private final double masteryExposure;

    /**
     * Private constructor enforcing the singleton pattern and initializing config values.
     */
    private ProgressionSystem() {
        this.masteryExposure = ModConfig.getConfig().getMasteryExposure();
    }

    /**
     * Retrieves the global singleton instance of the progression system.
     *
     * @return The active {@link ProgressionSystem} instance.
     */
    public static ProgressionSystem getInstance() {
        if (instance == null) {
            instance = new ProgressionSystem();
        }
        return instance;
    }

    /**
     * Replaces the singleton instance with a custom or mocked instance for unit testing.
     *
     * @param mockInstance The instance to inject for testing purposes.
     */
    public static void setInstanceForTesting(ProgressionSystem mockInstance) {
        instance = mockInstance;
    }

    /**
     * Applies an interaction event to a word's progress data, enforcing cooldowns,
     * updating exposure or penalties, and triggering cache invalidation on level changes.
     *
     * @param progress  The {@link WordProgress} instance to update.
     * @param expEvents The type of exposure event triggered by player action.
     */
    public void applyEvent(WordProgress progress, ExpEvents expEvents) {
        long cooldownMs = 5000;
        long now = System.currentTimeMillis();
        long timeSinceLastSeen = now - progress.getLastSeen();

        // Enforces a brief cooldown between repeated exposure events for the same word
        if (timeSinceLastSeen < cooldownMs) {
            return;
        }

        int oldLevel = progress.getScriptLevel();
        progress.setLastSeen(now);

        switch (expEvents) {
            case HOVER -> addExposure(progress, ModConfig.getConfig().getEventHover());
            case SEEN -> addExposure(progress, ModConfig.getConfig().getEventSeen());
            case HUD_LOOK -> addExposure(progress, ModConfig.getConfig().getEventHudSeen());
            case HOVER_LOOKUP, LOOKUP -> {
                progress.incrementLookupCount();

                if (progress.getState() == LearningState.MASTERED) {
                    // Demotes mastered words back to active state when manually looked up
                    progress.setState(LearningState.ACTIVE);
                    double dropAmount = progress.getExposure() - (this.masteryExposure - 20.0);
                    progress.updateExposure(-Math.max(0, dropAmount));
                } else {
                    // Applies lookup penalties to active learning words
                    double penalty = (expEvents == ExpEvents.HOVER_LOOKUP) ?
                            ModConfig.getConfig().getEventHoverLookup() : ModConfig.getConfig().getEventLookup();

                    double droppedExposure = Math.max(0.0, progress.getExposure() - penalty);
                    progress.updateExposure(droppedExposure - progress.getExposure());
                }
            }
        }

        int newLevel = progress.getScriptLevel();
        // Flags translation caches for clearance if script level changes affect visual rendering
        if (oldLevel != newLevel) {
            TranslationCacheManager.pendingClear = true;
        }
    }

    /**
     * Adds exposure points to a word, applying a relearn bonus multiplier if recovering lost score.
     *
     * @param progress   The target {@link WordProgress} to increase.
     * @param baseAmount The base exposure score to add.
     */
    private void addExposure(WordProgress progress, double baseAmount) {
        double multiplier = (progress.getExposure() < progress.getPeakExposure()) ?
                ModConfig.getConfig().getRelearnMultiplier() : 1.0;

        progress.updateExposure(baseAmount * multiplier);
        progress.incrementSeenCount();
    }

    /**
     * Periodic maintenance task that calculates inactivity decay, demotes forgotten words,
     * promotes mastered words, and fills empty slots in the active queue with waiting words.
     *
     * @param vocabulary Map containing all tracked vocabulary progress indexed by token keys.
     */
    public void updateStates(Map<String, WordProgress> vocabulary) {
        long now = System.currentTimeMillis();
        long inactivityTimeThreshold = ModConfig.getConfig().getInactivityTimeThreshold();

        int activeCount = 0;

        // APPLY DECAY AND DEMOTIONS TO ACTIVE WORDS
        for (WordProgress progress : vocabulary.values()) {
            if (progress.getState() != LearningState.ACTIVE) continue;

            long lastSeen = progress.getLastSeen();
            long timeInactive = now - lastSeen;

            // If exposure reaches or exceeds the threshold, mark as MASTERED
            if (progress.getExposure() >= this.masteryExposure) {
                progress.setState(LearningState.MASTERED);
                continue;
            }

            // Demotes words to WAITING if inactive past the demotion timeout
            if (timeInactive > ModConfig.getConfig().getDemotionTimeThreshold()) {
                progress.setState(LearningState.WAITING);
                continue;
            }

            // Calculates exposure decay for inactive words past the grace period
            if (timeInactive > inactivityTimeThreshold) {
                double cyclesMissed = Math.floor((double) timeInactive / inactivityTimeThreshold);
                double totalDecay = cyclesMissed * ModConfig.getConfig().getExpLossPerInactivityCycle();
                double decayFloor = progress.getPeakExposure() * ModConfig.getConfig().getMaxExpLossPercentage();

                double newExposure = Math.max(decayFloor, progress.getExposure() - totalDecay);

                if (newExposure < progress.getExposure()) {
                    progress.updateExposure(newExposure - progress.getExposure());
                    progress.setLastSeen(now); // Resets the decay clock after applying decay
                }
            }

            // Word remained ACTIVE throughout all checks
            activeCount++;
        }

        // FILL EMPTY SLOTS IN THE ACTIVE QUEUE
        int freeSlots = ModConfig.getConfig().getMaxActiveWords() - activeCount;

        if (freeSlots > 0) {
            // Promotes the most frequently seen WAITING words to ACTIVE
            List<WordProgress> waitingWords = vocabulary.values().stream()
                    .filter(p -> p.getState() == LearningState.WAITING)
                    .sorted(Comparator.comparingInt(WordProgress::getSeenCount).reversed())
                    .toList();

            int limit = Math.min(freeSlots, waitingWords.size());
            for (int i = 0; i < limit; i++) {
                WordProgress p = waitingWords.get(i);
                p.setState(LearningState.ACTIVE);
            }
        }
    }
}