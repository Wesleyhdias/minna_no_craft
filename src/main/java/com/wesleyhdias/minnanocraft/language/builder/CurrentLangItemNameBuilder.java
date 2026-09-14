package com.wesleyhdias.minnanocraft.language.builder;

import com.wesleyhdias.minnanocraft.language.morpheme.MorphemeProvider;
import com.wesleyhdias.minnanocraft.language.TranslationCacheManager;
import com.wesleyhdias.minnanocraft.language.resolver.TokenProvider;
import com.wesleyhdias.minnanocraft.language.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.language.dictionary.*;

import java.util.ArrayList;
import java.util.Locale;
import java.util.List;

/**
 * Utility builder responsible for progressively replacing native language words
 * in item names with Japanese script renderings based on the player's SRS script levels.
 * <p>
 * Evaluates the item's word structure and substitutes matching localized native words
 * with their progressive Japanese script equivalents, caching results in
 * {@link TranslationCacheManager#BUILDER_CACHE}.
 */
public class CurrentLangItemNameBuilder {

    /** Provider pipeline used to resolve tokens into rendered script strings. */
    private static List<TokenProvider> providers = List.of(
            new CompoundDictionaryProvider(),
            new DictionaryProvider(),
            new MorphemeProvider()
    );

    /**
     * Overrides the provider pipeline for unit testing purposes.
     *
     * @param testProviders List of mock or test {@link TokenProvider} instances.
     */
    public static void setProvidersForTesting(List<TokenProvider> testProviders) {
        providers = testProviders;
    }

    /**
     * Builds the item name in the native language template, replacing learned words
     * with their respective Japanese script renderings based on current SRS progress.
     *
     * @param translationKey The unique translation key of the item.
     * @param originalText   The original native item display name.
     * @return The modified item name with partially or fully translated words.
     */
    public static String build(String translationKey, String originalText) {

        // Returns cached result if available
        if (TranslationCacheManager.BUILDER_CACHE.containsKey(translationKey)) {
            return TranslationCacheManager.BUILDER_CACHE.get(translationKey);
        }

        List<String> structure = ItemStructureLoader.getStructures().get(translationKey);

        if (structure == null) {
            return originalText;
        }

        String result = originalText;
        String remainingToMatch = originalText.toLowerCase(Locale.ROOT);

        for (String token : structure) {
            List<String> localTranslations = getLocalTranslationsForToken(token);

            for (String translation : localTranslations) {
                if (translation == null || translation.isBlank()) continue;

                // Removes matched words from remaining text tracker to prevent redundant operations
                String lowerSearch = translation.toLowerCase(Locale.ROOT);
                if (remainingToMatch.contains(lowerSearch)) {
                    remainingToMatch = remainingToMatch.replace(lowerSearch, " ");
                }

                // Replaces target native word with its script-rendered value
                String renderedValue = resolveFromProviders(token);
                if (renderedValue != null) {
                    result = replaceIgnoreCase(result, translation, renderedValue);
                }
            }

            // Terminates early if all native words in the item name have been matched and processed
            if (remainingToMatch.trim().isEmpty()) {
                break;
            }
        }

        TranslationCacheManager.BUILDER_CACHE.put(translationKey, result);
        return result;
    }

    /**
     * Resolves a token into its rendered string representation by querying registered providers sequentially.
     *
     * @param token The dictionary key or compound token to resolve.
     * @return The resolved rendered string, or {@code null} if no provider could resolve the token.
     */
    private static String resolveFromProviders(String token) {
        for (TokenProvider provider : providers) {
            String value = provider.resolve(token);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * Retrieves all native local translation strings associated with a given token or compound components.
     *
     * @param token The dictionary or compound word token.
     * @return A list of localized native translation strings.
     */
    private static List<String> getLocalTranslationsForToken(String token) {
        CompoundWord compound = CompoundDictionaryLoader.getDictionary().get(token);
        if (compound != null) {
            List<String> compoundLocals = compound.getLocalTranslations();
            if (compoundLocals != null && !compoundLocals.isEmpty()) {
                return compoundLocals;
            }
            // Fallback: collect native translations from individual compound components
            List<String> collected = new ArrayList<>();
            for (String compToken : compound.components()) {
                collected.addAll(getLocalTranslationsForToken(compToken));
            }
            return collected;
        }

        Word word = DictionaryLoader.getDictionary().get(token);
        if (word != null) {
            return word.getLocalTranslations() != null ? word.getLocalTranslations() : List.of();
        }

        return List.of();
    }

    /**
     * Replaces all occurrences of a target search substring within a text string,
     * ignoring case sensitivity while preserving non-matching original text casing.
     *
     * @param text        The full source string.
     * @param search      The target substring to search for.
     * @param replacement The replacement string to substitute.
     * @return The updated string with all occurrences substituted.
     */
    private static String replaceIgnoreCase(String text, String search, String replacement) {
        if (text == null || search == null || search.isEmpty() || replacement == null) {
            return text;
        }

        String lowerText = text.toLowerCase(Locale.ROOT);
        String lowerSearch = search.toLowerCase(Locale.ROOT);

        int index = lowerText.indexOf(lowerSearch);
        if (index < 0) {
            return text;
        }

        StringBuilder sb = new StringBuilder();
        int lastIndex = 0;

        while (index >= 0) {
            sb.append(text, lastIndex, index);
            sb.append(replacement);
            lastIndex = index + search.length();
            index = lowerText.indexOf(lowerSearch, lastIndex);
        }

        sb.append(text.substring(lastIndex));

        return sb.toString();
    }
}