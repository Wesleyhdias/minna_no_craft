package com.wesleyhdias.minnanocraft.config.modmenu.progress;

import com.wesleyhdias.minnanocraft.language.TranslationCacheManager;
import com.wesleyhdias.minnanocraft.srs.PlayerVocabularyManager;
import com.wesleyhdias.minnanocraft.language.dictionary.Word;
import com.wesleyhdias.minnanocraft.srs.models.WordProgress;
import com.wesleyhdias.minnanocraft.config.data.ConfigData;
import com.wesleyhdias.minnanocraft.config.data.ModConfig;

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

    private Button btnIncrementLevel;
    private Button btnDecrementLevel;

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
                Button.builder(Component.translatable("gui.back"), button -> this.onClose())
                        .bounds(this.width / 2 - 100, this.height - 30, 200, 20)
                        .build()
        );

        this.listX = 10;
        this.listWidth = (this.width / 2) - 5;

        int listY = 45;
        int listHeight = this.height - 85;

        this.listWidget = new PlayerProgressListWidget(this.minecraft, this.listWidth, listHeight, this.listX, listY, 20);
        this.addRenderableWidget(this.listWidget);

        int rightPanelX = this.width / 2 + 20;
        int levelY = 116; // Alinhado verticalmente com o Y = 120 do texto de nível

        // CORREÇÃO: Usando 'this.btnDecrementLevel' em vez de criar uma variável local 'Button btnDecrementLevel'
        this.btnDecrementLevel = this.addRenderableWidget(
                Button.builder(Component.literal("-"), button -> this.adjustSelectedLevel(-1))
                        .bounds(rightPanelX + 80, levelY, 16, 16)
                        .build()
        );

        // Botão de Aumentar (+)
        this.btnIncrementLevel = this.addRenderableWidget(
                Button.builder(Component.literal("+"), button -> this.adjustSelectedLevel(1))
                        .bounds(rightPanelX + 100, levelY, 16, 16)
                        .build()
        );
    }

    private void adjustSelectedLevel(int delta) {
        PlayerProgressListEntry selected = this.listWidget.getSelected();
        if (selected == null || selected.getProgressObj() == null) return;

        WordProgress progress = selected.getProgressObj();
        int currentLevel = progress.getScriptLevel();

        // Trava o nível entre 0 e 4
        int newLevel = Math.clamp(currentLevel + delta, 0, 4);

        if (newLevel != currentLevel) {
            // Pega qual deve ser a nova EXP exata
            double targetExp = getExpForLevel(newLevel);

            // Calcula a diferença (o 'delta') que falta para chegar lá
            double expDifference = targetExp - progress.getExposure();

            progress.updateExposure(expDifference);

            PlayerVocabularyManager.save();
        }
    }

    private double getExpForLevel(int level) {
        ConfigData config = ModConfig.getConfig();
        return switch (level) {
            case 1 -> config.getExpLevel1();
            case 2 -> config.getExpLevel2();
            case 3 -> config.getExpLevel3();
            case 4 -> config.getExpLevel4();
            default -> 0.0; // Nível 0
        };
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
        int col1End = this.listX + (int) (this.listWidth * 0.40);
        int col2End = col1End + (int) (this.listWidth * 0.35);

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

        // --- PAINEL DIREITO ---
        int rightPanelX = this.width / 2 + 20;

        PlayerProgressListEntry selected = this.listWidget.getSelected();
        boolean hasSelection = (selected != null && selected.getWordObj() != null && selected.getProgressObj() != null);

        // CORREÇÃO: A visibilidade dos botões é atualizada SEMPRE, independente de ter seleção ou não
        if (this.btnDecrementLevel != null) this.btnDecrementLevel.visible = hasSelection;
        if (this.btnIncrementLevel != null) this.btnIncrementLevel.visible = hasSelection;

        if (hasSelection) {
            try {
                Word word = selected.getWordObj();
                WordProgress progress = selected.getProgressObj();

                // 1. Kanji (apenas se existir)
                if (word.kanji() != null && !word.kanji().isBlank()) {
                    guiGraphics.text(this.font, "Kanji: ", rightPanelX, 40, 0xFFAAAAAA, false);

                    float scale = 1.5f;
                    guiGraphics.pose().pushMatrix();
                    guiGraphics.pose().scale(scale, scale);

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

                // Estado dos botões (desativa se estiver nos limites)
                int currentLevel = progress.getScriptLevel();
                this.btnDecrementLevel.active = (currentLevel > 0);
                this.btnIncrementLevel.active = (currentLevel < 4);

                // 5. Dados do Progresso
                guiGraphics.text(this.font, "Nível Atual: " + currentLevel, rightPanelX, 120, 0xFF55FF55, false);

                String expText = String.format("Exposição Total: %.1f", progress.getExposure());
                guiGraphics.text(this.font, expText, rightPanelX, 135, 0xFFAAAAAA, false);

                guiGraphics.text(this.font, "Visto no Mundo: " + progress.getSeenCount() + " vezes", rightPanelX, 150, 0xFFAAAAAA, false);
                guiGraphics.text(this.font, "Total de Consultas: " + progress.getLookupCount(), rightPanelX, 165, 0xFFAAAAAA, false);

            } catch (Exception e) {
                guiGraphics.text(this.font, "ERRO: " + e.getClass().getSimpleName(), rightPanelX, 40, 0xFFFF0000, false);
            }
        } else {
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

    @Override
    public void onClose() {
        PlayerVocabularyManager.updateProgression();
        TranslationCacheManager.clearAll();
        this.minecraft.setScreen(this.parent);
    }
}