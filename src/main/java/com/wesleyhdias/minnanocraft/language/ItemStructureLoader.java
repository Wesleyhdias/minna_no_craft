package com.wesleyhdias.minnanocraft.language;

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
import java.util.List;
import java.util.Map;

/**
 * Service responsible for loading and caching item name token structures from JSON.
 */
public class ItemStructureLoader {

    private static Map<String, List<String>> structures;

    /**
     * Gets the unmodifiable item structures map, loading it from disk if not yet cached.
     *
     * @return The cached map of translation keys to token lists.
     */
    public static Map<String, List<String>> getStructures() {
        if (structures == null) {
            load();
        }
        return structures;
    }

    /**
     * Loads the item structure configuration from resources using UTF-8 encoding.
     */
    private static void load() {
        try (InputStream is = ItemStructureLoader.class
                .getClassLoader()
                .getResourceAsStream("assets/structures/item_structures.json")) {

            if (is == null) {
                MinnaNoCraft.LOGGER.error("Item structures file not found!");
                structures = new HashMap<>();
                return;
            }

            Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            Type type = new TypeToken<Map<String, List<String>>>() {}.getType();

            structures = Collections.unmodifiableMap(new Gson().fromJson(reader, type));
            MinnaNoCraft.LOGGER.info("Item structures loaded successfully!");

        } catch (Exception e) {
            MinnaNoCraft.LOGGER.error("Error encountered while loading item structures!", e);
            structures = new HashMap<>();
        }
    }
}
