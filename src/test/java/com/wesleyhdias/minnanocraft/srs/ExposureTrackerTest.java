package com.wesleyhdias.minnanocraft.srs;

import com.wesleyhdias.minnanocraft.language.TranslationCacheManager;
import com.wesleyhdias.minnanocraft.language.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.srs.models.ExpEvents;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExposureTrackerTest {

    private PlayerVocabularyManager mockVocabManager;
    private ExposureTracker tracker;

    @BeforeEach
    void setUp() {
        // 1. Cria e injeta o mock do PlayerVocabularyManager
        mockVocabManager = mock(PlayerVocabularyManager.class);
        PlayerVocabularyManager.setInstanceForTesting(mockVocabManager);

        // 2. Injeta o MAP diretamente
        Map<String, List<String>> mockStructures = Map.of(
                "minecraft:apple", List.of("ringo")
        );
        ItemStructureLoader.setInstanceForTesting(mockStructures);

        // 3. Inicializa o Tracker exigindo 100ms de foco para disparar o HOVER
        tracker = new ExposureTracker(100, ExpEvents.HOVER);

        // Garante que o cache começa limpo
        TranslationCacheManager.pendingClear = false;
    }

    @AfterEach
    void tearDown() {
        PlayerVocabularyManager.setInstanceForTesting(null);
        ItemStructureLoader.setInstanceForTesting(new HashMap<>());
    }

    @Test
    void shouldNotAwardExpIfFocusTimeNotMet() {
        String target = "minecraft:apple";

        // Act - Primeira atualização (inicia o contador)
        tracker.update(target, true);

        // Segunda atualização IMEDIATA (não deu tempo de bater os 100ms)
        tracker.update(target, true);

        // Assert - Verifica que registerEvent NUNCA foi chamado
        verify(mockVocabManager, never()).registerEvent(anyString(), any());
    }

    @Test
    void shouldNotAwardExpIfExtraConditionIsFalse() throws InterruptedException {
        String target = "minecraft:apple";

        tracker.update(target, true);
        Thread.sleep(60);
        // Tempo passou, MAS a condição extra virou false
        tracker.update(target, false);

        verify(mockVocabManager, never()).registerEvent(anyString(), any());
    }

    @Test
    void shouldAwardExpOnlyOncePerFocusSession() throws InterruptedException {
        String target = "minecraft:apple";

        // Ganha a EXP no primeiro ciclo completo
        tracker.update(target, true);
        Thread.sleep(60);
        tracker.update(target, true); // <--- Ganhou a EXP aqui (1x)

        // O jogador CONTINUA olhando para o mesmo item nos próximos frames:
        Thread.sleep(50);
        tracker.update(target, true);
        Thread.sleep(50);
        tracker.update(target, true);

        // O verify garante que registerEvent foi chamado EXATAMENTE 1 vez, e não 3
        verify(mockVocabManager, times(1)).registerEvent("ringo", ExpEvents.HOVER);
    }

    @Test
    void shouldNotAwardExpIfNoTokenToUpgrade() throws InterruptedException {
        // "minecraft:stone" não existe no nosso mockStructures do setUp()
        // Então ele vai retornar null para a estrutura, e o Selector não fará nada.
        String target = "minecraft:stone";

        tracker.update(target, true);
        Thread.sleep(120);
        tracker.update(target, true);

        verify(mockVocabManager, never()).registerEvent(anyString(), any());
    }

    @Test
    void shouldAwardExpWhenTimeAndConditionMet() throws InterruptedException {
        String target = "minecraft:apple";

        // Act
        tracker.update(target, true); // Frame 1: Focou no item (t = 0ms)

        Thread.sleep(50);
        tracker.update(target, true); // Frame 2: Mantém foco (t = 50ms)

        Thread.sleep(50);
        tracker.update(target, true); // Frame 3: Atingiu os 100ms exigidos (t = 100ms)

        // Assert
        verify(mockVocabManager, times(1)).registerEvent("ringo", ExpEvents.HOVER);
    }

    @Test
    void shouldResetTrackerIfTargetChanges() throws InterruptedException {
        tracker.update("minecraft:apple", true); // Focou na maçã (t = 0)
        Thread.sleep(50);

        // Jogador trocou rapidamente para a espada antes de dar os 100ms da maçã
        tracker.update("minecraft:sword", true);

        // Assert
        verify(mockVocabManager, never()).registerEvent(anyString(), any());
        assertEquals("minecraft:sword", tracker.getCurrentKey());
    }

    @Test
    void shouldResetOnTimeout() throws InterruptedException {
        // Act - Foca no item
        tracker.update("minecraft:apple", true);

        // Passa 200ms (mais do que o limite de timeout padrão)
        Thread.sleep(200);

        // Uma nova atualização acontece
        tracker.update("minecraft:apple", true);

        // Assert - Como deu timeout, o tracker resetou a contagem. O evento não deve ser chamado.
        verify(mockVocabManager, never()).registerEvent(anyString(), any());
    }
}