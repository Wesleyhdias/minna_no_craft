package com.wesleyhdias.minnanocraft.language.resolver;

import com.wesleyhdias.minnanocraft.language.dictionary.DictionaryLoader;
import com.wesleyhdias.minnanocraft.language.kana.RomajiSyllableParser;
import com.wesleyhdias.minnanocraft.language.dictionary.CompoundWord;
import com.wesleyhdias.minnanocraft.language.morpheme.MorphemeLoader;
import com.wesleyhdias.minnanocraft.srs.PlayerVocabularyManager;
import com.wesleyhdias.minnanocraft.language.morpheme.Morpheme;
import com.wesleyhdias.minnanocraft.language.dictionary.Word;
import com.wesleyhdias.minnanocraft.srs.models.WordProgress;

import java.util.List;

/**
 * Utility class responsible for determining the appropriate string representation
 * of Japanese words, morphemes, and compound words based on the player's SRS progress.
 */
public class DifficultyResolver {

    /**
     * Renders a simple {@link Word} based on the target SRS level.
     */
    public static String render(Word word, int level) {
        return switch (level) {
            case 1, 2 -> word.romaji();
            case 3 -> word.hiragana();
            case 4 -> word.kanji();
            default -> null;
        };
    }

    /**
     * Renders a {@link Morpheme} based on the target SRS level.
     */
    public static String render(Morpheme morpheme, int level) {
        if (level > 2) {
            return morpheme.kanji() != null ? morpheme.kanji() : morpheme.hiragana();
        } else if (level > 1) {
            return morpheme.hiragana();
        } else {
            return morpheme.romaji();
        }
    }

    /**
     * Renders a {@link CompoundWord} by resolving each of its internal components.
     *
     * @return The formatted string representation, or {@code null} if the player has
     * no progress in any part of the compound word.
     */
    public static String renderCompound(CompoundWord compound) {
        StringBuilder builder = new StringBuilder();
        boolean hasAnyProgress = false;
        List<String> components = compound.components();

        int lastWordLevel = 0;

        for (String compToken : components) {

            if(compToken.contains(" ")) {
                builder.append(" ");
                continue;
            }

            Word compWord = DictionaryLoader.getDictionary().get(compToken);
            if (compWord != null) {

                WordProgress progress = PlayerVocabularyManager.getInstance().getProgress(compToken);
                lastWordLevel = (progress != null) ? progress.getScriptLevel() : 0;

                if (lastWordLevel > 0) {
                    hasAnyProgress = true;
                    builder.append(render(compWord, lastWordLevel));
                } else {

                    builder.append(compWord.getLocalTranslations().getFirst());
                }
                continue;
            }

            Morpheme mw = MorphemeLoader.getMorphemes().get(compToken);
            if (mw != null) {
                builder.append(" ");

                WordProgress sepProgress = PlayerVocabularyManager.getInstance().getProgress(compToken);
                int sepLevel = (sepProgress != null) ? sepProgress.getScriptLevel() : 0;

                builder.append(render(mw, sepLevel));
                builder.append(" ");
            }

            if (lastWordLevel >= 3) {
                // If the previous word is already on hiragana level or above
                String hiraganaText = RomajiSyllableParser.toHiragana(compToken);
                builder.append(hiraganaText).append(" ");
            } else {
                // If the word is still on romaji level
                builder.append(compToken).append(" ");
            }
        }

        if (!hasAnyProgress) {
            return null;
        }

        return builder.toString();
    }

    /**
     * Retrieves the string representation of the word from the previous difficulty level.
     */
    public static String renderPrevious(Word word, int level) {
        return switch (level) {
            case 4 -> word.hiragana();
            case 3 -> word.romaji();
            default -> null;
        };
    }
}