package com.wesleyhdias.minnanocraft.language;

/**
 * Utility class for constructing translation keys related to language tokens.
 */
public class TokenTextHelper {
    private static final String PREFIX = "minnanocraft.token.";

    /**
     * Returns the description translation key for the specified token.
     * <p>
     * Example: {@code minnanocraft.token.tipped.desc}
     *
     * @param token the token identifier
     * @return the formatted translation key string for the description
     */
    public static String getDescriptionKey(String token) {
        return PREFIX + token + ".desc";
    }

    /**
     * Returns the example translation key for the specified token.
     * <p>
     * Example: {@code minnanocraft.token.tipped.example}
     *
     * @param token the token identifier
     * @return the formatted translation key string for the example sentence
     */
    public static String getExampleKey(String token) {
        return PREFIX + token + ".example";
    }
}