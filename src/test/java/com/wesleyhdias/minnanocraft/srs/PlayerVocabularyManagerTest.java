package com.wesleyhdias.minnanocraft.srs;

import com.wesleyhdias.minnanocraft.language.dictionary.DictionaryLoader;
import com.wesleyhdias.minnanocraft.language.dictionary.Word;
import com.wesleyhdias.minnanocraft.srs.models.ExpEvents;
import com.wesleyhdias.minnanocraft.srs.models.WordProgress;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlayerVocabularyManagerTest {

    private PlayerVocabularyRepository mockRepository;
    private ProgressionSystem mockProgressionSystem;
    private PlayerVocabularyManager manager;

    @BeforeEach
    void setUp() {
        // 1. Prepara os mocks das dependências
        mockRepository = mock(PlayerVocabularyRepository.class);
        PlayerVocabularyRepository.setInstanceForTesting(mockRepository);

        mockProgressionSystem = mock(ProgressionSystem.class);
        ProgressionSystem.setInstanceForTesting(mockProgressionSystem);

        // 2. Injeta um dicionário falso diretamente na memória
        Word dummyWord = new Word(Map.of(), "林檎", "りんご", "ringo");

        Map<String, Word> fakeDictionary = Map.of(
                "ringo", dummyWord,
                "taberu", dummyWord
        );
        DictionaryLoader.setDictionaryForTesting(fakeDictionary);

        // 3. Reseta e recria o Manager para ele puxar os mocks no construtor
        PlayerVocabularyManager.setInstanceForTesting(null);
        manager = PlayerVocabularyManager.getInstance();
    }

    @AfterEach
    void tearDown() {
        PlayerVocabularyManager.setInstanceForTesting(null);
        PlayerVocabularyRepository.setInstanceForTesting(null);
        ProgressionSystem.setInstanceForTesting(null);
        DictionaryLoader.setDictionaryForTesting(null);
    }

    @Test
    void testLoadAndSaveDelegatesToRepository() {
        // Arrange
        ConcurrentHashMap<String, WordProgress> fakeCache = new ConcurrentHashMap<>();
        fakeCache.put("ringo", new WordProgress("ringo"));
        when(mockRepository.loadAll()).thenReturn(fakeCache);

        // Act - Load
        manager.load();

        // Assert - Verifica se carregou pro cache interno
        assertNotNull(manager.getProgress("ringo"));
        assertEquals(1, manager.getVocabularyCache().size());

        // Act - Save
        manager.save();

        // Assert - Verifica se repassou o cache atual pro repositório salvar
        verify(mockRepository, times(1)).saveAll(manager.getVocabularyCache());
    }

    @Test
    void testGetOrCreateProgress() {
        // Token novo
        WordProgress newProgress = manager.getOrCreateProgress("mizu");
        assertNotNull(newProgress);
        assertEquals("mizu", newProgress.getWord());

        // Mesmo token não deve criar outra instância
        WordProgress existingProgress = manager.getOrCreateProgress("mizu");
        assertSame(newProgress, existingProgress, "Deve retornar exatamente a mesma instância da memória.");
    }

    @Test
    void testRegisterEventIgnoresSpam() {
        // Cria a palavra e simula que ela foi vista há apenas 1 segundo (1000ms)
        WordProgress progress = manager.getOrCreateProgress("ringo");
        progress.setLastSeen(System.currentTimeMillis() - 1000);

        manager.registerEvent("ringo", ExpEvents.HOVER);

        // O filtro antispam de 5000ms deve bloquear
        verify(mockProgressionSystem, never()).applyEvent(any(), any());
    }

    @Test
    void testRegisterEventAppliesWhenOutsideCooldown() {
        // Cria a palavra e simula que ela foi vista há 4 segundos (4000ms)
        WordProgress progress = manager.getOrCreateProgress("ringo");
        progress.setLastSeen(System.currentTimeMillis() - 6000);

        manager.registerEvent("ringo", ExpEvents.HOVER);

        // Passou do cooldown, então DEVE chamar o ProgressionSystem
        verify(mockProgressionSystem, times(1)).applyEvent(progress, ExpEvents.HOVER);
    }

    @Test
    void testIsParticle() {
        // "ringo" está no fakeDictionary (Palavra de conteúdo)
        assertFalse(manager.isParticle("ringo"));

        // "ga" NÃO está no fakeDictionary (Partícula)
        assertTrue(manager.isParticle("ga"));
    }

    @Test
    void testUpdateProgressionDelegatesToSystem() {
        manager.getOrCreateProgress("ringo"); // Adiciona algo ao cache

        manager.updateProgression();

        // Verifica se a chamada do loop de atualização foi repassada
        verify(mockProgressionSystem, times(1)).updateStates(manager.getVocabularyCache());
    }
}