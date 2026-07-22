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

            // 1. Atualiza rastreador de mouse (para saber se tirou o mouse do item)
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
                            for (String token : structure) {
                                VocabularyManager.registerEvent(token, Event.SEEN, currentTick);
                            }
                        }
                    }
                }
            }

            // 3. Auto-save e recálculo do sistema (A cada 10 segundos / 200 ticks)
            if (currentTick % 200 == 0) {
                VocabularyManager.updateProgression(currentTick);
            }
        });
    }
}