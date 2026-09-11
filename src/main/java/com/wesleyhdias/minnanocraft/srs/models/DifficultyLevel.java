package com.wesleyhdias.minnanocraft.srs.models;

/**
 * Represents the rendering difficulty or script display level for vocabulary elements.
 * <p>
 * Maps numeric level indices (0 through 4) to specific rendering states, ranging
 * from native language text up to full Kanji script.
 */
public enum DifficultyLevel {

    /** Native script rendering level (Portuguese/English baseline). */
    PORTUGUESE(0),

    /** Standard Romaji script rendering level. */
    ROMAJI(1),

    /** Inverted Romaji structure rendering level. */
    INVERSED(2),

    /** Kana/Hiragana script rendering level. */
    HIRAGANA(3),

    /** Kanji character script rendering level. */
    KANJI(4);

    /** The integer representation associated with the difficulty level. */
    private final int level;

    /**
     * Constructs a DifficultyLevel enum constant with its corresponding integer index.
     *
     * @param level The numeric level index.
     */
    DifficultyLevel(int level) {
        this.level = level;
    }

    /**
     * Retrieves the numeric index of this difficulty level.
     *
     * @return The level integer.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Resolves an integer level value to its corresponding {@link DifficultyLevel}.
     * Falls back to {@link #ROMAJI} if no exact match is found.
     *
     * @param value The numeric level value to look up.
     * @return The matching {@link DifficultyLevel}, or {@link #ROMAJI} as default fallback.
     */
    public static DifficultyLevel fromInt(int value) {
        for (DifficultyLevel dl : values()) {
            if (dl.level == value) {
                return dl;
            }
        }
        return ROMAJI;
    }
}
