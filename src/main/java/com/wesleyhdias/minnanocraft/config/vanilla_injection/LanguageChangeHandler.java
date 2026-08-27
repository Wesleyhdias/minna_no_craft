package com.wesleyhdias.minnanocraft.config.vanilla_injection;

import com.wesleyhdias.minnanocraft.MinnaNoCraft;
import com.wesleyhdias.minnanocraft.config.data.ModConfig;
import com.wesleyhdias.minnanocraft.config.data.ConfigData;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * Monitors vanilla client ticks to detect language updates and enforce mod availability.
 */
public class LanguageChangeHandler {

    private static String lastLanguage = null;

    /**
     * Registers the client tick listener responsible for tracking language transitions.
     */
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            String currentLang = client.getLanguageManager().getSelected();

            // Detects initial startup or language change
            if (lastLanguage == null || !lastLanguage.equals(currentLang)) {
                lastLanguage = currentLang;

                // Automatically disables the mod if the selected language is unsupported
                if (!MinnaNoCraft.SUPPORTED_LANGUAGES.contains(currentLang)) {
                    ConfigData config = ModConfig.getConfig();

                    if (config.isEnabled()) {
                        config.setEnabled(false);
                        ModConfig.save();
                    }
                }
            }
        });
    }
}