package com.wesleyhdias.minnanocraft;

import com.wesleyhdias.minnanocraft.config.vanilla_injection.LanguageScreenHandler;
import com.wesleyhdias.minnanocraft.client.tooltip.TooltipEventHandler;
import com.wesleyhdias.minnanocraft.srs.PlayerVocabularyManager;
import com.wesleyhdias.minnanocraft.client.ClientTickHandler;

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
        PlayerVocabularyManager.load();
        MinnaNoCraft.LOGGER.info("MinnaNoCraft (Client) initialized and progress loaded successfully!");

        // 2. Registers modular event handlers
        ClientTickHandler.register();
        TooltipEventHandler.register();

        // Ensures the latest state is written to JSON before the client shuts down
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            PlayerVocabularyManager.save();
            MinnaNoCraft.LOGGER.info("MinnaNoCraft (Client) stopping. Progress saved.");
        });
    }
}