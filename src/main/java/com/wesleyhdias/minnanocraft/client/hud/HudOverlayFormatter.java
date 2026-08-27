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
 * Factory responsible for assembling the data needed for the HUD Overlay.
 * Separates data logic (text formatting, colors, item resolving) from rendering logic.
 */
public class HudOverlayFormatter {

    // Um "pacote" imutável contendo tudo que o Renderizador precisa para desenhar.
    public record TooltipData(ItemStack icon, List<Component> lines, int titleColor, int borderColor) {}

    public static TooltipData create(String translationKey, Minecraft client) {
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
            } catch (Exception ignored) {}

            // If no secondary lines exist, append a generic category
            if (tooltipLines.size() == 1) {
                tooltipLines.add(Component.literal("§7" + getCategory(translationKey, stack)));
            }
        }

        return new TooltipData(stack, tooltipLines, titleColor, borderColor);
    }

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

    private static String getCategory(String translationKey, ItemStack stack) {
        if (stack.getItem() instanceof BlockItem) return "Block";
        if (translationKey.contains("sword") || translationKey.contains("bow") || (translationKey.contains("axe") && !translationKey.contains("pickaxe"))) return "Combat";
        if (translationKey.contains("pickaxe") || translationKey.contains("shovel") || translationKey.contains("hoe")) return "Tool";
        return "Item";
    }
}
