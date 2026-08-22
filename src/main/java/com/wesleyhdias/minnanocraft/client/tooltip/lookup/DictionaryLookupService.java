package com.wesleyhdias.minnanocraft.client.tooltip.lookup;

import com.wesleyhdias.minnanocraft.language.dictionary.Word;

public class DictionaryLookupService {

    private static boolean isOpen = false;
    private static Word currentWord = null;

    /**
     * Opens the dictionary overlay with the specified word data.
     */
    public static void open(Word word) {
        currentWord = word;
        isOpen = true;
    }

    /**
     * Closes the dictionary overlay and clears the current word data.
     */
    public static void close() {
        isOpen = false;
        currentWord = null;
    }

    /**
     * Returns true if the dictionary overlay is currently active.
     */
    public static boolean isOpen() {
        return isOpen;
    }

    /**
     * Gets the currently displayed word data.
     */
    public static Word getCurrentWord() {
        return currentWord;
    }
}