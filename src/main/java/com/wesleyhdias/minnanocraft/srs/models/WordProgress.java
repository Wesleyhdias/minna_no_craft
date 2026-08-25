package com.wesleyhdias.minnanocraft.srs.models;

import com.wesleyhdias.minnanocraft.config.data.ModConfig;

/**
 * Represents the dynamic learning progress of a specific word by the player.
 * This class is mutable and is serialized/deserialized to JSON via Gson.
 */
public class WordProgress {

    private LearningState state = LearningState.WAITING;
    private String word;

    private double exposure = 0.0;
    private double peakExposure = 0.0;

    private int seenCount = 0;
    private long lastSeen = 0;

    /**
     * Tracks how many times the player used the dictionary/lookup feature.
     * Note: The core mechanic for this feature is planned but not yet implemented.
     */
    private int lookupCount = 0;

    /**
     * Default constructor required for JSON deserialization (Gson).
     */
    public WordProgress() {}

    /**
     * Initializes a new WordProgress for a specific word.
     *
     * @param word The target word being tracked.
     */
    public WordProgress(String word) {
        this.word = word;
    }

    /**
     * Updates the current exposure and recalculates the peak exposure if necessary.
     * The exposure value will never drop below 0.0.
     *
     * @param delta The amount of exposure to add (or subtract, if negative).
     */
    public void updateExposure(double delta) {
        this.exposure = Math.max(0.0, this.exposure + delta);
        if (this.exposure > this.peakExposure) {
            this.peakExposure = this.exposure;
        }
        System.out.println("Exposure final: " + this.exposure);
    }

    /**
     * Gets the current script level based on the current exposure.
     *
     * @return The script level (0 = Native, 1 = Romaji, 2 = Hiragana, 3 = Kanji).
     */
    public int getScriptLevel() {
        return calculateLevelFromExposure(this.exposure);
    }

    /**
     * Internal logic to map an exposure value to a script level.
     *
     * @param exp The exposure value to evaluate.
     * @return The corresponding script level.
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
    public String getWord() { return word; }

    public LearningState getState() { return state; }
    public void setState(LearningState state) { this.state = state; }

    public double getExposure() { return exposure; }
    public double getPeakExposure() { return peakExposure; }

    public long getLastSeen() { return lastSeen; }
    public void setLastSeen(long lastSeen) { this.lastSeen = lastSeen; }

    public void incrementSeenCount() { this.seenCount++; }
    public int getSeenCount() { return seenCount; }

    public void incrementLookupCount() { this.lookupCount++; }
    public int getLookupCount() { return lookupCount; }

}