package com.wesleyhdias.minnanocraft.services;

import com.wesleyhdias.minnanocraft.data.loader.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.data.models.Event;

import java.util.List;

public class TooltipHoverTracker {

    private static String currentKey = null;
    private static long hoverStartTick = 0;
    private static boolean pointAwarded = false;
    private static long lastUnhoverTick = 0;
    private static long lastFrameTick = 0;


    // Chamado a cada frame em que o mouse está sobre um item no inventário
    public static void onTooltipRendered(String key, long currentTick) {
        lastFrameTick = currentTick;

        // Se o mouse mudou para OUTRO item no inventário
        if (!key.equals(currentKey)) {
            if (currentKey != null) {
                lastUnhoverTick = currentTick; // Registra a saída do item anterior
            }
            currentKey = key;
            hoverStartTick = currentTick;
            pointAwarded = false; // Reseta a flag para o novo item
            return;
        }

        // Se continua no MESMO item e o ponto AINDA NÃO foi entregue
        if (!pointAwarded) {
            long duration = currentTick - hoverStartTick;
            long timeSinceLastUnhover = currentTick - lastUnhoverTick;

            // REGRAS:
            // 1. Deve ficar pelo menos 1 segundo (20 ticks) PARADO sobre o item.
            // 2. Deve ter se passado pelo menos 1 segundo (20 ticks) desde que tirou o mouse de um item.
            if (duration >= 20 && timeSinceLastUnhover >= 20) {
                List<String> structure = ItemStructureLoader.getStructures().get(key);

                if (structure != null) {
                    String targetToken = VocabularyManager.getNextTokenToUpgrade(structure);
                    if (targetToken != null) {
                        VocabularyManager.registerEvent(targetToken, Event.HOVER, currentTick);
                    }
                }

                pointAwarded = true; // MARCA QUE JÁ PONTUOU NESSA SESSÃO (Não pontua mais até tirar o mouse)
            }
        }
    }


    // Chamado a cada tick do jogo para detectar quando o mouse SAIU do item
    public static void tick(long currentTick) {
        // Se passou mais de 2 ticks sem desenhar nenhuma tooltip, significa que o mouse saiu do item
        if (currentKey != null && (currentTick - lastFrameTick) > 2) {
            currentKey = null;
            pointAwarded = false;
            lastUnhoverTick = currentTick; // Registra o momento exato que tirou o mouse
        }
    }
}