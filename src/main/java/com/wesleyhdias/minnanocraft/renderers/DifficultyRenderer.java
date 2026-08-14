package com.wesleyhdias.minnanocraft.renderers;

import com.wesleyhdias.minnanocraft.data.models.Word;

/**
 * Utility class responsible for determining the appropriate string representation
 * of a Japanese word based on the player's current vocabulary difficulty level.
 */
public class DifficultyRenderer {

    /**
     * Renders the word based on the target difficulty level.
     * Progression scales from phonetic alphabet (Romaji) up to Kanji.
     *
     * @param word  The {@link Word} object containing the Japanese text variations.
     * @param level The current SRS difficulty level for this word (1-4).
     * @return The appropriate string representation, or {@code null} if the level is invalid.
     */
    public static String render(
            Word word,
            int level
    ) {
        return switch (level) {
            case 1, 2 -> word.romaji();
            case 3 -> word.hiragana();
            case 4 -> word.kanji();
            default -> null;
        };
    }

    /**
     * Retrieves the string representation of the word from the previous difficulty level.
     * This is typically used for providing hints or sub-tooltips (e.g., showing the hiragana
     * reading when the player hovers over a kanji character).
     *
     * @param word  The {@link Word} object containing the Japanese text variations.
     * @param level The current SRS difficulty level for this word.
     * @return The string representation from the previous level, or {@code null} if there is no previous hint available.
     */
    public static String renderPrevious(Word word, int level) {
        return switch (level) {
            case 4 -> word.hiragana();     // Kanji (4) -> Shows Hiragana (3)
            case 2, 3 -> word.romaji();    // Hiragana (3) -> Shows Romaji (1/2)
            default -> null;
        };
    }
}