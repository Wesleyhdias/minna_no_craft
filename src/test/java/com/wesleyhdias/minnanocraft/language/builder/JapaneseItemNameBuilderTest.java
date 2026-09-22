package com.wesleyhdias.minnanocraft.language.builder;

import com.wesleyhdias.minnanocraft.language.TranslationCacheManager;
import com.wesleyhdias.minnanocraft.language.resolver.TokenProvider;
import com.wesleyhdias.minnanocraft.language.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.srs.PlayerVocabularyManager;
import com.wesleyhdias.minnanocraft.srs.models.WordProgress;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JapaneseItemNameBuilderTest {

    private TokenProvider mockProvider1;
    private TokenProvider mockProvider2;

    @BeforeEach
    void setUp() {
        TranslationCacheManager.BUILDER_CACHE.clear();

        mockProvider1 = mock(TokenProvider.class);
        mockProvider2 = mock(TokenProvider.class);

        JapaneseItemNameBuilder.setProvidersForTesting(List.of(mockProvider1, mockProvider2));

        WordProgress mockProgress = mock(WordProgress.class);
        when(mockProgress.getScriptLevel()).thenReturn(0);

        PlayerVocabularyManager mockManager = mock(PlayerVocabularyManager.class);
        when(mockManager.getProgress(anyString())).thenReturn(mockProgress);

        PlayerVocabularyManager.setInstanceForTesting(mockManager);
    }

    @AfterEach
    void tearDown() {
        TranslationCacheManager.BUILDER_CACHE.clear();
        ItemStructureLoader.setInstanceForTesting(null);

        JapaneseItemNameBuilder.setProvidersForTesting(null);

        PlayerVocabularyManager.setInstanceForTesting(null);
    }

    @Test
    void shouldReturnNullIfStructureNotFound() {
        ItemStructureLoader.setInstanceForTesting(Map.of());

        String result = JapaneseItemNameBuilder.build("item.minecraft.unknown");

        assertNull(result, "Deve retornar null para que o jogo use a tradução padrão.");
    }

    @Test
    void shouldResolveTokensJoinWithSpacesAndTrim() {
        ItemStructureLoader.setInstanceForTesting(Map.of(
                "item.minecraft.iron_sword", List.of("iron", "no", "sword")
        ));

        when(mockProvider1.resolve("iron")).thenReturn("鉄");
        when(mockProvider1.resolve("sword")).thenReturn("剣");
        when(mockProvider2.resolve("no")).thenReturn("の");

        // 3. Removemos aquele "new WordProgress("teste")" problemático.
        // O setup inicial já vai entregar o mockProgress com nível 0 perfeitamente.

        String result = JapaneseItemNameBuilder.build("item.minecraft.iron_sword");

        assertEquals("鉄 の 剣", result);
    }

    @Test
    void shouldKeepOriginalTokenIfNoProviderResolvesIt() {
        ItemStructureLoader.setInstanceForTesting(Map.of(
                "item.minecraft.strange_apple", List.of("strange", "apple")
        ));

        // O Mockito já retorna null por padrão para "strange", não precisamos declarar isso
        when(mockProvider1.resolve("apple")).thenReturn("林檎");

        String result = JapaneseItemNameBuilder.build("item.minecraft.strange_apple");

        assertEquals("strange 林檎", result);
    }

    @Test
    void shouldUseCacheOnSubsequentCalls() {
        ItemStructureLoader.setInstanceForTesting(Map.of(
                "item.minecraft.apple", List.of("apple")
        ));

        when(mockProvider1.resolve("apple")).thenReturn("林檎");

        String firstCall = JapaneseItemNameBuilder.build("item.minecraft.apple");
        assertEquals("林檎", firstCall);

        // Quebra o provedor para garantir que o cache está sendo usado
        when(mockProvider1.resolve("apple")).thenReturn(null);

        String secondCall = JapaneseItemNameBuilder.build("item.minecraft.apple");

        assertEquals("林檎", secondCall, "Deve retornar '林檎' direto do cache, ignorando o provedor quebrado.");
        verify(mockProvider1, times(1)).resolve("apple"); // Garante que o provedor só trabalhou 1 vez
    }
}