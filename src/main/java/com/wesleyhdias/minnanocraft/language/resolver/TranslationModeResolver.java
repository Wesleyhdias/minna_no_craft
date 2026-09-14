package com.wesleyhdias.minnanocraft.language.resolver;

import com.wesleyhdias.minnanocraft.language.dictionary.CompoundDictionaryLoader;
import com.wesleyhdias.minnanocraft.language.dictionary.DictionaryLoader;
import com.wesleyhdias.minnanocraft.language.dictionary.CompoundWord;
import com.wesleyhdias.minnanocraft.language.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.srs.PlayerVocabularyManager;
import com.wesleyhdias.minnanocraft.srs.models.WordProgress;

import java.util.List;

/**
 * Utility class responsible for determining which language template an item should use.
 */
public class TranslationModeResolver {

    /**
     * Determines if the item should switch to the Japanese template.
     * The item switches only when all translatable words (e.g., nouns)
     * in its structure have surpassed level 0 (Native Language).
     *
     * @param translationKey The unique translation key of the item.
     * @return true if the Japanese template should be used, false otherwise.
     */
    public static boolean useJapanese(String translationKey) {
        List<String> structure = ItemStructureLoader.getStructures().get(translationKey);
        if (structure == null || structure.isEmpty()) return false;

        for (String token : structure) {
            CompoundWord compound = CompoundDictionaryLoader.getDictionary().get(token);

            if (compound != null) {
                // If it's compound, checks the progress if each component
                for (String comp : compound.components()) {
                    if (!DictionaryLoader.getDictionary().containsKey(comp)) continue;

                    WordProgress progress = PlayerVocabularyManager.getInstance().getProgress(comp);
                    if (progress == null || progress.getScriptLevel() < 2) {
                        return false;
                    }
                }
            } else {

                if (!DictionaryLoader.getDictionary().containsKey(token)) continue;

                WordProgress progress = PlayerVocabularyManager.getInstance().getProgress(token);
                if (progress == null || progress.getScriptLevel() < 2) {
                    return false;
                }
            }
        }
        return true;
    }
}
