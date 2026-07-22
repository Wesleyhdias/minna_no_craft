package com.wesleyhdias.minnanocraft.services;

import com.wesleyhdias.minnanocraft.data.loader.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.data.loader.DictionaryLoader;
import com.wesleyhdias.minnanocraft.data.models.WordProgress;
import com.wesleyhdias.minnanocraft.data.models.Word;

import java.util.List;

public class TranslationModeResolver {

    public static boolean useJapanese(String translationKey) {

        List<String> structure = ItemStructureLoader.getStructures().get(translationKey);

        if (structure == null) {
            return false;
        }

        for (String token : structure) {

            Word word = DictionaryLoader.getDictionary().get(token);

            if (word == null) {
                continue;
            }

            // BUSCA ATUALIZADA: Consulta o VocabularyManager
            WordProgress progress = VocabularyManager.getProgress(token);
            int level = (progress != null) ? progress.getHighestScriptLevel() : 0;

            if (level == 0) {
                return false;
            }
        }

        return true;
    }
}
