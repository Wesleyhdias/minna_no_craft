package com.wesleyhdias.minnanocraft.language.kana;

import com.wesleyhdias.minnanocraft.MinnaNoCraft;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.lang.reflect.Type;
import java.util.Collections;
import java.io.InputStream;
import java.util.List;
import java.io.Reader;
import java.util.Map;

/**
 * Service responsible for loading and caching the Kana (Hiragana and Katakana) database from JSON.
 */
public class KanaLoader {

    private static Map<String, Kana> romajiMap;
    private static Map<String, String> kanaToRomajiMap;

    /**
     * Gets the unmodifiable Hiragana map, loading it from disk if not yet cached.
     *
     * @return The cached map of kana characters to their romaji representations.
     */
    public static Map<String, Kana> getRomajiMap() {
        if (romajiMap == null) {
            load();
        }
        return romajiMap;
    }

    /**
     * Gets the unmodifiable Katakana map, loading it from disk if not yet cached.
     *
     * @return The cached map of kana characters to their romaji representations.
     */
    public static Map<String, String> getKanaToRomajiMap() {
        if (kanaToRomajiMap == null) {
            load();
        }
        return kanaToRomajiMap;
    }

    /**
     * Loads Kana file from resources using UTF-8 encoding.
     */
    private static void load() {
        Map<String, Kana> tempRomajiMap = new LinkedHashMap<>();
        Map<String, String> tempKanaToRomajiMap = new LinkedHashMap<>();

        loadFromFile(tempRomajiMap, tempKanaToRomajiMap);

        romajiMap = Collections.unmodifiableMap(tempRomajiMap);
        kanaToRomajiMap = Collections.unmodifiableMap(tempKanaToRomajiMap);

        MinnaNoCraft.LOGGER.info("Loaded {} kana entries!", romajiMap.size());
    }

    /**
     * Helper method to load a specific JSON kana file into a target map.
     *
     * @param targetKanaToRomajiMap The map to get romaji by kana
     * @param targetRomajiMap   The map to get kana by romaji.
     */
    private static void loadFromFile(Map<String, Kana> targetRomajiMap, Map<String, String> targetKanaToRomajiMap) {
        try (InputStream is = KanaLoader.class
                .getClassLoader()
                .getResourceAsStream("assets/kana/kana.json")) {

            if (is == null) {
                MinnaNoCraft.LOGGER.error("Kana database file not found at: {}", "assets/kana/kana.json");
                return;
            }

            Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            Gson gson = new Gson();

            Type listType = new TypeToken<List<Kana>>() {}.getType();
            List<Kana> entries = gson.fromJson(reader, listType);

            if (entries != null) {
                for (Kana entry : entries) {
                    if (entry.hiragana() != null && entry.katakana() != null && entry.romaji() != null) {
                        targetRomajiMap.put(entry.romaji(), entry);

                        targetKanaToRomajiMap.put(entry.hiragana(), entry.romaji());
                        targetKanaToRomajiMap.put(entry.katakana(), entry.romaji());
                    }
                }
            }

        } catch (Exception e) {
            MinnaNoCraft.LOGGER.error("Error encountered while loading Kana file: {}", "assets/kana/kana.json", e);
        }
    }
}