package com.wesleyhdias.minnanocraft.data.provider;

import com.wesleyhdias.minnanocraft.data.loader.DictionaryLoader;
import com.wesleyhdias.minnanocraft.data.loader.ProgressLoader;
import com.wesleyhdias.minnanocraft.data.models.Word;
import com.wesleyhdias.minnanocraft.services.DifficultyRenderer;

public class DictionaryProvider implements TokenProvider {

    @Override
    public String resolve(String token) {

        Word word = DictionaryLoader.getDictionary().get(token);

        if (word == null) {
            return null;
        }

        int level = ProgressLoader.getLevel(token);

        return DifficultyRenderer.render(word, level);
    }
}
