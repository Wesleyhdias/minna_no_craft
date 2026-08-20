package com.wesleyhdias.minnanocraft;

import com.wesleyhdias.minnanocraft.events.LanguageScreenHandler;
import com.wesleyhdias.minnanocraft.services.VocabularyManager;
import com.wesleyhdias.minnanocraft.events.TooltipEventHandler;
import com.wesleyhdias.minnanocraft.events.ClientTickHandler;
import com.wesleyhdias.minnanocraft.config.ModConfig;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.api.ClientModInitializer;

/**
 * Client-side initializer for MinnaNoCraft.
 * Handles client startup loading, event registration, and safe shutdown saving.
 */
public class MinnaNoCraftClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        LanguageScreenHandler.register();

        // 1. Loads saved vocabulary progress
        VocabularyManager.load();
        MinnaNoCraft.LOGGER.info("MinnaNoCraft (Client) initialized and progress loaded successfully!");

        // 2. Registers modular event handlers
        ClientTickHandler.register();
        TooltipEventHandler.register();

        // Ensures the latest state is written to JSON before the client shuts down
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            VocabularyManager.save();
            MinnaNoCraft.LOGGER.info("MinnaNoCraft (Client) stopping. Progress saved.");
        });
    }
}