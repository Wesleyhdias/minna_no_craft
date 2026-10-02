package com.wesleyhdias.minnanocraft;

import com.wesleyhdias.minnanocraft.client.tooltip.pinnedTooltip.PinnedTooltipInputHandler;
import com.wesleyhdias.minnanocraft.client.tooltip.pinnedTooltip.PinnedTooltipService;
import com.wesleyhdias.minnanocraft.config.vanilla_injection.LanguageChangeHandler;
import com.wesleyhdias.minnanocraft.config.vanilla_injection.LanguageScreenHandler;
import com.wesleyhdias.minnanocraft.client.lookup.DictionaryLookupOverlayRenderer;
import com.wesleyhdias.minnanocraft.client.tooltip.TooltipEventHandler;
import com.wesleyhdias.minnanocraft.srs.PlayerVocabularyManager;
import com.wesleyhdias.minnanocraft.client.ClientTickHandler;
import com.wesleyhdias.minnanocraft.config.ModConfig;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.api.ClientModInitializer;

/**
 * Client-side initializer for MinnaNoCraft.
 * Handles client startup loading, event registration, and safe shutdown saving.
 */
public class MinnaNoCraftClient implements ClientModInitializer {

    /**
     * Initializes client-side subsystems, registers event listeners,
     * loads saved vocabulary progress, and configures lifecycle handlers.
     */
    @Override
    public void onInitializeClient() {

        LanguageScreenHandler.register();

        // Loads saved vocabulary progress
        PlayerVocabularyManager.getInstance().load();
        MinnaNoCraft.LOGGER.info("MinnaNoCraft (Client) initialized and progress loaded successfully!");

        // Registers modular event handlers
        ClientTickHandler.register();
        TooltipEventHandler.register();

        // Registers the vanilla language change observer
        LanguageChangeHandler.register();

        // Ensures the latest state is written to JSON before the client shuts down
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            PlayerVocabularyManager.getInstance().save();
            MinnaNoCraft.LOGGER.info("MinnaNoCraft (Client) stopping. Progress saved.");
        });

        ScreenEvents.BEFORE_INIT.register((_, screen, _, _) -> {

            // Intercepts the 'mouseClicked' before the screen process it
            ScreenMouseEvents.allowMouseClick(screen).register((_, mouse) -> {
                if (!ModConfig.getConfig().isEnabled()) return true;

                if (DictionaryLookupOverlayRenderer.mouseClicked(mouse.x(), mouse.y(), mouse.button())) {
                    return false;
                }

                boolean isPinned = PinnedTooltipService.isPinned();
                if (PinnedTooltipInputHandler.handleMouseClick(mouse.x(), mouse.y(), mouse.button())) {
                    return false;
                }

                if (isPinned) {
                    PinnedTooltipService.unpin();
                    return false;
                }

                return true;
            });

            // Intercepts the mouseReleased before the screen process it
            ScreenMouseEvents.allowMouseRelease(screen).register((_, _) -> {
                if (!ModConfig.getConfig().isEnabled()) return true;

                return !PinnedTooltipService.isPinned();
            });
        });
    }
}