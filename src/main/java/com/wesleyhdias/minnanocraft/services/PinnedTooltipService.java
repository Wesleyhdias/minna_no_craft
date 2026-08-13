package com.wesleyhdias.minnanocraft.services;

import net.minecraft.world.item.ItemStack;

/**
 * Service class responsible for managing the pinned tooltip state.
 * Stores the pinned item stack, mouse positioning coordinates, text alignment data,
 * and tracks word hover state and duration for SRS vocabulary progression.
 */
public class PinnedTooltipService {

    /** The item stack currently pinned on screen. */
    private static ItemStack pinnedStack = null;

    /** Flag indicating whether a tooltip is currently pinned. */
    private static boolean pinned = false;

    /** The X coordinate of the mouse cursor when the tooltip was pinned. */
    private static int pinMouseX = 0;

    /** The Y coordinate of the mouse cursor when the tooltip was pinned. */
    private static int pinMouseY = 0;

    /** The rendered starting X coordinate of the primary title text. */
    private static int textX = 0;

    /** The rendered starting Y coordinate of the primary title text. */
    private static int textY = 0;

    /** The ID of the token currently being hovered over in the pinned tooltip. */
    private static String currentHoveredToken = null;

    /** Flag indicating whether the hover state resulted in a penalty/punishment event. */
    private static boolean hoverPunished = false;

    /** Timestamp (in milliseconds) when the player started hovering over the current token. */
    private static long hoverStartTime = 0;

    /**
     * Checks if a tooltip is currently pinned on screen.
     *
     * @return {@code true} if pinned; {@code false} otherwise.
     */
    public static boolean isPinned() {
        return pinned;
    }

    /**
     * Retrieves the item stack that is currently pinned.
     *
     * @return The pinned {@link ItemStack}, or {@code null} if no item is pinned.
     */
    public static ItemStack getPinnedStack() {
        return pinnedStack;
    }

    /**
     * Gets the mouse X position at the moment the tooltip was pinned.
     *
     * @return The X coordinate in pixels.
     */
    public static int getPinMouseX() {
        return pinMouseX;
    }

    /**
     * Gets the mouse Y position at the moment the tooltip was pinned.
     *
     * @return The Y coordinate in pixels.
     */
    public static int getPinMouseY() {
        return pinMouseY;
    }

    /**
     * Gets the stored X position where the title text is drawn.
     *
     * @return The X coordinate in pixels.
     */
    public static int getTextX() {
        return textX;
    }

    /**
     * Gets the stored Y position where the title text is drawn.
     *
     * @return The Y coordinate in pixels.
     */
    public static int getTextY() {
        return textY;
    }

    /**
     * Sets the screen position coordinates for the rendered text block.
     *
     * @param x The rendered X coordinate.
     * @param y The rendered Y coordinate.
     */
    public static void setTextPosition(int x, int y) {
        textX = x;
        textY = y;
    }

    /**
     * Toggles the pinned state of a tooltip.
     * Unpins if already pinned, or pins a copy of the given item stack at the mouse coordinates.
     *
     * @param stack  The {@link ItemStack} to pin.
     * @param mouseX The current X coordinate of the mouse cursor.
     * @param mouseY The current Y coordinate of the mouse cursor.
     * @return {@code true} if the pinned state changed or an item was successfully pinned/unpinned.
     */
    public static boolean togglePin(ItemStack stack, int mouseX, int mouseY) {
        if (pinned) {
            unpin();
            return true;
        } else if (stack != null && !stack.isEmpty()) {
            pinnedStack = stack.copy();
            pinMouseX = mouseX;
            pinMouseY = mouseY;
            pinned = true;
            return true;
        }
        return false;
    }

    /**
     * Clears the pinned item state and resets all hover duration tracking.
     */
    public static void unpin() {
        pinned = false;
        pinnedStack = null;
        resetHoverState();
    }

    /**
     * Gets the token ID currently being hovered over in the pinned tooltip.
     *
     * @return The token ID string, or {@code null} if no token is being hovered.
     */
    public static String getCurrentHoveredToken() {
        return currentHoveredToken;
    }

    /**
     * Gets the timestamp when hovering began over the current token.
     *
     * @return Timestamp in milliseconds.
     */
    public static long getHoverStartTime() {
        return hoverStartTime;
    }

    /**
     * Checks if the active hover session triggered a punishment event.
     *
     * @return {@code true} if punished; {@code false} otherwise.
     */
    public static boolean isHoverPunished() {
        return hoverPunished;
    }

    /**
     * Updates the current hover state with token information and timestamps.
     *
     * @param token     The dictionary token ID being hovered.
     * @param startTime The start timestamp in milliseconds.
     * @param punished  Whether a penalty event was registered.
     */
    public static void updateHoverState(String token, long startTime, boolean punished) {
        currentHoveredToken = token;
        hoverStartTime = startTime;
        hoverPunished = punished;
    }

    /**
     * Resets the active hover tracking fields to default/null state.
     */
    public static void resetHoverState() {
        currentHoveredToken = null;
        hoverStartTime = 0;
        hoverPunished = false;
    }
}