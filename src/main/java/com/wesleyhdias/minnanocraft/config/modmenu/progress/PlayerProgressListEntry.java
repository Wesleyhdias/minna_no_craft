package com.wesleyhdias.minnanocraft.config.modmenu.progress;

import com.wesleyhdias.minnanocraft.language.dictionary.Word;
import com.wesleyhdias.minnanocraft.srs.models.WordProgress;

import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;

import org.jspecify.annotations.NonNull;

public class PlayerProgressListEntry extends ObjectSelectionList.Entry<PlayerProgressListEntry> {

    private final String firstText;
    private final String middleText;

    private final Word wordObj;
    private final WordProgress progressObj;

    private final int level;
    private final int listX;
    private final int listWidth;
    private final double exposure;

    public PlayerProgressListEntry(String firstText, String middleText, Word wordObj, WordProgress progressObj, int listX, int listWidth) {
        this.firstText = firstText;
        this.middleText = middleText;
        this.wordObj = wordObj;
        this.progressObj = progressObj;
        this.level = progressObj != null ? progressObj.getScriptLevel() : 0;
        this.exposure = progressObj != null ? progressObj.getExposure() : 0.0;
        this.listX = listX;
        this.listWidth = listWidth;
    }

    @Override
    public void extractContent(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
        Minecraft mc = Minecraft.getInstance();
        int x = this.listX;
        int y = this.getY();

        // --- CALCULANDO AS PORCENTAGENS DA LARGURA ---
        // Coluna 1 = 45% do tamanho da lista. Coluna 2 = 30%.
        int col1Width = (int) (this.listWidth * 0.45);
        int col2Width = (int) (this.listWidth * 0.30);

        // --- 1. PRIMEIRA COLUNA (Palavra) ---
        int wordMaxWidth = col1Width - 15; // 15px de margem de segurança
        renderScrollingText(graphics, mc, this.firstText, x + 6, y + 6, wordMaxWidth, 0xFFFFFFFF);

        // --- 2. SEGUNDA COLUNA (Tradução) ---
        // Ela começa exatamente onde a Coluna 1 termina
        int middleColumnX = x + col1Width;
        int middleMaxWidth = col2Width - 15;
        renderScrollingText(graphics, mc, this.middleText, middleColumnX, y + 6, middleMaxWidth, 0xFFFFFF55);

        // --- 3. TERCEIRA COLUNA (Status) ---
        String infoText = String.format("Lv. %d (%.1f)", this.level, this.exposure);
        int textWidth = mc.font.width(infoText);

        // Continua alinhado na direita, mas agora não vai sobrepor as outras
        int rightAlignX = x + this.listWidth - textWidth - 10;
        graphics.text(mc.font, infoText, rightAlignX, y + 6, 0xFFAAAAAA, false);
    }

    /**
     * Mágica do Efeito Marquee (Texto Rolante)
     */
    private void renderScrollingText(GuiGraphicsExtractor graphics, Minecraft mc, String text, int startX, int startY, int maxWidth, int color) {
        int textWidth = mc.font.width(text);

        if (textWidth <= maxWidth) {
            // A palavra é pequena e cabe no espaço, desenha normalmente.
            graphics.text(mc.font, text, startX, startY, color, false);
        } else {
            // A palavra é gigante! Vamos usar a matemática nativa de onda da Mojang
            double timeSec = System.currentTimeMillis() / 1000.0;
            int scrollOffset = getScrollOffset(maxWidth, textWidth, timeSec);

            // Aplica a tesoura e desenha
            graphics.enableScissor(startX, startY - 2, startX + maxWidth, startY + 12);
            graphics.text(mc.font, text, startX - scrollOffset, startY, color, false);
            graphics.disableScissor();
        }
    }

    private static int getScrollOffset(int maxWidth, int textWidth, double timeSec) {
        int overflow = textWidth - maxWidth;

        // --- CONTROLE DE VELOCIDADE ---
        // Duração do ciclo completo de ida e volta em segundos.
        // Coloquei 3.0 segundos base + um tempinho extra baseado no tamanho da palavra.
        // (Aumente o 3.0 para deixar a animação inteira mais lenta)
        double cycleDuration = 3.0 + (overflow * 0.05);

        // A fórmula mágica do Minecraft: cria uma onda perfeita que vai de 0.0 a 1.0 e volta
        // O aninhamento de sin e cos cria aquela "desacelerada" suave nas bordas.
        double wave = -Math.sin((Math.PI / 2.0) * Math.cos((Math.PI * 2.0) * timeSec / cycleDuration)) / 2.0 + 0.5;

        // O scroll será de 0 até o final da palavra, seguindo a onda
        return (int) (wave * overflow);
    }

    public String getFirstText() { return this.firstText; }

    public String getMiddleText() { return this.middleText; }

    public int getLevel() { return this.level; }

    public double getExposure() { return this.exposure; }

    public Word getWordObj() { return this.wordObj; }
    public WordProgress getProgressObj() { return this.progressObj; }

    @Override
    public @NonNull Component getNarration() {
        return Component.literal(this.firstText + ", Nível " + this.level);
    }
}