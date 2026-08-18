package com.wesleyhdias.minnanocraft.builders;

import com.wesleyhdias.minnanocraft.data.loader.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.utils.TranslationCacheManager;
import com.wesleyhdias.minnanocraft.data.loader.DictionaryLoader;
import com.wesleyhdias.minnanocraft.renderers.DifficultyRenderer;
import com.wesleyhdias.minnanocraft.services.VocabularyManager;
import com.wesleyhdias.minnanocraft.data.models.WordProgress;
import com.wesleyhdias.minnanocraft.data.models.Word;

import java.util.Locale;
import java.util.List;
import java.util.Map;

/**
 * Utility builder responsible for progressively replacing native language words
 * in item names with Japanese scripts based on the player's unlocked script level.
 */
public class PortugueseItemNameBuilder {

    /**
     * Builds the item name in the native language template, replacing learned words
     * with their respective Japanese script renderings.
     *
     * @param translationKey The unique translation key of the item.
     * @param originalText   The original native item name.
     * @return The modified item name with partially or fully translated words.
     */
    public static String build(String translationKey, String originalText) {

        if (TranslationCacheManager.BUILDER_CACHE.containsKey(translationKey)) {
            return TranslationCacheManager.BUILDER_CACHE.get(translationKey);
        }

        List<String> structure = ItemStructureLoader.getStructures().get(translationKey);

        if (structure == null) {
            return originalText;
        }

        String result = originalText;
        Map<String, Word> dictionary = DictionaryLoader.getDictionary();

        for (String token : structure) {
            Word word = dictionary.get(token);

            if (word == null) {
                continue;
            }

            // Retrieves progress from VocabularyManager to obtain the highest achieved script level
            WordProgress progress = VocabularyManager.getProgress(token);
            int level = (progress != null) ? progress.getScriptLevel() : 0;

            if (level == 0) {
                continue;
            }

            String replacement = DifficultyRenderer.render(word, level);

            for (String translation : word.translations()) {
                result = replaceIgnoreCase(result, translation, replacement);
            }
        }

        TranslationCacheManager.BUILDER_CACHE.put(translationKey, result);

        return result;
    }

    /**
     * Replaces all occurrences of a target search string within a text, ignoring case sensitivity.
     *
     * @param text        The full text to perform replacements on.
     * @param search      The target substring to search for.
     * @param replacement The string to substitute into the text.
     * @return The updated string with replaced text.
     */
    private static String replaceIgnoreCase(String text, String search, String replacement) {
        String lowerText = text.toLowerCase(Locale.ROOT);
        String lowerSearch = search.toLowerCase(Locale.ROOT);

        int index = lowerText.indexOf(lowerSearch);

        while (index >= 0) {
            text = text.substring(0, index)
                    + replacement
                    + text.substring(index + search.length());

            lowerText = text.toLowerCase(Locale.ROOT);
            index = lowerText.indexOf(lowerSearch);
        }

        return text;
    }
}