package com.wesleyhdias.minnanocraft.srs;

import com.wesleyhdias.minnanocraft.language.dictionary.DictionaryLoader;
import com.wesleyhdias.minnanocraft.language.dictionary.Word;
import com.wesleyhdias.minnanocraft.srs.models.WordProgress;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenUpgradeSelectorTest {

    private PlayerVocabularyManager mockManager;

    @BeforeEach
    void setUp() {
        mockManager = mock(PlayerVocabularyManager.class);

        Word word = new Word(null, null, null, null);
        DictionaryLoader.setDictionaryForTesting(Map.of("ringo", word, "watashi", word, "taberu", word));
    }

    @AfterEach
    void tearDown() {
        DictionaryLoader.setDictionaryForTesting(null);
    }

    /**
     * Helper para mockar rapidamente o estado de uma palavra/partícula no Manager.
     */
    private void mockToken(String token, boolean isParticle, int level) {
        when(mockManager.isParticle(token)).thenReturn(isParticle);

        WordProgress progress = mock(WordProgress.class);
        when(progress.getScriptLevel()).thenReturn(level);
        when(mockManager.getProgress(token)).thenReturn(progress);
    }

    @Test
    void shouldReturnNullForEmptyOrNullStructure() {
        assertNull(TokenUpgradeSelector.getNextTokenToUpgrade(null, mockManager));
        assertNull(TokenUpgradeSelector.getNextTokenToUpgrade(Collections.emptyList(), mockManager));
    }

    @Test
    void shouldReturnLowestParticleIfOnlyParticlesPresent() {
        mockToken("ga", true, 3);
        mockToken("wo", true, 1);
        mockToken("ni", true, 2);

        List<String> structure = List.of("ga", "wo", "ni");
        String target = TokenUpgradeSelector.getNextTokenToUpgrade(structure, mockManager);

        assertEquals("wo", target, "Deve priorizar a partícula com o menor nível quando não há conteúdo.");
    }

    @Test
    void shouldPrioritizeContentInNativeMode() {
        // Native mode: Palavras de conteúdo estão no nível 0
        mockToken("watashi", false, 0);
        mockToken("ha", true, 0);
        mockToken("ringo", false, 0);

        List<String> structure = List.of("watashi", "ha", "ringo");
        String target = TokenUpgradeSelector.getNextTokenToUpgrade(structure, mockManager);

        assertEquals("watashi", target, "No nível 0, o foco é 100% no conteúdo, pegando a primeira da esquerda para a direita.");
    }

    @Test
    void shouldPrioritizeLaggingParticleInJapaneseMode() {
        // Japanese mode: Conteúdo já subiu de nível, mas uma partícula ficou para trás
        mockToken("taberu", false, 2);
        mockToken("wo", true, 0); // Partícula atrasada
        mockToken("ringo", false, 1);

        List<String> structure = List.of("taberu", "wo", "ringo");
        String target = TokenUpgradeSelector.getNextTokenToUpgrade(structure, mockManager);

        assertEquals("wo", target, "Partículas com nível menor que o menor nível de conteúdo devem ter prioridade de catch-up.");
    }

    @Test
    void shouldPrioritizeContentInTieBreaker() {
        // Tie-breaker: Conteúdo e partículas estão no mesmo nível
        mockToken("watashi", false, 2);
        mockToken("no", true, 2);
        mockToken("mizu", false, 3);

        List<String> structure = List.of("watashi", "no", "mizu");
        String target = TokenUpgradeSelector.getNextTokenToUpgrade(structure, mockManager);

        assertEquals("watashi", target, "Quando inauguram um novo nível juntos, as palavras de conteúdo têm prioridade.");
    }

    @Test
    void shouldReturnFirstLowestContentToken() {
        // Várias palavras de conteúdo em níveis diferentes
        mockToken("akai", false, 3);
        mockToken("ringo", false, 1); // Menor nível
        mockToken("taberu", false, 1); // Mesmo menor nível

        List<String> structure = List.of("akai", "ringo", "taberu");
        String target = TokenUpgradeSelector.getNextTokenToUpgrade(structure, mockManager);

        assertEquals("ringo", target, "Deve retornar a primeira palavra de conteúdo com o menor nível (leitura da esquerda para a direita).");
    }

    @Test
    void shouldHandleUnknownTokensGracefully() {
        // Simula um token que ainda não tem progresso salvo (manager retorna null)
        when(mockManager.isParticle("ringo")).thenReturn(false);
        when(mockManager.getProgress("ringo")).thenReturn(null);

        List<String> structure = List.of("ringo");
        String target = TokenUpgradeSelector.getNextTokenToUpgrade(structure, mockManager);

        assertEquals("ringo", target, "Tokens sem progresso salvo (null) devem ser tratados como nível 0 e selecionados corretamente.");
    }
}