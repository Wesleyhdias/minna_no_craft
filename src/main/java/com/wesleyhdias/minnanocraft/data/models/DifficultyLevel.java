package com.wesleyhdias.minnanocraft.data.models;

public enum DifficultyLevel {
    PORTUGUESE(0),
    ROMAJI(1),
    HIRAGANA(2),
    KANJI(3);

    private final int level;

    DifficultyLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public static DifficultyLevel fromInt(int value) {
        for (DifficultyLevel dl : values()) {
            if (dl.level == value) {
                return dl;
            }
        }
        return ROMAJI;
    }
}
