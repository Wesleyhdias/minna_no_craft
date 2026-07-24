package com.wesleyhdias.minnanocraft.services;

import com.wesleyhdias.minnanocraft.data.loader.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.data.models.WordProgress;

import java.util.List;

public class TranslationModeResolver {

    /**
     * O item muda para o template japonês quando todas as palavras que possuem
     * tradução (substantivos) saírem do nível 0 (Português).
     */
    public static boolean useJapanese(String translationKey) {
        List<String> structure = ItemStructureLoader.getStructures().get(translationKey);
        if (structure == null || structure.isEmpty()) return false;

        for (String token : structure) {
            // Ignora os morfemas (ex: "no") pois eles não definem se o item deve mudar de formato
            if (VocabularyManager.isParticle(token)) continue;

            WordProgress progress = VocabularyManager.getProgress(token);
            if (progress == null || progress.getScriptLevel() == 0) {
                return false;
            }
        }

        return true;
    }
}
