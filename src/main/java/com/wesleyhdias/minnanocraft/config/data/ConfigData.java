package com.wesleyhdias.minnanocraft.config.data;

import java.util.List;

/**
 * Data structure acting as a schema template for Gson
 * serialization and deserialization of the mod's configuration JSON file.
 */
public class ConfigData {

    /** Indicates whether the main mod features are enabled. */
    private boolean enabled = true;

    /** Supported target language codes available in the mod. */
    private List<String> supportedLanguages = List.of("pt_br");

    // --- System Rules ---

    /** Maximum number of words actively gaining exposure at the same time. */
    private int maxActiveWords = 30;

    /**
     * Elapsed time in milliseconds before a word starts losing exposure points due to player inactivity.
     * Default: 1 day (86,400,000 ms).
     */
    private long inactivityTimeThreshold = 1000L * 60 * 60 * 24;

    /**
     * Elapsed time in milliseconds before an inactive word is demoted back to the WAITING learning state.
     * Default: 3 days (inactivityTimeThreshold * 3).
     */
    private long demotionTimeThreshold = inactivityTimeThreshold * 3;

    // --- Progression & Balancing Settings ---

    /** Bonus multiplier applied to exposure gain when relearning a previously forgotten word. */
    private float relearnMultiplier = 2.5f;

    /** Amount of exposure points lost per inactivity decay cycle. */
    private float expLossPerInactivityCycle = 5.0f;

    /** Minimum exposure retention floor relative to peak exposure (e.g., 0.7 = 70% retention). */
    private float maxExpLossPercentage = 0.7f;

    /** Exposure required to unlock Script Level 1 (e.g., Romaji). */
    private double expLevel1 = 15.0;

    /** Exposure required to unlock Script Level 2 (e.g., Romaji inverted structure). */
    private double expLevel2 = 30.0;

    /** Exposure required to unlock Script Level 3 (e.g., Hiragana/Katakana). */
    private double expLevel3 = 45.0;

    /** Exposure required to unlock Script Level 4 (e.g., Kanji). */
    private double expLevel4 = 100.0;

    /** Exposure required to consider a word fully mastered. */
    private double masteryExposure = 115.0;

    /** Exposure points awarded when encountering a word in the world. */
    private double eventSeen = 2.0;

    /** Exposure points awarded when seeing a word on the HUD. */
    private double eventHudSeen = 1.0;

    /** Exposure points awarded when hovering over a target item or word. */
    private double eventHover = 0.5;

    /** Exposure points deducted when actively using the dictionary lookup feature. */
    private double eventLookup = 5.0;

    /** Exposure points deducted when looking up a word during hover. */
    private double eventHoverLookup = 5.0;

    // =========================================================
    // Getters and Setters
    // =========================================================

    public double getEventSeen() {
        return eventSeen;
    }

    public void setEventSeen(double eventSeen) {
        this.eventSeen = eventSeen;
    }

    public double getEventHudSeen() {
        return eventHudSeen;
    }

    public void setEventHudSeen(double eventHudSeen) {
        this.eventHudSeen = eventHudSeen;
    }

    public double getEventHover() {
        return eventHover;
    }

    public void setEventHover(double eventHover) {
        this.eventHover = eventHover;
    }

    public double getEventLookup() {
        return eventLookup;
    }

    public void setEventLookup(double eventLookup) {
        this.eventLookup = eventLookup;
    }

    public double getEventHoverLookup() {
        return eventHoverLookup;
    }

    public void setEventHoverLookup(double eventHoverLookup) {
        this.eventHoverLookup = eventHoverLookup;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {}

    public void toggleEnabled() {
        this.enabled = !this.enabled;
    }

    public List<String> getSupportedLanguages() {
        return supportedLanguages;
    }

    public void setSupportedLanguages(List<String> supportedLanguages) {
        this.supportedLanguages = supportedLanguages;
    }

    public int getMaxActiveWords() {
        return maxActiveWords;
    }

    public void setMaxActiveWords(int maxActiveWords) {
        this.maxActiveWords = maxActiveWords;
    }

    public long getInactivityTimeThreshold() {
        return inactivityTimeThreshold;
    }

    public void setInactivityTimeThreshold(long inactivityTimeThreshold) {
        this.inactivityTimeThreshold = inactivityTimeThreshold;
    }

    public long getDemotionTimeThreshold() {
        return demotionTimeThreshold;
    }

    public void setDemotionTimeThreshold(long demotionTimeThreshold) {
        this.demotionTimeThreshold = demotionTimeThreshold;
    }

    public float getRelearnMultiplier() {
        return relearnMultiplier;
    }

    public void setRelearnMultiplier(float relearnMultiplier) {
        this.relearnMultiplier = relearnMultiplier;
    }

    public float getExpLossPerInactivityCycle() {
        return expLossPerInactivityCycle;
    }

    public void setExpLossPerInactivityCycle(float expLossPerInactivityCycle) {
        this.expLossPerInactivityCycle = expLossPerInactivityCycle;
    }

    public float getMaxExpLossPercentage() {
        return maxExpLossPercentage;
    }

    public void setMaxExpLossPercentage(float maxExpLossPercentage) {
        this.maxExpLossPercentage = maxExpLossPercentage;
    }

    public double getExpLevel1() {
        return expLevel1;
    }

    public void setExpLevel1(double expLevel1) {
        this.expLevel1 = expLevel1;
    }

    public double getExpLevel2() {
        return expLevel2;
    }

    public void setExpLevel2(double expLevel2) {
        this.expLevel2 = expLevel2;
    }

    public double getExpLevel3() {
        return expLevel3;
    }

    public void setExpLevel3(double expLevel3) {
        this.expLevel3 = expLevel3;
    }

    public double getExpLevel4() {
        return expLevel4;
    }

    public void setExpLevel4(double expLevel4) {
        this.expLevel4 = expLevel4;
    }

    public double getMasteryExposure() {
        return masteryExposure;
    }

    public void setMasteryExposure(double masteryExposure) {
        this.masteryExposure = masteryExposure;
    }
}