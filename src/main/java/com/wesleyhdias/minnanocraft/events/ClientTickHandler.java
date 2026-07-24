package com.wesleyhdias.minnanocraft.events;

import com.wesleyhdias.minnanocraft.data.loader.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.services.TooltipHoverTracker;
import com.wesleyhdias.minnanocraft.services.VocabularyManager;
import com.wesleyhdias.minnanocraft.data.models.Event;
import com.wesleyhdias.minnanocraft.MinnaNoCraft;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ClientTickHandler {

    private static int lastSlot = -1;
    private static String lastItemTranslationKey = "";

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            MinnaNoCraft.incrementClientTicks();
            long currentTick = MinnaNoCraft.getClientTicks();

            // 1. Atualiza rastreador de mouse
            TooltipHoverTracker.tick(currentTick);

            // 2. Lógica da Hotbar (SEEN)
            if (client.player != null) {
                int currentSlot = client.player.getInventory().getSelectedSlot();
                ItemStack mainHandStack = client.player.getMainHandItem();
                String currentKey = mainHandStack.isEmpty() ? "" : mainHandStack.getItem().getDescriptionId();

                if (currentSlot != lastSlot || !currentKey.equals(lastItemTranslationKey)) {
                    lastSlot = currentSlot;
                    lastItemTranslationKey = currentKey;

                    if (!mainHandStack.isEmpty()) {
                        List<String> structure = ItemStructureLoader.getStructures().get(currentKey);

                        if (structure != null) {

                            String targetToken = VocabularyManager.getNextTokenToUpgrade(structure);
                            if (targetToken != null) {
                                VocabularyManager.registerEvent(targetToken, Event.SEEN, currentTick);
                            }
                        }
                    }
                }
            }
            // 3. Auto-save (A cada 5 segundos)
            if (currentTick % 6000 == 0) {
                VocabularyManager.updateProgression();
            }
        });
    }
}