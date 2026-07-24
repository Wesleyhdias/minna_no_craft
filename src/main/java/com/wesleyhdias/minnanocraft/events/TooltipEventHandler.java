package com.wesleyhdias.minnanocraft.events;

import com.wesleyhdias.minnanocraft.data.models.WordProgress;
import com.wesleyhdias.minnanocraft.services.*;
import com.wesleyhdias.minnanocraft.data.loader.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.MinnaNoCraft;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;

import java.util.List;

public class TooltipEventHandler {

    // Variáveis para congelar o texto enquanto o mouse estiver sobre o item
    private static String currentHoverKey = "";
    private static Component cachedComponent = null;
    private static long lastRenderTick = 0;

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (stack.isEmpty() || lines.isEmpty() || stack.has(DataComponents.CUSTOM_NAME)) return;

            String translationKey = stack.getItem().getDescriptionId();
            long currentTick = MinnaNoCraft.getClientTicks();

            // 1. AVISA O RASTREADOR DE HOVER (Para dar os pontos internamente)
            TooltipHoverTracker.onTooltipRendered(translationKey, currentTick);

            // 2. CONGELA O NOME DURANTE A SESSÃO DE HOVER
            // Se passou mais de 2 ticks sem renderizar (mouse saiu) ou mudou de item: recalcula!
            if (currentTick - lastRenderTick > 2 || !translationKey.equals(currentHoverKey)) {
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

            lastRenderTick = currentTick; // Atualiza o relógio do cache

            // 3. APLICA O TEXTO CONGELADO (Ignora mudanças de nível que ocorrerem agora)
            if (cachedComponent != null) {
                lines.set(0, cachedComponent);
            }

            if (type.isAdvanced()) {
                lines.add(Component.literal(" ")); // Linha em branco para separar
                lines.add(Component.literal("§e[MinnaNoCraft Debug]"));

                List<String> structure = ItemStructureLoader.getStructures().get(translationKey);

                String target = VocabularyManager.getNextTokenToUpgrade(structure);
                lines.add(Component.literal("§7Alvo da Fila: §f" + (target != null ? target : "Nenhum")));

                for (String token : structure) {
                    WordProgress p = VocabularyManager.getProgress(token);
                    double exp = (p != null) ? p.getExposure() : 0.0;
                    int level = (p != null) ? p.getScriptLevel() : 0;

                    // Mostra a palavra, o nível atual e a barra de XP com 2 casas decimais
                    lines.add(Component.literal(String.format("§8- %s: Lvl %d (XP: %.2f)", token, level, exp)));
                }
            }
        });
    }
}