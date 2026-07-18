package com.wesleyhdias.minnanocraft.data.loader;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.io.InputStream;
import java.util.HashMap;
import java.io.Reader;
import java.util.List;
import java.util.Map;

public class ItemStructureLoader {

    private static Map<String, List<String>> structures;

    public static Map<String, List<String>> getStructures() {

        if (structures == null) {
            load();
        }

        return structures;
    }

    private static void load() {

        try (InputStream is = ItemStructureLoader.class
                .getClassLoader()
                .getResourceAsStream("assets/lang/item_structures.json")) {

            if (is == null) {
                structures = new HashMap<>();
                return;
            }

            Reader reader = new InputStreamReader(is);

            Type type = new TypeToken<Map<String, List<String>>>() {}.getType();

            structures = Collections.unmodifiableMap(
                    new Gson().fromJson(reader, type));

        } catch (Exception e) {
            structures = new HashMap<>();
        }
    }
}
