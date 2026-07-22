package com.wesleyhdias.minnanocraft.services;

import com.wesleyhdias.minnanocraft.data.loader.DictionaryLoader;
import com.wesleyhdias.minnanocraft.data.loader.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.data.loader.ProgressLoader;
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

            if (ProgressLoader.getLevel(token) == 0) {
                return false;
            }
        }
        return true;
    }
}
