package com.wesleyhdias.minnanocraft.language.builder;

import com.wesleyhdias.minnanocraft.language.dictionary.CompoundDictionaryLoader;
import com.wesleyhdias.minnanocraft.language.dictionary.DictionaryLoader;
import com.wesleyhdias.minnanocraft.language.dictionary.CompoundWord;
import com.wesleyhdias.minnanocraft.language.morpheme.MorphemeLoader;
import com.wesleyhdias.minnanocraft.language.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.language.morpheme.Morpheme;
import com.wesleyhdias.minnanocraft.language.dictionary.Word;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class FullJapaneseItemNameBuilderTest {

    private static Map<String, String> gabarito;
    private static final List<String> missingStructures = new ArrayList<>();
    private static final Set<String> missingWords = new TreeSet<>();
    private static final List<String> mismatchedNames = new ArrayList<>();
    private static int numRegister = 0;

    private static final List<String> PREFIXOS_IGNORADOS = List.of(
            // Interfaces e Chat
            "menu.",
            "death.attack.",
            "container.",

            "block.minecraft.attached_melon_stem",
            "block.minecraft.attached_pumpkin_stem",
            "block.minecraft.player_head.named",
            "block.minecraft.red_shrub",
            "block.minecraft.set_spawn",
            "block.minecraft.shelf_mushroom",
            "block.minecraft.straw_bed",
            "block.minecraft.tnt.disabled",
            "entity.minecraft.evoker_fangs",
            "entity.minecraft.falling_block_type",
            "entity.minecraft.interaction",
            "entity.minecraft.mannequin.label",
            "item.minecraft.crossbow.projectile.multiple",
            "item.minecraft.crossbow.projectile.single",
            "item.minecraft.disc_fragment_5.desc",
            "item.minecraft.firework_rocket.flight_duration",
            "item.minecraft.firework_rocket.multiple_stars",
            "item.minecraft.firework_rocket.single_star",
            "item.minecraft.firework_star.fade_to",
            "item.minecraft.music_disc_bounce",
            "item.minecraft.music_disc_bounce.desc",
            "item.minecraft.smithing_template.applies_to",
            "item.minecraft.straw_bed",

            // Casos especiais de Itens/Blocos para pular do gabarito por enquanto
            "block.minecraft.bed.",
            "block.minecraft.spawn.",
            "block.minecraft.spawner.d",
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

    @BeforeAll
    public static void setup() throws Exception {
        // 1. Carrega o gabarito em Japonês
        try (FileReader reader = new FileReader("run/lang_dump/ja_jp_filtered.json")) {
            gabarito = new Gson().fromJson(reader, new TypeToken<Map<String, String>>() {
            }.getType());
        }

        // 2. Injeta Providers de Teste forçando o Nível 4 (Kanji) com suporte a Compostas
        JapaneseItemNameBuilder.setProvidersForTesting(List.of(
                token -> {
                    // Tenta achar Palavra Composta e resolve os componentes
                    CompoundWord compound = CompoundDictionaryLoader.getDictionary().get(token);
                    if (compound != null) {
                        StringBuilder compoundBuilder = new StringBuilder();
                        List<String> components = compound.components();

                        for (String component : components) {
                            Word cw = DictionaryLoader.getDictionary().get(component);
                            if (cw != null) {
                                compoundBuilder.append(cw.kanji());
                                continue;
                            }

                            Morpheme m = MorphemeLoader.getMorphemes().get(component);
                            if (m != null) {
                                compoundBuilder.append(m.kanji() != null ? m.kanji() : m.hiragana());
                            }
                        }
                        return compoundBuilder.toString();
                    }

                    // Tenta achar a palavra simples e devolve o Kanji
                    Word w = DictionaryLoader.getDictionary().get(token);
                    if (w != null) return w.kanji();

                    // Tenta achar o morfema (morfemas podem não ter kanji, então recai pro hiragana)
                    Morpheme m = MorphemeLoader.getMorphemes().get(token);
                    if (m != null) return m.kanji() != null ? m.kanji() : m.hiragana();

                    return null;
                }
        ));
    }

    @Test
    public void validateTranslations() {

        numRegister = 0;

        // Pega as estruturas direto do mod
        Map<String, List<String>> structures = ItemStructureLoader.getStructures();

        for (Map.Entry<String, String> entry : gabarito.entrySet()) {
            String key = entry.getKey();
            String expectedName = entry.getValue();

            // Pula as chaves de interface/chat que você não quer validar ainda
            boolean ignorar = PREFIXOS_IGNORADOS.stream().anyMatch(key::startsWith);
            boolean ignorarTrecho = TRECHOS_IGNORADOS.stream().anyMatch(key::contains);

            if (ignorar || ignorarTrecho) {
                continue;
            }

            // --- FILTRO 1: Estruturas Órfãs ---
            if (!structures.containsKey(key)) {
                missingStructures.add("Chave: " + key + " | Gabarito pedia: " + expectedName);
                continue;
            }

            // --- FILTRO 2: Palavras Faltando (Agora abrindo as compostas) ---
            List<String> structTokens = structures.get(key);
            boolean hasMissingWord = false;

            for (String token : structTokens) {
                CompoundWord compound = CompoundDictionaryLoader.getDictionary().get(token);

                if (compound != null) {

                    // Verifica se os componentes internos existem
                    for (String compToken : compound.components()) {
                        if(Objects.equals(compToken, " ")) continue;
                        if (DictionaryLoader.getDictionary().get(compToken) == null && MorphemeLoader.getMorphemes().get(compToken) == null) {

                            missingWords.add(compToken);
                            hasMissingWord = true;
                        }
                    }
                    // Verifica se o separador existe (caso não seja vazio)
                    String sepToken = compound.getSafeSeparator();
                    if (!sepToken.isEmpty() && !sepToken.equals(" ")) {
                        if (MorphemeLoader.getMorphemes().get(sepToken) == null) {
                            missingWords.add(sepToken);
                            hasMissingWord = true;
                        }
                    }
                } else {
                    // Checa se o token simples/morfema existe no banco
                    if (DictionaryLoader.getDictionary().get(token) == null && MorphemeLoader.getMorphemes().get(token) == null) {
                        missingWords.add(token);
                        hasMissingWord = true;
                    }
                }
            }
            if (hasMissingWord) continue;

            numRegister += 1;

            // --- FILTRO 3: Montagem Incorreta ---
            String generatedName = JapaneseItemNameBuilder.build(key);

            // Remove espaços do builder e do gabarito para comparar apenas os ideogramas
            String generatedClean = generatedName != null ? generatedName.replace(" ", "") : "";
            String expectedClean = expectedName.replace(" ", "");

            if (!generatedClean.equals(expectedClean)) {
                mismatchedNames.add("Chave: " + key + "\n   Gerado  : " + generatedClean + "\n   Gabarito: " + expectedClean + "\n");
            }
        }
    }

    @AfterAll
    public static void writeReports() throws Exception {

        if (!missingStructures.isEmpty()) {
            Files.write(Paths.get("run/test_reports/japanese/report_missing_structures.txt"), missingStructures);
        }
        if (!missingWords.isEmpty()) {
            Files.write(Paths.get("run/test_reports/japanese/report_missing_words.txt"), missingWords);
        }
        if (!mismatchedNames.isEmpty()) {
            Files.write(Paths.get("run/test_reports/japanese/report_mismatched_names.txt"), mismatchedNames);
        }

        boolean allPassed = missingStructures.isEmpty() && missingWords.isEmpty() && mismatchedNames.isEmpty();

//        System.out.println("Olha o que tem aqui: " + allPassed);
//        System.out.println("e aqui: " + missingStructures);
//        System.out.println("aqui...: " + missingWords);
//        System.out.println("já sabe: " + mismatchedNames);

        Assertions.assertTrue(allPassed,
                "Foram encontrados erros no JSON! " +
                        "Estruturas faltando: " + missingStructures.size() + " | " +
                        "Palavras faltando: " + missingWords.size() + " | " +
                        "Nomes errados: " + mismatchedNames.size() + " | " +
                        "Total de nomes gerados: " + numRegister + " | " +
                        "Verifique os arquivos .txt gerados na raiz do projeto.");
    }
}