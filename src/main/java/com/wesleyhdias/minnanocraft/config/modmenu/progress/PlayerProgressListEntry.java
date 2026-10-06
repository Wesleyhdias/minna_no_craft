package com.wesleyhdias.minnanocraft.config.modmenu.progress;

import com.wesleyhdias.minnanocraft.config.modmenu.ScrollingTextUtil;
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
        ScrollingTextUtil.renderScrollingText(graphics, mc, this.firstText, x + 6, y + 6, wordMaxWidth, 0xFFFFFFFF);

        // --- 2. Second Column: Translation / Kanji ---
        // Begins immediately at the end boundary of Column 1
        int middleColumnX = x + col1Width;
        int middleMaxWidth = col2Width - 15;
        ScrollingTextUtil.renderScrollingText(graphics, mc, this.middleText, middleColumnX, y + 6, middleMaxWidth, 0xFFFFFF55);

        // --- 3. Third Column: Level & Exposure Statistics ---
        String infoText = String.format("Lv. %d (%.1f)", this.level, this.exposure);
        int textWidth = mc.font.width(infoText);

        // Right-aligned text positioning within entry boundaries
        int rightAlignX = x + this.listWidth - textWidth - 10;
        graphics.text(mc.font, infoText, rightAlignX, y + 6, 0xFFAAAAAA, false);
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