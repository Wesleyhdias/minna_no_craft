package com.wesleyhdias.minnanocraft.config.modmenu.progress;

import com.wesleyhdias.minnanocraft.client.syllabary_screen.SyllabaryScreen;
import com.wesleyhdias.minnanocraft.language.TranslationCacheManager;
import com.wesleyhdias.minnanocraft.language.kana.RomajiSyllableParser;
import com.wesleyhdias.minnanocraft.srs.PlayerVocabularyManager;
import com.wesleyhdias.minnanocraft.language.TokenTextHelper;
import com.wesleyhdias.minnanocraft.language.dictionary.Word;
import com.wesleyhdias.minnanocraft.srs.models.WordProgress;
import com.wesleyhdias.minnanocraft.config.data.ConfigData;
import com.wesleyhdias.minnanocraft.config.ModConfig;

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

    private enum RightPanelTab {
        LINGUISTIC,
        STATISTICS
    }

    /**
     * Controls which tab is currently visible in the right panel.
     */
    private RightPanelTab activeTab = RightPanelTab.LINGUISTIC;

    /**
     * Button to select the Linguistic tab.
     */
    private Button tabLinguisticBtn;

    /**
     * Button to select the Statistics tab.
     */
    private Button tabStatsBtn;

    /**
     * Indicates whether readings should be displayed in Romaji instead of Hiragana.
     */
    private boolean showRomaji = false;

    /**
     * Button to toggle the reading display mode.
     */
    private Button toggleReadingBtn;

    /**
     * Reference to the parent screen to return to upon closing.
     */
    private final Screen parent;

    /**
     * Scrollable list widget displaying player vocabulary entries.
     */
    private PlayerProgressListWidget listWidget;

    /**
     * Left boundary X position for the vocabulary list layout.
     */
    private int listX;

    /**
     * Calculated width allocation for the vocabulary list layout.
     */
    private int listWidth;

    /**
     * Interactive button to manually increment selected word script level.
     */
    private Button btnIncrementLevel;

    /**
     * Interactive button to manually decrement selected word script level.
     */
    private Button btnDecrementLevel;

    /**
     * Tracks the currently active sorting column.
     */
    private PlayerProgressListWidget.SortColumn currentSortCol = PlayerProgressListWidget.SortColumn.NONE;

    /**
     * Tracks the currently active sorting direction.
     */
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
                Button.builder(Component.translatable("gui.back"), _ -> this.onClose())
                        .bounds(this.width / 2 - 100, this.height - 30, 200, 20)
                        .build()
        );

        this.listX = 10;
        this.listWidth = (this.width / 2) - 5;

        int listY = 45;
        int listHeight = this.height - 85;

        this.listWidget = new PlayerProgressListWidget(this.minecraft, this.listWidth, listHeight, this.listX, listY, 20);
        this.addRenderableWidget(this.listWidget);

        int rightPanelX = this.width / 2 + 40;

        int tabY = 24;
        this.tabLinguisticBtn = this.addRenderableWidget(
                Button.builder(Component.literal("Linguística"), _ -> this.activeTab = RightPanelTab.LINGUISTIC)
                        .bounds(rightPanelX, tabY, 70, 16)
                        .build()
        );

        this.tabStatsBtn = this.addRenderableWidget(
                Button.builder(Component.literal("Progresso"), _ -> this.activeTab = RightPanelTab.STATISTICS)
                        .bounds(rightPanelX + 75, tabY, 70, 16)
                        .build()
        );

        // Toggle button to switch between Hiragana and Romaji
        this.toggleReadingBtn = this.addRenderableWidget(
                Button.builder(Component.literal("👁"), _ -> this.showRomaji = !this.showRomaji)
                        .bounds(rightPanelX + 140, 60, 16, 16) // O Y será ajustado no render
                        .build()
        );

        int levelY = 63;

        // Decrement Level (-) Button
        this.btnDecrementLevel = this.addRenderableWidget(
                Button.builder(Component.literal("-"), _ -> this.adjustSelectedLevel(-1))
                        .bounds(rightPanelX + 85, levelY, 16, 16)
                        .build()
        );

        // Increment Level (+) Button
        this.btnIncrementLevel = this.addRenderableWidget(
                Button.builder(Component.literal("+"), _ -> this.adjustSelectedLevel(1))
                        .bounds(rightPanelX + 105, levelY, 16, 16)
                        .build()
        );

        // Kana Screen Button
        this.addRenderableWidget(Button.builder(
                        Component.literal("あ"),
                        _ -> this.minecraft.setScreen(new SyllabaryScreen(this))) // Abre o popup
                .bounds(rightPanelX + 150, tabY, 16, 16)
                .build());
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

            PlayerVocabularyManager.getInstance().save();
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
            } else if (mouseX >= col1End && mouseX < col2End) {
                toggleSort(PlayerProgressListWidget.SortColumn.MIDDLE);
                return true;
            } else if (mouseX >= col2End && mouseX <= this.listX + this.listWidth) {
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

        if (hasSelection) {
            // Shows the tab buttons and deactivates (presses down) the button of the currently active tab
            this.tabLinguisticBtn.visible = true;
            this.tabStatsBtn.visible = true;
            this.tabLinguisticBtn.active = (this.activeTab != RightPanelTab.LINGUISTIC);
            this.tabStatsBtn.active = (this.activeTab != RightPanelTab.STATISTICS);

            try {
                Word word = selected.getWordObj();
                WordProgress progress = selected.getProgressObj();
                int padding = 4;

                // ==========================================
                // TAB 1: LINGUISTICS (Vocabulary and Translations)
                // ==========================================
                if (this.activeTab == RightPanelTab.LINGUISTIC) {
                    this.btnDecrementLevel.visible = false;
                    this.btnIncrementLevel.visible = false;

                    int currentY = 47;

                    // 1. Highlight: Kanji + Reading in parentheses
                    String titleWord = word.kanji();
                    if (titleWord == null || titleWord.isBlank())
                        titleWord = progress.getWord(); // Fallback prevention if it has no kanji

                    String currentReading = this.showRomaji ? word.romaji() : word.hiragana();
                    if (currentReading == null || currentReading.isBlank()) currentReading = "-";

                    // Scale Protection: If the Kanji text is colossal, scales it down to fit before the button
                    int baseKanjiWidth = this.font.width(titleWord);
                    float kanjiScale = 2.0f;
                    if (baseKanjiWidth * kanjiScale > 140) {
                        kanjiScale = 140.0f / baseKanjiWidth;
                    }

                    guiGraphics.pose().pushMatrix();
                    guiGraphics.pose().scale(kanjiScale, kanjiScale);
                    int scaledKanjiX = (int) (rightPanelX / kanjiScale);
                    int scaledKanjiY = (int) (currentY / kanjiScale);
                    guiGraphics.text(this.font, titleWord, scaledKanjiX, scaledKanjiY, 0xFFAAFFFF, false);
                    guiGraphics.pose().popMatrix();

                    int kanjiWidth = (int) (baseKanjiWidth * kanjiScale);

                    // Logic to decide where the reading will be drawn (side-by-side or below)
                    String formattedReading = "(" + currentReading + ")";
                    float readingScale = 1.2f;
                    int readingWidth = (int) (this.font.width(formattedReading) * readingScale);

                    int readingX;
                    int readingY;

                    // If the sum of Kanji + Reading exceeds the limit of approx 150 pixels (space before the button)
                    if (kanjiWidth + 6 + readingWidth > 150) {
                        readingX = rightPanelX;
                        readingY = currentY + (int) (this.font.lineHeight * kanjiScale) + 2; // Wraps to the next line
                        currentY = readingY + (int) (this.font.lineHeight * readingScale) + 8; // Updates global Y
                    } else {
                        readingX = rightPanelX + kanjiWidth + 6;
                        readingY = currentY + (int) ((this.font.lineHeight * kanjiScale - this.font.lineHeight * readingScale) / 2) + 1; // Aligns side-by-side
                        currentY += (int) (this.font.lineHeight * kanjiScale) + 10; // Updates global Y
                    }

                    guiGraphics.pose().pushMatrix();
                    guiGraphics.pose().scale(readingScale, readingScale);
                    int scaledReadingX = (int) (readingX / readingScale);
                    int scaledReadingY = (int) (readingY / readingScale);
                    guiGraphics.text(this.font, formattedReading, scaledReadingX, scaledReadingY, 0xFFFF8888, false);
                    guiGraphics.pose().popMatrix();

                    // Compact Toggle button with "👁" icon
                    if (this.toggleReadingBtn != null) {
                        this.toggleReadingBtn.visible = true;
                        this.toggleReadingBtn.setX(rightPanelX + 160);
                        this.toggleReadingBtn.setY(47 + 2); // Fixed at the top, where it always starts
                    }

                    // 2. Translation
                    String trad = "- / -";
                    if (word.getLocalTranslations() != null && !word.getLocalTranslations().isEmpty()) {
                        trad = word.getLocalTranslations().getFirst();
                    }
                    Component transLabel = Component.translatable("progress_screen.minnanocraft.column_translation").append(": ");
                    int transLabelWidth = this.font.width(transLabel);
                    guiGraphics.text(this.font, transLabel, rightPanelX, currentY, 0xFFAAAAAA, false);
                    guiGraphics.text(this.font, trad, rightPanelX + transLabelWidth + padding, currentY, 0xFFFFFFAA, false);
                    currentY += 20;

                    // 3. Explanation (Scaled and with Word Wrap)
                    int maxWidth = 180;
                    String token = progress.getWord();
                    float textScale = 0.85f;

                    guiGraphics.pose().pushMatrix();
                    guiGraphics.pose().scale(textScale, textScale);

                    int scaledMaxWidth = (int) (maxWidth / textScale);
                    int scaledX = (int) (rightPanelX / textScale);
                    int scaledY = (int) (currentY / textScale);

                    Component descText = Component.translatable(TokenTextHelper.getDescriptionKey(token));
                    var descLines = this.font.split(descText, scaledMaxWidth);
                    for (var line : descLines) {
                        guiGraphics.text(this.font, line, scaledX, scaledY, 0xFFAAAAAA, false);
                        scaledY += this.font.lineHeight + 2;
                    }
                    guiGraphics.pose().popMatrix();

                    currentY += (int) (descLines.size() * (this.font.lineHeight + 2) * textScale) + 8;

                    // 4. Example (Dynamic parsing, Scaled, and Full Word Wrap)
                    guiGraphics.text(this.font, Component.literal("Exemplo:"), rightPanelX, currentY, 0xFF555555, false);
                    currentY += 12;

                    guiGraphics.pose().pushMatrix();
                    guiGraphics.pose().scale(textScale, textScale);

                    scaledX = (int) (rightPanelX / textScale);
                    scaledY = (int) (currentY / textScale);

                    String fullExample = Component.translatable(TokenTextHelper.getExampleKey(token)).getString();
                    String jpText = fullExample;
                    String reading = "";
                    String translation = "";

                    String[] newlineSplit = fullExample.split("\n");
                    if (newlineSplit.length > 1) {
                        jpText = newlineSplit[0];
                        translation = newlineSplit[1];
                    }

                    int openParen = jpText.lastIndexOf('(');
                    int closeParen = jpText.lastIndexOf(')');
                    if (openParen != -1 && closeParen > openParen) {
                        reading = jpText.substring(openParen + 1, closeParen);
                        jpText = jpText.substring(0, openParen).trim();
                    }

                    // Renders Japanese (NOW WITH WORD WRAP)
                    var jpLines = this.font.split(Component.literal(jpText), scaledMaxWidth);
                    for (var line : jpLines) {
                        guiGraphics.text(this.font, line, scaledX, scaledY, 0xFFFFFFFF, false);
                        scaledY += this.font.lineHeight + 2;
                    }

                    // Renders Reading (NOW WITH WORD WRAP)
                    if (!reading.isEmpty()) {
                        String displayReading = this.showRomaji ? reading : RomajiSyllableParser.toHiragana(reading);
                        var readingLines = this.font.split(Component.literal(displayReading), scaledMaxWidth);
                        for (var line : readingLines) {
                            guiGraphics.text(this.font, line, scaledX, scaledY, 0xFFFFFF55, false);
                            scaledY += this.font.lineHeight + 2;
                        }
                    }

                    // Renders Translation (WITH WORD WRAP)
                    if (!translation.isEmpty()) {
                        var transLines = this.font.split(Component.literal(translation), scaledMaxWidth);
                        for (var line : transLines) {
                            guiGraphics.text(this.font, line, scaledX, scaledY, 0xFFAAAAAA, false);
                            scaledY += this.font.lineHeight + 2;
                        }
                    }

                    guiGraphics.pose().popMatrix();
                }
                // ==========================================
                // TAB 2: STATISTICS (Learning Progress)
                // ==========================================
                else if (this.activeTab == RightPanelTab.STATISTICS) {
                    this.btnDecrementLevel.visible = true;
                    this.btnIncrementLevel.visible = true;
                    if (this.toggleReadingBtn != null)
                        this.toggleReadingBtn.visible = false; // Hide the "👁" in statistics

                    int currentY = 47;

                    // Statistics Title
                    guiGraphics.text(this.font, Component.translatable("progress_screen.minnanocraft.title.lerning_statistics"), rightPanelX, currentY, 0xFF555555, false);
                    currentY += 25;

                    // Updates the activation of level buttons (to prevent exceeding 4 or falling below 0)
                    int currentLevel = progress.getScriptLevel();
                    this.btnDecrementLevel.active = (currentLevel > 0);
                    this.btnIncrementLevel.active = (currentLevel < 4);

                    // Level (Aligned with buttons)
                    guiGraphics.text(this.font, Component.translatable("progress_screen.minnanocraft.word.current_level", currentLevel), rightPanelX, currentY, 0xFF55FF55, false);
                    currentY += 20;

                    // Exposure
                    String formattedExposure = String.format("%.1f", progress.getExposure());
                    Component expText = Component.translatable("progress_screen.minnanocraft.total_exposure", formattedExposure);
                    guiGraphics.text(this.font, expText, rightPanelX, currentY, 0xFFAAAAAA, false);
                    currentY += 20;

                    // Seen Count
                    guiGraphics.text(this.font, Component.translatable("progress_screen.minnanocraft.seen_count", progress.getSeenCount()), rightPanelX, currentY, 0xFFAAAAAA, false);
                    currentY += 20;

                    // Lookup Count
                    guiGraphics.text(this.font, Component.translatable("progress_screen.minnanocraft.total_lookups", progress.getLookupCount()), rightPanelX, currentY, 0xFFAAAAAA, false);
                }

            } catch (Exception e) {
                guiGraphics.text(this.font, "ERROR: " + e.getClass().getSimpleName(), rightPanelX, 40, 0xFFFF0000, false);
            }
        } else {
            // When NOTHING is selected in the list, hides ALL right-side buttons
            if (this.tabLinguisticBtn != null) this.tabLinguisticBtn.visible = false;
            if (this.tabStatsBtn != null) this.tabStatsBtn.visible = false;
            if (this.toggleReadingBtn != null) this.toggleReadingBtn.visible = false;
            if (this.btnDecrementLevel != null) this.btnDecrementLevel.visible = false;
            if (this.btnIncrementLevel != null) this.btnIncrementLevel.visible = false;

            // Original placeholder
            guiGraphics.text(this.font, Component.translatable("progress_screen.minnanocraft.ui_hint"), rightPanelX, 47, 0xFFAAAAAA, false);
            guiGraphics.text(this.font, Component.translatable("progress_screen.minnanocraft.ui_hint_1"), rightPanelX, 67, 0xFF555555, false);
            guiGraphics.text(this.font, Component.translatable("progress_screen.minnanocraft.ui_hint_2"), rightPanelX, 82, 0xFF555555, false);
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
        PlayerVocabularyManager.getInstance().updateProgression();
        TranslationCacheManager.clearAll();
        this.minecraft.setScreen(this.parent);
    }
}