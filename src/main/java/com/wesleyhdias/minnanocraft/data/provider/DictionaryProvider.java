package com.wesleyhdias.minnanocraft.data.provider;

import com.wesleyhdias.minnanocraft.data.loader.DictionaryLoader;
import com.wesleyhdias.minnanocraft.services.VocabularyManager;
import com.wesleyhdias.minnanocraft.render.DifficultyRenderer;
import com.wesleyhdias.minnanocraft.data.models.WordProgress;
import com.wesleyhdias.minnanocraft.data.models.Word;

public class DictionaryProvider implements TokenProvider {

    @Override
    public String resolve(String token) {
        Word word = DictionaryLoader.getDictionary().get(token);

        if (word == null) {
            return null;
        }

        // Busca o progresso dinâmico do jogador no Manager
        WordProgress progress = VocabularyManager.getProgress(token);

        // Se a palavra for nova (progress == null), começa no nível 1 (Romaji)
        int level = (progress != null) ? progress.getHighestScriptLevel() : 0;

        return DifficultyRenderer.render(word, level);
    }
}
