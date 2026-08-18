package com.wesleyhdias.minnanocraft.events;

import com.wesleyhdias.minnanocraft.builders.PortugueseItemNameBuilder;
import com.wesleyhdias.minnanocraft.config.ModConfig;
import com.wesleyhdias.minnanocraft.data.loader.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.utils.TranslationModeResolver;
import com.wesleyhdias.minnanocraft.trackers.TooltipHoverTracker;
import com.wesleyhdias.minnanocraft.builders.ItemNameBuilder;
import com.wesleyhdias.minnanocraft.data.models.WordProgress;
import com.wesleyhdias.minnanocraft.services.*;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Event handler responsible for intercepting item tooltips.
 * It modifies the item name dynamically and triggers hover tracking events.
 */
public class TooltipEventHandler {

    // Variables to freeze the text while the mouse is hovering over the item
    private static String currentHoverKey = "";
    private static Component cachedComponent = null;
    private static long lastRenderTime = 0;

    /**
     * Registers the tooltip callback to the Fabric event bus.
     */
    public static void register() {

        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {

            if (!ModConfig.isEnabled()) {
                return;
            }

            // Skips if the stack is empty, has no lines, or has a custom renamed name (e.g., via Anvil)
            if (stack.isEmpty() || lines.isEmpty() || stack.has(DataComponents.CUSTOM_NAME)) return;

            String translationKey = stack.getItem().getDescriptionId();
            long now = System.currentTimeMillis();

            // 1. NOTIFY HOVER TRACKER (To award progression points internally)
            TooltipHoverTracker.onTooltipRendered(translationKey);

            // 2. FREEZE NAME DURING HOVER SESSION
            // If more than 100 ms (~2 ticks) passed without rendering (mouse left) or item changed: recalculate!
            if (now - lastRenderTime > 100 || !translationKey.equals(currentHoverKey)) {
                currentHoverKey = translationKey;
                cachedComponent = null;

                List<String> structure = ItemStructureLoader.getStructures().get(translationKey);
                if (structure != null && !structure.isEmpty()) {
                    Component originalComponent = lines.getFirst();
                    String originalText = originalComponent.getString();
                    String customText;

                    if (TranslationModeResolver.useJapanese(translationKey)) {
                        customText = ItemNameBuilder.build(translationKey);
                    } else {
                        customText = PortugueseItemNameBuilder.build(translationKey, originalText);
                    }

                    if (customText != null) {
                        cachedComponent = Component.literal(customText).withStyle(originalComponent.getStyle());
                    }
                }
            }

            lastRenderTime = now; // Updates the cache clock

            // 3. APPLY FROZEN TEXT (Ignores level changes that might happen during this exact hover session)
            if (cachedComponent != null) {
                lines.set(0, cachedComponent);
            }

            // =========================================================
            // DEBUG MODE (Advanced Tooltips F3 + H)
            // =========================================================
            if (type.isAdvanced()) {
                lines.add(Component.literal(" ")); // Blank line for separation
                lines.add(Component.literal("§e[MinnaNoCraft Debug]"));

                List<String> structure = ItemStructureLoader.getStructures().get(translationKey);

                if (structure != null) {
                    String target = VocabularyManager.getNextTokenToUpgrade(structure);
                    lines.add(Component.literal("§7Priority Target: §f" + (target != null ? target : "None")));

                    for (String token : structure) {
                        WordProgress p = VocabularyManager.getProgress(token);
                        double exp = (p != null) ? p.getExposure() : 0.0;
                        int level = (p != null) ? p.getScriptLevel() : 0;

                        // Shows the word, current level, and XP bar with 2 decimal places
                        lines.add(Component.literal(String.format("§8- %s: Lvl %d (XP: %.2f)", token, level, exp)));
                    }
                } else {
                    lines.add(Component.literal("§cNo structure mapped for this item."));
                }
            }
        });
    }
}