package com.wesleyhdias.minnanocraft.services;

import com.wesleyhdias.minnanocraft.data.models.Word;

public class DifficultyRenderer {

    public static String render(
            Word word,
            int level
    ) {

        return switch (level) {
            case 1 -> word.romanji();
            case 2 -> word.hiragana();
            default -> word.kanji();
        };
    }
}