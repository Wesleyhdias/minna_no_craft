package com.wesleyhdias.minnanocraft.config;

import net.fabricmc.loader.api.FabricLoader;

import com.google.gson.GsonBuilder;
import com.google.gson.Gson;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.io.Reader;
import java.io.Writer;

/**
 * Manages global configurations for the MinnaNoCraft mod, handling
 * JSON serialization, file I/O, and in-memory data states.
 */
public class ModConfig {

    /** Pre-configured Gson instance with pretty-printing enabled for readable JSON files. */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** Directory path: .minecraft/config/minnanocraft/ */
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("minnanocraft");

    /** File path: .minecraft/config/minnanocraft/config.json */
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.json");

    /** Static instance holding the configuration values in RAM. */
    private static ConfigData data = new ConfigData();

    /**
     * Internal data structure acting as a schema template
     * for Gson to read and write the configuration JSON.
     */
    private static class ConfigData {
        boolean enabled = true;
        List<String> supportedLanguages = List.of("pt_br");
    }

    /**
     * Checks whether the mod is currently enabled.
     *
     * @return true if enabled, false otherwise
     */
    public static boolean isEnabled() {
        return data.enabled;
    }

    /**
     * Gets the list of supported languages configured.
     *
     * @return a list of language codes
     */
    public static List<String> getSupportedLanguages() {
        return data.supportedLanguages;
    }

    // ==========================================
    // INTERACTION ACTIONS
    // ==========================================

    /**
     * Toggles the mod's active state (Enabled ⇾ Disabled ⇾ Enabled) and saves it to disk.
     */
    public static void toggle() {
        data.enabled = !data.enabled;
        save();
    }

    /**
     * Forces a specific state for the mod configuration and saves it if changed.
     *
     * @param value the target state to set
     */
    public static void setEnabled(boolean value) {
        if (data.enabled != value) {
            data.enabled = value;
            save();
        }
    }

    // ==========================================
    // I/O OPERATIONS (LOAD & SAVE)
    // ==========================================

    /**
     * Loads configurations from the JSON file into memory.
     * This should be called ONCE during mod initialization.
     */
    public static void load() {
        try {
            // Creates the "minnanocraft" directory if it does not exist
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }

            // If config.json already exists, read it and map to memory
            if (Files.exists(CONFIG_FILE)) {
                try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
                    ConfigData loadedData = GSON.fromJson(reader, ConfigData.class);
                    if (loadedData != null) {
                        data = loadedData;
                    }
                }
            } else {
                // If the file doesn't exist (first time launching), create it with defaults
                save();
            }
        } catch (Exception e) {
            System.err.println("[MinnaNoCraft] Failed to load global configurations: " + e.getMessage());
        }
    }

    /**
     * Serializes memory data back into JSON format and saves it to disk.
     */
    public static void save() {
        try {
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }
            try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
                GSON.toJson(data, writer);
            }
        } catch (Exception e) {
            System.err.println("[MinnaNoCraft] Failed to save global configurations: " + e.getMessage());
        }
    }
}