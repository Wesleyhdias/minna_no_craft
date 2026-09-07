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
 * in item names with Japanese scripts based on the player's unlocked script level.
 */
public class CurrentLangItemNameBuilder {

    private static List<TokenProvider> providers = List.of(
            new CompoundDictionaryProvider(),
            new DictionaryProvider(),
            new MorphemeProvider()
    );

    public static void setProvidersForTesting(List<TokenProvider> testProviders) {
        providers = testProviders;
    }

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
        String remainingToMatch = originalText.toLowerCase(Locale.ROOT);

        for (String token : structure) {
            List<String> localTranslations = getLocalTranslationsForToken(token);

            // 1. Apaga as palavras encontradas da string de controle
            for (String translation : localTranslations) {
                if (translation == null || translation.isBlank()) continue;
                String lowerSearch = translation.toLowerCase(Locale.ROOT);
                if (remainingToMatch.contains(lowerSearch)) {
                    remainingToMatch = remainingToMatch.replace(lowerSearch, " ");
                }
            }

            // 2. Pergunta para os providers se esse token tem tradução renderizada
            String renderedValue = resolveFromProviders(token);

            // 3. Substitui no texto original apenas se tiver uma tradução válida
            if (renderedValue != null) {
                for (String translation : localTranslations) {
                    if (translation == null || translation.isBlank()) continue;
                    result = replaceIgnoreCase(result, translation, renderedValue);
                }
            }

            // 4. Se a string de controle ficou vazia, encerra precocemente
            if (remainingToMatch.trim().isEmpty()) {
                break;
            }
        }

        TranslationCacheManager.BUILDER_CACHE.put(translationKey, result);
        return result;
    }

    /**
     * Resolves a single token
     *
     * @param token The dictionary key.
     * @return The string of the word after resolving the token or null.
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

    private static List<String> getLocalTranslationsForToken(String token) {
        CompoundWord compound = CompoundDictionaryLoader.getDictionary().get(token);
        if (compound != null) {
            List<String> compoundLocals = compound.getLocalTranslations();
            if (compoundLocals != null && !compoundLocals.isEmpty()) {
                return compoundLocals;
            }
            // Se for palavra composta sem tradução própria, resolve os subcomponentes
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
     * Replaces all occurrences of a target search string within a text,
     * ignoring case sensitivity.
     *
     * @param text        The full text to perform replacements on.
     * @param search      The target substring to search for.
     * @param replacement The string to substitute into the text.
     * @return The updated string with replaced text.
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
            // Copia o trecho que veio antes da correspondência
            sb.append(text, lastIndex, index);
            // Adiciona o valor substituído
            sb.append(replacement);

            // Avança o índice para DEPOIS do trecho original encontrado
            lastIndex = index + search.length();

            // Busca a PRÓXIMA ocorrência apenas a partir de lastIndex
            index = lowerText.indexOf(lowerSearch, lastIndex);
        }

        // Copia o restante do texto que sobrou
        sb.append(text.substring(lastIndex));

        return sb.toString();
    }
}