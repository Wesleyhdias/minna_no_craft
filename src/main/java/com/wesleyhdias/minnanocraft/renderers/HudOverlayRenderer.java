package com.wesleyhdias.minnanocraft.renderers;

import com.wesleyhdias.minnanocraft.builders.PortugueseItemNameBuilder;
import com.wesleyhdias.minnanocraft.data.loader.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.utils.TranslationModeResolver;
import com.wesleyhdias.minnanocraft.trackers.WorldTargetTracker;
import com.wesleyhdias.minnanocraft.utils.TargetItemResolver;
import com.wesleyhdias.minnanocraft.builders.ItemNameBuilder;
import com.wesleyhdias.minnanocraft.trackers.ExposureTracker;
import com.wesleyhdias.minnanocraft.data.models.Event;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Renderer responsible for drawing the floating HUD overlay at the top of the screen.
 * This overlay appears when the player aims their crosshair at a block or entity in the world,
 * displaying its translated vocabulary name, icon, and lore.
 */
public class HudOverlayRenderer {

    private static final ExposureTracker hudTracker = new ExposureTracker(2000, Event.HUD_LOOK);

    /**
     * Main entry point for rendering the HUD overlay.
     * Called every frame while the player is in-game.
     *
     * @param graphics The GUI graphics extractor used for drawing.
     */
    public static void renderOverlay(GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();

        // Do not render if the player is not in-world or if the GUI is hidden (F1)
        if (client.player == null || client.options.hideGui) return;

        // Get the translation key of the block or entity currently in the crosshair
        String targetKey = WorldTargetTracker.getTargetTranslationKey();

        if (targetKey == null || targetKey.isBlank()) {
            hudTracker.reset();
            return;
        }

        // Update the tracker to award exposure points if the player stares long enough
        hudTracker.update(targetKey);

        // Render the actual visual box
        renderFloatingBox(graphics, client, targetKey);
    }

    /**
     * Calculates dimensions, formats text, and renders the floating box, item icon,
     * and text lines onto the screen.
     *
     * @param graphics       The GUI graphics extractor used for drawing.
     * @param client         The current Minecraft client instance.
     * @param translationKey The translation key of the target being looked at.
     */
    private static void renderFloatingBox(GuiGraphicsExtractor graphics, Minecraft client, String translationKey) {
        // Resolve an actual item representation for the target to get its icon and rarity
        ItemStack stack = TargetItemResolver.resolve(translationKey);
        String customName = resolveDisplayText(translationKey);

        int titleColor = 0xFFFFFFFF;
        int borderColor = 0x505000FF; // Default dark purple border

        // Adjust border and title color based on item rarity
        if (!stack.isEmpty()) {
            switch (stack.getRarity()) {
                case UNCOMMON -> { titleColor = 0xFFFFFF55; borderColor = 0x50FFFF55; }
                case RARE -> { titleColor = 0xFF55FFFF; borderColor = 0x5055FFFF; }
                case EPIC -> { titleColor = 0xFFFF55FF; borderColor = 0x50FF55FF; }
            }
        }

        List<Component> tooltipLines = new ArrayList<>();
        tooltipLines.add(Component.literal(customName));

        if (!stack.isEmpty()) {
            try {
                // Extract vanilla tooltip lines (like enchantments or lore)
                List<Component> vanillaLines = stack.getTooltipLines(Item.TooltipContext.EMPTY, client.player, TooltipFlag.NORMAL);
                for (int i = 1; i < vanillaLines.size(); i++) {
                    tooltipLines.add(vanillaLines.get(i));
                }
            } catch (Exception ignored) {
                // Silently ignore errors parsing complex items without a level context
            }

            // If no secondary lines exist, append a generic category
            if (tooltipLines.size() == 1) {
                tooltipLines.add(Component.literal("§7" + getCategory(translationKey, stack)));
            }
        }

        // --- MATH & DRAWING ---
        int padding = 6;
        int iconSize = 16;
        int spacing = 4;

        // Calculate the maximum width among all text lines
        int maxTextWidth = 0;
        for (Component line : tooltipLines) {
            maxTextWidth = Math.max(maxTextWidth, client.font.width(line));
        }

        // Calculate overall box dimensions
        int boxWidth = padding + iconSize + spacing + maxTextWidth + padding;
        int textHeightTotal = tooltipLines.size() * 10;
        int boxHeight = Math.max(iconSize, textHeightTotal) + (padding * 2);

        // Center the box horizontally at the top of the screen
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int boxX = (screenWidth - boxWidth) / 2;
        int boxY = 10;

        // Render background and border
        graphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xF0100010);
        graphics.outline(boxX, boxY, boxWidth, boxHeight, borderColor);

        // Vertically center the icon and text block relative to each other
        int iconOffsetY = Math.max(0, (textHeightTotal - iconSize) / 2);
        int textOffsetY = Math.max(0, (iconSize - textHeightTotal) / 2);

        // Render the target's representative item icon
        if (!stack.isEmpty()) {
            graphics.fakeItem(stack, boxX + padding, boxY + padding + iconOffsetY);
        }

        int textX = boxX + padding + iconSize + spacing;
        int textStartY = boxY + padding + textOffsetY + 1;

        // Render each line of text
        for (int i = 0; i < tooltipLines.size(); i++) {
            int lineColor = (i == 0) ? titleColor : 0xFFFFFFFF;
            graphics.text(client.font, tooltipLines.get(i), textX, textStartY + (i * 10), lineColor, true);
        }
    }

    /**
     * Resolves the custom display name using the SRS dictionary builders.
     * Applies Japanese or Portuguese construction based on the player's vocabulary level.
     *
     * @param translationKey The translation key of the target.
     * @return The formatted string to be displayed as the main title.
     */
    private static String resolveDisplayText(String translationKey) {
        List<String> structure = ItemStructureLoader.getStructures().get(translationKey);
        if (structure != null && !structure.isEmpty()) {
            if (TranslationModeResolver.useJapanese(translationKey)) {
                return ItemNameBuilder.build(translationKey);
            } else {
                return PortugueseItemNameBuilder.build(translationKey, Component.translatable(translationKey).getString());
            }
        }
        // Fallback to the vanilla translated name if no custom structure exists
        return Component.translatable(translationKey).getString();
    }

    /**
     * Determines a generic category name based on the item type or translation key.
     * Used as a fallback subtitle when an item has no other tooltip lore.
     *
     * @param translationKey The translation key of the target.
     * @param stack          The item stack representing the target.
     * @return A string representing the category (e.g., "Block", "Combat", "Tool", "Item").
     */
    private static String getCategory(String translationKey, ItemStack stack) {
        if (stack.getItem() instanceof BlockItem) return "Block";
        if (translationKey.contains("sword") || translationKey.contains("bow") || (translationKey.contains("axe") && !translationKey.contains("pickaxe"))) return "Combat";
        if (translationKey.contains("pickaxe") || translationKey.contains("shovel") || translationKey.contains("hoe")) return "Tool";
        return "Item";
    }
}