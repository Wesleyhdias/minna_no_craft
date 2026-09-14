package com.wesleyhdias.minnanocraft.client.tooltip.lookup;

import com.wesleyhdias.minnanocraft.language.dictionary.DictionaryLoader;
import com.wesleyhdias.minnanocraft.language.dictionary.CompoundWord;
import com.wesleyhdias.minnanocraft.language.dictionary.Word;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;

import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders the dictionary lookup overlay UI on screen.
 * Displays word details such as kanji, kana, romaji, and translations inside a modal card.
 */
public class DictionaryLookupOverlayRenderer {

    private static int currentPageIndex = 0;
    private static int currentTotalPages = 1;

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

        // Central card dimensions
        int width = 240;
        int height = 135;
        int x = (screenWidth - width) / 2;
        int y = (screenHeight - height) / 2;

        // Modal background and borders
        graphics.fill(x, y, x + width, y + height, 0xF0121212);

        int borderColor = 0xFF444444;
        graphics.fill(x - 1, y - 1, x + width + 1, y, borderColor);
        graphics.fill(x - 1, y + height, x + width + 1, y + height + 1, borderColor);
        graphics.fill(x - 1, y, x, y + height, borderColor);
        graphics.fill(x + width, y, x + width + 1, y + height, borderColor);

        // Header & Divider
        graphics.text(mc.font, Component.literal("MINNA NO CRAFT"), x + 12, y + 10, 0xFFFFAA00, true);
        graphics.fill(x + 10, y + 22, x + width - 10, y + 23, 0xFF333333);

        // builds the words list
        List<Word> wordsToRender = new ArrayList<>();
        CompoundWord compoundObj = DictionaryLookupService.getCurrentCompWord();

        if (compoundObj != null) {
            for (String compToken : compoundObj.components()) {
                if (compToken.equals(" ")) continue;
                Word compWord = DictionaryLoader.getDictionary().get(compToken);
                if (compWord != null) wordsToRender.add(compWord);
            }
        } else {
            Word simpleWord = DictionaryLookupService.getCurrentWord();
            if (simpleWord != null) wordsToRender.add(simpleWord);
        }

        currentTotalPages = wordsToRender.size();

        // Render the word in the current page
        if (currentTotalPages > 0) {
            if (currentPageIndex >= currentTotalPages) currentPageIndex = 0;
            if (currentPageIndex < 0) currentPageIndex = 0;

            Word word = wordsToRender.get(currentPageIndex);

            String kanji = word.kanji();
            String hiragana = word.hiragana();
            String romaji = word.romaji();
            String translation = String.valueOf(word.getLocalTranslations());

            // Fallback logic for the main title
            // If there is no Kanji, use Hiragana/Katakana as the main title
            String mainText = (kanji != null && !kanji.isBlank()) ? kanji : hiragana;
            if (mainText == null || mainText.isBlank()) mainText = romaji;

            // Assemble the reading line (e.g., "ひらがな • hiragana" or just "hiragana")
            String readingText = getReadingText(kanji, hiragana, romaji);

            // Translation text
            String translationText = (translation != null && !translation.isBlank())
                    ? translation : "Sem tradução cadastrada";

            // Render fixed position
            float scale = 1.5f;
            graphics.pose().pushMatrix();
            graphics.pose().scale(scale, scale);

            int scaledX = (int) ((x + 12) / scale);
            int scaledY = (int) ((y + 28) / scale);

            graphics.text(mc.font, Component.literal(mainText), scaledX, scaledY + 2, 0xFF55FFFF, true);
            graphics.pose().popMatrix();

            // Reading / Pronunciation (Hiragana + Romaji)
            graphics.text(mc.font, Component.translatable("lookup_overlay.minnanocraft.reading", readingText), x + 12, y + 52, 0xFFDCDCDC, true);

            // Translation / Meaning
            graphics.text(mc.font, Component.translatable("lookup_overlay.minnanocraft.meaning"), x + 12, y + 72, 0xFF888888, true);
            graphics.text(mc.font, Component.literal(translationText), x + 12, y + 90, 0xFF55FF55, true);

        } else {
            graphics.text(mc.font, Component.literal("Nenhuma palavra encontrada."), x + 12, y + 50, 0xFFFF5555, true);
        }

        // 3. Render Footer
        int pageY = y + height - 16;

        if (currentTotalPages > 1) {
            // left arrow
            int prevColor = (currentPageIndex > 0) ? 0xFFFFFFFF : 0xFF555555; // Branco se ativo, Cinza se inativo
            graphics.text(mc.font, Component.literal("<"), x + 12, pageY, prevColor, true);

            // atual index / max index
            String pageText = (currentPageIndex + 1) + " / " + currentTotalPages;
            graphics.text(mc.font, Component.literal(pageText), x + 28, pageY, 0xFFAAAAAA, true);

            // right arrow
            int nextX = x + 28 + mc.font.width(pageText) + 8;
            int nextColor = (currentPageIndex < currentTotalPages - 1) ? 0xFFFFFFFF : 0xFF555555;
            graphics.text(mc.font, Component.literal(">"), nextX, pageY, nextColor, true);
        }

        Component closeHint = Component.translatable("lookup_overlay.minnanocraft.close_overlay");
        int hintWidth = mc.font.width(closeHint);
        graphics.text(mc.font, closeHint, x + width - hintWidth - 12, pageY, 0xFF666666, true);
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
    public static boolean mouseClicked(double mouseX, double mouseY, int button) {

        if (!DictionaryLookupService.isOpen()) return false;

        // Uses only left click
        if (button != 0) return true;

        // Don't check click if there's only one page
        if (currentTotalPages > 1) {
            Minecraft mc = Minecraft.getInstance();
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();

            int width = 240;
            int height = 135;
            int x = (screenWidth - width) / 2;
            int y = (screenHeight - height) / 2;

            int pageY = y + height - 16;
            String pageText = (currentPageIndex + 1) + " / " + currentTotalPages;
            int nextX = x + 28 + mc.font.width(pageText) + 8;

            // left arrow hitbox
            if (currentPageIndex > 0) {
                if (mouseX >= x && mouseX <= (x + 35) && mouseY >= (pageY - 10) && mouseY <= (pageY + 20)) {
                    currentPageIndex--;
                    return true;
                }
            }

            // right arrow hitbox
            if (currentPageIndex < currentTotalPages - 1) {
                if (mouseX >= (nextX - 15) && mouseX <= (nextX + 35) && mouseY >= (pageY - 10) && mouseY <= (pageY + 20)) {
                    currentPageIndex++;
                    return true;
                }
            }
        }

        return true;
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
            currentPageIndex = 0;
            DictionaryLookupService.close();
            return true;
        }

        // Freeze all other key actions while the modal is open
        return true;
    }
}