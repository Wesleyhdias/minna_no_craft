package com.wesleyhdias.minnanocraft.config.data;

import java.util.List;

/**
 * Internal data structure acting as a schema template
 * for Gson to read and write the configuration JSON.
 */
public class ConfigData {

    private boolean enabled = true;
    private List<String> supportedLanguages = List.of("pt_br");

    // --- REGRAS DO SISTEMA ---

    /**
     * Maximum number of words actively gaining Exposure at the same time.
     */
    private int maxActiveWords = 30;

    /**
     * Time (Ms) the player can go without seeing a word before it starts losing XP.
     */
    private long inactivityTimeThreshold = 1000L * 60 * 60 * 24;
    // 1000L * 60 * 60 * 24 = 1 Day

    /**
     * Time without seeing a word before it gets demoted back to WAITING (3 days).
     */
    private long demotionTimeThreshold = inactivityTimeThreshold * 3;


    // --- BALANCEAMENTO (MATEMÁTICA DE XP) ---

    /**
     * Bonus multiplier applied when relearning a forgotten word.
     */
    private float relearnMultiplier = 2.5f;

    /**
     * Amount of XP lost per missed time cycle.
     */
    private float expLossPerInactivityCycle = 5.0f;

    /**
     * The minimum percentage of the peak exposure a word can drop to (70%).
     */
    private float maxExpLossPercentage = 0.7f;

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
}
