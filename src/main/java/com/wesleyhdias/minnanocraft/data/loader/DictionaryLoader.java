package com.wesleyhdias.minnanocraft.data.loader;

import com.wesleyhdias.minnanocraft.data.models.Word;
import com.wesleyhdias.minnanocraft.MinnaNoCraft;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.io.InputStream;
import java.util.HashMap;
import java.io.Reader;
import java.util.Map;

/**
 * Service responsible for loading and caching the main dictionary database from JSON.
 */
public class DictionaryLoader {

    private static Map<String, Word> dictionary;

    /**
     * Gets the unmodifiable dictionary map, loading it from disk if not yet cached.
     *
     * @return The cached map of word tokens to Word objects.
     */
    public static Map<String, Word> getDictionary() {
        if (dictionary == null) {
            load();
        }
        return dictionary;
    }

    /**
     * Loads the dictionary file from resources using UTF-8 encoding.
     */
    private static void load() {
        try (InputStream is = DictionaryLoader.class
                .getClassLoader()
                .getResourceAsStream("assets/lang/banco_palavras_v3.json")) {

            if (is == null) {
                MinnaNoCraft.LOGGER.error("Dictionary file not found!");
                dictionary = new HashMap<>();
                return;
            }

            Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            Gson gson = new Gson();

            Type type = new TypeToken<Map<String, Word>>() {}.getType();
            dictionary = Collections.unmodifiableMap(gson.fromJson(reader, type));

            MinnaNoCraft.LOGGER.info("Dictionary loaded successfully!");

        } catch (Exception e) {
            MinnaNoCraft.LOGGER.error("Error encountered while loading dictionary!", e);
            dictionary = new HashMap<>();
        }
    }
}