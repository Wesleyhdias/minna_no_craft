package com.wesleyhdias.minnanocraft.render;

import com.wesleyhdias.minnanocraft.data.models.Word;

public class DifficultyRenderer {

    public static String render(
            Word word,
            int level
    ) {
        return switch (level) {
            case 1 -> word.romaji();
            case 2 -> word.hiragana();
            case 3 -> word.kanji();
            default -> null;
        };
    }
}