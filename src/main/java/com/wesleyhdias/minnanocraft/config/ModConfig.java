package com.wesleyhdias.minnanocraft.config;

import com.wesleyhdias.minnanocraft.data.loader.ConfigLoader;

/**
 * Manages the in-memory state of the global configurations.
 */
public class ModConfig {

    private static ConfigData config;

    /**
     * Universal access point to read or alter configurations.
     * Lazy-loads the config the first time it is called.
     */
    public static ConfigData getConfig() {
        if (config == null) {
            config = ConfigLoader.load();
            // Salva logo após carregar para garantir que o arquivo seja criado na primeira vez
            ConfigLoader.save(config);
        }
        return config;
    }

    /**
     * Tells the loader to save the current state to disk.
     */
    public static void save() {
        if (config != null) {
            ConfigLoader.save(config);
        }
    }
}