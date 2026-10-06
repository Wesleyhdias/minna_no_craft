package com.wesleyhdias.minnanocraft.config.modmenu.progress;

import com.wesleyhdias.minnanocraft.language.dictionary.DictionaryLoader;
import com.wesleyhdias.minnanocraft.language.resolver.DifficultyResolver;
import com.wesleyhdias.minnanocraft.srs.PlayerVocabularyManager;
import com.wesleyhdias.minnanocraft.language.dictionary.Word;
import com.wesleyhdias.minnanocraft.srs.models.WordProgress;

import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.Minecraft;

import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;

/**
 * Scrollable selection list widget for rendering player vocabulary progress entries.
 * Handles dynamic loading of word progress, visual rendering resolution based on script levels,
 * scrollbar positioning, and column-based sorting capabilities.
 */
public class PlayerProgressListWidget extends ObjectSelectionList<PlayerProgressListEntry> {

    /** Cached original list entries used to restore default unsorted state. */
    private final List<PlayerProgressListEntry> originalEntries = new ArrayList<>();

    /** Thread-safe map holding cached word progress instances mapped by token. */
    private final ConcurrentHashMap<String, WordProgress> progressMap = PlayerVocabularyManager.getInstance().getVocabularyCache();

    /** X position boundary for the list widget layout. */
    private final int listX;

    /** Total width allocated for the list widget layout. */
    private final int listWidth;

    private String currentSearchQuery = "";
    private SortColumn currentSortCol = SortColumn.NONE;
    private SortDir currentSortDir = SortDir.NONE;

    /**
     * Constructs a new player progress list widget.
     *
     * @param minecraft  Minecraft engine client instance.
     * @param width      Width bounds of the selection list.
     * @param height     Height bounds of the selection list.
     * @param x          X coordinate origin.
     * @param y          Y coordinate origin.
     * @param itemHeight Individual row item height in pixels.
     */
    public PlayerProgressListWidget(Minecraft minecraft, int width, int height, int x, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
        this.setX(x);
        this.listX = x;
        this.listWidth = width;
        loadWords();
    }

    /** Columns available for sorting operations within the list widget. */
    public enum SortColumn { NONE, WORD, MIDDLE, LEVEL }

    /** Sorting direction states. */
    public enum SortDir { NONE, ASC, DESC }

    /**
     * Loads word progress data from cache, resolves display representations via dictionary
     * and difficulty resolvers, builds list entries, and caches original order.
     */
    private void loadWords() {

        // Display placeholder row if vocabulary cache contains no entries
        if (progressMap.isEmpty()) {
            this.addEntry(new PlayerProgressListEntry("Nenhuma palavra, vai jogar", "-/-", null, null, this.listX, this.listWidth));
            return;
        }

        for (WordProgress progress : progressMap.values()) {
            String token = progress.getWord();
            int level = progress.getScriptLevel();

            String middleText;
            String displayText;
            Word word = DictionaryLoader.getDictionary().get(token);
            if (word == null) {
                continue;
            }

            middleText = word.kanji();
            String rendered = DifficultyResolver.render(word, level);
            if (rendered != null && !rendered.isBlank()) {
                displayText = rendered;
            } else {
                displayText = word.getLocalTranslations().getFirst();
            }

            this.addEntry(new PlayerProgressListEntry(
                    displayText,
                    middleText,
                    word,
                    progress,
                    this.listX,
                    this.listWidth
            ));
        }

        // Cache baseline list order for sorting resets
        this.originalEntries.clear();
        this.originalEntries.addAll(this.children());
    }

    /**
     * Defines the selection background highlight width for hovered list items.
     * Returns full list width minus safety margin for the scrollbar.
     *
     * @return Row width in pixels.
     */
    @Override
    public int getRowWidth() {
        return this.listWidth - 12;
    }

    /**
     * Calculates exact X coordinate for rendering the scrollbar.
     * Placed at list X origin plus total width minus scrollbar offset width.
     *
     * @return Scrollbar X position.
     */
    @Override
    protected int scrollBarX() {
        return this.listX + this.listWidth - 6;
    }

    /**
     * Core method that applies search filtering and column sorting in sequence
     * without mutating the original cached entries.
     */
    private void updateDisplayList() {
        // Step 1: Apply search filtering
        List<PlayerProgressListEntry> processedList = new ArrayList<>();

        if (this.currentSearchQuery.isEmpty()) {
            // If query is empty, include all baseline entries
            processedList.addAll(this.originalEntries);
        } else {
            // Otherwise, filter entries against the search query
            for (PlayerProgressListEntry entry : this.originalEntries) {

                // Skip empty placeholder row if present
                if (entry.getFirstText() != null && entry.getFirstText().equals("Nenhuma palavra")) {
                    continue;
                }

                String wordText = entry.getFirstText() != null ? entry.getFirstText().toLowerCase() : "";
                String midText = entry.getMiddleText() != null ? entry.getMiddleText().toLowerCase() : "";

                // Check if search query matches primary display text or translation/Kanji column
                if (wordText.contains(this.currentSearchQuery) || midText.contains(this.currentSearchQuery)) {
                    processedList.add(entry);
                }
            }
        }

        // Step 2: Apply sorting to the filtered entry subset
        if (this.currentSortCol != SortColumn.NONE && this.currentSortDir != SortDir.NONE) {
            processedList.sort((a, b) -> {
                int cmp = 0;
                switch (this.currentSortCol) {
                    case WORD -> cmp = a.getFirstText().compareToIgnoreCase(b.getFirstText());
                    case MIDDLE -> {
                        String s1 = a.getMiddleText() == null ? "" : a.getMiddleText();
                        String s2 = b.getMiddleText() == null ? "" : b.getMiddleText();
                        cmp = s1.compareToIgnoreCase(s2);
                    }
                    case LEVEL -> {
                        cmp = Integer.compare(a.getLevel(), b.getLevel());
                        if (cmp == 0) cmp = Double.compare(a.getExposure(), b.getExposure());
                    }
                }
                return this.currentSortDir == SortDir.ASC ? cmp : -cmp;
            });
        }

        // Step 3: Update widget entries and UI state
        this.replaceEntries(processedList);
        this.setScrollAmount(0); // Reset scroll position to top when display updates
    }

    /**
     * Applies sorting to the list entries based on specified target column and direction,
     *
     * @param col Target column to sort by.
     * @param dir Sort direction order.
     */
    public void applySorting(SortColumn col, SortDir dir) {
        this.currentSortCol = col;
        this.currentSortDir = dir;
        updateDisplayList();
    }

    /**
     * Updates the search query and process the list display.
     * @param query The text typed by the user
     */
    public void filterItems(String query) {
        this.currentSearchQuery = query == null ? "" : query.toLowerCase().trim();
        updateDisplayList();
    }
}