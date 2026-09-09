package com.wesleyhdias.minnanocraft.language.builder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.wesleyhdias.minnanocraft.language.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.language.TranslationCacheManager;
import com.wesleyhdias.minnanocraft.language.dictionary.CompoundDictionaryLoader;
import com.wesleyhdias.minnanocraft.language.dictionary.CompoundWord;
import com.wesleyhdias.minnanocraft.language.dictionary.DictionaryLoader;
import com.wesleyhdias.minnanocraft.language.dictionary.Word;
import com.wesleyhdias.minnanocraft.srs.PlayerVocabularyManager;
import com.wesleyhdias.minnanocraft.srs.models.WordProgress;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.LanguageManager;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.FileReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class FullCurrentLangItemNameBuilderTest {

    private static Map<String, String> ptBrDump = new HashMap<>();
    private static Map<String, String> enUsDump = new HashMap<>();

    private static final List<Map<String, Object>> unmatchedLocalTranslations = new ArrayList<>();

    private static int totalProcessed = 0;

    private static MockedStatic<Minecraft> mockedMinecraft;
    private static String currentActiveLang = "pt_br";

    private static final List<String> PREFIXOS_IGNORADOS = List.of(
            "menu.",
            "death.attack.",
            "container.",
            "block.minecraft.bed.",
            "block.minecraft.spawn.",
            "item.minecraft.debug",
            "block.minecraft.banner.",
            "block.minecraft.pattern_item."
    );

    private static final List<String> TRECHOS_IGNORADOS = List.of(
            "concrete_slab",
            "concrete_stairs",
            "wool_slab",
            "wool_stairs",
            "poplar_",
            "sulfur",
            "cinnabar",
            "description",
            "cushion",
            "illusioner",
            "candle_cake"

    );

    // Lista de palavras de ligação (partículas) que sobram após consumir as traduções
    private static final List<String> PARTICULAS_PT_BR = List.of("de", "do", "da", "dos", "das", "com", "e", "em", "o", "a", "os", "as", "um", "uma", "para");
    private static final List<String> PARTICULAS_EN_US = List.of("of", "the", "with", "and", "a", "an", "in", "on", "for", "s", "o");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    @BeforeAll
    public static void setup() throws Exception {
        TranslationCacheManager.BUILDER_CACHE.clear();

        // Mock do Minecraft estático com leitura dinâmica da variável
        Minecraft mockMc = mock(Minecraft.class);
        LanguageManager mockLangManager = mock(LanguageManager.class);

        when(mockLangManager.getSelected()).thenAnswer(inv -> currentActiveLang);
        when(mockMc.getLanguageManager()).thenReturn(mockLangManager);

        mockedMinecraft = mockStatic(Minecraft.class);
        mockedMinecraft.when(Minecraft::getInstance).thenReturn(mockMc);

        // Carrega apenas PT-BR e EN-US
        ptBrDump = loadJsonIfExists("run/lang_dump/pt_br_filtered.json");
        enUsDump = loadJsonIfExists("run/lang_dump/en_us_filtered.json");

        // Mock do Vocabulário (Nível 0 simulando que nada foi traduzido ainda)
        PlayerVocabularyManager mockVocabManager = mock(PlayerVocabularyManager.class);
        WordProgress maxProgress = mock(WordProgress.class);
        when(maxProgress.getScriptLevel()).thenReturn(0);
        when(mockVocabManager.getProgress(anyString())).thenReturn(maxProgress);

        PlayerVocabularyManager.setInstanceForTesting(mockVocabManager);
    }

    private static Map<String, String> loadJsonIfExists(String path) {
        Path p = Paths.get(path);
        if (!Files.exists(p)) return Map.of();
        try (FileReader reader = new FileReader(p.toFile())) {
            return GSON.fromJson(reader, new TypeToken<Map<String, String>>(){}.getType());
        } catch (Exception e) {
            return Map.of();
        }
    }

    @Test
    public void validateDualLanguageTranslations() {
        totalProcessed = 0;
        Map<String, List<String>> structures = ItemStructureLoader.getStructures();

        Set<String> allKeys = ptBrDump.keySet();

        for (String key : allKeys) {
            boolean ignorar = PREFIXOS_IGNORADOS.stream().anyMatch(key::startsWith);
            boolean ignorarTrecho = TRECHOS_IGNORADOS.stream().anyMatch(key::contains);

            if (ignorar || ignorarTrecho) {
                continue;
            }

            String ptText = ptBrDump.getOrDefault(key, "");
            String enText = enUsDump.getOrDefault(key, "");

            if (!structures.containsKey(key)) {
                continue;
            }

            List<String> structTokens = structures.get(key);

            // Cria as strings de controle que vão ser consumidas
            String ptRemaining = ptText.toLowerCase(Locale.ROOT);
            String enRemaining = enText.toLowerCase(Locale.ROOT);

            for (String token : structTokens) {
                // Tenta consumir as traduções em PT-BR
                currentActiveLang = "pt_br";
                List<String> ptTranslations = getTranslationsForToken(token);
                ptRemaining = consumeTranslations(ptRemaining, ptTranslations);

                // Tenta consumir as traduções em EN-US
                currentActiveLang = "en_us";
                List<String> enTranslations = getTranslationsForToken(token);
                enRemaining = consumeTranslations(enRemaining, enTranslations);
            }

            // Limpa conectivos e caracteres especiais (hifens, aspas, etc)
            ptRemaining = cleanParticlesAndSymbols(ptRemaining, "pt_br");
            enRemaining = cleanParticlesAndSymbols(enRemaining, "en_us");

            boolean hasError = false;
            Map<String, Object> errorDetail = new LinkedHashMap<>();
            errorDetail.put("key", key);
            errorDetail.put("structure", structTokens);

            // Se ainda sobrou texto que não é conectivo, registramos o erro
            if (!ptRemaining.isEmpty()) {
                hasError = true;
                Map<String, Object> ptDetail = new LinkedHashMap<>();
                ptDetail.put("originalText", ptText);
                ptDetail.put("leftover", ptRemaining);
                errorDetail.put("pt_br", ptDetail);
            }

            if (!enRemaining.isEmpty()) {
                hasError = true;
                Map<String, Object> enDetail = new LinkedHashMap<>();
                enDetail.put("originalText", enText);
                enDetail.put("leftover", enRemaining);
                errorDetail.put("en_us", enDetail);
            }

            if (hasError) {
                unmatchedLocalTranslations.add(errorDetail);
            } else {
                totalProcessed++;
            }
        }
    }

    /**
     * Busca as traduções locais (PT ou EN) tentando tanto no dicionário composto quanto no simples.
     */
    private List<String> getTranslationsForToken(String token) {
        CompoundWord compound = CompoundDictionaryLoader.getDictionary().get(token);
        if (compound != null) {
            return compound.getLocalTranslations() != null ? compound.getLocalTranslations() : List.of();
        }
        Word word = DictionaryLoader.getDictionary().get(token);
        if (word != null) {
            return word.getLocalTranslations() != null ? word.getLocalTranslations() : List.of();
        }
        return List.of();
    }

    /**
     * Apaga as palavras encontradas substituindo-as por espaços.
     */
    private String consumeTranslations(String remainingText, List<String> translations) {
        String result = remainingText;
        for (String trans : translations) {
            String lowerSearch = trans.toLowerCase(Locale.ROOT);
            // (?U) garante que o Regex entenda acentos do português (á, é, ã, ç)
            // \b garante que só apague a palavra exata (não apaga "pá" dentro de "pássaro")
            String regex = "(?U)\\b" + java.util.regex.Pattern.quote(lowerSearch) + "\\b";
            result = result.replaceAll(regex, " ");
        }
        return result;
    }

    /**
     * Remove símbolos inúteis (-, ', parênteses) e preposições (de, of, the...)
     * para verificar se sobrou alguma palavra real (substantivo/adjetivo/verbo).
     */
    private String cleanParticlesAndSymbols(String text, String lang) {
        // Remove símbolos especiais comuns no minecraft
        String cleaned = text.replaceAll("[-'\\[\\]():&]", " ");

        List<String> particles = lang.equals("pt_br") ? PARTICULAS_PT_BR : PARTICULAS_EN_US;

        for (String p : particles) {
            // Regex \b (word boundary) garante que "de" não vai apagar o "de" de "madeira"
            cleaned = cleaned.replaceAll("\\b" + p + "\\b", " ");
        }

        // Troca múltiplos espaços por um só e dá trim
        return cleaned.replaceAll("\\s+", " ").trim();
    }

    @AfterAll
    public static void writeReportsAndAssert() throws Exception {
        Path reportsDir = Paths.get("run/test_reports/currentLang");
        Files.createDirectories(reportsDir);

        if (!unmatchedLocalTranslations.isEmpty()) {
            try (Writer writer = Files.newBufferedWriter(reportsDir.resolve("report_unmatched_local_translations.json"))) {
                GSON.toJson(unmatchedLocalTranslations, writer);
            }
        }

        TranslationCacheManager.BUILDER_CACHE.clear();
        PlayerVocabularyManager.setInstanceForTesting(null);
        if (mockedMinecraft != null) {
            mockedMinecraft.close();
        }

        boolean allPassed = unmatchedLocalTranslations.isEmpty();

        Assertions.assertTrue(allPassed,
                "Validação do CurrentLangItemNameBuilder falhou! " +
                        "Itens com sobras não traduzidas: " + unmatchedLocalTranslations.size() + " | " +
                        "Itens processados com sucesso: " + totalProcessed + " | " +
                        "Verifique os relatórios em " + reportsDir.toAbsolutePath());
    }
}