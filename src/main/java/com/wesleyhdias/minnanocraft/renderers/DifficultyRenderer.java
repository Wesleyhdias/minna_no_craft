package com.wesleyhdias.minnanocraft.renderers;

import com.wesleyhdias.minnanocraft.data.models.Word;

public class DifficultyRenderer {

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

    public static String renderPrevious(Word word, int level) {
        return switch (level) {
            case 4 -> word.hiragana();     // Kanji (4) -> Mostra Hiragana (3)
            case 3 -> word.romaji();       // Hiragana (3) -> Mostra Romaji (1)
            default -> null;
        };
    }
}