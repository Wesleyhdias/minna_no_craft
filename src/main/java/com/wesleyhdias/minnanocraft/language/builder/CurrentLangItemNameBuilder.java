package com.wesleyhdias.minnanocraft.language.builder;

import com.wesleyhdias.minnanocraft.language.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.language.TranslationCacheManager;
import com.wesleyhdias.minnanocraft.language.dictionary.DictionaryLoader;
import com.wesleyhdias.minnanocraft.language.dictionary.Word;
import com.wesleyhdias.minnanocraft.language.resolver.DifficultyResolver;
import com.wesleyhdias.minnanocraft.srs.PlayerVocabularyManager;
import com.wesleyhdias.minnanocraft.srs.models.WordProgress;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Utility builder responsible for progressively replacing native language words
 * in item names with Japanese scripts based on the player's unlocked script level.
 */
public class CurrentLangItemNameBuilder {

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

        // If no structure exists, keep the original Minecraft translation.
        if (structure == null) {
            return originalText;
        }

        String result = originalText;

        for (String token : structure) {
            result = resolve(token, result);
        }

        TranslationCacheManager.BUILDER_CACHE.put(translationKey, result);

        return result;
    }

    /**
     * Resolves a single dictionary token and replaces its native-language
     * translations in the current item name if the player has learned it.
     *
     * @param token The dictionary key.
     * @param text  The current item name.
     * @return The item name after resolving the token.
     */
    private static String resolve(String token, String text) {

        Map<String, Word> dictionary = DictionaryLoader.getDictionary();
        Word word = dictionary.get(token);

        if (word == null) {
            return text;
        }

        WordProgress progress = PlayerVocabularyManager.getInstance().getProgress(token);

        int level = (progress != null) ? progress.getScriptLevel() : 0;

        if (level == 0) {
            return text;
        }

        String replacement = DifficultyResolver.render(word, level);

        for (String translation : word.getLocalTranslations()) {
            text = replaceIgnoreCase(text, translation, replacement);
        }

        return text;
    }

    /**
     * Replaces all occurrences of a target search string within a text,
     * ignoring case sensitivity.
     *
     * @param text        The full text to perform replacements on.
     * @param search      The target substring to search for.
     * @param replacement The string to substitute into the text.
     * @return The updated string with replaced text.
     */
    private static String replaceIgnoreCase(
            String text,
            String search,
            String replacement
    ) {
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
