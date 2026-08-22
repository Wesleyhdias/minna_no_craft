package com.wesleyhdias.minnanocraft.language.builder;

import com.wesleyhdias.minnanocraft.language.dictionary.DictionaryProvider;
import com.wesleyhdias.minnanocraft.language.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.language.morpheme.MorphemeProvider;
import com.wesleyhdias.minnanocraft.language.TranslationCacheManager;
import com.wesleyhdias.minnanocraft.language.resolver.TokenProvider;

import java.util.List;

/**
 * Utility builder responsible for constructing an item's Japanese name
 * by resolving its tokens through a list of providers.
 */
public class ItemNameBuilder {

    private static final List<TokenProvider> PROVIDERS = List.of(
            new DictionaryProvider(),
            new MorphemeProvider()
    );

    /**
     * Builds the resolved item name for a given translation key.
     *
     * @param translationKey The unique translation key of the item.
     * @return The fully built item name, or null if no structure is found.
     */
    public static String build(String translationKey) {

        if (TranslationCacheManager.BUILDER_CACHE.containsKey(translationKey)) {
            return TranslationCacheManager.BUILDER_CACHE.get(translationKey);
        }

        List<String> structure = ItemStructureLoader.getStructures().get(translationKey);

        // If no structure exists, return null so the game can fall back to standard translation
        if (structure == null) {
            return null;
        }

        StringBuilder result = new StringBuilder();

        for (String token : structure) {
            result.append(resolve(token)).append(" ");
        }

        String finalResult = result.toString().trim();
        TranslationCacheManager.BUILDER_CACHE.put(translationKey, finalResult);

        return finalResult;
    }

    /**
     * Resolves an individual token using the available token providers.
     *
     * @param token The token to resolve (a dictionary key or morpheme).
     * @return The resolved text, or the original token if no provider can handle it.
     */
    private static String resolve(String token) {
        for (TokenProvider provider : PROVIDERS) {
            String value = provider.resolve(token);

            if (value != null) {
                return value;
            }
        }
        // Unknown token: keep it exactly as it came
        return token;
    }
}