package com.wesleyhdias.minnanocraft.utils;

import com.wesleyhdias.minnanocraft.builders.PortugueseItemNameBuilder;
import com.wesleyhdias.minnanocraft.data.loader.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.data.loader.DictionaryLoader;
import com.wesleyhdias.minnanocraft.renderers.DifficultyRenderer;
import com.wesleyhdias.minnanocraft.services.VocabularyManager;
import com.wesleyhdias.minnanocraft.data.models.WordProgress;
import com.wesleyhdias.minnanocraft.builders.ItemNameBuilder;
import com.wesleyhdias.minnanocraft.data.models.Word;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class responsible for formatting item names in tooltips.
 * It processes the built item name strings, identifies interactive vocabulary tokens,
 * and applies specific visual styles for rendering in-game.
 */
public class TooltipFormatter {

    /** Style applied to interactive dictionary words (yellow and underlined). */
    private static final Style INTERACTIVE_STYLE = Style.EMPTY.withUnderlined(true).withColor(0xFFFF55);

    /** Default style applied to standard, non-interactive words (white). */
    private static final Style DEFAULT_STYLE = Style.EMPTY.withColor(0xFFFFFF);

    /**
     * Helper data class to map and hold information about a single parsed word
     * from the generated item name string.
     */
    public static class ParsedWord {
        /** The actual text string of the word to be rendered. */
        public String text;

        /** Indicates whether this word is a mapped vocabulary token and should be interactive. */
        public boolean isInteractive;

        /** The original dictionary token associated with this word, if applicable. */
        public String token;

        /** The text to be displayed when the user hovers over or interacts with this word. */
        public String prevText;
    }

    /**
     * Parses the final generated item name phrase and identifies which words
     * are interactive dictionary tokens.
     *
     * @param translationKey The translation key of the item.
     * @param originalName   The original localized name of the item.
     * @return A list of {@link ParsedWord} objects containing formatting and mapping data.
     */
    public static List<ParsedWord> parseBuilderText(String translationKey, String originalName) {
        List<ParsedWord> result = new ArrayList<>();

        // Retrieve the exact final string, regardless of which builder was used (e.g., "Espada de Tetsu")
        String fullText;
        if (TranslationModeResolver.useJapanese(translationKey)) {
            fullText = ItemNameBuilder.build(translationKey);
        } else {
            fullText = PortugueseItemNameBuilder.build(translationKey, originalName);
        }

        // Fallback to the original name if the builder fails
        if (fullText == null) fullText = originalName;
        if (fullText == null) return result;

        List<String> structure = ItemStructureLoader.getStructures().get(translationKey);

        // Split the complete phrase into individual words
        String[] words = fullText.split(" ");

        for (String word : words) {
            ParsedWord pw = new ParsedWord();
            pw.text = word;
            pw.isInteractive = false;

            if (structure != null) {
                for (String token : structure) {
                    // Skip grammar particles as they are not standard vocabulary tokens
                    if (VocabularyManager.isParticle(token)) continue;

                    Word wordObj = DictionaryLoader.getDictionary().get(token);
                    if (wordObj == null) continue;

                    WordProgress progress = VocabularyManager.getProgress(token);
                    int level = (progress != null) ? progress.getScriptLevel() : 0;

                    // 1. Get the rendered text for the current difficulty level.
                    // Returns null if the level is 0.
                    String renderedText = DifficultyRenderer.render(wordObj, level);

                    // 2. Match Logic: Check if the word matches the rendered text, the raw token,
                    // or ANY of the loaded translations.
                    boolean matchRender = (word.equalsIgnoreCase(renderedText));
                    boolean matchToken = word.equalsIgnoreCase(token);
                    boolean matchTranslation = wordObj.translations() != null &&
                            wordObj.translations().stream().anyMatch(word::equalsIgnoreCase);

                    if (matchRender || matchToken || matchTranslation) {
                        pw.isInteractive = true;
                        pw.token = token;

                        // Resolve the text to be shown on hover
                        String prevText = DifficultyRenderer.renderPrevious(wordObj, level);
                        if (prevText == null) {
                            // If no previous render level exists, default to the first translation or the raw token
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
        return result;
    }

    /**
     * Constructs a styled Minecraft text component from the parsed words,
     * applying specific colors and formatting to interactive elements.
     *
     * @param translationKey The translation key of the item.
     * @param originalName   The original localized name of the item.
     * @return A {@link Component} containing the fully formatted and styled item name.
     */
    public static Component formatItemName(String translationKey, String originalName) {
        List<ParsedWord> parsedWords = parseBuilderText(translationKey, originalName);
        MutableComponent finalName = Component.empty();

        for (int i = 0; i < parsedWords.size(); i++) {
            ParsedWord pw = parsedWords.get(i);

            // Apply the interactive style ONLY if the word is a mapped dictionary token
            if (pw.isInteractive) {
                finalName.append(Component.literal(pw.text).withStyle(INTERACTIVE_STYLE));
            } else {
                finalName.append(Component.literal(pw.text).withStyle(DEFAULT_STYLE));
            }

            // Append a space between words, except after the last word
            if (i < parsedWords.size() - 1) {
                finalName.append(Component.literal(" "));
            }
        }

        return finalName;
    }
}