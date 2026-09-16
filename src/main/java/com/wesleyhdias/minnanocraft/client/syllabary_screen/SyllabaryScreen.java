package com.wesleyhdias.minnanocraft.client.syllabary_screen;

import com.wesleyhdias.minnanocraft.language.kana.KanaLoader;
import com.wesleyhdias.minnanocraft.language.kana.Kana;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

/**
 * Interface modal para consulta rápida do silabário em Hiragana e Katakana.
 */
public class SyllabaryScreen extends Screen {

    private final Screen parent;

    private static final int CARD_WIDTH = 420;
    private static final int CARD_HEIGHT = 220;

    public SyllabaryScreen(Screen parent) {
        super(Component.literal("Silabário Japonês"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cardX = (this.width - CARD_WIDTH) / 2;
        int cardY = (this.height - CARD_HEIGHT) / 2;

        this.addRenderableWidget(Button.builder(
                        Component.literal("✕"),
                        button -> this.onClose())
                .bounds(cardX + CARD_WIDTH - 22, cardY + 8, 14, 14)
                .build());
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        if (this.parent != null) {
            this.parent.extractRenderState(graphics, -1, -1, delta);
        }

        graphics.fill(0, 0, this.width, this.height, 0x90000000);

        int cardX = (this.width - CARD_WIDTH) / 2;
        int cardY = (this.height - CARD_HEIGHT) / 2;

        graphics.fill(cardX, cardY, cardX + CARD_WIDTH, cardY + CARD_HEIGHT, 0xF0141414);
        graphics.outline(cardX, cardY, CARD_WIDTH, CARD_HEIGHT, 0xFF444444);

        graphics.text(this.font, "Silabário (ひらがな / カタカナ)", cardX + 12, cardY + 10, 0xFFFFAA00, true);
        graphics.fill(cardX + 10, cardY + 24, cardX + CARD_WIDTH - 10, cardY + 25, 0xFF333333);

        int gridStartX = cardX + 15;
        int gridStartY = cardY + 35;

        int cellWidth = 35;
        int cellHeight = 35;

        for (int row = 0; row < SyllabaryGrid.HGRID.length; row++) {

            for (int col = 0; col < SyllabaryGrid.HGRID[row].length; col++) {
                String romajiKey = SyllabaryGrid.HGRID[row][col];
                if (romajiKey.isEmpty()) continue;

                int cellX = gridStartX + (col * cellWidth);
                int cellY = gridStartY + (row * cellHeight);

                graphics.fill(cellX, cellY, cellX + cellWidth - 2, cellY + cellHeight - 2, 0xFF1C1C1C);
                graphics.outline(cellX, cellY, cellWidth - 2, cellHeight - 2, 0xFF3D3D3D);

                Kana kanaObj = KanaLoader.getRomajiMap().get(romajiKey);
                if (kanaObj != null) {
                    String kanaPair = kanaObj.hiragana() + " " + kanaObj.katakana();

                    // Hiragana + Katakana
                    float kanaScale = 1.0f;
                    int unscaledKanaWidth = this.font.width(kanaPair);
                    float scaledKanaWidth = unscaledKanaWidth * kanaScale;
                    float kanaX = (cellX + ((cellWidth - 2) - scaledKanaWidth) / 2.0f) / kanaScale;

                    float kanaY = (cellY + 6) / kanaScale;

                    graphics.pose().pushMatrix();
                    graphics.pose().scale(kanaScale, kanaScale);
                    graphics.text(this.font, kanaPair, (int) kanaX, (int) kanaY, 0xFF55FFFF, false);
                    graphics.pose().popMatrix();

                    // Romaji
                    float romajiScale = 0.7f;
                    int unscaledRomajiWidth = this.font.width(romajiKey);
                    float scaledRomajiWidth = unscaledRomajiWidth * romajiScale;
                    float romajiX = (cellX + ((cellWidth - 2) - scaledRomajiWidth) / 2.0f) / romajiScale;

                    float romajiY = (cellY + 22) / romajiScale;

                    graphics.pose().pushMatrix();
                    graphics.pose().scale(romajiScale, romajiScale);
                    graphics.text(this.font, romajiKey, (int) romajiX, (int) romajiY, 0xFFAAAAAA, false);
                    graphics.pose().popMatrix();
                }
            }
        }

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}