package com.wesleyhdias.minnanocraft.config.data;

import com.wesleyhdias.minnanocraft.MinnaNoCraft;
import net.fabricmc.loader.api.FabricLoader;

import com.google.gson.GsonBuilder;
import com.google.gson.Gson;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.Reader;
import java.io.Writer;

/**
 * Service responsible for loading and saving the mod's configuration JSON.
 */
public class ConfigLoader {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("minnanocraft");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.json");

    /**
     * Loads the configuration from disk, or returns a default instance if it fails/doesn't exist.
     */
    public static ConfigData load() {
        try {
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }

            if (Files.exists(CONFIG_FILE)) {
                try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
                    ConfigData loadedData = GSON.fromJson(reader, ConfigData.class);
                    if (loadedData != null) {
                        return loadedData;
                    }
                }
            }
        } catch (Exception e) {
            MinnaNoCraft.LOGGER.error("Failed to load global configurations!", e);
        }

        // Se falhar ou o arquivo não existir, retorna um novo com os valores padrão
        return new ConfigData();
    }

    /**
     * Saves the current configuration data to the JSON file.
     */
    public static void save(ConfigData data) {
        try {
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }
            try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
                GSON.toJson(data, writer);
            }
        } catch (Exception e) {
            MinnaNoCraft.LOGGER.error("Failed to save global configurations!", e);
        }
    }
}