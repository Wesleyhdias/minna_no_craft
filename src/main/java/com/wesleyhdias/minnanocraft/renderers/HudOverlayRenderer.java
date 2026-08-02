package com.wesleyhdias.minnanocraft.renderers;

import com.wesleyhdias.minnanocraft.builders.PortugueseItemNameBuilder;
import com.wesleyhdias.minnanocraft.data.loader.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.utils.TranslationModeResolver;
import com.wesleyhdias.minnanocraft.trackers.WorldTargetTracker;
import com.wesleyhdias.minnanocraft.builders.ItemNameBuilder;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;

import java.util.List;

public class HudOverlayRenderer {

    public static void renderOverlay(GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) return;

        String targetKey = WorldTargetTracker.getTargetTranslationKey();

        if (targetKey == null || targetKey.isBlank()) return;

        renderFloatingBox(graphics, client, targetKey);
    }

    private static void renderFloatingBox(GuiGraphicsExtractor graphics, Minecraft client, String translationKey) {
        String displayText;
        List<String> structure = ItemStructureLoader.getStructures().get(translationKey);

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
            displayText = translationKey;
        }

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