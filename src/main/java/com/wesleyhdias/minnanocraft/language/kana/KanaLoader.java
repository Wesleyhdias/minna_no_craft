package com.wesleyhdias.minnanocraft.language.kana;

import com.wesleyhdias.minnanocraft.MinnaNoCraft;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.lang.reflect.Type;
import java.io.InputStream;
import java.util.List;
import java.io.Reader;
import java.util.Map;

/**
 * Service responsible for loading and caching the Kana (Hiragana and Katakana) database from JSON.
 */
public class KanaLoader {

    private static Map<String, String> hiraganaMap;
    private static Map<String, String> katakanaMap;

    /**
     * Gets the unmodifiable Hiragana map, loading it from disk if not yet cached.
     *
     * @return The cached map of kana characters to their romaji representations.
     */
    public static Map<String, String> getHiraganaMap() {
        if (hiraganaMap == null) {
            load();
        }
        return hiraganaMap;
    }

    /**
     * Gets the unmodifiable Katakana map, loading it from disk if not yet cached.
     *
     * @return The cached map of kana characters to their romaji representations.
     */
    public static Map<String, String> getKatakanaMap() {
        if (katakanaMap == null) {
            load();
        }
        return katakanaMap;
    }

    /**
     * Loads both Kana files from resources using UTF-8 encoding.
     */
    private static void load() {
        Map<String, String> tempHiragana = new LinkedHashMap<>();
        Map<String, String> tempKatakana = new LinkedHashMap<>();

        loadFromFile("assets/kana/hiragana.json", tempHiragana);
        loadFromFile("assets/kana/katakana.json", tempKatakana);

        hiraganaMap = Collections.unmodifiableMap(tempHiragana);
        katakanaMap = Collections.unmodifiableMap(tempKatakana);

        MinnaNoCraft.LOGGER.info("Loaded {} Hiragana and {} Katakana entries!", hiraganaMap.size(), katakanaMap.size());
    }

    /**
     * Helper method to load a specific JSON kana file into a target map.
     *
     * @param resourcePath The path to the json file inside resources.
     * @param targetMap    The map to populate with entries.
     */
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
}