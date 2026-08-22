package com.wesleyhdias.minnanocraft.language.morpheme;

import com.wesleyhdias.minnanocraft.language.resolver.TokenProvider;
import com.wesleyhdias.minnanocraft.srs.VocabularyManager;
import com.wesleyhdias.minnanocraft.srs.models.WordProgress;

/**
 * Provider responsible for resolving grammatical particles and morphemes
 * into Japanese scripts based on the player's current progression level.
 */
public class MorphemeProvider implements TokenProvider {

    @Override
    public String resolve(String token) {
        Morpheme morpheme = MorphemeLoader.getMorphemes().get(token);

        if (morpheme == null) {
            return null;
        }

        // Retrieves progress for this morpheme using the same logic as content words
        WordProgress progress = VocabularyManager.getProgress(token);
        int level = (progress != null) ? progress.getScriptLevel() : 0;

        if (level == 0) {
            return null;
        }

        // Renders script based on level (3 = Kanji, 2 = Hiragana, 1 = Romaji)
        if (level >= 3 && morpheme.kanji() != null && !morpheme.kanji().isBlank()) {
            return morpheme.kanji();
        } else if (level >= 2) {
            return morpheme.hiragana();
        } else {
            return morpheme.romaji();
        }
    }
}
