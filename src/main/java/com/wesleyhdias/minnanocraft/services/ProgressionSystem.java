package com.wesleyhdias.minnanocraft.services;

import com.wesleyhdias.minnanocraft.data.models.LearningState;
import com.wesleyhdias.minnanocraft.data.models.WordProgress;
import com.wesleyhdias.minnanocraft.data.models.Event;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * The brain of the mod.
 * Controls exposure gain, state transitions, and real-time forgetting mechanics.
 */
public class ProgressionSystem {

    // =========================================================
    // QUEUE SETTINGS & BALANCING
    // =========================================================

    /** Maximum number of words actively gaining Exposure at the same time. */
    private final int maxActiveWords = 30;

    /** Exposure points required to reach the MASTERED state. */
    private final double masteryExposure = 100.0;

    // REAL-TIME SETTINGS (In Milliseconds)
    // 1 Day = 1000L * 60 * 60 * 24; (Use 5000L for 5-second testing)

    /** Time the player can go without seeing a word before it starts losing XP. */
    private final long gracePeriodMs = 1000L * 60 * 60 * 24;

    /** Time without seeing a word before it gets demoted back to WAITING (3 days). */
    private final long demotionTimeoutMs = gracePeriodMs * 3;

    /** Amount of XP lost per missed time cycle. */
    private final double decayPerCycle = 5.0;

    /** The minimum percentage of the peak exposure a word can drop to (70%). */
    private final double decayFloorRatio = 0.70;

    /** Bonus multiplier applied when relearning a forgotten word. */
    private final double relearnMultiplier = 2.5;

    /**
     * Default constructor.
     */
    public ProgressionSystem() {}

    // =========================================================
    // PLAYER EVENTS
    // =========================================================

    /**
     * Applies a learning event to a specific word's progress.
     *
     * @param progress The word progress to update.
     * @param event    The type of event triggered by the player.
     */
    public void applyEvent(WordProgress progress, Event event) {

        long cooldownMs = 5000;
        long now = System.currentTimeMillis();
        long timeSinceLastSeen = now - progress.getLastSeen();

        if (timeSinceLastSeen < cooldownMs) {
            return;
        }

        progress.setLastSeen(now);

        switch (event) {
            case HOVER -> addExposure(progress, 2.0);
            case HUD_LOOK ->  addExposure(progress, 1.0);
            case SEEN -> addExposure(progress, 0.5);
            case LOOKUP -> {
                // TODO: UI trigger for LOOKUP is not yet implemented.
                progress.incrementLookupCount();

                // LOOKUP BREAKS MASTERY! The player admitted they forgot the word.
                if (progress.getState() == LearningState.MASTERED) {
                    progress.setState(LearningState.ACTIVE);

                    // Drops XP below the mastery threshold (down to 80.0)
                    double dropAmount = progress.getExposure() - (this.masteryExposure - 20.0);
                    progress.updateExposure(-Math.max(0, dropAmount));

                } else {
                    double droppedExposure = Math.max(0.0, progress.getExposure() - 5.0);
                    progress.updateExposure(droppedExposure - progress.getExposure());
                }
            }
        }
    }

    /**
     * Adds exposure points to a word, applying the relearn multiplier if recovering lost XP.
     *
     * @param progress   The word progress to update.
     * @param baseAmount The base amount of exposure to add.
     */
    private void addExposure(WordProgress progress, double baseAmount) {
        double multiplier = (progress.getExposure() < progress.getPeakExposure()) ? this.relearnMultiplier : 1.0;
        progress.updateExposure(baseAmount * multiplier);
        progress.incrementSeenCount();
    }

    // =========================================================
    // QUEUE & FORGETTING MANAGER (Auto-Save Loop)
    // =========================================================

    /**
     * Processes real-time decay, demotes inactive words, and promotes waiting words.
     * This should be called periodically (during the auto-save tick).
     *
     * @param vocabulary The full vocabulary map of the player.
     */
    public void updateStates(Map<String, WordProgress> vocabulary) {
        long now = System.currentTimeMillis();

        // 1. APPLY DECAY AND DEMOTIONS TO ACTIVE WORDS
        for (WordProgress progress : vocabulary.values()) {
            if (progress.getState() != LearningState.ACTIVE) continue;

            Long lastSeen = progress.getLastSeen();
            if (lastSeen == null) continue;

            long timeInactive = now - lastSeen;

            // If XP reached 100, master it and free up an ACTIVE slot
            if (progress.getExposure() >= this.masteryExposure) {
                progress.setState(LearningState.MASTERED);
                continue;
            }

            // If the player hasn't seen the word for the demotion timeout, send it back to WAITING
            if (timeInactive > this.demotionTimeoutMs) {
                progress.setState(LearningState.WAITING);
            }
            // Otherwise, calculate if it lost XP due to inactivity (past the grace period)
            else if (timeInactive > this.gracePeriodMs) {
                double cyclesMissed = Math.floor((double) timeInactive / gracePeriodMs);
                double totalDecay = cyclesMissed * decayPerCycle;
                double decayFloor = progress.getPeakExposure() * decayFloorRatio;

                double newExposure = Math.max(decayFloor, progress.getExposure() - totalDecay);

                if (newExposure < progress.getExposure()) {
                    progress.updateExposure(newExposure - progress.getExposure());
                    progress.setLastSeen(now); // Resets the decay clock
                }
            }
        }

        // 2. FILL EMPTY SLOTS IN THE QUEUE
        long activeCount = vocabulary.values().stream()
                .filter(p -> p.getState() == LearningState.ACTIVE)
                .count();

        int freeSlots = this.maxActiveWords - (int) activeCount;

        if (freeSlots > 0) {
            // Gets the most seen WAITING words and promotes them to ACTIVE
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