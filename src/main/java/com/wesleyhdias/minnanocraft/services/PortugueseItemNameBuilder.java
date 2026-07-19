package com.wesleyhdias.minnanocraft.services;

import com.wesleyhdias.minnanocraft.data.loader.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.data.loader.DictionaryLoader;
import com.wesleyhdias.minnanocraft.data.loader.ProgressLoader;
import com.wesleyhdias.minnanocraft.data.models.Word;

import java.util.Locale;
import java.util.List;
import java.util.Map;

public class PortugueseItemNameBuilder {

    public static String build(
            String translationKey,
            String originalText
    ) {

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

            int level = ProgressLoader.getLevel(token);

            if (level == 0) {
                continue;
            }

            String replacement =
                    getDisplayText(word, level);

            for (String translation : word.translations()) {

                result = replaceIgnoreCase(
                        result,
                        translation,
                        replacement
                );
            }
        }

        return result;
    }

    private static String getDisplayText(
            Word word,
            int level
    ) {

        return switch (level) {

            case 1 -> word.romanji();
            case 2 -> word.hiragana();
            default -> word.kanji();
        };
    }

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
                            + text.substring(
                            index + search.length()
                    );

            lowerText = text.toLowerCase(Locale.ROOT);

            index = lowerText.indexOf(lowerSearch);
        }

        return text;
    }
}