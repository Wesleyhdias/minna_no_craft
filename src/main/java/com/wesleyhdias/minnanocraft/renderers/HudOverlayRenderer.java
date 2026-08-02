package com.wesleyhdias.minnanocraft.renderers;

import com.wesleyhdias.minnanocraft.builders.PortugueseItemNameBuilder;
import com.wesleyhdias.minnanocraft.data.loader.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.utils.TranslationModeResolver;
import com.wesleyhdias.minnanocraft.trackers.WorldTargetTracker;
import com.wesleyhdias.minnanocraft.services.VocabularyManager;
import com.wesleyhdias.minnanocraft.builders.ItemNameBuilder;
import com.wesleyhdias.minnanocraft.data.models.Event;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;

import java.util.List;

public class HudOverlayRenderer {

    // Variáveis de controle de tempo para o HUD_LOOK
    private static String currentLookKey = "";
    private static long lookStartTime = 0;
    private static boolean expAwarded = false;
    private static final long REQUIRED_FOCUS_TIME_MS = 2000; // 2 segundos

    // 1. ORQUESTRADOR: Apenas decide o fluxo
    public static void renderOverlay(GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) return;

        String targetKey = WorldTargetTracker.getTargetTranslationKey();

        if (targetKey == null || targetKey.isBlank()) {
            resetFocusTracker();
            return;
        }

        handleFocusProgression(targetKey);
        renderFloatingBox(graphics, client, targetKey);
    }

    // 2. LÓGICA DE PROGRESSÃO: Cuida apenas do temporizador e do XP
    private static void handleFocusProgression(String targetKey) {
        long now = System.currentTimeMillis();

        // Se mudou de alvo, reseta o cronômetro
        if (!targetKey.equals(currentLookKey)) {
            currentLookKey = targetKey;
            lookStartTime = now;
            expAwarded = false;
        }

        // Se olhou tempo suficiente e ainda não ganhou XP
        if (!expAwarded && (now - lookStartTime) >= REQUIRED_FOCUS_TIME_MS) {
            List<String> structure = ItemStructureLoader.getStructures().get(targetKey);

            if (structure != null && !structure.isEmpty()) {
                 String targetToken = VocabularyManager.getNextTokenToUpgrade(structure);
                 VocabularyManager.registerEvent(targetToken, Event.HUD_LOOK);
            }

            expAwarded = true;
        }
    }

    private static void resetFocusTracker() {
        currentLookKey = "";
        expAwarded = false;
    }

    // 3. LÓGICA DE TEXTO: Cuida apenas de descobrir qual string exibir
    private static String resolveDisplayText(String translationKey) {
        List<String> structure = ItemStructureLoader.getStructures().get(translationKey);
        String displayText;

        if (structure != null && !structure.isEmpty()) {
            if (TranslationModeResolver.useJapanese(translationKey)) {
                displayText = ItemNameBuilder.build(translationKey);
            } else {
                displayText = PortugueseItemNameBuilder.build(translationKey, Component.translatable(translationKey).getString());
            }
        } else {
            displayText = Component.translatable(translationKey).getString();
        }

        if (displayText == null || displayText.isBlank()) {
            return translationKey;
        }
        return displayText;
    }

    // 4. LÓGICA DE DESENHO: Cuida apenas das formas e cores na tela
    private static void renderFloatingBox(GuiGraphicsExtractor graphics, Minecraft client, String translationKey) {
        String displayText = resolveDisplayText(translationKey);

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int textWidth = client.font.width(displayText);

        int padding = 8;
        int boxWidth = textWidth + (padding * 2);
        int boxHeight = 18;
        int boxX = (screenWidth - boxWidth) / 2;
        int boxY = 10;

        graphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xC0000000);
        graphics.outline(boxX, boxY, boxWidth, boxHeight, 0x50FFFFFF);

        int textX = boxX + padding;
        int textY = boxY + (boxHeight - 8) / 2;

        graphics.text(client.font, Component.literal(displayText), textX, textY, 0xFFFFFFFF, true);
    }
}