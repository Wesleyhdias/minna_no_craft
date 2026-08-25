package com.wesleyhdias.minnanocraft.srs;

import com.wesleyhdias.minnanocraft.language.TranslationCacheManager;
import com.wesleyhdias.minnanocraft.srs.models.LearningState;
import com.wesleyhdias.minnanocraft.srs.models.WordProgress;
import com.wesleyhdias.minnanocraft.config.data.ModConfig;
import com.wesleyhdias.minnanocraft.srs.models.ExpEvents;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * The core brain of the mod's Spaced Repetition System (SRS).
 * Controls exposure gain, state transitions (Waiting -> Active -> Mastered),
 * and real-time forgetting/decay mechanics based on player interactions.
 */
public class ProgressionSystem {

    // =========================================================
    // QUEUE SETTINGS & BALANCING
    // =========================================================

    /**
     * Exposure points required to reach the MASTERED state.
     */
    private final double masteryExposure = ModConfig.getConfig().getMasteryExposure();

    /**
     * Default constructor.
     */
    public ProgressionSystem() {
    }

    // =========================================================
    // PLAYER EVENTS
    // =========================================================

    /**
     * Applies a learning event to a specific word's progress, enforcing cooldowns
     * and triggering cache invalidation if the script level changes.
     *
     * @param progress The word progress data object to update.
     * @param expEvents    The type of event triggered by the player.
     */
    public void applyEvent(WordProgress progress, ExpEvents expEvents) {
        long cooldownMs = 5000;
        long now = System.currentTimeMillis();
        long timeSinceLastSeen = now - progress.getLastSeen();

        // Enforce a cooldown between repeated events for the same word
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
                    progress.setState(LearningState.ACTIVE);
                    double dropAmount = progress.getExposure() - (this.masteryExposure - 20.0);
                    progress.updateExposure(-Math.max(0, dropAmount));
                } else {
                    // Set the penalty value based on which lookup event was triggered
                    double penalty = (expEvents == ExpEvents.HOVER_LOOKUP) ?
                            ModConfig.getConfig().getEventHoverLookup() : ModConfig.getConfig().getEventLookup();

                    double droppedExposure = Math.max(0.0, progress.getExposure() - penalty);
                    progress.updateExposure(droppedExposure - progress.getExposure());
                }
            }
        }

        int newLevel = progress.getScriptLevel();
        // If the script level changed, flag the translation cache for clearance
        if (oldLevel != newLevel) {
            TranslationCacheManager.pendingClear = true;
        }
    }

    /**
     * Adds exposure points to a word, applying the relearn multiplier if recovering lost XP.
     *
     * @param progress   The word progress to update.
     * @param baseAmount The base amount of exposure to add.
     */
    private void addExposure(WordProgress progress, double baseAmount) {
        double multiplier = (progress.getExposure() < progress.getPeakExposure()) ?
                ModConfig.getConfig().getRelearnMultiplier() : 1.0;

        progress.updateExposure(baseAmount * multiplier);
        progress.incrementSeenCount();
    }

    // =========================================================
    // QUEUE & FORGETTING MANAGER (Auto-Save Loop)
    // =========================================================

    /**
     * Processes real-time decay, demotes inactive words, promotes waiting words,
     * and handles mastery thresholds. This should be called periodically (during the auto-save tick).
     *
     * @param vocabulary The full vocabulary map of the player.
     */
    public void updateStates(Map<String, WordProgress> vocabulary) {
        long now = System.currentTimeMillis();
        long inactivityTimeThreshold = ModConfig.getConfig().getInactivityTimeThreshold();

        // 1. APPLY DECAY AND DEMOTIONS TO ACTIVE WORDS
        for (WordProgress progress : vocabulary.values()) {
            if (progress.getState() != LearningState.ACTIVE) continue;

            long lastSeen = progress.getLastSeen();
            long timeInactive = now - lastSeen;

            // If XP reached mastery threshold, master it and free up an ACTIVE slot
            if (progress.getExposure() >= this.masteryExposure) {
                progress.setState(LearningState.MASTERED);
                continue;
            }

            // If the player hasn't seen the word for the demotion timeout, send it back to WAITING
            if (timeInactive > ModConfig.getConfig().getDemotionTimeThreshold()) {
                progress.setState(LearningState.WAITING);
            }
            // Otherwise, calculate if it lost XP due to inactivity (past the grace period)
            else if (timeInactive > inactivityTimeThreshold) {
                double cyclesMissed = Math.floor((double) timeInactive / inactivityTimeThreshold);
                double totalDecay = cyclesMissed * ModConfig.getConfig().getExpLossPerInactivityCycle();
                double decayFloor = progress.getPeakExposure() * ModConfig.getConfig().getMaxExpLossPercentage();

                double newExposure = Math.max(decayFloor, progress.getExposure() - totalDecay);

                if (newExposure < progress.getExposure()) {
                    progress.updateExposure(newExposure - progress.getExposure());
                    progress.setLastSeen(now); // Resets the decay clock
                }
            }
        }

        // 2. FILL EMPTY SLOTS IN THE ACTIVE QUEUE
        long activeCount = vocabulary.values().stream()
                .filter(p -> p.getState() == LearningState.ACTIVE)
                .count();

        int freeSlots = ModConfig.getConfig().getMaxActiveWords() - (int) activeCount;

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