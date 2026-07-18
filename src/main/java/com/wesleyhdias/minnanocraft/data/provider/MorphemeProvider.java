package com.wesleyhdias.minnanocraft.data.provider;

import com.wesleyhdias.minnanocraft.data.loader.MorphemeLoader;
import com.wesleyhdias.minnanocraft.data.models.Morpheme;

public class MorphemeProvider implements TokenProvider {

    @Override
    public String resolve(String token) {

        Morpheme morpheme = MorphemeLoader
                .getMorphemes()
                .get(token);

        if (morpheme == null) {
            return null;
        }

        if (morpheme.kanji() != null &&
                !morpheme.kanji().isBlank()) {

            return morpheme.kanji();
        }

        return morpheme.hiragana();
    }
}
