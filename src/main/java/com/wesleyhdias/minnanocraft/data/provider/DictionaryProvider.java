package com.wesleyhdias.minnanocraft.data.provider;

import com.wesleyhdias.minnanocraft.data.loader.DictionaryLoader;
import com.wesleyhdias.minnanocraft.services.VocabularyManager;
import com.wesleyhdias.minnanocraft.renderers.DifficultyRenderer;
import com.wesleyhdias.minnanocraft.data.models.WordProgress;
import com.wesleyhdias.minnanocraft.data.models.Word;

/**
 * Provider responsible for resolving standard dictionary content words
 * using the DifficultyRenderer based on player progress.
 */
public class DictionaryProvider implements TokenProvider {

    @Override
    public String resolve(String token) {
        Word word = DictionaryLoader.getDictionary().get(token);

        if (word == null) {
            return null;
        }

        // Retrieves dynamic player progress from VocabularyManager (defaults to level 0 if untracked)
        WordProgress progress = VocabularyManager.getProgress(token);
        int level = (progress != null) ? progress.getHighestScriptLevel() : 0;

        return DifficultyRenderer.render(word, level);
    }
}
