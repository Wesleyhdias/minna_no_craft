package com.wesleyhdias.minnanocraft.utils;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class HitboxCalculator {

    public record TokenHitbox(String token, int xStart, int xEnd, int yStart, int yEnd, String prevText) {}
    private static final List<TokenHitbox> activeHitboxes = new ArrayList<>();

    public static List<TokenHitbox> getActiveHitboxes() {
        return activeHitboxes;
    }

    public static void rebuildHitboxes(Minecraft mc, String translationKey, int textX, int textY, String originalName) {
        activeHitboxes.clear();

        // Pega a frase idêntica mapeada pelo Formatter
        List<TooltipFormatter.ParsedWord> parsedWords = TooltipFormatter.parseBuilderText(translationKey, originalName);

        int currentX = textX;
        int lineHeight = mc.font.lineHeight;

        for (int i = 0; i < parsedWords.size(); i++) {
            TooltipFormatter.ParsedWord pw = parsedWords.get(i);
            int wordWidth = mc.font.width(pw.text);

            // Só cria hitbox de clique/hover se for uma palavra interativa do dicionário!
            if (pw.isInteractive) {
                activeHitboxes.add(new TokenHitbox(
                        pw.tokenId,
                        currentX, currentX + wordWidth,
                        textY, textY + lineHeight,
                        pw.prevText
                ));
            }

            currentX += wordWidth; // Avança o tamanho da palavra
            if (i < parsedWords.size() - 1) {
                currentX += mc.font.width(" "); // Avança o tamanho do espaço
            }
        }
    }

    public static TokenHitbox getHitboxAt(int mouseX, int mouseY) {
        for (TokenHitbox hb : activeHitboxes) {
            if (mouseX >= hb.xStart() && mouseX <= hb.xEnd() && mouseY >= hb.yStart() && mouseY <= hb.yEnd()) {
                return hb;
            }
        }
        return null;
    }
}