package com.wesleyhdias.minnanocraft.config.vanilla_injection;

import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;

/**
 * Handles screen registration events to detect and inject custom components
 * (such as configuration toggles) into existing Minecraft screens.
 */
public class LanguageScreenHandler {

    /**
     * Registers client screen event callbacks. This should be called
     * during client initialization to listen for screen openings.
     */
    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {

            // If the opened screen is the language selection screen, pass control to the custom renderer
            if (screen instanceof LanguageSelectScreen) {
                LanguageScreenRenderer.inject(client, screen, scaledWidth);
            }

        });
    }
}