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
 * Repository responsible for persisting and retrieving player vocabulary progress data
 * to and from the disk in JSON format.
 * <p>
 * Manages thread-safe data access via {@link ConcurrentHashMap} and handles file I/O
 * operations using Gson serialization inside the Fabric configuration directory.
 */
public class PlayerVocabularyRepository {

    /** Pretty-printed Gson serializer configured for human-readable JSON files. */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** Type reference required by Gson to deserialize generic collections properly. */
    private static final Type PROGRESS_MAP_TYPE = new TypeToken<ConcurrentHashMap<String, WordProgress>>() {}.getType();

    private static PlayerVocabularyRepository instance;

    /** Path pointing to the progress storage file within the mod's config directory. */
    private final Path saveFilePath;

    /**
     * Private constructor enforcing singleton initialization and setting the default save location.
     */
    private PlayerVocabularyRepository() {
        this.saveFilePath = FabricLoader.getInstance().getConfigDir().resolve("minnanocraft/player_progress.json");
    }

    /**
     * Retrieves the global singleton instance of the repository.
     *
     * @return The active {@link PlayerVocabularyRepository} instance.
     */
    public static PlayerVocabularyRepository getInstance() {
        if (instance == null) {
            instance = new PlayerVocabularyRepository();
        }
        return instance;
    }

    /**
     * Replaces the singleton instance with a custom or mock repository for unit testing.
     *
     * @param mockInstance The instance to inject for testing scenarios.
     */
    public static void setInstanceForTesting(PlayerVocabularyRepository mockInstance) {
        instance = mockInstance;
    }

    /**
     * Loads saved player vocabulary progress from the JSON file on disk.
     * <p>
     * If the file does not exist or deserialization fails, an empty {@link ConcurrentHashMap}
     * is returned to ensure non-null operational state.
     *
     * @return A thread-safe map of token keys to their corresponding {@link WordProgress} instances.
     */
    public ConcurrentHashMap<String, WordProgress> loadAll() {
        if (!Files.exists(this.saveFilePath)) {
            MinnaNoCraft.LOGGER.info("Progress file not found. Creating a new profile for the player.");
            return new ConcurrentHashMap<>();
        }

        try (Reader reader = Files.newBufferedReader(this.saveFilePath, StandardCharsets.UTF_8)) {
            // Uses the pre-allocated Type reference to avoid unnecessary object creation
            ConcurrentHashMap<String, WordProgress> loaded = GSON.fromJson(reader, PROGRESS_MAP_TYPE);

            MinnaNoCraft.LOGGER.info("Player progress loaded successfully!");
            return loaded != null ? loaded : new ConcurrentHashMap<>();

        } catch (Exception e) {
            MinnaNoCraft.LOGGER.error("Failed to load player progress!", e);
            return new ConcurrentHashMap<>();
        }
    }

    /**
     * Writes the current player vocabulary progress map to the JSON storage file using UTF-8 encoding.
     * Creates any missing parent directories prior to writing.
     *
     * @param progressMap The thread-safe map containing active word progress data to serialize.
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