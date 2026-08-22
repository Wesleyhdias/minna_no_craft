package com.wesleyhdias.minnanocraft.srs.models;

public enum DifficultyLevel {
    PORTUGUESE(0),
    ROMAJI(1),
    INVERSED(2),
    HIRAGANA(3),
    KANJI(4);

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
