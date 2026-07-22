package com.wesleyhdias.minnanocraft;

import com.wesleyhdias.minnanocraft.events.ClientTickHandler;
import com.wesleyhdias.minnanocraft.events.TooltipEventHandler;
import com.wesleyhdias.minnanocraft.services.VocabularyManager;
import net.fabricmc.api.ClientModInitializer;

public class MinnaNoCraftClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // 1. Carrega os dados salvos
        VocabularyManager.load();
        MinnaNoCraft.LOGGER.info("MinnaNoCraft (Client) inicializado e progresso carregado!");

        // 2. Registra os eventos modulares
        ClientTickHandler.register();
        TooltipEventHandler.register();
    }
}