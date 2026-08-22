package com.wesleyhdias.minnanocraft.client.tooltip;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class responsible for calculating and managing interactive hitboxes
 * for individual words rendered in custom tooltips.
 */
public class HitboxCalculator {

    /**
     * A record representing the clickable/hoverable rectangular area of a specific vocabulary token.
     *
     * @param token    The original dictionary token ID.
     * @param xStart   The starting X coordinate of the word's hitbox.
     * @param xEnd     The ending X coordinate of the word's hitbox.
     * @param yStart   The starting Y coordinate of the word's hitbox.
     * @param yEnd     The ending Y coordinate of the word's hitbox.
     * @param prevText The previous rendered text (or translation) to show on hover.
     */
    public record TokenHitbox(String token, int xStart, int xEnd, int yStart, int yEnd, String prevText) {}

    /** The global list of currently active hitboxes on the screen. */
    private static final List<TokenHitbox> activeHitboxes = new ArrayList<>();

    /**
     * Retrieves the list of currently active hitboxes.
     *
     * @return A list of {@link TokenHitbox}.
     */
    public static List<TokenHitbox> getActiveHitboxes() {
        return activeHitboxes;
    }

    /**
     * Rebuilds the interactive hitboxes for a given translated item name.
     * This should be called whenever the tooltip is rendered or changes position.
     *
     * @param mc             The current Minecraft client instance.
     * @param translationKey The translation key of the item.
     * @param textX          The starting X coordinate where the text is being drawn.
     * @param textY          The starting Y coordinate where the text is being drawn.
     * @param originalName   The original localized name of the item.
     */
    public static void rebuildHitboxes(Minecraft mc, String translationKey, int textX, int textY, String originalName) {
        activeHitboxes.clear();

        // Retrieve the exact parsed phrase mapped by the Formatter
        List<TooltipFormatter.ParsedWord> parsedWords = TooltipFormatter.parseBuilderText(translationKey, originalName);

        int currentX = textX;
        int lineHeight = mc.font.lineHeight;

        for (int i = 0; i < parsedWords.size(); i++) {
            TooltipFormatter.ParsedWord pw = parsedWords.get(i);
            int wordWidth = mc.font.width(pw.text);

            // Only create a clickable/hoverable hitbox if the word is an interactive dictionary token!
            if (pw.isInteractive) {
                activeHitboxes.add(new TokenHitbox(
                        pw.token,
                        currentX, currentX + wordWidth,
                        textY, textY + lineHeight,
                        pw.prevText
                ));
            }

            // Advance the X coordinate by the width of the word
            currentX += wordWidth;

            if (i < parsedWords.size() - 1) {
                // Advance the X coordinate by the width of a space character
                currentX += mc.font.width(" ");
            }
        }
    }

    /**
     * Checks if the given mouse coordinates intersect with any active hitbox.
     *
     * @param mouseX The current X coordinate of the mouse.
     * @param mouseY The current Y coordinate of the mouse.
     * @return The {@link TokenHitbox} being hovered over, or null if none match.
     */
    public static TokenHitbox getHitboxAt(int mouseX, int mouseY) {
        for (TokenHitbox hb : activeHitboxes) {
            if (mouseX >= hb.xStart() && mouseX <= hb.xEnd() && mouseY >= hb.yStart() && mouseY <= hb.yEnd()) {
                return hb;
            }
        }
        return null;
    }
}