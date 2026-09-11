package com.wesleyhdias.minnanocraft.client.hud;

import com.wesleyhdias.minnanocraft.language.builder.CurrentLangItemNameBuilder;
import com.wesleyhdias.minnanocraft.language.builder.JapaneseItemNameBuilder;
import com.wesleyhdias.minnanocraft.language.ItemStructureLoader;

import com.wesleyhdias.minnanocraft.language.resolver.TranslationModeResolver;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory responsible for assembling data required by the HUD overlay.
 * <p>
 * Decouples data assembly (text formatting, rarity color calculations, category resolution,
 * and item stack resolution) from the visual rendering pipeline.
 */
public class HudOverlayFormatter {

    /**
     * Immutable data container holding processed rendering information for the HUD tooltip overlay.
     *
     * @param icon        The target {@link ItemStack} icon to display, or empty stack if non-item.
     * @param lines       The list of formatted {@link Component} text lines to render.
     * @param titleColor  The ARGB color integer used for the primary title text.
     * @param borderColor The ARGB color integer used for the tooltip border outline.
     */
    public record TooltipData(ItemStack icon, List<Component> lines, int titleColor, int borderColor) {}

    /**
     * Assembles a {@link TooltipData} object containing formatted text lines, colors,
     * and item representations for a given translation key.
     *
     * @param translationKey The target translation key to resolve data for.
     * @param client         The active {@link Minecraft} client instance.
     * @return A fully populated {@link TooltipData} instance ready for rendering.
     */
    public static TooltipData create(String translationKey, Minecraft client) {
        ItemStack stack = TargetItemResolver.resolve(translationKey);
        String customName = resolveDisplayText(translationKey);

        int titleColor = 0xFFFFFFFF;    // Default white title text
        int borderColor = 0x505000FF;   // Default dark purple border (ARGB)

        // Adjusts title and border color based on item rarity
        if (!stack.isEmpty()) {
            switch (stack.getRarity()) {
                case UNCOMMON -> { titleColor = 0xFFFFFF55; borderColor = 0x50FFFF55; }
                case RARE     -> { titleColor = 0xFF55FFFF; borderColor = 0x5055FFFF; }
                case EPIC     -> { titleColor = 0xFFFF55FF; borderColor = 0x50FF55FF; }
            }
        }

        List<Component> tooltipLines = new ArrayList<>();
        tooltipLines.add(Component.literal(customName));

        if (!stack.isEmpty()) {
            try {
                // Extracts native vanilla tooltip lines (e.g., enchantments, attributes, or lore)
                List<Component> vanillaLines = stack.getTooltipLines(Item.TooltipContext.EMPTY, client.player, TooltipFlag.NORMAL);
                for (int i = 1; i < vanillaLines.size(); i++) {
                    tooltipLines.add(vanillaLines.get(i));
                }
            } catch (Exception ignored) {
                // Safely catches potential exceptions thrown during vanilla tooltip extraction
            }

            // Appends generic category text if no extra tooltip lines were extracted
            if (tooltipLines.size() == 1) {
                tooltipLines.add(Component.literal("§7" + getCategory(translationKey, stack)));
            }
        }

        return new TooltipData(stack, tooltipLines, titleColor, borderColor);
    }

    /**
     * Resolves the custom display name for a translation key based on SRS vocabulary
     * progression and active translation mode rules.
     *
     * @param translationKey The target translation key string.
     * @return The formatted display text string.
     */
    private static String resolveDisplayText(String translationKey) {
        List<String> structure = ItemStructureLoader.getStructures().get(translationKey);
        if (structure != null && !structure.isEmpty()) {
            if (TranslationModeResolver.useJapanese(translationKey)) {
                return JapaneseItemNameBuilder.build(translationKey);
            } else {
                return CurrentLangItemNameBuilder.build(translationKey, Component.translatable(translationKey).getString());
            }
        }
        return Component.translatable(translationKey).getString();
    }

    /**
     * Determines a broad item category label based on item type and translation key keywords.
     *
     * @param translationKey The target translation key string.
     * @param stack          The resolved {@link ItemStack}.
     * @return A localized category label string (e.g., "Block", "Combat", "Tool", or "Item").
     */
    private static String getCategory(String translationKey, ItemStack stack) {
        if (stack.getItem() instanceof BlockItem) {
            return "Block";
        }
        if (translationKey.contains("sword") || translationKey.contains("bow") || (translationKey.contains("axe") && !translationKey.contains("pickaxe"))) {
            return "Combat";
        }
        if (translationKey.contains("pickaxe") || translationKey.contains("shovel") || translationKey.contains("hoe")) {
            return "Tool";
        }
        return "Item";
    }
}