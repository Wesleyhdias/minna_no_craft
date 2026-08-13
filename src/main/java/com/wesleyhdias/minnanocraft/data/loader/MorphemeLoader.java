package com.wesleyhdias.minnanocraft.data.loader;

import com.wesleyhdias.minnanocraft.data.models.Morpheme;
import com.wesleyhdias.minnanocraft.MinnaNoCraft;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.io.InputStream;
import java.util.HashMap;
import java.io.Reader;
import java.util.Map;

/**
 * Service responsible for loading and caching the morpheme (grammatical particles) database from JSON.
 */
public class MorphemeLoader {

    private static Map<String, Morpheme> morphemes;

    /**
     * Gets the unmodifiable morphemes map, loading it from disk if not yet cached.
     *
     * @return The cached map of morpheme tokens to Morpheme objects.
     */
    public static Map<String, Morpheme> getMorphemes() {
        if (morphemes == null) {
            load();
        }
        return morphemes;
    }

    /**
     * Loads the morpheme database from resources using UTF-8 encoding.
     */
    private static void load() {
        try (InputStream is = MorphemeLoader.class
                .getClassLoader()
                .getResourceAsStream("assets/dictionary/bancoMorfemas.json")) {

            if (is == null) {
                MinnaNoCraft.LOGGER.error("Morpheme file not found!");
                morphemes = new HashMap<>();
                return;
            }

            Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            Type type = new TypeToken<Map<String, Morpheme>>() {}.getType();

            morphemes = Collections.unmodifiableMap(new Gson().fromJson(reader, type));
            MinnaNoCraft.LOGGER.info("Morphemes loaded successfully!");

        } catch (Exception e) {
            MinnaNoCraft.LOGGER.error("Error encountered while loading morphemes!", e);
            morphemes = new HashMap<>();
        }
    }
}