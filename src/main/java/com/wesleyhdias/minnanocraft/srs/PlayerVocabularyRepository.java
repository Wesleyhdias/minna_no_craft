package com.wesleyhdias.minnanocraft.srs;

import com.wesleyhdias.minnanocraft.srs.models.WordProgress;
import com.wesleyhdias.minnanocraft.MinnaNoCraft;
import com.google.gson.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import com.google.gson.Gson;

import net.fabricmc.loader.api.FabricLoader;

import java.util.concurrent.ConcurrentHashMap;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.*;

/**
 * Repository responsible for persisting and loading the player's vocabulary progress to/from disk in JSON format.
 */
public class PlayerVocabularyRepository {

    /** Pretty-printed Gson instance for readable JSON files. */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static PlayerVocabularyRepository instance;

    /** File path for storing player progress inside the Minecraft configuration folder. */
    private final Path saveFilePath;

    private PlayerVocabularyRepository() {

        this.saveFilePath = FabricLoader.getInstance().getConfigDir().resolve("minnanocraft/player_progress.json");
    }

    public static PlayerVocabularyRepository getInstance() {
        if (instance == null) {
            instance = new PlayerVocabularyRepository();
        }
        return instance;
    }

    public static void setInstanceForTesting(PlayerVocabularyRepository mockInstance) {
        instance = mockInstance;
    }

    /**
     * Loads the progress file from disk.
     * Returns an empty ConcurrentHashMap if the file does not exist or an error occurs.
     *
     * @return The loaded player progress map.
     */
    public ConcurrentHashMap<String, WordProgress> loadAll() {
        if (!Files.exists(this.saveFilePath)) {
            MinnaNoCraft.LOGGER.info("Progress file not found. Creating a new profile for the player.");
            return new ConcurrentHashMap<>();
        }

        try (Reader reader = Files.newBufferedReader(this.saveFilePath, StandardCharsets.UTF_8)) {
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
            if (this.saveFilePath.getParent() != null) {
                Files.createDirectories(this.saveFilePath.getParent());
            }

            try (Writer writer = Files.newBufferedWriter(this.saveFilePath, StandardCharsets.UTF_8)) {
                GSON.toJson(progressMap, writer);
            }
        } catch (Exception e) {
            MinnaNoCraft.LOGGER.error("Failed to save player progress!", e);
        }
    }
}
