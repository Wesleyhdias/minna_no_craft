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

/**
 * GUI Screen for displaying and managing player vocabulary progress.
 * Provides a split-screen layout containing a scrollable word list on the left
 * and a detailed inspection panel with level controls on the right.
 */
public class PlayerProgressScreen extends Screen {

    /** Reference to the parent screen to return to upon closing. */
    private final Screen parent;

    /** Scrollable list widget displaying player vocabulary entries. */
    private PlayerProgressListWidget listWidget;

    /** Left boundary X position for the vocabulary list layout. */
    private int listX;

    /** Calculated width allocation for the vocabulary list layout. */
    private int listWidth;

    /** Interactive button to manually increment selected word script level. */
    private Button btnIncrementLevel;

    /** Interactive button to manually decrement selected word script level. */
    private Button btnDecrementLevel;

    /** Tracks the currently active sorting column. */
    private PlayerProgressListWidget.SortColumn currentSortCol = PlayerProgressListWidget.SortColumn.NONE;

    /** Tracks the currently active sorting direction. */
    private PlayerProgressListWidget.SortDir currentSortDir = PlayerProgressListWidget.SortDir.NONE;

    /**
     * Constructs the vocabulary progress inspection screen.
     *
     * @param parent The parent screen instance.
     */
    public PlayerProgressScreen(Screen parent) {
        super(Component.literal("Meu Progresso e Vocabulário"));
        this.parent = parent;
    }

    /**
     * Initializes screen components, list layout bounds, and level control buttons.
     */
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
        int levelY = 146; // Vertically aligned with Y = 120 level text

        // Decrement Level (-) Button
        this.btnDecrementLevel = this.addRenderableWidget(
                Button.builder(Component.literal("-"), button -> this.adjustSelectedLevel(-1))
                        .bounds(rightPanelX + 85, levelY, 16, 16)
                        .build()
        );

