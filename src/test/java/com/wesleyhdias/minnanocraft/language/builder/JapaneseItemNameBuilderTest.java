package com.wesleyhdias.minnanocraft.language.builder;

import com.wesleyhdias.minnanocraft.language.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.language.TranslationCacheManager;
import com.wesleyhdias.minnanocraft.language.resolver.TokenProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JapaneseItemNameBuilderTest {

    private TokenProvider mockProvider1;
    private TokenProvider mockProvider2;

    @BeforeEach
    void setUp() {
        // Limpa o cache para evitar interferência entre os testes
        TranslationCacheManager.BUILDER_CACHE.clear();

        // Cria nossos provedores falsos
        mockProvider1 = mock(TokenProvider.class);
        mockProvider2 = mock(TokenProvider.class);

        // Injeta os provedores falsos no Builder
        JapaneseItemNameBuilder.setProvidersForTesting(List.of(mockProvider1, mockProvider2));
    }

    @AfterEach
    void tearDown() {
        TranslationCacheManager.BUILDER_CACHE.clear();
        ItemStructureLoader.setInstanceForTesting(new HashMap<>());
        JapaneseItemNameBuilder.setProvidersForTesting(null);
    }

    @Test
    void shouldReturnNullIfStructureNotFound() {
        ItemStructureLoader.setInstanceForTesting(Map.of());

        String result = JapaneseItemNameBuilder.build("item.minecraft.unknown");

        assertNull(result, "Deve retornar null para que o jogo use a tradução padrão.");
    }

    @Test
    void shouldResolveTokensJoinWithSpacesAndTrim() {
        // 1. Configura a estrutura do item
        ItemStructureLoader.setInstanceForTesting(Map.of(
                "item.minecraft.iron_sword", List.of("iron", "no", "sword")
        ));

        // 2. Configura os provedores para responderem aos tokens
        // O provider1 sabe traduzir "iron" e "sword"
        when(mockProvider1.resolve("iron")).thenReturn("鉄");
        when(mockProvider1.resolve("sword")).thenReturn("剣");
        when(mockProvider1.resolve("no")).thenReturn(null); // Não sabe traduzir "no"

        // O provider2 sabe traduzir a partícula "no"
        when(mockProvider2.resolve("no")).thenReturn("の");

        // 3. Executa o Builder
        String result = JapaneseItemNameBuilder.build("item.minecraft.iron_sword");

        // 4. Verifica se montou corretamente, com espaços e sem espaço sobrando no final (trim)
        assertEquals("鉄 の 剣", result);
    }

    @Test
    void shouldKeepOriginalTokenIfNoProviderResolvesIt() {
        ItemStructureLoader.setInstanceForTesting(Map.of(
                "item.minecraft.strange_apple", List.of("strange", "apple")
        ));

        // O provider só sabe traduzir "apple", mas não conhece "strange"
        when(mockProvider1.resolve("apple")).thenReturn("林檎");
        when(mockProvider1.resolve("strange")).thenReturn(null);
        when(mockProvider2.resolve(anyString())).thenReturn(null);

        String result = JapaneseItemNameBuilder.build("item.minecraft.strange_apple");

        // A palavra desconhecida "strange" deve permanecer intacta
        assertEquals("strange 林檎", result);
    }

    @Test
    void shouldUseCacheOnSubsequentCalls() {
        ItemStructureLoader.setInstanceForTesting(Map.of(
                "item.minecraft.apple", List.of("apple")
        ));

        when(mockProvider1.resolve("apple")).thenReturn("林檎");

        // Primeira chamada processa a string e salva no cache
        String firstCall = JapaneseItemNameBuilder.build("item.minecraft.apple");
        assertEquals("林檎", firstCall);

        // Quebra o provedor para garantir que o cache está sendo usado
        when(mockProvider1.resolve("apple")).thenReturn(null);

        // Segunda chamada
        String secondCall = JapaneseItemNameBuilder.build("item.minecraft.apple");

        assertEquals("林檎", secondCall, "Deve retornar '林檎' direto do cache, ignorando o provedor quebrado.");
        verify(mockProvider1, times(1)).resolve("apple"); // Garante que o provedor só trabalhou 1 vez
    }
}