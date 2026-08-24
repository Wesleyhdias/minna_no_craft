package com.wesleyhdias.minnanocraft.config.modmenu.progress;

import com.wesleyhdias.minnanocraft.language.dictionary.Word;
import com.wesleyhdias.minnanocraft.srs.models.WordProgress;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PlayerProgressScreen extends Screen {

    private final Screen parent;
    private PlayerProgressListWidget listWidget;

    // Precisamos guardar essas medidas para saber onde o jogador clicou
    private int listX;
    private int listWidth;

    // Controle de estado da ordenação
    private PlayerProgressListWidget.SortColumn currentSortCol = PlayerProgressListWidget.SortColumn.NONE;
    private PlayerProgressListWidget.SortDir currentSortDir = PlayerProgressListWidget.SortDir.NONE;

    public PlayerProgressScreen(Screen parent) {
        super(Component.literal("Meu Progresso e Vocabulário"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(
                Button.builder(Component.translatable("gui.back"), button -> this.minecraft.setScreen(this.parent))
                        .bounds(this.width / 2 - 100, this.height - 30, 200, 20)
                        .build()
        );

        this.listX = 10;
        this.listWidth = (this.width / 2) - 5;

        // Descemos o Y da lista de 40 para 45 para dar mais respiro para o cabeçalho
        int listY = 45;
        int listHeight = this.height - 85;

        this.listWidget = new PlayerProgressListWidget(this.minecraft, this.listWidth, listHeight, this.listX, listY, 20);
        this.addRenderableWidget(this.listWidget);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();

        if (button == 0 && mouseY >= 25 && mouseY <= 40) {
            // Recriando os mesmos limites fracionados
            int col1End = this.listX + (int) (this.listWidth * 0.45);
            int col2End = col1End + (int) (this.listWidth * 0.30);

            if (mouseX >= this.listX && mouseX < col1End) {
                toggleSort(PlayerProgressListWidget.SortColumn.WORD);
                return true;
            }
            else if (mouseX >= col1End && mouseX < col2End) {
                toggleSort(PlayerProgressListWidget.SortColumn.MIDDLE);
                return true;
            }
            else if (mouseX >= col2End && mouseX <= this.listX + this.listWidth) {
                toggleSort(PlayerProgressListWidget.SortColumn.LEVEL);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void toggleSort(PlayerProgressListWidget.SortColumn col) {
        if (currentSortCol == col) {
            // Rotaciona: ASC -> DESC -> NONE
            currentSortDir = currentSortDir == PlayerProgressListWidget.SortDir.ASC ? PlayerProgressListWidget.SortDir.DESC
                    : currentSortDir == PlayerProgressListWidget.SortDir.DESC ? PlayerProgressListWidget.SortDir.NONE
                    : PlayerProgressListWidget.SortDir.ASC;
        } else {
            // Nova coluna clicada, começa com ASC
            currentSortCol = col;
            currentSortDir = PlayerProgressListWidget.SortDir.ASC;
        }

        if (currentSortDir == PlayerProgressListWidget.SortDir.NONE) {
            currentSortCol = PlayerProgressListWidget.SortColumn.NONE;
        }

        this.listWidget.applySorting(currentSortCol, currentSortDir);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x99000000);
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.centeredText(this.font, this.title, this.width / 2, 10, 0xFFFFFFFF);

        // --- RENDERIZA O CABEÇALHO CLICÁVEL ---
        int headerY = 28;
        int hoverColor = 0xFFFFFFFF;
        int normalColor = 0xFFAAAAAA;

        // Limites para detectar onde o mouse está passando
        int col1End = this.listX + (int) (this.listWidth * 0.45);
        int col2End = col1End + (int) (this.listWidth * 0.30);

        // 1. Cabecalho Palavra
        boolean hoverWord = mouseY >= 25 && mouseY <= 40 && mouseX >= this.listX && mouseX < col1End;
        String wordHead = "Palavra" + getSortIcon(PlayerProgressListWidget.SortColumn.WORD);
        guiGraphics.text(this.font, wordHead, this.listX + 6, headerY, hoverWord ? hoverColor : normalColor, false);

        // 2. Cabecalho Meio (Tradução)
        boolean hoverMid = mouseY >= 25 && mouseY <= 40 && mouseX >= col1End && mouseX < col2End;
        String midHead = "Tradução" + getSortIcon(PlayerProgressListWidget.SortColumn.MIDDLE);
        guiGraphics.text(this.font, midHead, col1End, headerY, hoverMid ? hoverColor : normalColor, false);

        // 3. Cabecalho Nível
        boolean hoverLvl = mouseY >= 25 && mouseY <= 40 && mouseX >= col2End && mouseX <= this.listX + this.listWidth;
        String lvlHead = "Nível" + getSortIcon(PlayerProgressListWidget.SortColumn.LEVEL);
        int lvlWidth = this.font.width(lvlHead);
        guiGraphics.text(this.font, lvlHead, this.listX + this.listWidth - lvlWidth - 10, headerY, hoverLvl ? hoverColor : normalColor, false);

        // Painel Direito original
        int rightPanelX = this.width / 2 + 20;

        PlayerProgressListEntry selected = this.listWidget.getSelected();

        if (selected != null) {
            try {
                // Pegamos os objetos fresquinhos direto da lista! Sem buscas no dicionário.
                Word word = selected.getWordObj();
                WordProgress progress = selected.getProgressObj();

                if (word != null && progress != null) {

                    // 1 Kanji
                    if (word.kanji() != null && !word.kanji().isBlank()) {
                        guiGraphics.text(this.font, "Kanji: ", rightPanelX, 40, 0xFFAAAAAA, false);

                        float scale = 1.5f;

                        guiGraphics.pose().pushMatrix(); // Entra na pilha 2D
                        guiGraphics.pose().scale(scale, scale); // Escala apenas X e Y

                        int scaledX = (int) ((rightPanelX + 30) / scale);
                        int scaledY = (int) (38 / scale);

                        guiGraphics.text(this.font, word.kanji(), scaledX, scaledY, 0xFFAAFFFF, false);

                        guiGraphics.pose().popMatrix();
                    }

                    // 2. Romaji
                    if (word.romaji() != null && !word.romaji().isBlank()) {
                        guiGraphics.text(this.font, "Romaji: ", rightPanelX, 60, 0xFFAAAAAA, false);
                        guiGraphics.text(this.font, word.romaji(), rightPanelX + 37, 60, 0xFFFF8888, false);
                    }

                    // 3. Tradução
                    String trad = "Nenhuma";
                    if (word.translations() != null && !word.translations().isEmpty()) {
                        trad = word.translations().getFirst();
                    }
                    guiGraphics.text(this.font, "Tradução: ", rightPanelX, 80, 0xFFAAAAAA, false);
                    guiGraphics.text(this.font, trad, rightPanelX + 55, 80, 0xFFFFFFAA, false);

                    // 4. Linha Divisória de Status
                    guiGraphics.text(this.font, "--- Estatísticas de Aprendizado ---", rightPanelX, 105, 0xFF555555, false);

                    // 5. Dados do Progresso puxando direto do objeto WordProgress
                    guiGraphics.text(this.font, "Nível Atual: " + progress.getScriptLevel(), rightPanelX, 120, 0xFF55FF55, false);

                    String expText = String.format("Exposição Total: %.1f", progress.getExposure());
                    guiGraphics.text(this.font, expText, rightPanelX, 135, 0xFFAAAAAA, false);

                    guiGraphics.text(this.font, "Visto no Mundo: " + progress.getSeenCount() + " vezes", rightPanelX, 150, 0xFFAAAAAA, false);
                    guiGraphics.text(this.font, "Total de Consultas: " + progress.getLookupCount(), rightPanelX, 165, 0xFFAAAAAA, false);
                }
            } catch (Exception e) {
                guiGraphics.text(this.font, "ERRO: " + e.getClass().getSimpleName(), rightPanelX, 40, 0xFFFF0000, false);
            }
        }else {
            // Nenhum item selecionado (Tela padrão)
            guiGraphics.text(this.font, "Detalhes da Palavra", rightPanelX, 40, 0xFFAAAAAA, false);
            guiGraphics.text(this.font, "Selecione uma palavra na lista", rightPanelX, 60, 0xFF555555, false);
            guiGraphics.text(this.font, "para inspecionar os detalhes...", rightPanelX, 75, 0xFF555555, false);
        }
    }
    private String getSortIcon(PlayerProgressListWidget.SortColumn col) {
        if (currentSortCol != col) return "";
        return currentSortDir == PlayerProgressListWidget.SortDir.ASC ? " ▲" : " ▼";
    }
}