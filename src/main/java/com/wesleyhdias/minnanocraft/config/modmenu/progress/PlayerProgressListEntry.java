package com.wesleyhdias.minnanocraft.config.modmenu.progress;

import com.wesleyhdias.minnanocraft.language.dictionary.Word;
import com.wesleyhdias.minnanocraft.srs.models.WordProgress;

import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;

import org.jspecify.annotations.NonNull;

/**
 * Represents a single row entry within the {@link PlayerProgressListWidget}.
 * Renders word data across three formatted columns (Display Word, Kanji/Translation, Level & Exposure)
 * and applies a marquee scrolling effect for text entries that exceed column width constraints.
 */
public class PlayerProgressListEntry extends ObjectSelectionList.Entry<PlayerProgressListEntry> {

    /** Primary display string rendered in the first column. */
    private final String firstText;

    /** Secondary display string (e.g., Kanji representation) rendered in the second column. */
    private final String middleText;

    /** Reference to the domain word data object. */
    private final Word wordObj;

    /** Reference to the player progress object associated with this word. */
    private final WordProgress progressObj;

    /** Cached script level value extracted from word progress. */
    private final int level;

    /** X coordinate origin for rendering this entry within the list layout. */
    private final int listX;

    /** Total width allocated for the list row entry. */
    private final int listWidth;

    /** Cached total exposure points extracted from word progress. */
    private final double exposure;

    /**
     * Constructs a new progress list entry row.
     *
     * @param firstText   Primary display text for the word entry.
     * @param middleText  Secondary text component (e.g., Kanji or translation).
     * @param wordObj     Associated domain word object.
     * @param progressObj Associated player progression tracking object.
     * @param listX       Left boundary X coordinate for layout alignment.
     * @param listWidth   Total width allocation for row items.
     */
    public PlayerProgressListEntry(String firstText, String middleText, Word wordObj, WordProgress progressObj, int listX, int listWidth) {
        this.firstText = firstText;
        this.middleText = middleText;
        this.wordObj = wordObj;
        this.progressObj = progressObj;
        this.level = progressObj != null ? progressObj.getScriptLevel() : 0;
        this.exposure = progressObj != null ? progressObj.getExposure() : 0.0;
        this.listX = listX;
        this.listWidth = listWidth;
    }

    /**
     * Extracts and processes rendering operations for the individual column entries in this row.
     *
     * @param graphics Context extractor for GUI graphics rendering.
     * @param mouseX   Current mouse cursor X position.
     * @param mouseY   Current mouse cursor Y position.
     * @param hovered  True if the entry row is hovered by the mouse.
     * @param a        Render tick delta time.
     */
    @Override
    public void extractContent(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
        Minecraft mc = Minecraft.getInstance();
        int x = this.listX;
        int y = this.getY();

        // Calculate proportional column widths (Column 1 = 45%, Column 2 = 30%)
        int col1Width = (int) (this.listWidth * 0.45);
        int col2Width = (int) (this.listWidth * 0.30);

        // --- 1. First Column: Display Word ---
        int wordMaxWidth = col1Width - 15; // 15px right margin safety offset
        renderScrollingText(graphics, mc, this.firstText, x + 6, y + 6, wordMaxWidth, 0xFFFFFFFF);

        // --- 2. Second Column: Translation / Kanji ---
        // Begins immediately at the end boundary of Column 1
        int middleColumnX = x + col1Width;
        int middleMaxWidth = col2Width - 15;
        renderScrollingText(graphics, mc, this.middleText, middleColumnX, y + 6, middleMaxWidth, 0xFFFFFF55);

        // --- 3. Third Column: Level & Exposure Statistics ---
        String infoText = String.format("Lv. %d (%.1f)", this.level, this.exposure);
        int textWidth = mc.font.width(infoText);

        // Right-aligned text positioning within entry boundaries
        int rightAlignX = x + this.listWidth - textWidth - 10;
        graphics.text(mc.font, infoText, rightAlignX, y + 6, 0xFFAAAAAA, false);
    }

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
    private void renderScrollingText(GuiGraphicsExtractor graphics, Minecraft mc, String text, int startX, int startY, int maxWidth, int color) {
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
    private static int getScrollOffset(int maxWidth, int textWidth, double timeSec) {
        int overflow = textWidth - maxWidth;

        // Base full-cycle duration in seconds plus additional time scaling based on overflow length
        double cycleDuration = 3.0 + (overflow * 0.05);

        // Mathematical smooth wave oscillating between 0.0 and 1.0 with edge deceleration
        double wave = -Math.sin((Math.PI / 2.0) * Math.cos((Math.PI * 2.0) * timeSec / cycleDuration)) / 2.0 + 0.5;

        // Scale normalized wave output to total overflow pixel offset
        return (int) (wave * overflow);
    }

    /** @return Primary word display string. */
    public String getFirstText() { return this.firstText; }

    /** @return Secondary display text string. */
    public String getMiddleText() { return this.middleText; }

    /** @return Script level value. */
    public int getLevel() { return this.level; }

    /** @return Total exposure point value. */
    public double getExposure() { return this.exposure; }

    /** @return Associated domain {@link Word} instance. */
    public Word getWordObj() { return this.wordObj; }

    /** @return Associated {@link WordProgress} instance. */
    public WordProgress getProgressObj() { return this.progressObj; }

    /**
     * Returns accessible narration text for screen reader accessibility support.
     *
     * @return Formatted narration component.
     */
    @Override
    public @NonNull Component getNarration() {
        return Component.literal(this.firstText + ", Nível " + this.level);
    }
}