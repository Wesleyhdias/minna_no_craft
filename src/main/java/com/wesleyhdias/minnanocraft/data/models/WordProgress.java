package com.wesleyhdias.minnanocraft.data.models;

import org.jetbrains.annotations.Nullable;

/**
 * Representa o progresso dinâmico de aprendizado de uma palavra pelo jogador.
 * Essa classe é mutável e é salva/carregada via JSON pelo Gson.
 */
public class WordProgress {

    private String word;
    private LearningState state = LearningState.WAITING;

    private double exposure = 0.0;
    private double peakExposure = 0.0;

    private int scriptLevel = 0; // 1 = Romaji, 2 = Hiragana, 3 = Kanji
    private int highestScriptLevel = 1; // Trava permanente

    private int successes = 0;
    private int failures = 0;
    private int seenCount = 0;
    private int lookupCount = 0;
    private int totalSeen = 0;
    private int totalLookup = 0;


    @Nullable
    private Long firstSeen = null;

    @Nullable
    private Long lastSeen = null;

    @Nullable
    private Long promotedAt = null;

    public WordProgress() {}

    public WordProgress(String word) {
        this.word = word;
    }

    public void updateExposure(double delta) {
        this.exposure = Math.max(0.0, this.exposure + delta);
        if (this.exposure > this.peakExposure) {
            this.peakExposure = this.exposure;
        }
    }

    public String getWord() { return word; }

    public LearningState getState() { return state; }
    public void setState(LearningState state) { this.state = state; }

    public double getExposure() { return exposure; }
    public double getPeakExposure() { return peakExposure; }

    public int getScriptLevel() { return scriptLevel; }
    public void setScriptLevel(int scriptLevel) { this.scriptLevel = scriptLevel; }

    public int getHighestScriptLevel() { return highestScriptLevel; }
    public void setHighestScriptLevel(int highestScriptLevel) { this.highestScriptLevel = highestScriptLevel; }

    public DifficultyLevel getHighestDifficultyEnum() {
        return DifficultyLevel.fromInt(this.highestScriptLevel);
    }

    public Long getFirstSeen() { return firstSeen; }
    public void setFirstSeen(Long firstSeen) { this.firstSeen = firstSeen; }

    public Long getLastSeen() { return lastSeen; }
    public void setLastSeen(Long lastSeen) { this.lastSeen = lastSeen; }

    public Long getPromotedAt() { return promotedAt; }
    public void setPromotedAt(Long promotedAt) { this.promotedAt = promotedAt; }

    public int getTotalSeen() {
        return totalSeen;
    }

    public void incrementSeenCount() {
        this.seenCount++;
        this.totalSeen++;
    }

    public void incrementLookupCount() {
        this.lookupCount++;
        this.totalLookup++;
    }
}