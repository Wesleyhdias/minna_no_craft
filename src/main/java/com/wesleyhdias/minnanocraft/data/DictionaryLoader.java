package com.wesleyhdias.minnanocraft.data;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import com.wesleyhdias.minnanocraft.MinnaNoCraft;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.io.InputStream;
import java.util.Collections;
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
                .getResourceAsStream("assets/lang/novo_banco_palavras.json")) {

            if (is == null) {
                MinnaNoCraft.LOGGER.error("Dicionario não encontrado! ");
                dictionary = new HashMap<>();
                return;
            }

            Reader reader = new InputStreamReader(is);
            Gson gson = new Gson();

            Type type = new TypeToken<Map<String, Word>>() {}.getType();
            dictionary = Collections.unmodifiableMap(gson.fromJson(reader, type));

        } catch (Exception e) {
            MinnaNoCraft.LOGGER.error("Dicionario não encontrado!", e);
            dictionary = new HashMap<>();
        }
    }
}