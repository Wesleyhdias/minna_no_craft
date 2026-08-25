package com.wesleyhdias.minnanocraft.config.modmenu;

import com.wesleyhdias.minnanocraft.config.modmenu.progress.PlayerProgressScreen;
import com.wesleyhdias.minnanocraft.config.data.ModConfig;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Main GUI screen for managing MinnaNoCraft mod configuration settings.
 * Displays a scrollable list of configurable parameters and provides navigation
 * to the vocabulary progress sub-screen.
 */
public class MinnaNoCraftConfigScreen extends Screen {

    /** Reference to the previous screen to return to upon closing. */
    private final Screen parent;

    /** Scrollable widget container holding configuration options. */
    private ConfigListWidget configList;

    /**
     * Constructs the main configuration screen.
     *
     * @param parent The parent screen to display when this screen is closed.
     */
    public MinnaNoCraftConfigScreen(Screen parent) {
        super(Component.literal("MinnaNoCraft - Configurações"));
        this.parent = parent;
    }

    /**
     * Initializes screen components, widget layouts, and interactive buttons.
     */
    @Override
    protected void init() {
        super.init();

        int center = this.width / 2;

        // Initialize scrollable configuration list widget
        int listY = 40;
        int listHeight = this.height - 80;
        this.configList = new ConfigListWidget(this.minecraft, this.width, listHeight, listY, 25);
        this.addRenderableWidget(this.configList);

        // --- Bottom Navigation Buttons ---
        int buttonWidth = 150;
        int buttonHeight = 20;
        int bottomY = this.height - 28;

        // Left button: Opens Dictionary & Vocabulary Progress Screen
        this.addRenderableWidget(
                Button.builder(Component.literal("Dicionário e Progresso"), button ->
                                this.minecraft.setScreen(new PlayerProgressScreen(this)))
                        .bounds(center - buttonWidth - 5, bottomY, buttonWidth, buttonHeight)
                        .build()
        );

        // Right button: Saves current configurations and returns to parent screen
        this.addRenderableWidget(
                Button.builder(Component.translatable("gui.done"), button -> {
                            ModConfig.save();
                            this.minecraft.setScreen(this.parent);
                        })
                        .bounds(center + 5, bottomY, buttonWidth, buttonHeight)
                        .build()
        );
    }

    /**
     * Extracts and processes rendering states for background overlays, widgets, and header text.
     *
     * @param guiGraphics The graphics extractor context.
     * @param mouseX      Current mouse cursor X position.
     * @param mouseY      Current mouse cursor Y position.
     * @param partialTick Render tick delta time.
     */
    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Draw standard semi-transparent dark background
        guiGraphics.fill(0, 0, this.width, this.height, 0x99000000);

        // Render child widgets and list contents
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        // Draw centered title text at top
        guiGraphics.centeredText(this.font, this.title, this.width / 2, 10, 0xFFFFFFFF);
    }

    /**
     * Triggered when the screen is closed (e.g., via ESC key).
     * Ensures all configuration modifications are persisted to disk.
     */
    @Override
    public void onClose() {
        ModConfig.save();
        this.minecraft.setScreen(this.parent);
    }
}