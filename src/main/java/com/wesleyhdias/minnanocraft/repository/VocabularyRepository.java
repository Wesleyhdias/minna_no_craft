package com.wesleyhdias.minnanocraft.repository;

import com.wesleyhdias.minnanocraft.data.models.WordProgress;
import com.wesleyhdias.minnanocraft.MinnaNoCraft;

import com.google.gson.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import com.google.gson.Gson;

import java.util.concurrent.ConcurrentHashMap;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.io.*;

/**
 * Repository responsible for persisting and loading the player's vocabulary progress to/from disk in JSON format.
 */
public class VocabularyRepository {

    /** Pretty-printed Gson instance for human-readable JSON files. */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** File path for storing player progress inside the Minecraft configuration folder. */
    private static final File SAVE_FILE = new File("config/minnanocraft/player_progress.json");

    /**
     * Loads the progress file from disk.
     * Returns an empty ConcurrentHashMap if the file does not exist or an error occurs.
     *
     * @return The loaded player progress map.
     */
    public ConcurrentHashMap<String, WordProgress> loadAll() {
        if (!SAVE_FILE.exists()) {
            MinnaNoCraft.LOGGER.info("Progress file not found. Creating a new profile for the player.");
            return new ConcurrentHashMap<>();
        }

        try (Reader reader = Files.newBufferedReader(SAVE_FILE.toPath(), StandardCharsets.UTF_8)) {
            Type type = new TypeToken<ConcurrentHashMap<String, WordProgress>>() {}.getType();
            ConcurrentHashMap<String, WordProgress> loaded = GSON.fromJson(reader, type);

            MinnaNoCraft.LOGGER.info("Player progress loaded successfully!");
            return loaded != null ? loaded : new ConcurrentHashMap<>();

        } catch (Exception e) {
            MinnaNoCraft.LOGGER.error("Failed to load player progress!", e);
            return new ConcurrentHashMap<>();
        }
    }

    /**
     * Saves the current player progress state to the JSON file using UTF-8 encoding.
     *
     * @param progressMap The map containing all current word progress data.
     */
    public void saveAll(ConcurrentHashMap<String, WordProgress> progressMap) {
        try {
            // Ensures parent directories (config/minnanocraft) exist before saving
            if (SAVE_FILE.getParentFile() != null) {
                SAVE_FILE.getParentFile().mkdirs();
            }

            try (Writer writer = Files.newBufferedWriter(SAVE_FILE.toPath(), StandardCharsets.UTF_8)) {
                GSON.toJson(progressMap, writer);
            }
        } catch (Exception e) {
            MinnaNoCraft.LOGGER.error("Failed to save player progress!", e);
        }
    }
}
