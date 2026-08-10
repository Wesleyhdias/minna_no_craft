package com.wesleyhdias.minnanocraft.trackers;

import com.wesleyhdias.minnanocraft.data.loader.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.services.VocabularyManager;
import com.wesleyhdias.minnanocraft.data.models.Event;

import java.util.List;

public class FocusTracker {
    private static String currentLookKey = "";
    private static long lookStartTime = 0;
    private static boolean expAwarded = false;
    private static final long REQUIRED_FOCUS_TIME_MS = 2000;

    public static void update(String targetKey) {
        if (targetKey == null || targetKey.isBlank()) {
            reset();
            return;
        }

        long now = System.currentTimeMillis();

        if (!targetKey.equals(currentLookKey)) {
            currentLookKey = targetKey;
            lookStartTime = now;
            expAwarded = false;
        }

        if (!expAwarded && (now - lookStartTime) >= REQUIRED_FOCUS_TIME_MS) {
            List<String> structure = ItemStructureLoader.getStructures().get(targetKey);
            if (structure != null && !structure.isEmpty()) {
                String targetToken = VocabularyManager.getNextTokenToUpgrade(structure);
                VocabularyManager.registerEvent(targetToken, Event.HUD_LOOK);
            }
            expAwarded = true;
        }
    }

    public static void reset() {
        currentLookKey = "";
        expAwarded = false;
    }
}