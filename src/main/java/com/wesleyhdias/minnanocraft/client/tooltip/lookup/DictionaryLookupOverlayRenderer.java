package com.wesleyhdias.minnanocraft.client.tooltip.lookup;

import com.wesleyhdias.minnanocraft.language.dictionary.Word;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;

import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

/**
 * Renders the dictionary lookup overlay UI on screen.
 * Displays word details such as kanji, kana, romaji, and translations inside a modal card.
 */
public class DictionaryLookupOverlayRenderer {

    /**
     * Renders the dictionary lookup UI onto the extracted graphics context.
     *
     * @param graphics The GUI graphics extractor instance provided by ScreenMixin.
     */
    public static void render(GuiGraphicsExtractor graphics) {
        if (!DictionaryLookupService.isOpen()) return;

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // 1. Central card dimensions
        int width = 240;
        int height = 135;
        int x = (screenWidth - width) / 2;
        int y = (screenHeight - height) / 2;

        // 2. Modal background and borders
        graphics.fill(x, y, x + width, y + height, 0xF0121212); // Semi-solid dark background

        int borderColor = 0xFF444444;
        graphics.fill(x - 1, y - 1, x + width + 1, y, borderColor);          // Top
        graphics.fill(x - 1, y + height, x + width + 1, y + height + 1, borderColor); // Bottom
        graphics.fill(x - 1, y, x, y + height, borderColor);                // Left
        graphics.fill(x + width, y, x + width + 1, y + height, borderColor); // Right

        // 3. Header
        graphics.text(mc.font, Component.literal("MINNA NO CRAFT"), x + 12, y + 10, 0xFFFFAA00, true);

        // Upper divider line
        graphics.fill(x + 10, y + 22, x + width - 10, y + 23, 0xFF333333);

        Word word = DictionaryLookupService.getCurrentWord();

        if (word != null) {
            // 1. Extract fields from the Word class
            String kanji = word.kanji();
            String hiragana = word.hiragana();
            String romaji = word.romaji();
            String portuguese = String.valueOf(word.getLocalTranslations());

            // 2. Fallback logic for the main title
            // If there is no Kanji, use Hiragana/Katakana as the main title
            String mainText = (kanji != null && !kanji.isBlank()) ? kanji : hiragana;
            if (mainText == null || mainText.isBlank()) {
                mainText = romaji; // Extreme fallback case
            }

            // 3. Assemble the reading line (e.g., "ひらがな • hiragana" or just "hiragana")
            String readingText = getReadingText(kanji, hiragana, romaji);

            // 4. Translation text
            String translationText = (portuguese != null && !portuguese.isBlank())
                    ? portuguese
                    : "Sem tradução cadastrada";

            // --- ON-SCREEN RENDERING ---

            // Featured Word (Kanji or Kana)
            assert mainText != null;
            graphics.text(mc.font, Component.literal(mainText), x + 12, y + 32, 0xFF55FFFF, true);

            // Reading / Pronunciation (Hiragana + Romaji)
            graphics.text(mc.font, Component.translatable("lookup_overlay.minnanocraft.reading", readingText), x + 12, y + 48, 0xFFDCDCDC, true);

            // Central Divider Line
            graphics.fill(x + 10, y + 64, x + width - 10, y + 65, 0xFF222222);

            // Translation / Meaning
            graphics.text(mc.font, Component.translatable("lookup_overlay.minnanocraft.meaning"), x + 12, y + 72, 0xFF888888, true);
            graphics.text(mc.font, Component.literal(translationText), x + 12, y + 86, 0xFF55FF55, true);

        } else {
            graphics.text(mc.font, Component.literal("Nenhuma palavra encontrada."), x + 12, y + 50, 0xFFFF5555, true);
        }

        // 7. Footer (Shortcut Hint)
        Component closeHint = Component.translatable("lookup_overlay.minnanocraft.close_overlay");
        int hintWidth = mc.font.width(closeHint);
        graphics.text(mc.font, closeHint, x + width - hintWidth - 12, y + height - 16, 0xFF666666, true);
    }

    /**
     * Assembles the combined reading string containing hiragana and romaji.
     *
     * @param kanji    The kanji representation.
     * @param hiragana The hiragana representation.
     * @param romaji   The romaji representation.
     * @return A formatted reading string, or a fallback dash if none are available.
     */
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