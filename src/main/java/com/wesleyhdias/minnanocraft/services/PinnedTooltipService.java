package com.wesleyhdias.minnanocraft.services;

import net.minecraft.world.item.ItemStack;

public class PinnedTooltipService {

    private static boolean pinned = false;
    private static ItemStack pinnedStack = null;
    private static int pinMouseX = 0;
    private static int pinMouseY = 0;

    private static int textX = 0;
    private static int textY = 0;

    private static String currentHoveredToken = null;
    private static long hoverStartTime = 0;
    private static boolean hoverPunished = false;

    public static boolean isPinned() {
        return pinned;
    }

    public static ItemStack getPinnedStack() {
        return pinnedStack;
    }

    public static int getPinMouseX() {
        return pinMouseX;
    }

    public static int getPinMouseY() {
        return pinMouseY;
    }

    public static int getTextX() {
        return textX;
    }

    public static int getTextY() {
        return textY;
    }

    public static void setTextPosition(int x, int y) {
        textX = x;
        textY = y;
    }

    public static boolean togglePin(ItemStack stack, int mouseX, int mouseY) {
        if (pinned) {
            unpin();
            return true;
        } else if (stack != null && !stack.isEmpty()) {
            pinnedStack = stack;
            pinMouseX = mouseX;
            pinMouseY = mouseY;
            pinned = true;
            return true;
        }
        return false;
    }

    public static void unpin() {
        pinned = false;
        pinnedStack = null;
        resetHoverState();
    }

    public static String getCurrentHoveredToken() {
        return currentHoveredToken;
    }

    public static long getHoverStartTime() {
        return hoverStartTime;
    }

    public static boolean isHoverPunished() {
        return hoverPunished;
    }

    public static void updateHoverState(String token, long startTime, boolean punished) {
        currentHoveredToken = token;
        hoverStartTime = startTime;
        hoverPunished = punished;
    }

    public static void resetHoverState() {
        currentHoveredToken = null;
        hoverStartTime = 0;
        hoverPunished = false;
    }
}