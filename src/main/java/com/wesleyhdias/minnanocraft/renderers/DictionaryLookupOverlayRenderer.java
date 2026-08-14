package com.wesleyhdias.minnanocraft.renderers;

import com.wesleyhdias.minnanocraft.services.DictionaryLookupService;
import com.wesleyhdias.minnanocraft.data.models.Word;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class DictionaryLookupOverlayRenderer {

    /**
     * Renders the dictionary lookup UI onto the extracted graphics context.
     *
     * @param graphics    The GUI graphics extractor instance provided by ScreenMixin.
     */
    public static void render(GuiGraphicsExtractor graphics) {
        if (!DictionaryLookupService.isOpen()) return;

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // 1. Dimensões do Card Central
        int width = 240;
        int height = 135;
        int x = (screenWidth - width) / 2;
        int y = (screenHeight - height) / 2;

        // 2. Fundo e Bordas do Modal
        graphics.fill(x, y, x + width, y + height, 0xF0121212); // Fundo escuro semissólido

        int borderColor = 0xFF444444;
        graphics.fill(x - 1, y - 1, x + width + 1, y, borderColor);          // Topo
        graphics.fill(x - 1, y + height, x + width + 1, y + height + 1, borderColor); // Base
        graphics.fill(x - 1, y, x, y + height, borderColor);                // Esquerda
        graphics.fill(x + width, y, x + width + 1, y + height, borderColor); // Direita

        // 3. Cabeçalho
        graphics.text(mc.font, Component.literal("MINNA NO CRAFT"), x + 12, y + 10, 0xFFFFAA00, true);

        // Linha divisória superior
        graphics.fill(x + 10, y + 22, x + width - 10, y + 23, 0xFF333333);

        Word word = DictionaryLookupService.getCurrentWord();

        if (word != null) {
            // 1. Extração dos campos da classe Word
            String kanji = word.kanji();
            String hiragana = word.hiragana();
            String romaji = word.romaji();
            String portuguese = String.valueOf(word.translations());

            // 2. Lógica de Fallback para o Título Principal
            // Se não houver Kanji, usamos o Hiragana/Katakana como título principal
            String mainText = (kanji != null && !kanji.isBlank()) ? kanji : hiragana;
            if (mainText == null || mainText.isBlank()) {
                mainText = romaji; // Fallback caso extremo
            }

            // 3. Montagem da Linha de Leitura (Ex: "ひらがな • hiragana" ou apenas "hiragana")
            String readingText = getReadingText(kanji, hiragana, romaji);

            // 4. Tradução em português
            String translationText = (portuguese != null && !portuguese.isBlank())
                    ? portuguese
                    : "Sem tradução cadastrada";

            // --- RENDERIZAÇÃO NA TELA ---

            // Palavra em Destaque (Kanji ou Kana)
            assert mainText != null;
            graphics.text(mc.font, Component.literal(mainText), x + 12, y + 32, 0xFF55FFFF, true);

            // Leitura / Pronúncia (Hiragana + Romaji)
            graphics.text(mc.font, Component.literal("Leitura: " + readingText), x + 12, y + 48, 0xFFDCDCDC, true);

            // Linha Divisória Central
            graphics.fill(x + 10, y + 64, x + width - 10, y + 65, 0xFF222222);

            // Tradução / Significado
            graphics.text(mc.font, Component.literal("Significado:"), x + 12, y + 72, 0xFF888888, true);
            graphics.text(mc.font, Component.literal(translationText), x + 12, y + 86, 0xFF55FF55, true);

        } else {
            graphics.text(mc.font, Component.literal("Nenhuma palavra encontrada."), x + 12, y + 50, 0xFFFF5555, true);
        }

        // 7. Rodapé (Dica de Atalho)
        String closeHint = "[ESC] Fechar";
        int hintWidth = mc.font.width(closeHint);
        graphics.text(mc.font, Component.literal(closeHint), x + width - hintWidth - 12, y + height - 16, 0xFF666666, true);
    }

    private static @NonNull String getReadingText(String kanji, String hiragana, String romaji) {
        StringBuilder readingBuilder = new StringBuilder();
        if (kanji != null && !kanji.isBlank() && hiragana != null && !hiragana.isBlank()) {
            readingBuilder.append(hiragana);
        }
        if (romaji != null && !romaji.isBlank()) {
            if (!readingBuilder.isEmpty()) {
                readingBuilder.append(" • ");
            }
            readingBuilder.append(romaji);
        }
        return !readingBuilder.isEmpty() ? readingBuilder.toString() : "—";
    }

    /**
     * Handles mouse click inputs to prevent clicks from leaking into the background inventory.
     *
     * @return true if the event was consumed by this overlay.
     */
    public static boolean mouseClicked() {
        return DictionaryLookupService.isOpen();
        // Mouse isolation: consume all clicks while the dictionary is active
    }

    /**
     * Handles keyboard events (intercepting ESC to close the modal).
     *
     * @param keyCode The GLFW key code pressed.
     * @return true if the key press was consumed.
     */
    public static boolean keyPressed(int keyCode) {
        if (!DictionaryLookupService.isOpen()) return false;

        // Press ESC to close the lookup modal
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            DictionaryLookupService.close();
            return true;
        }

        // Freeze all other key actions while the modal is open
        return true;
    }
}