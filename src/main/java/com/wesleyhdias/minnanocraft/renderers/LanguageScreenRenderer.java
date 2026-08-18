package com.wesleyhdias.minnanocraft.renderers;

import com.wesleyhdias.minnanocraft.config.ModConfig;

import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.WeakHashMap;

/**
 * Handles the visual injection of custom components (such as toggle buttons and labels)
 * into the native Minecraft language selection screen.
 */
public class LanguageScreenRenderer {

    /** Cache to keep track of modified Y coordinates for the search box, isolated to the UI layer. */
    private static final WeakHashMap<EditBox, Integer> MODIFIED_Y_CACHE = new WeakHashMap<>();

    /**
     * Injects custom layout widgets and handles alignment relative to the search bar
     * on the language selection screen.
     *
     * @param client      the Minecraft client instance
     * @param screen      the current screen instance being initialized
     * @param scaledWidth the scaled width of the window
     */
    public static void inject(Minecraft client, Screen screen, int scaledWidth) {

        EditBox searchBox = null;
        AbstractSelectionList<?> languageList = null;

        // 1. Cleans up old custom widgets (including the "INCOMPATIBLE" label)
        Screens.getWidgets(screen).removeIf(widget -> {
            String txt = widget.getMessage().getString();
            return txt.contains("MinnaNoCraft:") || txt.equals("ENABLED") || txt.equals("DISABLED") || txt.equals("INCOMPATIBLE");
        });

        // 2. Finds the native search bar and selection list components
        for (AbstractWidget widget : Screens.getWidgets(screen)) {
            if (widget instanceof EditBox) {
                searchBox = (EditBox) widget;
            } else if (widget instanceof AbstractSelectionList) {
                languageList = (AbstractSelectionList<?>) widget;
            }
        }

        if (searchBox != null) {

            // --- LANGUAGE VALIDATION LOGIC ---
            String currentLanguage = client.options.languageCode;
            boolean isSupported = ModConfig.getSupportedLanguages().contains(currentLanguage);

            // Forces the config to turn off if the active language is not supported
            if (!isSupported) {
                ModConfig.setEnabled(false);
            }
            // ---------------------------------

            // 3. LAYOUT RESET DETECTION TRICK
            Integer expectedY = MODIFIED_Y_CACHE.get(searchBox);

            if (expectedY == null || searchBox.getY() != expectedY) {
                searchBox.setY(searchBox.getY() + 4);

                if (languageList != null) {
                    int extraSpace = 8;
                    languageList.setY(languageList.getY() + extraSpace);
                    languageList.setHeight(languageList.getHeight() - extraSpace);
                }

                MODIFIED_Y_CACHE.put(searchBox, searchBox.getY());
            }

            // 4. CENTER AXIS CALCULATION AND ALIGNMENT
            int searchCenterY = searchBox.getY() + (searchBox.getHeight() / 2);
            int center = scaledWidth / 2;
            int leftEdge = center - 155;
            int rightEdge = center + 155;

            // Button Construction
            int buttonWidth = 65;
            int buttonHeight = 20;
            int buttonX = rightEdge - buttonWidth;
            int buttonY = searchCenterY - (buttonHeight / 2);

            Button modToggleButton = Button.builder(getButtonStateText(isSupported), button -> {
                        ModConfig.toggle();
                        button.setMessage(getButtonStateText(isSupported));

                        Minecraft clientInstance = Minecraft.getInstance();
                        if (clientInstance.player != null) {
                            // Forces packet resync by broadcasting container menu changes
                            clientInstance.player.containerMenu.broadcastChanges();
                        }
                    })
                    .bounds(buttonX, buttonY, buttonWidth, buttonHeight)
                    .build();

            // VISUAL LOCK: Disables button interaction if the language is unsupported
            if (!isSupported) {
                modToggleButton.active = false;
            }

            // Construction of the "MinnaNoCraft: " label
            Component labelText = Component.literal("MinnaNoCraft: ");
            int labelWidth = client.font.width(labelText);
            int labelX = buttonX - labelWidth - 2;
            int labelY = searchCenterY - (client.font.lineHeight / 2);

            StringWidget modLabel = new StringWidget(labelX, labelY, labelWidth, client.font.lineHeight, labelText, client.font);

            // Adjusts the horizontal width of the native search bar to fit the new elements
            int searchWidth = labelX - leftEdge - 10;
            searchBox.setX(leftEdge);
            searchBox.setWidth(searchWidth);

            // Adds custom components to the screen widget list
            Screens.getWidgets(screen).add(modLabel);
            Screens.getWidgets(screen).add(modToggleButton);
        }
    }

    /**
     * Resolves the button text and color style based on the configuration and language support status.
     *
     * @param isSupported whether the current language is supported by the mod
     * @return the formatted Component label for the button
     */
    private static Component getButtonStateText(boolean isSupported) {
        if (!isSupported) {
            return Component.literal("INCOMPATIBLE").withStyle(ChatFormatting.GRAY);
        }

        if (ModConfig.isEnabled()) {
            return Component.literal("ENABLED").withStyle(ChatFormatting.GREEN);
        } else {
            return Component.literal("DISABLED").withStyle(ChatFormatting.RED);
        }
    }
}