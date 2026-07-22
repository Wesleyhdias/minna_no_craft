package com.wesleyhdias.minnanocraft.data.loader;

import com.wesleyhdias.minnanocraft.data.models.Word;
import com.wesleyhdias.minnanocraft.MinnaNoCraft;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.io.InputStream;
import java.util.HashMap;
import java.io.Reader;
import java.util.Map;

public class DictionaryLoader {

    private static Map<String, Word> dictionary;

    public static Map<String, Word> getDictionary() {
        if (dictionary == null) {
            load();
        }
        return dictionary;
    }

    private static void load() {
        try (InputStream is = DictionaryLoader.class
                .getClassLoader()
                .getResourceAsStream("assets/lang/banco_palavras_v3.json")) {

            if (is == null) {
                MinnaNoCraft.LOGGER.error("dictionary not found! ");
                dictionary = new HashMap<>();
                return;
            }

            Reader reader = new InputStreamReader(is);
            Gson gson = new Gson();
            MinnaNoCraft.LOGGER.info("Dictionary loaded! ");

            Type type = new TypeToken<Map<String, Word>>() {}.getType();
            dictionary = Collections.unmodifiableMap(gson.fromJson(reader, type));

        } catch (Exception e) {
            MinnaNoCraft.LOGGER.error("dictionary loader found an error! ", e);
            dictionary = new HashMap<>();
        }
    }
}