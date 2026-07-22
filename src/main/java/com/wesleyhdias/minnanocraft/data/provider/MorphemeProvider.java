package com.wesleyhdias.minnanocraft.data.provider;

import com.wesleyhdias.minnanocraft.services.VocabularyManager;
import com.wesleyhdias.minnanocraft.data.loader.MorphemeLoader;
import com.wesleyhdias.minnanocraft.data.models.WordProgress;
import com.wesleyhdias.minnanocraft.data.models.Morpheme;

public class MorphemeProvider implements TokenProvider {

    @Override
    public String resolve(String token) {
        Morpheme morpheme = MorphemeLoader.getMorphemes().get(token);

        if (morpheme == null) {
            return null;
        }

        // Busca o progresso deste morfema exatamente como fazemos com as palavras
        WordProgress progress = VocabularyManager.getProgress(token);
        int level = (progress != null) ? progress.getHighestScriptLevel() : 0;

        if (level == 0) {
            return null;
        }

        // Renderiza com base no nível (3 = Kanji, 2 = Hiragana, 1 = Romaji)
        if (level >= 3 && morpheme.kanji() != null && !morpheme.kanji().isBlank()) {
            return morpheme.kanji();
        } else if (level >= 2) {
            return morpheme.hiragana();
        } else {
            return morpheme.romaji();
        }
    }
}
