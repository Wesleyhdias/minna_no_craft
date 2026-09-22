package com.wesleyhdias.minnanocraft.language.dictionary;

import com.wesleyhdias.minnanocraft.MinnaNoCraft;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;

/**
 * Utility class responsible for lazy-loading and caching the compound words dictionary from JSON resources.
 * <p>
 * Provides read-only access to compound vocabulary mappings used to break down
 * complex multi-part terms into individual component tokens.
 */
public class CompoundDictionaryLoader {

    /** Cached unmodifiable map of compound word keys to {@link CompoundWord} definitions. */
    private static Map<String, CompoundWord> dictionary;

    /**
     * Retrieves the compound words dictionary, initializing and loading it from JSON if not already cached.
     *
     * @return An unmodifiable {@link Map} containing compound word mappings.
     */
    public static Map<String, CompoundWord> getDictionary() {
        if (dictionary == null) {
            load();
        }
        return dictionary;
    }

    /**
     * Overrides the dictionary map instance for unit testing purposes.
     *
     * @param mockInstance A mock {@link Map} instance containing test compound words.
     */
    public static void setDictionaryForTesting(Map<String, CompoundWord> mockInstance) {
        dictionary = mockInstance;
    }

    /**
     * Loads and parses the compound words JSON resource into an unmodifiable map.
     * Fallbacks to an empty map if the resource is missing or parsing fails.
     */
    private static void load() {
        try (InputStream is = CompoundDictionaryLoader.class
                .getClassLoader()
                .getResourceAsStream("assets/dictionary/compoundWords_dictionary.json")) {

            if (is == null) {
                MinnaNoCraft.LOGGER.error("Compound dictionary file not found!");
                dictionary = Map.of();
                return;
            }

            Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            Gson gson = new Gson();

            Type type = new TypeToken<Map<String, CompoundWord>>() {}.getType();
            Map<String, CompoundWord> loaded = gson.fromJson(reader, type);

            dictionary = (loaded != null) ? Collections.unmodifiableMap(loaded) : Map.of();

            MinnaNoCraft.LOGGER.info("Compound dictionary loaded successfully! with {} compound words", dictionary.size());

        } catch (Exception e) {
            MinnaNoCraft.LOGGER.error("Error encountered while loading compound dictionary!", e);
            dictionary = Map.of();
        }
    }
}