package com.wesleyhdias.minnanocraft.language.resolver;

import com.wesleyhdias.minnanocraft.language.dictionary.DictionaryLoader;
import com.wesleyhdias.minnanocraft.language.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.srs.PlayerVocabularyManager;
import com.wesleyhdias.minnanocraft.language.dictionary.Word;
import com.wesleyhdias.minnanocraft.srs.models.WordProgress;
import com.wesleyhdias.minnanocraft.config.data.ConfigData;
import com.wesleyhdias.minnanocraft.config.ModConfig;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TranslationModeResolverTest {

    private PlayerVocabularyManager mockManager;

    @BeforeEach
    void setUp() {
        mockManager = Mockito.mock(PlayerVocabularyManager.class);
        PlayerVocabularyManager.setInstanceForTesting(mockManager);

        ConfigData mockConfig = Mockito.mock(ConfigData.class);
        Mockito.when(mockConfig.getExpLevel1()).thenReturn(15.0);
        Mockito.when(mockConfig.getExpLevel2()).thenReturn(30.0);
        Mockito.when(mockConfig.getExpLevel3()).thenReturn(45.0);
        Mockito.when(mockConfig.getExpLevel4()).thenReturn(100.0);
        ModConfig.setInstanceForTesting(mockConfig);

        Word word = new Word(null, null, null, null);
        DictionaryLoader.setDictionaryForTesting(Map.of("ringo", word));
    }

    @AfterEach
    void tearDown() {
        PlayerVocabularyManager.setInstanceForTesting(null);
        ModConfig.setInstanceForTesting(null);
        ItemStructureLoader.setInstanceForTesting(null);
        DictionaryLoader.setDictionaryForTesting(null);
    }

    @Test
    public void testUseJapaneseReturnsFalseWhenStructureNotFound() {
        ItemStructureLoader.setInstanceForTesting(Map.of());

        boolean result = TranslationModeResolver.useJapanese("item.inexistente");
        assertFalse(result);
    }

    @Test
    public void testUseJapaneseReturnsFalseWhenLevelIsLow() {
        Map<String, List<String>> structures = Map.of("item.minecraft.apple", List.of("ringo"));
        ItemStructureLoader.setInstanceForTesting(structures);

        Mockito.when(mockManager.isParticle("ringo")).thenReturn(false);

        WordProgress lowProgress = new WordProgress("ringo");
        lowProgress.updateExposure(5.0);
        Mockito.when(mockManager.getProgress("ringo")).thenReturn(lowProgress);

        assertFalse(TranslationModeResolver.useJapanese("item.minecraft.apple"));
    }

    @Test
    public void testUseJapaneseReturnsTrueWhenLevelIsHighEnough() {
        Map<String, List<String>> structures = Map.of("item.minecraft.apple", List.of("ringo"));
        ItemStructureLoader.setInstanceForTesting(structures);

        Mockito.when(mockManager.isParticle("ringo")).thenReturn(false);

        WordProgress highProgress = new WordProgress("ringo");
        highProgress.updateExposure(35.0);
        Mockito.when(mockManager.getProgress("ringo")).thenReturn(highProgress);

        assertTrue(TranslationModeResolver.useJapanese("item.minecraft.apple"));
    }

    // =========================================================
    // NOVOS TESTES (Cobertura de bordas)
    // =========================================================

    @Test
    public void testUseJapaneseIgnoresParticles() {
        // Estrutura com substantivo "ringo" + partícula "no"
        Map<String, List<String>> structures = Map.of("item.minecraft.apple", List.of("ringo", "no"));
        ItemStructureLoader.setInstanceForTesting(structures);

        // "no" é partícula (deve ser ignorada), "ringo" é substantivo
        Mockito.when(mockManager.isParticle("no")).thenReturn(true);
        Mockito.when(mockManager.isParticle("ringo")).thenReturn(false);

        WordProgress highProgress = new WordProgress("ringo");
        highProgress.updateExposure(35.0); // Nível >= 2
        Mockito.when(mockManager.getProgress("ringo")).thenReturn(highProgress);

        // Deve retornar TRUE porque o único substantivo é nível >= 2 (a partícula foi ignorada)
        assertTrue(TranslationModeResolver.useJapanese("item.minecraft.apple"));
    }

    @Test
    public void testUseJapaneseReturnsFalseIfAnyWordInCompoundStructureIsLowLevel() {
        // Estrutura com 2 palavras: "kin" (ouro) e "ringo" (maçã)
        Map<String, List<String>> structures = Map.of("item.minecraft.golden_apple", List.of("kin", "ringo"));
        ItemStructureLoader.setInstanceForTesting(structures);

        Mockito.when(mockManager.isParticle(Mockito.anyString())).thenReturn(false);

        WordProgress highProgress = new WordProgress("kin");
        highProgress.updateExposure(35.0); // kin >= Nível 2

        WordProgress lowProgress = new WordProgress("ringo");
        lowProgress.updateExposure(5.0);  // ringo < Nível 2

        Mockito.when(mockManager.getProgress("kin")).thenReturn(highProgress);
        Mockito.when(mockManager.getProgress("ringo")).thenReturn(lowProgress);

        // Deve retornar FALSE porque uma das palavras da estrutura ainda está abaixo do Nível 2
        assertFalse(TranslationModeResolver.useJapanese("item.minecraft.golden_apple"));
    }

    @Test
    public void testUseJapaneseReturnsFalseWhenProgressIsNull() {
        Map<String, List<String>> structures = Map.of("item.minecraft.apple", List.of("ringo"));
        ItemStructureLoader.setInstanceForTesting(structures);

        Mockito.when(mockManager.isParticle("ringo")).thenReturn(false);
        // Garante retorno nulo para o progresso
        Mockito.when(mockManager.getProgress("ringo")).thenReturn(null);

        assertFalse(TranslationModeResolver.useJapanese("item.minecraft.apple"));
    }
}