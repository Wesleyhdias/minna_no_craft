package com.wesleyhdias.minnanocraft.services;

import com.wesleyhdias.minnanocraft.data.DictionaryLoader;
import com.wesleyhdias.minnanocraft.data.Word;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranslationManager {

    private static final TranslationManager INSTANCE = new TranslationManager();

    private final Map<String, Word> wordDatabase;

    private TranslationManager() {
        this.wordDatabase = DictionaryLoader.getDictionary();
    }

    public static TranslationManager getInstance() {
        return INSTANCE;
    }

    public String processTranslation(String key, String originalText) {

        if (!key.startsWith("item.minecraft.") && !key.startsWith("block.minecraft.")) {
            return originalText;
        }

        String itemName = key.substring(key.lastIndexOf('.') + 1);

        List<String> tokens = tokenize(itemName);

        String translated = build(tokens);

        return translated != null ? translated : originalText;
    }

    private List<String> tokenize(String itemName) {

        String[] parts = itemName.split("_");
        List<String> result = new ArrayList<>();

        int i = 0;

        while (i < parts.length) {

            String best = parts[i];
            int bestLength = 1;

            String current = parts[i];

            for (int j = i + 1; j < parts.length; j++) {

                current += "_" + parts[j];

                if (wordDatabase.containsKey(current)) {
                    best = current;
                    bestLength = j - i + 1;
                }
            }

            result.add(best);
            i += bestLength;
        }

        return result;
    }

    private String build(List<String> tokens) {

        StringBuilder result = new StringBuilder();
        boolean translatedAny = false;

        for (int i = 0; i < tokens.size(); i++) {

            String token = tokens.get(i);
            Word current = wordDatabase.get(token);

            if (current != null) {
                result.append(current.kanji());
                translatedAny = true;

                if (i < tokens.size() - 1) {
                    Word next = wordDatabase.get(tokens.get(i + 1));

                    if (needsNoParticle(current, next)) {
                        result.append("の");
                    }
                }
            } else {

                result.append(token);
            }
        }

        return translatedAny ? result.toString() : null;
    }

    private static boolean needsNoParticle(Word current, Word next) {
        return current != null &&
                ("no_particle".equals(current.role()) || (next != null && "no_particle".equals(next.role())));
    }
}
