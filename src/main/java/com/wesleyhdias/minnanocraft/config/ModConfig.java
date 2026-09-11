package com.wesleyhdias.minnanocraft.config;

import com.wesleyhdias.minnanocraft.config.data.ConfigLoader;
import com.wesleyhdias.minnanocraft.config.data.ConfigData;

/**
 * Singleton manager responsible for maintaining the in-memory state of global configurations.
 * Serves as the primary bridge between runtime application settings and disk persistence.
 */
public class ModConfig {

    public static ModConfig ModConfig;
    /** In-memory cached instance of the configuration data schema. */
    private static ConfigData config;

    /**
     * Universal access point to retrieve or modify global configurations.
     * Lazy-loads settings from disk on the first invocation and ensures
     * default file creation if the configuration file does not yet exist.
     *
     * @return The active {@link ConfigData} instance.
     */
    public static ConfigData getConfig() {
        if (config == null) {
            config = ConfigLoader.load();
        }
        return config;
    }

    public static void setInstanceForTesting(ConfigData mockInstance) {
        config = mockInstance;
    }

    /**
     * Persists the current in-memory configuration state to disk via {@link ConfigLoader}.
     */
    public static void save() {
        if (config != null) {
            ConfigLoader.save(config);
        }
    }
}