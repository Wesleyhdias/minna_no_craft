package com.wesleyhdias.minnanocraft.language;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TranslationCacheManagerTest {

    @BeforeEach
    void setUp() {
        TranslationCacheManager.BUILDER_CACHE.clear();
        TranslationCacheManager.pendingClear = false;
    }

    @Test
    void testAddAndRetrieveCache() {
        TranslationCacheManager.BUILDER_CACHE.put("item_espada", "Espada de Ferro");

        assertEquals(1, TranslationCacheManager.BUILDER_CACHE.size());
        assertEquals("Espada de Ferro", TranslationCacheManager.BUILDER_CACHE.get("item_espada"));
    }

    @Test
    void testPendingClearFlagBehavior() {
        assertFalse(TranslationCacheManager.pendingClear);

        TranslationCacheManager.pendingClear = true;

        assertTrue(TranslationCacheManager.pendingClear);
    }

    @Test
    void testMaxSizeLimitAndLRUEviction() {
        // Preenche com 55 itens (MAX_SIZE = 50)
        for (int i = 0; i < 55; i++) {
            TranslationCacheManager.BUILDER_CACHE.put("key_" + i, "value_" + i);
        }

        assertEquals(50, TranslationCacheManager.BUILDER_CACHE.size());

        // Primeiros itens (LRU) foram removidos
        assertNull(TranslationCacheManager.BUILDER_CACHE.get("key_0"));
        assertNull(TranslationCacheManager.BUILDER_CACHE.get("key_4"));

        // Mais recentes mantidos
        assertEquals("value_54", TranslationCacheManager.BUILDER_CACHE.get("key_54"));
    }

    @Test
    void testClearAllResetsCacheAndPendingFlag() {
        // Arrange: Preenche o cache e ativa a flag de limpeza pendente
        TranslationCacheManager.BUILDER_CACHE.put("item_maca", "Ringo");
        TranslationCacheManager.pendingClear = true;

        // Act: Invoca o método de limpeza total
        TranslationCacheManager.clearAll();

        // Assert: Garante que o cache foi esvaziado e a flag desativada
        assertTrue(TranslationCacheManager.BUILDER_CACHE.isEmpty(), "O BUILDER_CACHE deveria estar totalmente limpo.");
        assertFalse(TranslationCacheManager.pendingClear, "A flag pendingClear deveria voltar para false.");
    }
}