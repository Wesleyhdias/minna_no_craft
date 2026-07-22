package com.wesleyhdias.minnanocraft.services;

import com.wesleyhdias.minnanocraft.data.models.DifficultyLevel;
import com.wesleyhdias.minnanocraft.data.models.LearningState;
import com.wesleyhdias.minnanocraft.data.models.WordProgress;
import com.wesleyhdias.minnanocraft.data.models.Event;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * O cérebro matemático do mod. Controla o ganho de exposição,
 * transições de estados e mecânicas de esquecimento estilo Anki.
 */
public class ProgressionSystem {

    private final int maxActiveWords;
    private final double masteryExposure;
    private final int baseInactiveTimeout;
    private final double forgettingRate;
    private final double relearnMultiplier;

    public ProgressionSystem() {
        this.maxActiveWords = 15;
        this.masteryExposure = 100.0;
        this.baseInactiveTimeout = 40;
        this.forgettingRate = 0.02;
        this.relearnMultiplier = 2.5;
    }

    public ProgressionSystem(int maxActiveWords, double masteryExposure, int baseInactiveTimeout, double forgettingRate, double relearnMultiplier) {
        this.maxActiveWords = maxActiveWords;
        this.masteryExposure = masteryExposure;
        this.baseInactiveTimeout = baseInactiveTimeout;
        this.forgettingRate = forgettingRate;
        this.relearnMultiplier = relearnMultiplier;
    }

    // =========================================================
    // Métodos Auxiliares de Exposição
    // =========================================================
    private void addExposure(WordProgress progress, double baseAmount) {
        // Bônus de Reaprendizado se estiver abaixo do maior pico alcançado
        double multiplier = (progress.getExposure() < progress.getPeakExposure()) ? this.relearnMultiplier : 1.0;

        double newExposure = progress.getExposure() + (baseAmount * multiplier);
        progress.updateExposure(newExposure - progress.getExposure()); // Atualiza e trata o pico interno

        updateScriptLevel(progress);
    }

    private void updateScriptLevel(WordProgress progress) {
        int newLevel;
        if (progress.getExposure() >= 100) {
            newLevel = 3; // Kanji
        } else if (progress.getExposure() >= 50) {
            newLevel = 2; // Hiragana
        } else if (progress.getExposure() >= 15)  {
            newLevel = 1; // Romaji
        } else{
            newLevel = 0; // original
        }

        // Trava: o nível de escrita é sempre o MAIOR nível já alcançado
        if (newLevel > progress.getHighestScriptLevel()) {
            progress.setHighestScriptLevel(newLevel);
        }

        progress.setScriptLevel(progress.getHighestScriptLevel());
    }

    // =========================================================
    // Eventos (Ajustados para Mod Passivo)
    // =========================================================
    public void applyEvent(WordProgress progress, Event event, long now) {
        if (progress.getState() == LearningState.MASTERED) {
            return;
        }

        progress.setLastSeen(now);

        switch (event) {
            case HOVER -> {
                // Foco direto no inventário (ganha exposição normal)
                addExposure(progress, 1.0);
                progress.incrementSeenCount();
            }
            case SEEN -> {
                // Apareceu rápido na Hotbar (ganha menos exposição: +1.0)
                addExposure(progress, 0.2);
                progress.incrementSeenCount();
            }
            case LOOKUP -> {
                progress.incrementLookupCount();
                double droppedExposure = Math.max(0.0, progress.getExposure() - 5.0);
                progress.updateExposure(droppedExposure - progress.getExposure());
                updateScriptLevel(progress);
            }
        }
    }

    // =========================================================
    // Regras & Lógica Anki
    // =========================================================
    public double getInactiveTimeout(WordProgress progress) {
        // Quanto maior a estabilidade (peakExposure), mais tempo a palavra tolera inatividade
        double stabilityBonus = progress.getPeakExposure() * 1.5;
        return this.baseInactiveTimeout + stabilityBonus;
    }

    public boolean shouldDemote(WordProgress progress, long now) {
        Long lastSeen = progress.getLastSeen();
        if (lastSeen == null) {
            return false;
        }

        double timeout = getInactiveTimeout(progress);
        return (now - lastSeen) > timeout;
    }

    public void promote(WordProgress progress, long now) {
        progress.setState(LearningState.ACTIVE);

        Long lastSeen = progress.getLastSeen();
        if (lastSeen != null) {
            long timeInWaiting = now - lastSeen;
            double decay = timeInWaiting * this.forgettingRate;

            // Piso de Decaimento (nunca cai abaixo de 70% do pico histórico)
            double decayFloor = progress.getPeakExposure() * 0.70;
            double finalExp = Math.max(decayFloor, progress.getExposure() - decay);

            progress.updateExposure(finalExp - progress.getExposure());
        }

        updateScriptLevel(progress);
        progress.setPromotedAt(now);
    }

    public void demote(WordProgress progress) {
        progress.setState(LearningState.WAITING);
    }

    public void master(WordProgress progress) {
        progress.setState(LearningState.MASTERED);
    }

    public DifficultyLevel getDifficulty(WordProgress progress) {
        if (progress.getState() == LearningState.WAITING) {
            return DifficultyLevel.PORTUGUESE;
        }
        if (progress.getState() == LearningState.MASTERED) {
            return DifficultyLevel.KANJI;
        }
        if (progress.getScriptLevel() == 1) {
            return DifficultyLevel.ROMAJI;
        }
        if (progress.getScriptLevel() == 2) {
            return DifficultyLevel.HIRAGANA;
        }
        return DifficultyLevel.KANJI;
    }

    // =========================================================
    // Atualização de Estados (Loop Principal)
    // =========================================================
    public void updateStates(Map<String, WordProgress> vocabulary, long now) {
        for (WordProgress progress : vocabulary.values()) {
            if (progress.getState() == LearningState.ACTIVE) {
                if (progress.getExposure() >= this.masteryExposure) {
                    master(progress);
                } else if (shouldDemote(progress, now)) {
                    demote(progress);
                }
            }
        }

        // Calcula quantos slots livres temos para promover palavras em WAITING
        long activeCount = vocabulary.values().stream()
                .filter(p -> p.getState() == LearningState.ACTIVE)
                .count();

        int freeSlots = this.maxActiveWords - (int) activeCount;

        if (freeSlots <= 0) {
            return;
        }

        // Pega as palavras em WAITING ordenadas pelas mais vistas (relevância)
        List<WordProgress> waitingWords = vocabulary.values().stream()
                .filter(p -> p.getState() == LearningState.WAITING)
                .sorted(Comparator.comparingInt(WordProgress::getTotalSeen).reversed())
                .toList();

        // Promove as primeiras de acordo com os slots disponíveis
        int limit = Math.min(freeSlots, waitingWords.size());
        for (int i = 0; i < limit; i++) {
            promote(waitingWords.get(i), now);
        }
    }
}