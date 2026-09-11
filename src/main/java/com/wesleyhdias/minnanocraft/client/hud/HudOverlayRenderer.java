package com.wesleyhdias.minnanocraft.client.hud;

import com.wesleyhdias.minnanocraft.srs.models.ExpEvents;
import com.wesleyhdias.minnanocraft.srs.ExposureTracker;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;

/**
 * Renderer responsible for drawing the floating HUD overlay at the top of the screen.
 * <p>
 * Displays the localized/SRS target vocabulary name, icon, and secondary details
 * when the player targets a block or entity in the world, while updating continuous exposure tracking.
 */
public class HudOverlayRenderer {

    /** Exposure tracker requiring 2 seconds of continuous targeting to award HUD_LOOK experience. */
    private static final ExposureTracker hudTracker = new ExposureTracker(2000, ExpEvents.HUD_LOOK);

    /**
     * Renders the floating HUD overlay box if the player is targeting a valid world element
     * and in-game conditions allow GUI rendering.
     *
     * @param graphics The {@link GuiGraphicsExtractor} instance used for 2D rendering.
     */
    public static void renderOverlay(GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();

        // Cancels tracking and skips rendering if GUI is hidden, paused, or a screen is open
        if (client.player == null || client.options.hideGui || client.screen != null) {
            hudTracker.reset();
            return;
        }

        String targetKey = WorldTargetTracker.getTargetTranslationKey();

        // Skips rendering if no valid target is currently under the crosshair
        if (targetKey == null || targetKey.isBlank()) {
            hudTracker.reset();
            return;
        }

        hudTracker.update(targetKey);

        // Formats overlay text, colors, and item representation
        HudOverlayFormatter.TooltipData data = HudOverlayFormatter.create(targetKey, client);

        renderFloatingBox(graphics, client, data);
    }

    /**
     * Calculates layout dimensions and renders the floating box, icon, background, border, and text lines.
     *
     * @param graphics The {@link GuiGraphicsExtractor} instance used for 2D rendering.
     * @param client   The active {@link Minecraft} client instance.
     * @param data     The formatted {@link HudOverlayFormatter.TooltipData} containing content and style info.
     */
    private static void renderFloatingBox(GuiGraphicsExtractor graphics, Minecraft client, HudOverlayFormatter.TooltipData data) {
        int padding = 6;
        int iconSize = 16;
        int spacing = 4;

        // Calculates the maximum width across all text lines
        int maxTextWidth = 0;
        for (Component line : data.lines()) {
            maxTextWidth = Math.max(maxTextWidth, client.font.width(line));
        }

        // Calculates overall box dimensions
        int boxWidth = padding + iconSize + spacing + maxTextWidth + padding;
        int textHeightTotal = data.lines().size() * 10;
        int boxHeight = Math.max(iconSize, textHeightTotal) + (padding * 2);

        // Centers the box horizontally near the top of the screen
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int boxX = (screenWidth - boxWidth) / 2;
        int boxY = 10;

        // Renders semi-transparent dark background and custom border
        graphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xF0100010);
        graphics.outline(boxX, boxY, boxWidth, boxHeight, data.borderColor());

        // Calculates vertical alignment offsets for icon and text block
        int iconOffsetY = Math.max(0, (textHeightTotal - iconSize) / 2);
        int textOffsetY = Math.max(0, (iconSize - textHeightTotal) / 2);

        // Renders the representative item stack icon
        if (!data.icon().isEmpty()) {
            graphics.fakeItem(data.icon(), boxX + padding, boxY + padding + iconOffsetY);
        }

        int textX = boxX + padding + iconSize + spacing;
        int textStartY = boxY + padding + textOffsetY + 1;

        // Renders each text line with primary/secondary coloring
        for (int i = 0; i < data.lines().size(); i++) {
            int lineColor = (i == 0) ? data.titleColor() : 0xFFFFFFFF;
            graphics.text(client.font, data.lines().get(i), textX, textStartY + (i * 10), lineColor, true);
        }
    }
}