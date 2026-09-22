package com.wesleyhdias.minnanocraft.srs;

import com.wesleyhdias.minnanocraft.language.TranslationCacheManager;
import com.wesleyhdias.minnanocraft.srs.models.LearningState;
import com.wesleyhdias.minnanocraft.srs.models.WordProgress;
import com.wesleyhdias.minnanocraft.config.data.ConfigData;
import com.wesleyhdias.minnanocraft.srs.models.ExpEvents;
import com.wesleyhdias.minnanocraft.config.ModConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProgressionSystemTest {

    private ProgressionSystem progressionSystem;

    @BeforeEach
    void setUp() {
        // 1. Instancia o mock do ConfigData
        ConfigData mockConfig = mock(ConfigData.class);

        // 2. Define o comportamento. Nota: 2.0f (float) restaurado para evitar erro do Mockito
        when(mockConfig.getMasteryExposure()).thenReturn(100.0);
        when(mockConfig.getEventSeen()).thenReturn(5.0);
        when(mockConfig.getEventLookup()).thenReturn(15.0);
        when(mockConfig.getRelearnMultiplier()).thenReturn(2.0f);
        when(mockConfig.getInactivityTimeThreshold()).thenReturn(86400000L);
        when(mockConfig.getDemotionTimeThreshold()).thenReturn(259200000L);
        when(mockConfig.getMaxActiveWords()).thenReturn(5);
        when(mockConfig.getExpLevel1()).thenReturn(15.0);
        when(mockConfig.getExpLevel2()).thenReturn(30.0);
        when(mockConfig.getExpLevel3()).thenReturn(45.0);
        when(mockConfig.getExpLevel4()).thenReturn(100.0);


        ModConfig.setInstanceForTesting(mockConfig);

        // 4. Instancia o sistema
        progressionSystem = ProgressionSystem.getInstance();
        TranslationCacheManager.pendingClear = false;
    }

    @AfterEach
    void tearDown() {
        // Limpa as instâncias estáticas para não poluir outros testes
        ModConfig.setInstanceForTesting(null);
        ProgressionSystem.setInstanceForTesting(null);
    }

    @Test
    void shouldIgnoreEventIfInsideCooldown() {
        WordProgress progress = new WordProgress("test");
        progress.setLastSeen(System.currentTimeMillis() - 1000);

        progressionSystem.applyEvent(progress, ExpEvents.SEEN);

        assertEquals(0.0, progress.getExposure(), "Exposure should not change during cooldown.");
    }

    @Test
    void shouldApplyRelearnMultiplierWhenBelowPeakExposure() {
        WordProgress progress = new WordProgress("test");

        progress.updateExposure(50.0);
        progress.updateExposure(-20.0);
        progress.setLastSeen(System.currentTimeMillis() - 10000);

        progressionSystem.applyEvent(progress, ExpEvents.SEEN);

        assertEquals(40.0, progress.getExposure(), "Should gain 10.0 exposure using relearn multiplier.");
    }

    @Test
    void shouldDemoteMasteredWordOnLookup() {
        WordProgress progress = new WordProgress("test");
        progress.setState(LearningState.MASTERED);
        progress.updateExposure(100.0);
        progress.setLastSeen(System.currentTimeMillis() - 10000);

        progressionSystem.applyEvent(progress, ExpEvents.LOOKUP);

        assertEquals(LearningState.ACTIVE, progress.getState(), "Should revert to ACTIVE state.");
        assertTrue(progress.getExposure() < 100.0, "Exposure should drop on lookup.");
    }

    @Test
    void shouldPromoteActiveWordToMasteredOnUpdateStates() {
        WordProgress progress = new WordProgress("test");
        progress.setState(LearningState.ACTIVE);
        progress.updateExposure(105.0);

        Map<String, WordProgress> vocab = Map.of("test", progress);
        progressionSystem.updateStates(vocab);

        assertEquals(LearningState.MASTERED, progress.getState(), "Should promote word to MASTERED.");
    }

    @Test
    void shouldPromoteWaitingWordsToActiveQueue() {
        WordProgress active = new WordProgress("active1");
        active.setState(LearningState.ACTIVE);

        WordProgress waiting = new WordProgress("waiting1");
        waiting.setState(LearningState.WAITING);

        Map<String, WordProgress> vocab = new HashMap<>();
        vocab.put(active.getWord(), active);
        vocab.put(waiting.getWord(), waiting);

        progressionSystem.updateStates(vocab);

        assertEquals(LearningState.ACTIVE, waiting.getState(), "Waiting word should be promoted to ACTIVE.");
    }

    @Test
    void shouldBlockDifferentEventIfInsideCooldown() {
        WordProgress progress = new WordProgress("test");
        progress.setLastSeen(System.currentTimeMillis() - 1000); // 1s atrás (cooldown é 5s)

        // Aciona um evento DIFERENTE do anterior durante o cooldown
        progressionSystem.applyEvent(progress, ExpEvents.HOVER);

        assertEquals(0.0, progress.getExposure(), "Event should be ignored inside 5s cooldown regardless of event type.");
        assertEquals(0, progress.getLookupCount(), "Lookup count should not increment inside cooldown.");
    }

    @Test
    void shouldApplyHoverAndHudSeenEventsCorrectly() {
        // Configura o mock com os valores destes eventos
        when(ModConfig.getConfig().getEventHover()).thenReturn(2.0);
        when(ModConfig.getConfig().getEventHudSeen()).thenReturn(3.0);

        WordProgress progressHover = new WordProgress("hover_test");
        progressHover.setLastSeen(System.currentTimeMillis() - 10000);
        progressionSystem.applyEvent(progressHover, ExpEvents.HOVER);
        assertEquals(2.0, progressHover.getExposure(), "Should add HOVER exposure.");

        WordProgress progressHud = new WordProgress("hud_test");
        progressHud.setLastSeen(System.currentTimeMillis() - 10000);
        progressionSystem.applyEvent(progressHud, ExpEvents.HUD_LOOK);
        assertEquals(3.0, progressHud.getExposure(), "Should add HUD_LOOK exposure.");
    }

    @Test
    void shouldApplyHoverLookupPenaltyCorrectly() {
        when(ModConfig.getConfig().getEventHoverLookup()).thenReturn(8.0);

        WordProgress progress = new WordProgress("test");
        progress.updateExposure(20.0);
        progress.setLastSeen(System.currentTimeMillis() - 10000);

        progressionSystem.applyEvent(progress, ExpEvents.HOVER_LOOKUP);

        assertEquals(12.0, progress.getExposure(), "Should apply hover lookup penalty (20.0 - 8.0 = 12.0).");
        assertEquals(1, progress.getLookupCount());
    }

    @Test
    void shouldSetPendingClearTrueWhenScriptLevelChanges() {
        when(ModConfig.getConfig().getEventSeen()).thenReturn(20.0);

        TranslationCacheManager.pendingClear = false;

        WordProgress progress = new WordProgress("test");
        // Começa com 0.0 de XP (Nível inicial)
        progress.setLastSeen(System.currentTimeMillis() - 10000);

        // 3. Aplica o evento que vai somar +50.0 de XP e cruzar a barreira de nível
        progressionSystem.applyEvent(progress, ExpEvents.SEEN);

        // Assert: Como o XP saltou de 0.0 para 50.0, o nível mudou e o cache DEVE ser marcado para limpeza
        assertTrue(TranslationCacheManager.pendingClear, "Translation cache pendingClear flag should be set to true on level change.");
    }
}