package com.wesleyhdias.minnanocraft.utils;

import com.wesleyhdias.minnanocraft.data.loader.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.data.models.WordProgress;
import com.wesleyhdias.minnanocraft.services.VocabularyManager;

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
            // Ignores particles (like "no") since they don't dictate whether the item format should change
            if (VocabularyManager.isParticle(token)) continue;

            WordProgress progress = VocabularyManager.getProgress(token);
            if (progress == null || progress.getScriptLevel() == 0) {
                return false;
            }
        }

        return true;
    }
}
