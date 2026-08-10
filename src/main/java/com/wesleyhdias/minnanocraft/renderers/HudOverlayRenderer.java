package com.wesleyhdias.minnanocraft.renderers;

import com.wesleyhdias.minnanocraft.builders.PortugueseItemNameBuilder;
import com.wesleyhdias.minnanocraft.data.loader.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.utils.TranslationModeResolver;
import com.wesleyhdias.minnanocraft.trackers.WorldTargetTracker;
import com.wesleyhdias.minnanocraft.utils.TargetItemResolver;
import com.wesleyhdias.minnanocraft.builders.ItemNameBuilder;
import com.wesleyhdias.minnanocraft.trackers.FocusTracker;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.*;

import java.util.ArrayList;
import java.util.List;

public class HudOverlayRenderer {

    public static void renderOverlay(GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) return;

        String targetKey = WorldTargetTracker.getTargetTranslationKey();

        if (targetKey == null || targetKey.isBlank()) {
            FocusTracker.reset();
            return;
        }

        FocusTracker.update(targetKey);
        renderFloatingBox(graphics, client, targetKey);
    }

    private static void renderFloatingBox(GuiGraphicsExtractor graphics, Minecraft client, String translationKey) {
        ItemStack stack = TargetItemResolver.resolve(translationKey);
        String customName = resolveDisplayText(translationKey);

        int titleColor = 0xFFFFFFFF;
        int borderColor = 0x505000FF;

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
                List<Component> vanillaLines = stack.getTooltipLines(Item.TooltipContext.EMPTY, client.player, TooltipFlag.NORMAL);
                for (int i = 1; i < vanillaLines.size(); i++) {
                    tooltipLines.add(vanillaLines.get(i));
                }
            } catch (Exception ignored) {}

            if (tooltipLines.size() == 1) {
                tooltipLines.add(Component.literal("§7" + getCategory(translationKey, stack)));
            }
        }

        // --- MATH & DRAWING ---
        int padding = 6;
        int iconSize = 16;
        int spacing = 4;

        int maxTextWidth = 0;
        for (Component line : tooltipLines) {
            maxTextWidth = Math.max(maxTextWidth, client.font.width(line));
        }

        int boxWidth = padding + iconSize + spacing + maxTextWidth + padding;
        int textHeightTotal = tooltipLines.size() * 10;
        int boxHeight = Math.max(iconSize, textHeightTotal) + (padding * 2);

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int boxX = (screenWidth - boxWidth) / 2;
        int boxY = 10;

        graphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xF0100010);
        graphics.outline(boxX, boxY, boxWidth, boxHeight, borderColor);

        int iconOffsetY = Math.max(0, (textHeightTotal - iconSize) / 2);
        int textOffsetY = Math.max(0, (iconSize - textHeightTotal) / 2);

        if (!stack.isEmpty()) {
            graphics.fakeItem(stack, boxX + padding, boxY + padding + iconOffsetY);
        }

        int textX = boxX + padding + iconSize + spacing;
        int textStartY = boxY + padding + textOffsetY + 1;

        for (int i = 0; i < tooltipLines.size(); i++) {
            int lineColor = (i == 0) ? titleColor : 0xFFFFFFFF;
            graphics.text(client.font, tooltipLines.get(i), textX, textStartY + (i * 10), lineColor, true);
        }
    }

    private static String resolveDisplayText(String translationKey) {
        List<String> structure = ItemStructureLoader.getStructures().get(translationKey);
        if (structure != null && !structure.isEmpty()) {
            if (TranslationModeResolver.useJapanese(translationKey)) {
                return ItemNameBuilder.build(translationKey);
            } else {
                return PortugueseItemNameBuilder.build(translationKey, Component.translatable(translationKey).getString());
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