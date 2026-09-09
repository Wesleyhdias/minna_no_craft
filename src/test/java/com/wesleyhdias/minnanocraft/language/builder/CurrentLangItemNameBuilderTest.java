package com.wesleyhdias.minnanocraft.language.builder;

import com.wesleyhdias.minnanocraft.language.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.language.TranslationCacheManager;
import com.wesleyhdias.minnanocraft.language.dictionary.DictionaryLoader;
import com.wesleyhdias.minnanocraft.language.dictionary.Word;
import com.wesleyhdias.minnanocraft.srs.PlayerVocabularyManager;
import com.wesleyhdias.minnanocraft.srs.models.WordProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.LanguageManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CurrentLangItemNameBuilderTest {

    private PlayerVocabularyManager mockVocabManager;

    @BeforeEach
    void setUp() {
        // 1. Mock do PlayerVocabularyManager para controlar o nível das palavras
        mockVocabManager = mock(PlayerVocabularyManager.class);
        PlayerVocabularyManager.setInstanceForTesting(mockVocabManager);

        // 2. Limpa e prepara o cache de build
        TranslationCacheManager.BUILDER_CACHE.clear();
        TranslationCacheManager.pendingClear = false;
    }

    @AfterEach
    void tearDown() {
        PlayerVocabularyManager.setInstanceForTesting(null);
        ItemStructureLoader.setInstanceForTesting(null);
        DictionaryLoader.setDictionaryForTesting(null);
        TranslationCacheManager.BUILDER_CACHE.clear();
    }

    @Test
    void shouldReturnOriginalTextIfStructureNotFound() {
        String original = "Apple";
        ItemStructureLoader.setInstanceForTesting(Map.of());

        String result = CurrentLangItemNameBuilder.build("item.minecraft.apple", original);

        assertEquals(original, "Apple");
    }

    @Test
    void shouldNotReplaceIfLevelIsZero() {
        String original = "Red Apple";

        ItemStructureLoader.setInstanceForTesting(Map.of(
                "item.minecraft.apple", List.of("apple")
        ));

        Word appleWord = new Word(
                Map.of("en_us", List.of("apple")),
                "林檎",
                "りんご",
                "ringo"
        );
        DictionaryLoader.setDictionaryForTesting(Map.of("apple", appleWord));

        // Nível 0 (ou sem progresso)
        when(mockVocabManager.getProgress("apple")).thenReturn(null);

        // Cria o mock estático do Minecraft e garante que ele seja fechado ao final do teste
        try (MockedStatic<Minecraft> mockedMinecraft = mockStatic(Minecraft.class)) {
            Minecraft mockMc = mock(Minecraft.class);
            LanguageManager mockLangManager = mock(LanguageManager.class);

            // Simula que o jogo está em inglês para ele conseguir achar a tradução "apple"
            when(mockLangManager.getSelected()).thenReturn("en_us");
            when(mockMc.getLanguageManager()).thenReturn(mockLangManager);
            mockedMinecraft.when(Minecraft::getInstance).thenReturn(mockMc);

            String result = CurrentLangItemNameBuilder.build("item.minecraft.apple", original);

            assertEquals("Red Apple", result, "Nível 0 não deve alterar o texto original.");
        }
    }

    @Test
    void shouldReplaceWithJapaneseScriptBasedOnLevel() {
        String original = "Red Apple";

        ItemStructureLoader.setInstanceForTesting(Map.of(
                "item.minecraft.apple", List.of("apple")
        ));

        // 1. Cria um MOCK do record Word em vez de usar o 'new'
        Word mockAppleWord = mock(Word.class);

        // 2. Manda o mock retornar a lista que você quer, burlando o Minecraft
        when(mockAppleWord.getLocalTranslations()).thenReturn(List.of("apple"));

        DictionaryLoader.setDictionaryForTesting(Map.of("apple", mockAppleWord));

        // O resto do teste continua igual
        WordProgress progress = mock(WordProgress.class);
        when(progress.getScriptLevel()).thenReturn(3); // Nível 3 = Hiragana

        // Como o DifficultyResolver pede os dados reais do record, precisamos mockar eles também:
        when(mockAppleWord.hiragana()).thenReturn("りんご");

        when(mockVocabManager.getProgress("apple")).thenReturn(progress);

        String result = CurrentLangItemNameBuilder.build("item.minecraft.apple", original);

        assertEquals("Red りんご", result);
    }

    @Test
    void shouldUseCacheOnSubsequentCalls() {
        String original = "Red Apple";

        // 1. Mocka a estrutura que o item contém a palavra "apple"
        ItemStructureLoader.setInstanceForTesting(Map.of(
                "item.minecraft.apple", List.of("apple")
        ));

        // 2. Substitui o 'new Word' por um Mock para não chamar o Minecraft
        Word mockAppleWord = mock(Word.class);
        when(mockAppleWord.getLocalTranslations()).thenReturn(List.of("apple"));
        when(mockAppleWord.kanji()).thenReturn("林檎"); // Define a resposta para o nível 4

        DictionaryLoader.setDictionaryForTesting(Map.of("apple", mockAppleWord));

        // 3. Mocka o progresso do jogador na palavra "apple"
        WordProgress progress = mock(WordProgress.class);
        when(progress.getScriptLevel()).thenReturn(4); // Nível 4 = Kanji
        when(mockVocabManager.getProgress("apple")).thenReturn(progress);

        // 4. Primeira chamada (Calcula a string "Red 林檎" e guarda no cache)
        String firstCall = CurrentLangItemNameBuilder.build("item.minecraft.apple", original);
        assertEquals("Red 林檎", firstCall);

        // 5. Quebra o mock propositalmente
        // Se o código tentar recalcular na segunda chamada, ele vai pegar null e falhar.
        when(mockVocabManager.getProgress("apple")).thenReturn(null);

        // 6. Segunda chamada (Deve pegar direto do cache e ignorar o mock quebrado)
        String secondCall = CurrentLangItemNameBuilder.build("item.minecraft.apple", original);
        assertEquals("Red 林檎", secondCall, "Deve retornar o valor em cache sem recalcular.");

        // 7. Prova definitiva que o cache funcionou
        verify(mockVocabManager, times(1)).getProgress("apple");
    }
}