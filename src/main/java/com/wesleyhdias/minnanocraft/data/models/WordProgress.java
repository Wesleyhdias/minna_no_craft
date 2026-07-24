package com.wesleyhdias.minnanocraft.data.models;

import org.jetbrains.annotations.Nullable;

/**
 * Representa o progresso dinâmico de aprendizado de uma palavra pelo jogador.
 * Essa classe é mutável e é salva/carregada via JSON pelo Gson.
 */
public class WordProgress {

    private LearningState state = LearningState.WAITING;
    private String word;

    private double exposure = 0.0;
    private double peakExposure = 0.0;

    private int seenCount = 0;
    private int lookupCount = 0;

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

    public int getScriptLevel() {
        return calculateLevelFromExposure(this.exposure);
    }

    /*
     * Retorna o maior nível já alcançado usando o recorde (peakExposure).
     * Funciona como a trava permanente sem precisar de variável extra!
     */
    public int getHighestScriptLevel() {
        return calculateLevelFromExposure(this.peakExposure);
    }

    public DifficultyLevel getHighestDifficultyEnum() {
        return DifficultyLevel.fromInt(getHighestScriptLevel());
    }

    private int calculateLevelFromExposure(double exp) {
        if (exp >= 75.0) return 3;  // Kanji
        if (exp >= 40.0)  return 2; // Hiragana
        if (exp >= 15.0)  return 1; // Romaji
        return 0;                   // Português
    }

    public Long getFirstSeen() { return firstSeen; }

    public void setFirstSeen(Long firstSeen) { this.firstSeen = firstSeen; }

    public Long getLastSeen() { return lastSeen; }

    public void setLastSeen(Long lastSeen) { this.lastSeen = lastSeen; }

    public Long getPromotedAt() { return promotedAt; }
    public void setPromotedAt(Long promotedAt) { this.promotedAt = promotedAt; }

    public void incrementSeenCount() {
        this.seenCount++;
    }

    public void incrementLookupCount() {
        this.lookupCount++;
    }

    public int getSeenCount() {
        return seenCount;
    }

}