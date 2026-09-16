package com.wesleyhdias.minnanocraft.client.tooltip;

import com.wesleyhdias.minnanocraft.language.dictionary.CompoundDictionaryLoader;
import com.wesleyhdias.minnanocraft.language.builder.CurrentLangItemNameBuilder;
import com.wesleyhdias.minnanocraft.language.resolver.TranslationModeResolver;
import com.wesleyhdias.minnanocraft.language.builder.JapaneseItemNameBuilder;
import com.wesleyhdias.minnanocraft.language.dictionary.DictionaryLoader;
import com.wesleyhdias.minnanocraft.language.resolver.DifficultyResolver;
import com.wesleyhdias.minnanocraft.language.dictionary.CompoundWord;
import com.wesleyhdias.minnanocraft.language.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.srs.PlayerVocabularyManager;
import com.wesleyhdias.minnanocraft.language.dictionary.Word;
import com.wesleyhdias.minnanocraft.srs.models.WordProgress;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class responsible for formatting item names within tooltips.
 * <p>
 * It parses item text structures, identifies interactive vocabulary tokens,
 * applies Spaced Repetition System (SRS) color/style formatting, and utilizes
 * an LRU Cache system to optimize performance and prevent re-processing strings every render frame.
 */
public class TooltipFormatter {

    private static final Style INTERACTIVE_STYLE = Style.EMPTY.withUnderlined(true).withColor(0xFFFF55);
    private static final Style DEFAULT_STYLE = Style.EMPTY.withColor(0xFFFFFF);

    // ==========================================
    // CACHE SYSTEM (Max of 500 items to prevent RAM overflow)
    // ==========================================
    private static final int MAX_CACHE_SIZE = 500;

