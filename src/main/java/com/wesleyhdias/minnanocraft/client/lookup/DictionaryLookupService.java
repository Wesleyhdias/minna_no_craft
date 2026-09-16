package com.wesleyhdias.minnanocraft.client.lookup;

import com.wesleyhdias.minnanocraft.language.dictionary.CompoundWord;
import com.wesleyhdias.minnanocraft.language.dictionary.Word;

public class DictionaryLookupService {

    private static boolean isOpen = false;
    private static Word currentWord = null;
    private static CompoundWord currentCompWord = null;

    /**
     * Opens the dictionary overlay with the specified word data.
     */
    public static void openWord(Word word) {
        currentWord = word;
        currentCompWord = null;
        isOpen = true;
    }

    /**
     * Opens the dictionary overlay with the specified word data.
     */
    public static void openCompWord(CompoundWord word) {
        currentCompWord = word;
        currentWord = null;
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

    /**
     * Gets the currently displayed word data.
     */
    public static CompoundWord getCurrentCompWord() {
        return currentCompWord;
    }
}