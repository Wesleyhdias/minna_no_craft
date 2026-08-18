package com.wesleyhdias.minnanocraft.events;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.world.item.ItemStack;

import com.wesleyhdias.minnanocraft.data.loader.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.trackers.TooltipHoverTracker;
import com.wesleyhdias.minnanocraft.services.VocabularyManager;
import com.wesleyhdias.minnanocraft.data.models.Event;

import java.util.List;

/**
 * Handles client-side tick events for constant monitoring.
 * Responsible for hover un-tracking, hotbar item SEEN events, and auto-saving.
 */
public class ClientTickHandler {

    private static int lastSlot = -1;
    private static String lastItemTranslationKey = "";

    // Auto-save timer variables
    private static long lastSaveTime = System.currentTimeMillis();
    private static final long SAVE_INTERVAL_MS = 300000; // 5 minutes in milliseconds

    /**
     * Registers the tick handler to the Fabric event bus.
     */
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            // 1. Update Hover Tracker (checks if mouse left an item)
            TooltipHoverTracker.tick();

            // 2. Hotbar Logic (SEEN Event)
            if (client.player != null) {
                int currentSlot = client.player.getInventory().getSelectedSlot();
                ItemStack mainHandStack = client.player.getMainHandItem();
                String currentKey = mainHandStack.isEmpty() ? "" : mainHandStack.getItem().getDescriptionId();

                // If the player changed the selected slot OR the item in hand changed
                if (currentSlot != lastSlot || !currentKey.equals(lastItemTranslationKey)) {
                    lastSlot = currentSlot;
                    lastItemTranslationKey = currentKey;

                    if (!mainHandStack.isEmpty()) {
                        List<String> structure = ItemStructureLoader.getStructures().get(currentKey);

                        if (structure != null) {
                            String targetToken = VocabularyManager.getNextTokenToUpgrade(structure);
                            if (targetToken != null) {
                                // Awards the SEEN event for bringing a new item to the main hand
                                VocabularyManager.registerEvent(targetToken, Event.SEEN);
                            }
                        }
                    }
                }
            }

            // 3. Real-Time Auto-Save
            long now = System.currentTimeMillis();
            if (now - lastSaveTime >= SAVE_INTERVAL_MS) {
                VocabularyManager.updateProgression(); // Persists data to disk
                lastSaveTime = now;
            }
        });
    }
}