    private static final Map<String, List<ParsedWord>> PARSED_CACHE = new LinkedHashMap<String, List<ParsedWord>>(MAX_CACHE_SIZE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, List<ParsedWord>> eldest) {
            return size() > MAX_CACHE_SIZE;
        }
    };

    private static final Map<String, Component> COMPONENT_CACHE = new LinkedHashMap<String, Component>(MAX_CACHE_SIZE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Component> eldest) {
            return size() > MAX_CACHE_SIZE;
        }
    };

    /**
     * Clears all tooltip caches.
     * <p>
     * Should be called whenever a word's difficulty level changes or when cache invalidation is required.
     */
    public static void clearCache() {
        PARSED_CACHE.clear();
        COMPONENT_CACHE.clear();
    }

    /**
     * Generates a unique cache key combining the translation identifier and the original item name.
     *
     * @param translationKey The unique translation identifier for the item.
     * @param originalName   The fallback original name of the item.
     * @return A composite string key for cache mapping.
     */
    private static String getCacheKey(String translationKey, String originalName) {
        return translationKey + "|" + originalName;
    }

    /**
     * Represents a single parsed word component within an item's tooltip display name,
     * tracking its interactive state and corresponding SRS metadata.
     */
    public static class ParsedWord {
        public String text;
        public boolean isInteractive;
        public String token;
        public String prevText;
    }

    /**
     * Parses item display names into a list of {@link ParsedWord} tokens for interactive rendering and tooltips.
     * <p>
     * Splits formatted display text, expands compound structure tokens, and performs multi-word greedy matching
     * (up to 4 words) against registered vocabulary dictionary entries to identify interactive token segments.
     * Results are cached in {@code PARSED_CACHE} to minimize overhead.
     *
     * @param translationKey The translation key identifier of the item.
     * @param originalName   The native unformatted item display name.
     * @return A list of {@link ParsedWord} objects representing interactive and non-interactive text tokens.
     */
    public static List<ParsedWord> parseBuilderText(String translationKey, String originalName) {
        String cacheKey = getCacheKey(translationKey, originalName);

        // Checks whether the parsed result is already cached
        if (PARSED_CACHE.containsKey(cacheKey)) {
            return PARSED_CACHE.get(cacheKey);
        }

        List<ParsedWord> result = new ArrayList<>();

        String fullText;
        if (TranslationModeResolver.useJapanese(translationKey)) {
            fullText = JapaneseItemNameBuilder.build(translationKey);
        } else {
            fullText = CurrentLangItemNameBuilder.build(translationKey, originalName);
        }

        if (fullText == null) fullText = originalName;
        if (fullText == null) return result;

        List<String> structure = ItemStructureLoader.getStructures().get(translationKey);
        String[] words = fullText.split(" ");

        // Expands structure tokens to ensure compound words are broken down into active component keys
        List<String> availableTokens = new ArrayList<>();
        if (structure != null) {
            for (String token : structure) {
                CompoundWord compound = CompoundDictionaryLoader.getDictionary().get(token);
                if (compound != null) {
                    // Add the components first
                    availableTokens.addAll(compound.components());
                }
                availableTokens.add(token);
            }
        }

        int n = words.length;
        int i = 0;
        PlayerVocabularyManager vocabManager = PlayerVocabularyManager.getInstance();

        while (i < n) {
            boolean matched = false;

            // Try find from the smallest to the biggest block (4 word)
            for (int length = 1; length <= Math.min(n - i, 4); length++) {
                StringBuilder phraseBuilder = new StringBuilder();
                for (int j = 0; j < length; j++) {
                    if (j > 0) phraseBuilder.append(" ");
                    phraseBuilder.append(words[i + j]);
                }
                String candidate = phraseBuilder.toString();

                if (!availableTokens.isEmpty()) {
                    for (String token : availableTokens) {
                        if (vocabManager.isParticle(token)) continue;

                        CompoundWord compoundObj = CompoundDictionaryLoader.getDictionary().get(token);
                        Word wordObj = DictionaryLoader.getDictionary().get(token);

                        boolean matchRender = false;
                        boolean matchToken = candidate.equalsIgnoreCase(token);
                        boolean matchTranslation = false;
                        String prevText = token; // Fallback padrão

                        if (compoundObj != null) {
                            String renderedCompound = DifficultyResolver.renderCompound(compoundObj);
                            assert renderedCompound != null;
                            matchRender = candidate.equalsIgnoreCase(renderedCompound.trim());

                        } else if (wordObj != null) {
                            WordProgress progress = vocabManager.getProgress(token);
                            int level = (progress != null) ? progress.getScriptLevel() : 0;

                            matchRender = candidate.equalsIgnoreCase(DifficultyResolver.render(wordObj, level));

                            if (wordObj.getLocalTranslations() != null) {
                                for (String localTrans : wordObj.getLocalTranslations()) {
                                    if (candidate.equalsIgnoreCase(localTrans)) {
                                        matchTranslation = true;
                                        break;
                                    }
                                }
                            }

                            prevText = DifficultyResolver.renderPrevious(wordObj, level);
                            if (prevText == null) {
                                prevText = (wordObj.getLocalTranslations() != null && !wordObj.getLocalTranslations().isEmpty())
                                        ? wordObj.getLocalTranslations().getFirst()
                                        : token;
                            }

                        }

                        if (matchRender || matchToken || matchTranslation) {
                            // Adds each word in the matched phrase block as an interactive token bound to the key
                            for (int j = 0; j < length; j++) {
                                ParsedWord pw = new ParsedWord();
                                pw.text = words[i + j];
                                pw.isInteractive = true;
                                pw.token = token;
                                pw.prevText = prevText;
                                result.add(pw);
                            }

                            // Remove o token da lista para "marcar como mexido" e evitar duplicação
                            availableTokens.remove(token);

                            i += length; // Avança o ponteiro pelo tamanho da frase capturada
                            matched = true;
                            break; // Sai do for do availableTokens
                        }
                    }
                }
                if (matched) break; // Sai do for do tamanho da frase
            }

            // Handles unmatched words as standard non-interactive tokens
            if (!matched) {
                ParsedWord pw = new ParsedWord();
                pw.text = words[i];
                pw.isInteractive = false;
                result.add(pw);
                i++;
            }
        }

        // Caches the result list before returning
        PARSED_CACHE.put(cacheKey, result);
        return result;
    }

    /**
     * Formats the final Minecraft {@link Component} for the item name, applying specific styling
     * (colors and underlines) depending on whether a token is interactive or standard text.
     *
     * @param translationKey The unique translation identifier for the item.
     * @param originalName   The fallback original name of the item.
     * @return A styled {@link Component} ready to be displayed inside tooltips.
     */
    public static Component formatItemName(String translationKey, String originalName) {
        String cacheKey = getCacheKey(translationKey, originalName);

        // Check if the visual component is already built and cached
        if (COMPONENT_CACHE.containsKey(cacheKey)) {
            return COMPONENT_CACHE.get(cacheKey);
        }

        List<ParsedWord> parsedWords = parseBuilderText(translationKey, originalName);
        MutableComponent finalName = Component.empty();

        for (int i = 0; i < parsedWords.size(); i++) {
            ParsedWord pw = parsedWords.get(i);

            if (pw.isInteractive) {
                finalName.append(Component.literal(pw.text).withStyle(INTERACTIVE_STYLE));
            } else {
                finalName.append(Component.literal(pw.text).withStyle(DEFAULT_STYLE));
            }

            if (i < parsedWords.size() - 1) {
                finalName.append(Component.literal(" "));
            }
        }

        // Save the visual component into the cache
        COMPONENT_CACHE.put(cacheKey, finalName);
        return finalName;
    }
}