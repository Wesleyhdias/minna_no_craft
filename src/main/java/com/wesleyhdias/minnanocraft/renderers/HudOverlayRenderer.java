package com.wesleyhdias.minnanocraft.renderers;

import com.wesleyhdias.minnanocraft.builders.PortugueseItemNameBuilder;
import com.wesleyhdias.minnanocraft.data.loader.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.utils.TranslationModeResolver;
import com.wesleyhdias.minnanocraft.trackers.WorldTargetTracker;
import com.wesleyhdias.minnanocraft.services.VocabularyManager;
import com.wesleyhdias.minnanocraft.builders.ItemNameBuilder;
import com.wesleyhdias.minnanocraft.data.models.Event;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.*;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class HudOverlayRenderer {

    // Time control variables for the HUD_LOOK event
    private static String currentLookKey = "";
    private static long lookStartTime = 0;
    private static boolean expAwarded = false;
    private static final long REQUIRED_FOCUS_TIME_MS = 2000; // 2 seconds


    private static void resetFocusTracker() {
        currentLookKey = "";
        expAwarded = false;
    }

    // 1. ORCHESTRATOR: Controls the rendering flow
    public static void renderOverlay(GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) return;

        String targetKey = WorldTargetTracker.getTargetTranslationKey();

        if (targetKey == null || targetKey.isBlank()) {
            resetFocusTracker();
            return;
        }

        handleFocusProgression(targetKey);
        renderFloatingBox(graphics, client, targetKey);
    }

    // 2. PROGRESSION LOGIC: Handles the timer and XP awarding
    private static void handleFocusProgression(String targetKey) {
        long now = System.currentTimeMillis();

        // If the target changed, reset the timer
        if (!targetKey.equals(currentLookKey)) {
            currentLookKey = targetKey;
            lookStartTime = now;
            expAwarded = false;
        }

        // If the player looked long enough and XP wasn't awarded yet
        if (!expAwarded && (now - lookStartTime) >= REQUIRED_FOCUS_TIME_MS) {
            List<String> structure = ItemStructureLoader.getStructures().get(targetKey);

            if (structure != null && !structure.isEmpty()) {
                String targetToken = VocabularyManager.getNextTokenToUpgrade(structure);
                VocabularyManager.registerEvent(targetToken, Event.HUD_LOOK);
            }

            expAwarded = true;
        }
    }

    // 3. ITEM LOGIC: Dynamic resolution (Base Item -> Fallback to Spawn Egg)
    private static ItemStack getTargetItemStack(String translationKey) {
        if (translationKey == null || translationKey.isBlank()) return ItemStack.EMPTY;

        try {
            // Se NÃO for uma entidade (ex: bloco ou item normal), resolve direto e sai
            if (!translationKey.startsWith("entity.minecraft.")) {
                return resolveItem(translationKey);
            }

            // É uma entidade. Extraímos o nome limpo (ex: "armor_stand", "rabbit", "zombie")
            String entityName = translationKey.replace("entity.minecraft.", "");

            // 1. EXCEÇÕES INEVITÁVEIS (Bosses e animais que dropam carne com o mesmo nome deles)
            switch (entityName) {
                case "ender_dragon" -> { return resolveItem("block.minecraft.dragon_head"); }
                case "wither" -> { return resolveItem("block.minecraft.wither_skeleton_skull"); }
                case "chicken", "rabbit", "salmon", "cod", "pufferfish", "tropical_fish" -> {
                    return resolveItem("item.minecraft." + entityName + "_spawn_egg");
                }
            }

            // 2. TENTATIVA PRIMÁRIA: Tenta achar um item com o nome exato (Resolve Armor Stand, Item Frame, Barcos, etc)
            ItemStack baseItem = resolveItem("item.minecraft." + entityName);
            if (!baseItem.isEmpty()) {
                return baseItem;
            }

            // 3. TENTATIVA SECUNDÁRIA: Falhou? Então tenta achar o Ovo de Spawn (Resolve Zumbi, Creeper, Porco, etc)
            ItemStack spawnEgg = resolveItem("item.minecraft." + entityName + "_spawn_egg");
            if (!spawnEgg.isEmpty()) {
                return spawnEgg;
            }

        } catch (Exception e) {
            // Silently ignore failures
        }
        return ItemStack.EMPTY;
    }


    // 4. TEXT LOGIC: Resolves the appropriate display text (Japanese, Portuguese, or Native)
    private static String resolveDisplayText(String translationKey) {
        List<String> structure = ItemStructureLoader.getStructures().get(translationKey);
        String displayText;

        if (structure != null && !structure.isEmpty()) {
            if (TranslationModeResolver.useJapanese(translationKey)) {
                displayText = ItemNameBuilder.build(translationKey);
            } else {
                displayText = PortugueseItemNameBuilder.build(translationKey, Component.translatable(translationKey).getString());
            }
        } else {
            displayText = Component.translatable(translationKey).getString();
        }
        return (displayText == null || displayText.isBlank()) ? translationKey : displayText;
    }

    // 5. DRAWING LOGIC: Renders the box, icon, stacked text, rarities, and centers dynamically
    private static void renderFloatingBox(GuiGraphicsExtractor graphics, Minecraft client, String translationKey) {
        ItemStack stack = getTargetItemStack(translationKey);
        String customName = resolveDisplayText(translationKey);

        // --- RARITY COLORS ---
        int titleColor = 0xFFFFFFFF; // Default White (COMMON)
        int borderColor = 0x505000FF; // Default Purple

        if (!stack.isEmpty()) {
            Rarity rarity = stack.getRarity();
            switch (rarity) {
                case UNCOMMON -> { titleColor = 0xFFFFFF55; borderColor = 0x50FFFF55; } // Yellow
                case RARE -> { titleColor = 0xFF55FFFF; borderColor = 0x5055FFFF; } // Aqua
                case EPIC -> { titleColor = 0xFFFF55FF; borderColor = 0x50FF55FF; } // Light Purple
            }
        }

        // Prepare the list of text lines to display
        List<Component> tooltipLines = new ArrayList<>();
        tooltipLines.add(Component.literal(customName)); // Line 1: Translated/Japanese name

        if (!stack.isEmpty()) {
            try {
                List<Component> vanillaLines = stack.getTooltipLines(Item.TooltipContext.EMPTY, client.player, TooltipFlag.NORMAL);

                for (int i = 1; i < vanillaLines.size(); i++) {
                    tooltipLines.add(vanillaLines.get(i));
                }
            } catch (Exception e) {
                // Fallback silently
            }

            // --- CUSTOM CATEGORY FALLBACK ---
            // If the item has no extra vanilla tooltip lines, we categorize it safely
            if (tooltipLines.size() == 1) {
                String category = getCategory(translationKey, stack);

                // Add the category in gray color (using Minecraft's native §7 color code)
                tooltipLines.add(Component.literal("§7" + category));
            }
        }

        // --- DYNAMIC BOX MATH ---
        int padding = 6;
        int iconSize = 16;
        int spacing = 4; // Space between icon and text

        int maxTextWidth = 0;
        for (Component line : tooltipLines) {
            int w = client.font.width(line);
            if (w > maxTextWidth) maxTextWidth = w;
        }

        int boxWidth = padding + iconSize + spacing + maxTextWidth + padding;
        int textHeightTotal = tooltipLines.size() * 10;

        // Height: Box wraps the tallest element (Icon vs Text block)
        int boxHeight = Math.max(iconSize, textHeightTotal) + (padding * 2);

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int boxX = (screenWidth - boxWidth) / 2;
        int boxY = 10;

        // --- RENDERING BACKGROUND & BORDER ---
        graphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xF0100010);
        graphics.outline(boxX, boxY, boxWidth, boxHeight, borderColor);

        // --- VERTICAL CENTERING LOGIC ---
        int iconOffsetY = Math.max(0, (textHeightTotal - iconSize) / 2);
        int textOffsetY = Math.max(0, (iconSize - textHeightTotal) / 2);

        // Draw the item icon (Centered vertically)
        if (!stack.isEmpty()) {
            graphics.fakeItem(stack, boxX + padding, boxY + padding + iconOffsetY);
        }

        // Draw the stacked text lines (Centered vertically)
        int textX = boxX + padding + iconSize + spacing;
        int textStartY = boxY + padding + textOffsetY + 1; // +1 for visual font alignment

        for (int i = 0; i < tooltipLines.size(); i++) {
            int lineColor = (i == 0) ? titleColor : 0xFFFFFFFF;
            graphics.text(client.font, tooltipLines.get(i), textX, textStartY + (i * 10), lineColor, true);
        }
    }

    // HELPER: Faz a busca real no motor do jogo para evitar repetição de código
    private static ItemStack resolveItem(String fullKey) {
        String[] parts = fullKey.split("\\.");
        if (parts.length >= 3) {
            String namespace = parts[1]; // Ex: minecraft
            String path = parts[2];      // Ex: zombie_spawn_egg

            Identifier id = Identifier.tryParse(namespace + ":" + path);
            if (id != null) {
                var optionalHolder = BuiltInRegistries.ITEM.get(id);

                if (optionalHolder.isPresent()) {
                    Item item = optionalHolder.get().value();
                    if (item != Items.AIR) {
                        return new ItemStack(item);
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }


    private static @NonNull String getCategory(String translationKey, ItemStack stack) {
        Item item = stack.getItem();
        String category = "Item"; // Default

        // Safe check using BlockItem (if it exists) or key patterns
        if (item instanceof BlockItem) {
            category = "Block";
        } else if (translationKey.contains("sword") || translationKey.contains("bow") || translationKey.contains("axe") && !translationKey.contains("pickaxe")) {
            category = "Combat";
        } else if (translationKey.contains("pickaxe") || translationKey.contains("shovel") || translationKey.contains("hoe")) {
            category = "Tool";
        }
        return category;
    }
}