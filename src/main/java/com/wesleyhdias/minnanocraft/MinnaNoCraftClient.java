package com.wesleyhdias.minnanocraft;

import com.wesleyhdias.minnanocraft.events.ClientTickHandler;
import com.wesleyhdias.minnanocraft.events.TooltipEventHandler;
import com.wesleyhdias.minnanocraft.services.VocabularyManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public class MinnaNoCraftClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // 1. Carrega os dados salvos
        VocabularyManager.load();
        MinnaNoCraft.LOGGER.info("MinnaNoCraft (Client) inicializado e progresso carregado!");

        // 2. Registra os eventos modulares
        ClientTickHandler.register();
        TooltipEventHandler.register();

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            // Garante que o último estado seja gravado no JSON antes de fechar o cliente
            VocabularyManager.save();
        });
    }
}