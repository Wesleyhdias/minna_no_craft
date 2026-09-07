package com.wesleyhdias.minnanocraft.language.dictionary;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wesleyhdias.minnanocraft.MinnaNoCraft;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CompoundDictionaryLoader {

    private static Map<String, CompoundWord> dictionary;

    public static Map<String, CompoundWord> getDictionary() {
        if (dictionary == null) {
            load();
        }
        return dictionary;
    }

    public static void setDictionaryForTesting(Map<String, CompoundWord> mockInstance) {
        dictionary = mockInstance;
    }

    private static void load() {
        try (InputStream is = CompoundDictionaryLoader.class
                .getClassLoader()
                .getResourceAsStream("assets/dictionary/compoundWords_dictionary.json")) {

            if (is == null) {
                MinnaNoCraft.LOGGER.error("Compound dictionary file not found!");
                dictionary = new HashMap<>();
                return;
            }

            Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            Gson gson = new Gson();

            Type type = new TypeToken<Map<String, CompoundWord>>() {}.getType();
            dictionary = Collections.unmodifiableMap(gson.fromJson(reader, type));

            MinnaNoCraft.LOGGER.info("Compound dictionary loaded successfully!");

        } catch (Exception e) {
            MinnaNoCraft.LOGGER.error("Error encountered while loading compound dictionary!", e);
            dictionary = new HashMap<>();
        }
    }
}