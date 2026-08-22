package com.wesleyhdias.minnanocraft.client.tooltip;

import com.wesleyhdias.minnanocraft.language.builder.PortugueseItemNameBuilder;
import com.wesleyhdias.minnanocraft.language.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.language.dictionary.DictionaryLoader;
import com.wesleyhdias.minnanocraft.language.resolver.DifficultyResolver;
import com.wesleyhdias.minnanocraft.srs.VocabularyManager;
import com.wesleyhdias.minnanocraft.srs.models.WordProgress;
import com.wesleyhdias.minnanocraft.language.builder.ItemNameBuilder;
import com.wesleyhdias.minnanocraft.language.dictionary.Word;

import com.wesleyhdias.minnanocraft.language.resolver.TranslationModeResolver;
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
    // ==========================================

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
     * Parses the item builder text into a list of structured words, identifying which parts
     * correspond to interactive vocabulary tokens based on the player's current SRS difficulty level.
     *
     * @param translationKey The unique translation identifier for the item.
     * @param originalName   The fallback original name of the item.
     * @return A list of {@link ParsedWord} elements representing the tokenized item name.
     */
    public static List<ParsedWord> parseBuilderText(String translationKey, String originalName) {
        String cacheKey = getCacheKey(translationKey, originalName);

        // 1. Check if the parsed result is already cached
        if (PARSED_CACHE.containsKey(cacheKey)) {
            return PARSED_CACHE.get(cacheKey);
        }

        List<ParsedWord> result = new ArrayList<>();

        String fullText;
        if (TranslationModeResolver.useJapanese(translationKey)) {
            fullText = ItemNameBuilder.build(translationKey);
        } else {
            fullText = PortugueseItemNameBuilder.build(translationKey, originalName);
        }

        if (fullText == null) fullText = originalName;
        if (fullText == null) return result;

        List<String> structure = ItemStructureLoader.getStructures().get(translationKey);
        String[] words = fullText.split(" ");

        for (String word : words) {
            ParsedWord pw = new ParsedWord();
            pw.text = word;
            pw.isInteractive = false;

            if (structure != null) {
                for (String token : structure) {
                    if (VocabularyManager.isParticle(token)) continue;

                    Word wordObj = DictionaryLoader.getDictionary().get(token);
                    if (wordObj == null) continue;

                    WordProgress progress = VocabularyManager.getProgress(token);
                    int level = (progress != null) ? progress.getScriptLevel() : 0;

                    String renderedText = DifficultyResolver.render(wordObj, level);

                    boolean matchRender = (word.equalsIgnoreCase(renderedText));
                    boolean matchToken = word.equalsIgnoreCase(token);
                    boolean matchTranslation = wordObj.translations() != null &&
                            wordObj.translations().stream().anyMatch(word::equalsIgnoreCase);

                    if (matchRender || matchToken || matchTranslation) {
                        pw.isInteractive = true;
                        pw.token = token;

                        String prevText = DifficultyResolver.renderPrevious(wordObj, level);
                        if (prevText == null) {
                            prevText = (wordObj.translations() != null && !wordObj.translations().isEmpty())
                                    ? wordObj.translations().getFirst()
                                    : token;
                        }
                        pw.prevText = prevText;
                        break;
                    }
                }
            }
            result.add(pw);
        }

        // 2. Save the result into the cache before returning
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

        // 1. Check if the visual component is already built and cached
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

        // 2. Save the visual component into the cache
        COMPONENT_CACHE.put(cacheKey, finalName);
        return finalName;
    }
}