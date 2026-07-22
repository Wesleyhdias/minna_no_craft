package com.wesleyhdias.minnanocraft.data.loader;

import com.wesleyhdias.minnanocraft.data.models.Morpheme;
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

public class MorphemeLoader {

    private static Map<String, Morpheme> morphemes;

    public static Map<String, Morpheme> getMorphemes() {
        if (morphemes == null) {
            load();
        }
        return morphemes;
    }

    private static void load() {

        try (InputStream is = MorphemeLoader.class
                .getClassLoader()
                .getResourceAsStream("assets/lang/bancoMorfemas.json")) {

            if (is == null) {
                MinnaNoCraft.LOGGER.error("Morpheme file not found! ");
                morphemes = new HashMap<>();
                return;
            }

            Reader reader = new InputStreamReader(is);

            Type type = new TypeToken<Map<String, Morpheme>>() {}.getType();

            morphemes = Collections.unmodifiableMap(
                    new Gson().fromJson(reader, type));
            MinnaNoCraft.LOGGER.info("morphemes loaded! ");

        } catch (Exception e) {
            MinnaNoCraft.LOGGER.error("morpheme loader found an error ", e);
            morphemes = new HashMap<>();
        }
    }
}