package com.wesleyhdias.minnanocraft.language.resolver;

import com.wesleyhdias.minnanocraft.language.dictionary.Word;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DifficultyResolverTest {

    private Word sampleWord;

    @BeforeEach
    void setUp() {
        // Prepara uma palavra de teste com todas as representações bem definidas
        sampleWord = new Word(
                Map.of(),
                "林檎",    // Kanji
                "りんご",  // Hiragana
                "ringo"   // Romaji
        );
    }

    @Test
    void testRenderLevels() {
        // Níveis 1 e 2 devem retornar Romaji
        assertEquals("ringo", DifficultyResolver.render(sampleWord, 1));
        assertEquals("ringo", DifficultyResolver.render(sampleWord, 2));

        // Nível 3 deve retornar Hiragana
        assertEquals("りんご", DifficultyResolver.render(sampleWord, 3));

        // Nível 4 deve retornar Kanji
        assertEquals("林檎", DifficultyResolver.render(sampleWord, 4));
    }

    @Test
    void testRenderInvalidLevelsReturnsNull() {
        assertNull(DifficultyResolver.render(sampleWord, 0));
        assertNull(DifficultyResolver.render(sampleWord, 5));
        assertNull(DifficultyResolver.render(sampleWord, -1));
    }

    @Test
    void testRenderPreviousLevels() {
        // Nível 4 (Kanji) -> Mostra Hiragana anterior
        assertEquals("りんご", DifficultyResolver.renderPrevious(sampleWord, 4));

        // Níveis 3 e 2 -> Mostram Romaji anterior
        assertEquals("ringo", DifficultyResolver.renderPrevious(sampleWord, 3));
        assertEquals("ringo", DifficultyResolver.renderPrevious(sampleWord, 2));
    }

    @Test
    void testRenderPreviousInvalidLevelsReturnsNull() {
        assertNull(DifficultyResolver.renderPrevious(sampleWord, 1)); // Nível 1 não tem anterior
        assertNull(DifficultyResolver.renderPrevious(sampleWord, 0));
        assertNull(DifficultyResolver.renderPrevious(sampleWord, 5));
    }
}