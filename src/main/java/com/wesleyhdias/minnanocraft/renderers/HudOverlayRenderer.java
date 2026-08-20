package com.wesleyhdias.minnanocraft.renderers;

import com.wesleyhdias.minnanocraft.trackers.WorldTargetTracker;
import com.wesleyhdias.minnanocraft.utils.HudOverlayFormatter;
import com.wesleyhdias.minnanocraft.trackers.ExposureTracker;
import com.wesleyhdias.minnanocraft.data.models.Event;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.*;

/**
 * Renderer responsible for drawing the floating HUD overlay at the top of the screen.
 * This overlay appears when the player aims their crosshair at a block or entity in the world,
 * displaying its translated vocabulary name and icon
 */
public class HudOverlayRenderer {

    private static final ExposureTracker hudTracker = new ExposureTracker(2000, Event.HUD_LOOK);

    public static void renderOverlay(GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();

        if (client.player == null || client.options.hideGui || client.screen != null) {
            hudTracker.reset();
            return;
        }

        String targetKey = WorldTargetTracker.getTargetTranslationKey();

        if (targetKey == null || targetKey.isBlank()) {
            hudTracker.reset();
            return;
        }

        hudTracker.update(targetKey);

        // Generate organized data with the formatter and render it
        HudOverlayFormatter.TooltipData data = HudOverlayFormatter.create(targetKey, client);

        renderFloatingBox(graphics, client, data);
    }

    private static void renderFloatingBox(GuiGraphicsExtractor graphics, Minecraft client, HudOverlayFormatter.TooltipData data) {
        // --- MATH & DRAWING ---
        int padding = 6;
        int iconSize = 16;
        int spacing = 4;

        // Calculate the maximum width among all text lines
        int maxTextWidth = 0;
        for (Component line : data.lines()) {
            maxTextWidth = Math.max(maxTextWidth, client.font.width(line));
        }

        // Calculate overall box dimensions
        int boxWidth = padding + iconSize + spacing + maxTextWidth + padding;
        int textHeightTotal = data.lines().size() * 10;
        int boxHeight = Math.max(iconSize, textHeightTotal) + (padding * 2);

        // Center the box horizontally at the top of the screen
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int boxX = (screenWidth - boxWidth) / 2;
        int boxY = 10;

        // Render background and border
        graphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xF0100010);
        graphics.outline(boxX, boxY, boxWidth, boxHeight, data.borderColor());

        // Vertically center the icon and text block relative to each other
        int iconOffsetY = Math.max(0, (textHeightTotal - iconSize) / 2);
        int textOffsetY = Math.max(0, (iconSize - textHeightTotal) / 2);

        // Render the target's representative item icon
        if (!data.icon().isEmpty()) {
            graphics.fakeItem(data.icon(), boxX + padding, boxY + padding + iconOffsetY);
        }

        int textX = boxX + padding + iconSize + spacing;
        int textStartY = boxY + padding + textOffsetY + 1;

        // Render each line of text
        for (int i = 0; i < data.lines().size(); i++) {
            int lineColor = (i == 0) ? data.titleColor() : 0xFFFFFFFF;
            graphics.text(client.font, data.lines().get(i), textX, textStartY + (i * 10), lineColor, true);
        }
    }
}