package com.wesleyhdias.minnanocraft.srs.models;

import com.wesleyhdias.minnanocraft.config.ModConfig;

/**
 * Represents the dynamic learning progress of a specific word or token for a player.
 * <p>
 * Tracks learning states, exposure values, peak exposure, interaction counters,
 * and timestamp metadata required for Spaced Repetition System (SRS) calculations.
 * Designed to be serialized and deserialized to/from JSON via Gson.
 */
public class WordProgress {

    /** Current learning lifecycle state of the word (e.g., WAITING, ACTIVE, MASTERED). */
    private LearningState state = LearningState.WAITING;

    /** Unique token key or word string being tracked. */
    private String word;

    /** Current accumulated exposure score. */
    private double exposure = 0.0;

    /** Highest exposure score ever achieved by this word, used to establish decay floors. */
    private double peakExposure = 0.0;

    /** Total number of times this word has been observed or interacted with. */
    private int seenCount = 0;

    /** Timestamp in milliseconds of the last registered interaction event. */
    private long lastSeen = 0;

    /** Tracks how many times the player looked up this word's definition or translation. */
    private int lookupCount = 0;

    /**
     * Default constructor required for JSON deserialization via Gson.
     */
    public WordProgress() {}

    /**
     * Constructs a new tracking instance for a specific word key.
     *
     * @param word The target token key or word string to track.
     */
    public WordProgress(String word) {
        this.word = word;
    }

    /**
     * Updates the current exposure score by a delta amount and updates peak exposure if applicable.
     * Prevents exposure from dropping below 0.0.
     *
     * @param delta The positive or negative score adjustment to apply.
     */
    public void updateExposure(double delta) {
        this.exposure = Math.max(0.0, this.exposure + delta);
        if (this.exposure > this.peakExposure) {
            this.peakExposure = this.exposure;
        }
    }

    /**
     * Calculates the active script representation level based on current exposure points.
     *
     * @return The script level index (0 = Native, 1 = Romaji, 2 = Romaji Inverted, 3 = Hiragana, 4 = Kanji).
     */
    public int getScriptLevel() {
        return calculateLevelFromExposure(this.exposure);
    }

    /**
     * Evaluates exposure points against threshold levels defined in the mod configuration.
     *
     * @param exp The exposure score to evaluate.
     * @return The corresponding script level index from 0 to 4.
     */
    private int calculateLevelFromExposure(double exp) {
        if (exp >= ModConfig.getConfig().getExpLevel4()) return 4;  // Kanji
        if (exp >= ModConfig.getConfig().getExpLevel3()) return 3;  // Hiragana
        if (exp >= ModConfig.getConfig().getExpLevel2()) return 2;  // Romaji inverted structure
        if (exp >= ModConfig.getConfig().getExpLevel1()) return 1;  // Romaji
        return 0;                                                   // Native Language (Portuguese)
    }

    // =========================================================
    // Getters & Setters
    // =========================================================

    /**
     * Retrieves the word or token key.
     *
     * @return The word string key.
     */
    public String getWord() {
        return word;
    }

    /**
     * Retrieves the current SRS learning state.
     *
     * @return The active {@link LearningState}.
     */
    public LearningState getState() {
        return state;
    }

    /**
     * Sets the SRS learning state.
     *
     * @param state The new {@link LearningState} to apply.
     */
    public void setState(LearningState state) {
        this.state = state;
    }

    /**
     * Retrieves the current exposure score.
     *
     * @return The exposure value.
     */
    public double getExposure() {
        return exposure;
    }

    /**
     * Retrieves the highest exposure score ever reached by this word.
     *
     * @return The peak exposure value.
     */
    public double getPeakExposure() {
        return peakExposure;
    }

    /**
     * Retrieves the timestamp of the last interaction event.
     *
     * @return The timestamp in milliseconds.
     */
    public long getLastSeen() {
        return lastSeen;
    }

    /**
     * Updates the timestamp of the last interaction event.
     *
     * @param lastSeen The new timestamp in milliseconds.
     */
    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    /**
     * Increments the total count of times this word was seen or interacted with.
     */
    public void incrementSeenCount() {
        this.seenCount++;
    }

    /**
     * Retrieves the total count of times this word was seen.
     *
     * @return The seen count.
     */
    public int getSeenCount() {
        return seenCount;
    }

    /**
     * Increments the total lookup count for this word.
     */
    public void incrementLookupCount() {
        this.lookupCount++;
    }

    /**
     * Retrieves the total number of dictionary/lookup actions performed on this word.
     *
     * @return The lookup count.
     */
    public int getLookupCount() {
        return lookupCount;
    }
}