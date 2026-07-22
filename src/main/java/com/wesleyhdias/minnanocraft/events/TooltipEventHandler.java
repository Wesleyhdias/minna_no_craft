package com.wesleyhdias.minnanocraft.events;

import com.wesleyhdias.minnanocraft.services.PortugueseItemNameBuilder;
import com.wesleyhdias.minnanocraft.services.TranslationModeResolver;
import com.wesleyhdias.minnanocraft.data.loader.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.services.TooltipHoverTracker;
import com.wesleyhdias.minnanocraft.services.ItemNameBuilder;
import com.wesleyhdias.minnanocraft.MinnaNoCraft;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.network.chat.Component;

import java.util.List;

public class TooltipEventHandler {

    // Variáveis para congelar o texto enquanto o mouse estiver sobre o item
    private static String currentHoverKey = "";
    private static Component cachedComponent = null;
    private static long lastRenderTick = 0;

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (stack.isEmpty() || lines.isEmpty()) return;

            String translationKey = stack.getItem().getDescriptionId();
            long currentTick = MinnaNoCraft.getClientTicks();

            // 1. AVISA O RASTREADOR DE HOVER (Para dar os pontos internamente)
            TooltipHoverTracker.onTooltipRendered(translationKey, currentTick);

            // 2. CONGELA O NOME DURANTE A SESSÃO DE HOVER
            // Se passou mais de 2 ticks sem renderizar (mouse saiu) ou mudou de item: Recalcula!
            if (currentTick - lastRenderTick > 2 || !translationKey.equals(currentHoverKey)) {
                currentHoverKey = translationKey;
                cachedComponent = null;

                List<String> structure = ItemStructureLoader.getStructures().get(translationKey);
                if (structure != null && !structure.isEmpty()) {
                    Component originalComponent = lines.get(0);
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
        });
    }
}