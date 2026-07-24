package com.wesleyhdias.minnanocraft.services;

import com.wesleyhdias.minnanocraft.data.models.Event;
import com.wesleyhdias.minnanocraft.data.models.LearningState;
import com.wesleyhdias.minnanocraft.data.models.WordProgress;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ProgressionSystem {

    // =========================================================
    // CONFIGURAÇÕES DA FILA E BALANCEAMENTO
    // =========================================================
    private final int maxActiveWords = 30; // Limite de palavras mudando na tela ao mesmo tempo
    private final double masteryExposure = 100.0; // Pontos para virar MASTERED

    // TEMPOS REAIS (Em Milissegundos)
    // 1 Dia = 1000L * 60 * 60 * 24; (Use 5000L para testar como 5 segundos)
    private final long gracePeriodMs = 1000L * 60 * 60 * 24;

    // Tempo para chutar da lista (Ex: 3 dias sem ver a palavra)
    private final long demotionTimeoutMs = gracePeriodMs * 3;

    private final double decayPerCycle = 5.0;
    private final double decayFloorRatio = 0.70;
    private final double relearnMultiplier = 2.5;

    public ProgressionSystem() {}

    // =========================================================
    // EVENTOS DO JOGADOR
    // =========================================================
    public void applyEvent(WordProgress progress, Event event) {
        long now = System.currentTimeMillis();
        progress.setLastSeen(now);

        System.out.println("to aplicando evento aqui ó:" + event);
        System.out.println("nesse item:" + progress.getWord());

        switch (event) {
            case HOVER -> addExposure(progress, 2.0);
            case SEEN -> addExposure(progress, 0.5);
            case LOOKUP -> {
                progress.incrementLookupCount();

                // O LOOKUP QUEBRA O MASTERY! O jogador confessou que esqueceu.
                if (progress.getState() == LearningState.MASTERED) {
                    progress.setState(LearningState.ACTIVE);
                    // Derruba o XP para baixo da linha de mestre (ex: 80.0)
                    double dropAmount = progress.getExposure() - (this.masteryExposure - 20.0);
                    progress.updateExposure(-Math.max(0, dropAmount));
                } else {
                    double droppedExposure = Math.max(0.0, progress.getExposure() - 5.0);
                    progress.updateExposure(droppedExposure - progress.getExposure());
                }
            }
        }
    }

    private void addExposure(WordProgress progress, double baseAmount) {
        double multiplier = (progress.getExposure() < progress.getPeakExposure()) ? this.relearnMultiplier : 1.0;
        progress.updateExposure(baseAmount * multiplier);
        progress.incrementSeenCount();
    }

    // =========================================================
    // GERENCIADOR DE FILA E ESQUECIMENTO (Loop do Auto-Save)
    // =========================================================
    public void updateStates(Map<String, WordProgress> vocabulary) {
        long now = System.currentTimeMillis();

        // 1. APLICA O DECAY E DEMOTIONS NAS PALAVRAS ATIVAS
        for (WordProgress progress : vocabulary.values()) {
            if (progress.getState() != LearningState.ACTIVE) continue;

            Long lastSeen = progress.getLastSeen();
            if (lastSeen == null) continue;

            long timeInactive = now - lastSeen;

            // Se o XP passou de 100, vira mestre e libera vaga na fila ACTIVE
            if (progress.getExposure() >= this.masteryExposure) {
                progress.setState(LearningState.MASTERED);
                continue;
            }

            // Se o jogador ficou 3 dias sem ver a palavra, ela volta pra fila de espera (abre vaga)
            if (timeInactive > this.demotionTimeoutMs) {
                progress.setState(LearningState.WAITING);
            }
            // Se não, calculamos se ela perdeu um pouquinho de XP por inatividade (> 24h)
            else if (timeInactive > this.gracePeriodMs) {
                double cyclesMissed = Math.floor((double) timeInactive / gracePeriodMs);
                double totalDecay = cyclesMissed * decayPerCycle;
                double decayFloor = progress.getPeakExposure() * decayFloorRatio;

                double newExposure = Math.max(decayFloor, progress.getExposure() - totalDecay);

                if (newExposure < progress.getExposure()) {
                    progress.updateExposure(newExposure - progress.getExposure());
                    progress.setLastSeen(now); // Reseta o relógio do decay
                }
            }
        }

        // 2. PREENCHE AS VAGAS VAZIAS NA FILA
        long activeCount = vocabulary.values().stream()
                .filter(p -> p.getState() == LearningState.ACTIVE)
                .count();

        int freeSlots = this.maxActiveWords - (int) activeCount;

        if (freeSlots > 0) {
            // Pega as palavras em WAITING que o jogador mais olhou no jogo e promove para ACTIVE
            List<WordProgress> waitingWords = vocabulary.values().stream()
                    .filter(p -> p.getState() == LearningState.WAITING)
                    .sorted(Comparator.comparingInt(WordProgress::getSeenCount).reversed())
                    .toList();

            int limit = Math.min(freeSlots, waitingWords.size());
            for (int i = 0; i < limit; i++) {
                WordProgress p = waitingWords.get(i);
                p.setState(LearningState.ACTIVE);
                p.setPromotedAt(now);
            }
        }
    }
}