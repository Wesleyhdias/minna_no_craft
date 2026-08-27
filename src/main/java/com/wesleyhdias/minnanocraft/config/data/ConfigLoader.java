package com.wesleyhdias.minnanocraft.config.data;

import com.wesleyhdias.minnanocraft.MinnaNoCraft;

import net.fabricmc.loader.api.FabricLoader;

import com.google.gson.GsonBuilder;
import com.google.gson.Gson;

import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.Reader;
import java.io.Writer;

/**
 * Service class responsible for loading, reading, and persisting
 * the mod's global configuration settings from/to disk in JSON format.
 */
public class ConfigLoader {

    /** Pretty-printed Gson instance for human-readable JSON configuration files. */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** Directory path storing the mod's configuration files inside the Fabric config folder. */
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("minnanocraft");

    /** File path for the main JSON configuration file. */
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.json");

    /**
     * Loads the global configuration data from disk.
     * If the file does not exist or an error occurs during reading, a default {@link ConfigData} instance is returned.
     *
     * @return The loaded or default {@link ConfigData} instance.
     */
    public static ConfigData load() {
        try {
            // Ensure the configuration directory exists
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }

            // If the file does not exist (first run), create and save default data
            if (!Files.exists(CONFIG_FILE)) {
                ConfigData defaultData = new ConfigData();
                save(defaultData);
                return defaultData;
            }

            // Attempt to read the existing configuration file
            try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
                ConfigData loadedData = GSON.fromJson(reader, ConfigData.class);
                if (loadedData != null) {
                    return loadedData;
                }
            }
        } catch (Exception e) {
            MinnaNoCraft.LOGGER.error("Failed to load configurations! Creating a backup of the corrupted file.", e);

            // FAILSAFE: Rename the corrupted JSON to .bak to prevent data loss
            try {
                if (Files.exists(CONFIG_FILE)) {
                    Path backup = CONFIG_FILE.resolveSibling(CONFIG_FILE.getFileName() + ".bak");
                    Files.move(CONFIG_FILE, backup, StandardCopyOption.REPLACE_EXISTING);
                    MinnaNoCraft.LOGGER.info("Configuration backup saved to: {}", backup.toString());
                }
            } catch (Exception ex) {
                MinnaNoCraft.LOGGER.error("Failed to create config backup!", ex);
            }
        }

        // Fallback: Returns a fresh instance if reading fails or JSON was null/corrupted.
        // Since a backup was created above, it is safe to overwrite the corrupted file.
        ConfigData fallback = new ConfigData();
        save(fallback);
        return fallback;
    }

    /**
     * Persists the provided configuration data instance to the JSON file on disk.
     *
     * @param data The {@link ConfigData} instance containing current configuration settings.
     */
    public static void save(ConfigData data) {
        try {
            // Ensures the parent configuration directory exists before writing
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