        // Increment Level (+) Button
        this.btnIncrementLevel = this.addRenderableWidget(
                Button.builder(Component.literal("+"), button -> this.adjustSelectedLevel(1))
                        .bounds(rightPanelX + 105, levelY, 16, 16)
                        .build()
        );
    }

    /**
     * Adjusts the script level of the currently selected word by a delta offset,
     * recalculating exposure points and persisting changes to disk.
     *
     * @param delta The level change step (-1 or +1).
     */
    private void adjustSelectedLevel(int delta) {
        PlayerProgressListEntry selected = this.listWidget.getSelected();
        if (selected == null || selected.getProgressObj() == null) return;

        WordProgress progress = selected.getProgressObj();
        int currentLevel = progress.getScriptLevel();

        // Clamp script level boundary between Level 0 and Level 4
        int newLevel = Math.clamp(currentLevel + delta, 0, 4);

        if (newLevel != currentLevel) {
            // Retrieve required exposure threshold for target level
            double targetExp = getExpForLevel(newLevel);

            // Calculate exposure difference needed to reach target level
            double expDifference = targetExp - progress.getExposure();

            progress.updateExposure(expDifference);

            PlayerVocabularyManager.save();
        }
    }

    /**
     * Retrieves target exposure XP threshold required for a given script level.
     *
     * @param level Target script level (0-4).
     * @return Required exposure points.
     */
    private double getExpForLevel(int level) {
        ConfigData config = ModConfig.getConfig();
        return switch (level) {
            case 1 -> config.getExpLevel1();
            case 2 -> config.getExpLevel2();
            case 3 -> config.getExpLevel3();
            case 4 -> config.getExpLevel4();
            default -> 0.0; // Script Level 0
        };
    }

    /**
     * Handles mouse click input events to detect column header clicks for sorting operations.
     *
     * @param event       Mouse button event information.
     * @param doubleClick True if event originates from a double click.
     * @return True if mouse click event was processed.
     */
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();

        if (button == 0 && mouseY >= 25 && mouseY <= 40) {
            // Fractional bounds corresponding to header column widths
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

    /**
     * Toggles through column sorting states (ASC -> DESC -> NONE).
     *
     * @param col Target column to apply sorting on.
     */
    private void toggleSort(PlayerProgressListWidget.SortColumn col) {
        if (currentSortCol == col) {
            // Rotate sort direction cycle: ASC -> DESC -> NONE
            currentSortDir = currentSortDir == PlayerProgressListWidget.SortDir.ASC ? PlayerProgressListWidget.SortDir.DESC
                    : currentSortDir == PlayerProgressListWidget.SortDir.DESC ? PlayerProgressListWidget.SortDir.NONE
                    : PlayerProgressListWidget.SortDir.ASC;
        } else {
            // Selected new column, reset direction to ASC
            currentSortCol = col;
            currentSortDir = PlayerProgressListWidget.SortDir.ASC;
        }

        if (currentSortDir == PlayerProgressListWidget.SortDir.NONE) {
            currentSortCol = PlayerProgressListWidget.SortColumn.NONE;
        }

        this.listWidget.applySorting(currentSortCol, currentSortDir);
    }

    /**
     * Extracts and processes rendering states for screen elements, including
     * interactive headers and right-side word detail inspection panel.
     *
     * @param guiGraphics Graphics context extractor.
     * @param mouseX      Current mouse cursor X coordinate.
     * @param mouseY      Current mouse cursor Y coordinate.
     * @param partialTick Render tick delta time.
     */
    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x99000000);
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.centeredText(this.font, this.title, this.width / 2, 10, 0xFFFFFFFF);

        // --- Render Clickable List Header ---
        int headerY = 28;
        int hoverColor = 0xFFFFFFFF;
        int normalColor = 0xFFAAAAAA;

        // Hover detection column boundaries
        int col1End = this.listX + (int) (this.listWidth * 0.40);
        int col2End = col1End + (int) (this.listWidth * 0.35);

        // 1. "Word" Column Header
        boolean hoverWord = mouseY >= 25 && mouseY <= 40 && mouseX >= this.listX && mouseX < col1End;
        Component wordHead = Component.translatable("progress_screen.minnanocraft.column_word").append(getSortIcon(PlayerProgressListWidget.SortColumn.WORD));
        guiGraphics.text(this.font, wordHead, this.listX + 6, headerY, hoverWord ? hoverColor : normalColor, false);

        // 2. "Translation" Column Header
        boolean hoverMid = mouseY >= 25 && mouseY <= 40 && mouseX >= col1End && mouseX < col2End;
        Component midHead = Component.translatable("progress_screen.minnanocraft.column_translation").append(getSortIcon(PlayerProgressListWidget.SortColumn.MIDDLE));
        guiGraphics.text(this.font, midHead, col1End, headerY, hoverMid ? hoverColor : normalColor, false);

        // 3. "Level" Column Header
        boolean hoverLvl = mouseY >= 25 && mouseY <= 40 && mouseX >= col2End && mouseX <= this.listX + this.listWidth;
        Component lvlHead = Component.translatable("progress_screen.minnanocraft.column_level").append(getSortIcon(PlayerProgressListWidget.SortColumn.LEVEL));
        int lvlWidth = this.font.width(lvlHead);
        guiGraphics.text(this.font, lvlHead, this.listX + this.listWidth - lvlWidth - 10, headerY, hoverLvl ? hoverColor : normalColor, false);

        // --- Render Right Detail Inspection Panel ---
        int rightPanelX = this.width / 2 + 20;

        PlayerProgressListEntry selected = this.listWidget.getSelected();
        boolean hasSelection = (selected != null && selected.getWordObj() != null && selected.getProgressObj() != null);

        // Ensure level control buttons visibility tracks selection state
        if (this.btnDecrementLevel != null) this.btnDecrementLevel.visible = hasSelection;
        if (this.btnIncrementLevel != null) this.btnIncrementLevel.visible = hasSelection;

        if (hasSelection) {
            try {
                Word word = selected.getWordObj();
                WordProgress progress = selected.getProgressObj();

                int padding = 4;

                // 1. Kanji Representation (Rendered scaled if available)
                if (word.kanji() != null && !word.kanji().isBlank()) {
                    Component kanjiLabel = Component.literal("Kanji: ");
                    int labelWidth = this.font.width(kanjiLabel);

                    guiGraphics.text(this.font, kanjiLabel, rightPanelX, 40, 0xFFAAAAAA, false);

                    float scale = 1.5f;
                    guiGraphics.pose().pushMatrix();
                    guiGraphics.pose().scale(scale, scale);

                    // Adjust X according to the real width of the word "Kanji: "
                    int scaledX = (int) ((rightPanelX + labelWidth + padding) / scale);
                    int scaledY = (int) (38 / scale);

                    guiGraphics.text(this.font, word.kanji(), scaledX, scaledY, 0xFFAAFFFF, false);
                    guiGraphics.pose().popMatrix();
                }

                // 2. Hiragana Representation
                if (word.hiragana() != null && !word.hiragana().isBlank()) {
                    Component hiraLabel = Component.literal("Hiragana: ");
                    int labelWidth = this.font.width(hiraLabel);

                    guiGraphics.text(this.font, hiraLabel, rightPanelX, 60, 0xFFAAAAAA, false);
                    guiGraphics.text(this.font, word.hiragana(), rightPanelX + labelWidth + padding, 60, 0xFFFF8888, false);
                }

                // 3. Romaji Representation
                if (word.romaji() != null && !word.romaji().isBlank()) {
                    Component romajiLabel = Component.literal("Romaji: ");
                    int labelWidth = this.font.width(romajiLabel);

                    guiGraphics.text(this.font, romajiLabel, rightPanelX, 80, 0xFFAAAAAA, false);
                    guiGraphics.text(this.font, word.romaji(), rightPanelX + labelWidth + padding, 80, 0xFFFF8888, false);
                }

                // 4. Translation
                String trad = "- / -";
                if (word.getLocalTranslations() != null && !word.getLocalTranslations().isEmpty()) {
                    trad = word.getLocalTranslations().getFirst();
                }

                Component transLabel = Component.translatable("progress_screen.minnanocraft.column_translation").append(": ");
                int transLabelWidth = this.font.width(transLabel);

                guiGraphics.text(this.font, transLabel, rightPanelX, 100, 0xFFAAAAAA, false);
                guiGraphics.text(this.font, trad, rightPanelX + transLabelWidth + padding, 100, 0xFFFFFFAA, false);

                // 5. Statistics Separator Line
                guiGraphics.text(this.font, Component.translatable("progress_screen.minnanocraft.title.lerning_statistics"), rightPanelX, 120, 0xFF555555, false);

                // Update level control button active states based on boundary limits
                int currentLevel = progress.getScriptLevel();
                this.btnDecrementLevel.active = (currentLevel > 0);
                this.btnIncrementLevel.active = (currentLevel < 4);

                // 6. Progress Statistics
                guiGraphics.text(this.font, Component.translatable("progress_screen.minnanocraft.word.current_level", currentLevel), rightPanelX, 150, 0xFF55FF55, false);

                String formattedExposure = String.format("%.1f", progress.getExposure());
                Component expText = Component.translatable("progress_screen.minnanocraft.total_exposure", formattedExposure);
                guiGraphics.text(this.font, expText, rightPanelX, 165, 0xFFAAAAAA, false);

                guiGraphics.text(this.font, Component.translatable("progress_screen.minnanocraft.seen_count", progress.getSeenCount()), rightPanelX, 180, 0xFFAAAAAA, false);
                guiGraphics.text(this.font, Component.translatable("progress_screen.minnanocraft.total_lookups", progress.getLookupCount()), rightPanelX, 195, 0xFFAAAAAA, false);
            } catch (Exception e) {
                guiGraphics.text(this.font, "ERRO: " + e.getClass().getSimpleName(), rightPanelX, 40, 0xFFFF0000, false);
            }
        } else {
            // Default placeholder view when no item is selected
            guiGraphics.text(this.font, Component.translatable("progress_screen.minnanocraft.ui_hint"), rightPanelX, 40, 0xFFAAAAAA, false);
            guiGraphics.text(this.font, Component.translatable("progress_screen.minnanocraft.ui_hint_1"), rightPanelX, 60, 0xFF555555, false);
            guiGraphics.text(this.font, Component.translatable("progress_screen.minnanocraft.ui_hint_2"), rightPanelX, 75, 0xFF555555, false);
        }
    }

    /**
     * Returns a string arrow indicator matching active sorting state for column headers.
     *
     * @param col Target column.
     * @return Sorting indicator icon string or empty string.
     */
    private String getSortIcon(PlayerProgressListWidget.SortColumn col) {
        if (currentSortCol != col) return "";
        return currentSortDir == PlayerProgressListWidget.SortDir.ASC ? " ▲" : " ▼";
    }

    /**
     * Screen close handler, updating progression state and clearing active translation caches.
     */
    @Override
    public void onClose() {
        PlayerVocabularyManager.updateProgression();
        TranslationCacheManager.clearAll();
        this.minecraft.setScreen(this.parent);
    }
}