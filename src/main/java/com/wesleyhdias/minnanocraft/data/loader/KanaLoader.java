package com.wesleyhdias.minnanocraft.data.loader;

import com.wesleyhdias.minnanocraft.data.models.Kana;
import com.wesleyhdias.minnanocraft.MinnaNoCraft;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.lang.reflect.Type;
import java.io.InputStream;
import java.util.List;
import java.io.Reader;
import java.util.Map;

/**
 * Service responsible for loading and caching the Kana (Hiragana and Katakana) database from JSON.
 */
public class KanaLoader {

    // LinkedHashMap preserves insertion order from JSON
    private static final Map<String, String> HIRAGANA_MAP = new LinkedHashMap<>();
    private static final Map<String, String> KATAKANA_MAP = new LinkedHashMap<>();

    public static void load() {
        HIRAGANA_MAP.clear();
        KATAKANA_MAP.clear();

        loadFromFile("assets/kana/hiragana.json", HIRAGANA_MAP);
        loadFromFile("assets/kana/katakana.json", KATAKANA_MAP);

        MinnaNoCraft.LOGGER.info("Loaded {} Hiragana and {} Katakana entries!", HIRAGANA_MAP.size(), KATAKANA_MAP.size());
    }

    private static void loadFromFile(String resourcePath, Map<String, String> targetMap) {
        try (InputStream is = KanaLoader.class
                .getClassLoader()
                .getResourceAsStream(resourcePath)) {

            if (is == null) {
                MinnaNoCraft.LOGGER.error("Kana database file not found at: {}", resourcePath);
                return;
            }

            Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            Gson gson = new Gson();

            Type listType = new TypeToken<List<Kana>>() {}.getType();
            List<Kana> entries = gson.fromJson(reader, listType);

            if (entries != null) {
                for (Kana entry : entries) {
                    if (entry.kana() != null && entry.romaji() != null) {
                        targetMap.put(entry.kana(), entry.romaji());
                    }
                }
            }

        } catch (Exception e) {
            MinnaNoCraft.LOGGER.error("Error encountered while loading Kana file: {}", resourcePath, e);
        }
    }

    public static Map<String, String> getHiraganaMap() {
        return HIRAGANA_MAP;
    }

    public static Map<String, String> getKatakanaMap() {
        return KATAKANA_MAP;
    }
}