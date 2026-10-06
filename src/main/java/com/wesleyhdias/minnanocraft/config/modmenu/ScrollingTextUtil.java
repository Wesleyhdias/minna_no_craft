package com.wesleyhdias.minnanocraft.config.modmenu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class ScrollingTextUtil {

    /**
     * Renders text with a marquee scrolling effect if string width exceeds allocated column bounds.
     *
     * @param graphics Graphics context extractor.
     * @param mc       Minecraft client instance.
     * @param text     String text to be rendered.
     * @param startX   Starting X coordinate for text rendering.
     * @param startY   Starting Y coordinate for text rendering.
     * @param maxWidth Maximum allowed pixel width before applying marquee scroll.
     * @param color    ARGB color integer.
     */
    public static void renderScrollingText(GuiGraphicsExtractor graphics, Minecraft mc, String text, int startX, int startY, int maxWidth, int color) {
        int textWidth = mc.font.width(text);

        if (textWidth <= maxWidth) {
            // Text fits within allocated column bounds; render normally
            graphics.text(mc.font, text, startX, startY, color, false);
        } else {
            // Text overflows column width; compute scrolling offset and apply scissor clipping mask
            double timeSec = System.currentTimeMillis() / 1000.0;
            int scrollOffset = getScrollOffset(maxWidth, textWidth, timeSec);

            // Apply scissor bounding rectangle to clip overflowing text outside the column bounds
            graphics.enableScissor(startX, startY - 2, startX + maxWidth, startY + 12);
            graphics.text(mc.font, text, startX - scrollOffset, startY, color, false);
            graphics.disableScissor();
        }
    }

    /**
     * Computes the mathematical smooth oscillation offset for the marquee text animation using a sine wave.
     *
     * @param maxWidth  Maximum allowed text rendering width in pixels.
     * @param textWidth Full width of the unclipped text string in pixels.
     * @param timeSec   System elapsed time in seconds.
     * @return Horizontal pixel offset for marquee translation.
     */
    public static int getScrollOffset(int maxWidth, int textWidth, double timeSec) {
        int overflow = textWidth - maxWidth;

        // Base full-cycle duration in seconds plus additional time scaling based on overflow length
        double cycleDuration = 3.0 + (overflow * 0.05);

        // Mathematical smooth wave oscillating between 0.0 and 1.0 with edge deceleration
        double wave = -Math.sin((Math.PI / 2.0) * Math.cos((Math.PI * 2.0) * timeSec / cycleDuration)) / 2.0 + 0.5;

        // Scale normalized wave output to total overflow pixel offset
        return (int) (wave * overflow);
    }
}
