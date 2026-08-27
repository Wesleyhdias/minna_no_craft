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
    private final ConcurrentHashMap<String, WordProgress> progressMap = PlayerVocabularyManager.getVocabularyCache();

    /** X position boundary for the list widget layout. */
    private final int listX;

    /** Total width allocated for the list widget layout. */
    private final int listWidth;

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
            this.addEntry(new PlayerProgressListEntry("Nenhuma palavra", "-/-", null, null, this.listX, this.listWidth));
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
     * Applies sorting to the list entries based on specified target column and direction,
     * updating the displayed list items and resetting scroll position.
     *
     * @param col Target column to sort by.
     * @param dir Sort direction order.
     */
    public void applySorting(SortColumn col, SortDir dir) {
        // Restore baseline unsorted entries if sort state is reset or column is NONE
        if (dir == SortDir.NONE || col == SortColumn.NONE) {
            this.replaceEntries(this.originalEntries);
            return;
        }

        // Create shallow copy to sort entries without mutating original baseline
        List<PlayerProgressListEntry> sorted = new ArrayList<>(this.originalEntries);

        sorted.sort((a, b) -> {
            int cmp = 0;
            switch (col) {
                case WORD -> cmp = a.getFirstText().compareToIgnoreCase(b.getFirstText());
                case MIDDLE -> {
                    String s1 = a.getMiddleText() == null ? "" : a.getMiddleText();
                    String s2 = b.getMiddleText() == null ? "" : b.getMiddleText();
                    cmp = s1.compareToIgnoreCase(s2);
                }
                case LEVEL -> {
                    cmp = Integer.compare(a.getLevel(), b.getLevel());
                    // Secondary tie-breaker by exposure points if levels match
                    if (cmp == 0) cmp = Double.compare(a.getExposure(), b.getExposure());
                }
            }
            return dir == SortDir.ASC ? cmp : -cmp;
        });

        // Update displayed entries and reset scroll bar position to top
        this.replaceEntries(sorted);
        this.setScrollAmount(0);
    }
}