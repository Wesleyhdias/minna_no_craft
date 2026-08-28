package com.wesleyhdias.minnanocraft.srs.models;

import com.wesleyhdias.minnanocraft.config.data.ConfigData;
import com.wesleyhdias.minnanocraft.config.ModConfig;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WordProgressTest {

    @BeforeEach
    void setUp() {
        ConfigData mockConfig = mock(ConfigData.class);

        when(mockConfig.getExpLevel1()).thenReturn(10.0);
        when(mockConfig.getExpLevel2()).thenReturn(25.0);
        when(mockConfig.getExpLevel3()).thenReturn(50.0);
        when(mockConfig.getExpLevel4()).thenReturn(100.0);

        // Injeção via Singleton sem usar MockedStatic
        ModConfig.setInstanceForTesting(mockConfig);
    }

    @AfterEach
    void tearDown() {
        ModConfig.setInstanceForTesting(null);
    }

    @Test
    void testInitialValues() {
        WordProgress progress = new WordProgress("tetsu");

        assertEquals("tetsu", progress.getWord());
        assertEquals(0.0, progress.getExposure());
        assertEquals(0.0, progress.getPeakExposure());
        assertEquals(0, progress.getScriptLevel()); // Nível 0 (Português)
        assertEquals(LearningState.WAITING, progress.getState()); // Estado inicial padrão
        assertEquals(0, progress.getLastSeen());
    }

    @Test
    void testDefaultConstructorForGson() {
        // Testa o construtor padrão exigido na deserialização JSON
        WordProgress progress = new WordProgress();

        assertNull(progress.getWord());
        assertEquals(0.0, progress.getExposure());
        assertEquals(LearningState.WAITING, progress.getState());
    }

    @Test
    void testUpdateExposureAndLevels() {
        WordProgress progress = new WordProgress("ken");

        // Nível 1 (>= 10.0)
        progress.updateExposure(10.0);
        assertEquals(10.0, progress.getExposure());
        assertEquals(10.0, progress.getPeakExposure());
        assertEquals(1, progress.getScriptLevel());

        // Nível 2 (>= 25.0)
        progress.updateExposure(15.0); // Total 25.0
        assertEquals(25.0, progress.getExposure());
        assertEquals(2, progress.getScriptLevel());

        // Nível 3 (>= 50.0)
        progress.updateExposure(25.0); // Total 50.0
        assertEquals(3, progress.getScriptLevel());

        // Nível 4 (>= 100.0)
        progress.updateExposure(50.0); // Total 100.0
        assertEquals(4, progress.getScriptLevel());
    }

    @Test
    void testExposureNeverDropsBelowZero() {
        WordProgress progress = new WordProgress("test");

        progress.updateExposure(-50.0);

        assertEquals(0.0, progress.getExposure());
        assertEquals(0.0, progress.getPeakExposure());
    }

    @Test
    void testPeakExposureRetention() {
        WordProgress progress = new WordProgress("test");

        progress.updateExposure(50.0);
        assertEquals(50.0, progress.getPeakExposure());

        progress.updateExposure(-30.0);

        assertEquals(20.0, progress.getExposure());
        assertEquals(50.0, progress.getPeakExposure()); // O pico não pode cair!
    }

    @Test
    void testCountersAndStateMutations() {
        WordProgress progress = new WordProgress("test");

        // Vistas
        assertEquals(0, progress.getSeenCount());
        progress.incrementSeenCount();
        assertEquals(1, progress.getSeenCount());

        // Dicionário/Lookups
        assertEquals(0, progress.getLookupCount());
        progress.incrementLookupCount();
        assertEquals(1, progress.getLookupCount());

        // Last Seen
        long now = System.currentTimeMillis();
        progress.setLastSeen(now);
        assertEquals(now, progress.getLastSeen());

        // Trocas de Estado
        progress.setState(LearningState.ACTIVE);
        assertEquals(LearningState.ACTIVE, progress.getState());

        progress.setState(LearningState.MASTERED);
        assertEquals(LearningState.MASTERED, progress.getState());
    }
}