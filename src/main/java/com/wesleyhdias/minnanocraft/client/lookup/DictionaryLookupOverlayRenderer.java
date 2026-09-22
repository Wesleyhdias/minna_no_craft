package com.wesleyhdias.minnanocraft.client.lookup;

import com.wesleyhdias.minnanocraft.client.syllabary_screen.SyllabaryScreen;
import com.wesleyhdias.minnanocraft.language.dictionary.DictionaryLoader;
import com.wesleyhdias.minnanocraft.language.kana.RomajiSyllableParser;
import com.wesleyhdias.minnanocraft.language.dictionary.CompoundWord;
import com.wesleyhdias.minnanocraft.language.dictionary.Word;
import com.wesleyhdias.minnanocraft.language.TokenTextHelper;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.ChatFormatting;

import net.minecraft.util.FormattedCharSequence;
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
    private static boolean showRomaji = false;

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

        // Captures mouse position on GUI scale
        double mouseX = mc.mouseHandler.xpos() * (double) screenWidth / (double) mc.getWindow().getScreenWidth();
        double mouseY = mc.mouseHandler.ypos() * (double) screenHeight / (double) mc.getWindow().getScreenHeight();

        // Central card dimensions
        int width = 240;
        int height = 155;
        int x = (screenWidth - width) / 2;
        int y = (screenHeight - height) / 2;

        // Modal background and borders
        graphics.fill(x, y, x + width, y + height, 0xF0121212);

        int borderColor = 0xFF444444;
        graphics.fill(x - 1, y - 1, x + width + 1, y, borderColor);
        graphics.fill(x - 1, y + height, x + width + 1, y + height + 1, borderColor);
        graphics.fill(x - 1, y, x, y + height, borderColor);
        graphics.fill(x + width, y, x + width + 1, y + height, borderColor);

        // Header
        graphics.text(mc.font, Component.literal("MINNA NO CRAFT"), x + 12, y + 7, 0xFFFFAA00, true);

        // Syllabary screen button
        int sylBtnWidth = 18;
        int sylBtnHeight = 18;
        int sylBtnX = x + width - sylBtnWidth - 10;
        int sylBtnY = y + 3;

        graphics.fill(sylBtnX, sylBtnY, sylBtnX + sylBtnWidth, sylBtnY + sylBtnHeight, 0xFF333333);
        graphics.outline(sylBtnX, sylBtnY, sylBtnWidth, sylBtnHeight, 0xFF555555);
        graphics.text(mc.font, Component.literal("あ"), sylBtnX + 5, sylBtnY + 5, 0xFFFFFFFF, true);

        // Divider
        graphics.fill(x + 10, y + 24, x + width - 10, y + 25, 0xFF333333);

        // Load words and tokens
        List<Word> wordsToRender = new ArrayList<>();
        List<String> tokenWordsToRender = new ArrayList<>();
        CompoundWord compoundObj = DictionaryLookupService.getCurrentCompWord();

        if (compoundObj != null) {
            for (String compToken : compoundObj.components()) {
                if (compToken.equals(" ")) continue;
                Word compWord = DictionaryLoader.getDictionary().get(compToken);
                if (compWord != null) {
                    wordsToRender.add(compWord);
                    tokenWordsToRender.add(compToken);
                }
            }
        } else {
            Word simpleWord = DictionaryLookupService.getCurrentWord();
            if (simpleWord != null) {
                wordsToRender.add(simpleWord);
                tokenWordsToRender.add(DictionaryLookupService.getToken());
            }
        }

        currentTotalPages = wordsToRender.size();

        boolean hoveringInfoBtn = false;
        String wordExplanation = "";

        if (currentTotalPages > 0) {
            if (currentPageIndex >= currentTotalPages) currentPageIndex = 0;
            if (currentPageIndex < 0) currentPageIndex = 0;

            Word word = wordsToRender.get(currentPageIndex);
            String token = tokenWordsToRender.get(currentPageIndex);

            wordExplanation = net.minecraft.client.resources.language.I18n.get(TokenTextHelper.getDescriptionKey(token));

            String kanji = word.kanji();
            String hiragana = word.hiragana();
            String romaji = word.romaji();
            String translation = String.valueOf(word.getLocalTranslations());

            String currentReading = showRomaji ? romaji : hiragana;
            if (currentReading == null || currentReading.isBlank()) currentReading = "-";
            String formattedReading = "(" + currentReading + ")";

            int currentY = y + 30;
            int maxLineWidth = width - 24; // 216px of space

            // 2. Main word + reading
            float kanjiScale = 1.5f;
            int kanjiWidth = (int) (mc.font.width(kanji) * kanjiScale);

            float readingScale = 1.1f;
            int readingWidth = (int) (mc.font.width(formattedReading) * readingScale);
            int eyeBtnWidth = 14;
            int eyeBtnHeight = 14;

            boolean fitsInOneLine = (kanjiWidth + 8 + readingWidth + 6 + eyeBtnWidth) <= maxLineWidth;

            graphics.pose().pushMatrix();
            graphics.pose().scale(kanjiScale, kanjiScale);
            graphics.text(mc.font, Component.literal(kanji), (int) ((x + 12) / kanjiScale), (int) (currentY / kanjiScale), 0xFF55FFFF, true);
            graphics.pose().popMatrix();

            int eyeBtnX;
            int eyeBtnY;

            if (fitsInOneLine) {
                int readingX = x + 12 + kanjiWidth + 8;
                graphics.pose().pushMatrix();
                graphics.pose().scale(readingScale, readingScale);
                graphics.text(mc.font, Component.literal(formattedReading), (int) (readingX / readingScale), (int) ((currentY + 2) / readingScale), 0xFFDCDCDC, true);
                graphics.pose().popMatrix();

                eyeBtnX = readingX + readingWidth + 6;
                eyeBtnY = currentY;
                currentY += 20;
            } else {
                currentY += 18;

                int readingX = x + 12;
                graphics.pose().pushMatrix();
                graphics.pose().scale(readingScale, readingScale);
                graphics.text(mc.font, Component.literal(formattedReading), (int) (readingX / readingScale), (int) (currentY / readingScale), 0xFFDCDCDC, true);
                graphics.pose().popMatrix();

                eyeBtnX = readingX + readingWidth + 6;
                eyeBtnY = currentY - 1;
                currentY += 16;
            }

            // Toggle button "👁"
            graphics.fill(eyeBtnX, eyeBtnY, eyeBtnX + eyeBtnWidth, eyeBtnY + eyeBtnHeight, 0xFF333333);
            graphics.outline(eyeBtnX, eyeBtnY, eyeBtnWidth, eyeBtnHeight, 0xFF555555);
            graphics.text(mc.font, Component.literal("👁"), eyeBtnX + 3, eyeBtnY + 3, 0xFFFFFFFF, true);

            // Translation / Meaning
            String translationText = (translation != null && !translation.isBlank()) ? translation : "Sem tradução";
            Component transLabel = Component.translatable("lookup_overlay.minnanocraft.meaning");
            String fullTransText = transLabel.getString() + translationText;

            int transY = currentY;

            var transLines = mc.font.split(Component.literal(fullTransText), maxLineWidth);
            for (var line : transLines) {
                graphics.text(mc.font, line, x + 12, currentY, 0xFF55FF55, true);
                currentY += mc.font.lineHeight + 2;
            }

            // Explanation button
            if (!wordExplanation.isBlank()) {
                int infoBtnSize = 15;

                int firstLineWidth = transLines.isEmpty() ? 0 : mc.font.width(transLines.getFirst());
                int infoBtnX = x + 12 + firstLineWidth + 6;
                int infoBtnY = transY  - 3; // Alinha verticalmente com o texto

                if (infoBtnX + infoBtnSize > x + width - 12) {
                    infoBtnX = x + width - infoBtnSize - 12;
                }

                hoveringInfoBtn = mouseX >= infoBtnX && mouseX <= (infoBtnX + infoBtnSize) &&
                        mouseY >= infoBtnY && mouseY <= (infoBtnY + infoBtnSize);

                int btnBg = hoveringInfoBtn ? 0xFF555555 : 0xFF333333;
                graphics.fill(infoBtnX, infoBtnY, infoBtnX + infoBtnSize, infoBtnY + infoBtnSize, btnBg);
                graphics.outline(infoBtnX, infoBtnY, infoBtnSize, infoBtnSize, 0xFF777777);
                graphics.text(mc.font, Component.literal("ℹ"), infoBtnX + 3, infoBtnY + 4, 0xFF55FFFF, true);
            }

            // Example sentence
            String fullExample = net.minecraft.client.resources.language.I18n.get(TokenTextHelper.getExampleKey(token));
            String jpSentence = fullExample;
            String sentenceReading = "";
            String sentenceTranslation = "";

            String[] newlineSplit = fullExample.split("\n");
            if (newlineSplit.length > 1) {
                jpSentence = newlineSplit[0];
                sentenceTranslation = newlineSplit[1];
            }

            int openParen = jpSentence.lastIndexOf('(');
            int closeParen = jpSentence.lastIndexOf(')');
            if (openParen != -1 && closeParen > openParen) {
                sentenceReading = jpSentence.substring(openParen + 1, closeParen);
                jpSentence = jpSentence.substring(0, openParen).trim();
            }

            if (!jpSentence.isBlank()) {
                graphics.fill(x + 10, currentY + 4, x + width - 10, currentY + 5, 0xFF333333);
                graphics.text(mc.font, Component.literal("Exemplo de uso:"), x + 12, currentY + 10, 0xFFFFFFFF, true);

                currentY += 25;

                // Japanese
                var jpLines = mc.font.split(Component.literal(jpSentence), maxLineWidth);
                for (var line : jpLines) {
                    graphics.text(mc.font, line, x + 12, currentY, 0xFFFFFFFF, true);
                    currentY += mc.font.lineHeight + 2;
                }

                // reading
                if (!sentenceReading.isBlank()) {
                    String displayReading = showRomaji ? sentenceReading : RomajiSyllableParser.toHiragana(sentenceReading);
                    var readingLines = mc.font.split(Component.literal(displayReading), maxLineWidth);
                    for (var line : readingLines) {
                        graphics.text(mc.font, line, x + 12, currentY, 0xFFFFFF55, true);
                        currentY += mc.font.lineHeight + 2;
                    }
                }

                // Translation
                var jpTranslationLines = mc.font.split(Component.literal(sentenceTranslation), maxLineWidth);
                for (var line : jpTranslationLines) {
                    graphics.text(mc.font, line, x + 12, currentY, 0xFFAAAAAA, true);
                    currentY += mc.font.lineHeight + 2;
                }
            }

        } else {
            graphics.text(mc.font, Component.literal("Nenhuma palavra encontrada."), x + 12, y + 50, 0xFFFF5555, true);
        }

        // Footer
        int pageY = y + height - 16;

        if (currentTotalPages > 1) {
            int prevColor = (currentPageIndex > 0) ? 0xFFFFFFFF : 0xFF555555;
            graphics.text(mc.font, Component.literal("<"), x + 12, pageY, prevColor, true);

            String pageText = (currentPageIndex + 1) + " / " + currentTotalPages;
            graphics.text(mc.font, Component.literal(pageText), x + 28, pageY, 0xFFAAAAAA, true);

            int nextX = x + 28 + mc.font.width(pageText) + 8;
            int nextColor = (currentPageIndex < currentTotalPages - 1) ? 0xFFFFFFFF : 0xFF555555;
            graphics.text(mc.font, Component.literal(">"), nextX, pageY, nextColor, true);
        }

        Component closeHint = Component.translatable("lookup_overlay.minnanocraft.close_overlay");
        int hintWidth = mc.font.width(closeHint);
        graphics.text(mc.font, closeHint, x + width - hintWidth - 12, pageY, 0xFF666666, true);

        // render explanation on hover
        if (hoveringInfoBtn && !wordExplanation.isBlank()) {
            renderExplanationTooltip(graphics, mc, (int) mouseX, (int) mouseY, screenWidth, screenHeight, wordExplanation);
        }
    }

    /**
     * Renderiza o card flutuante de explicação detalhada na posição do mouse.
     */
    private static void renderExplanationTooltip(GuiGraphicsExtractor graphics, Minecraft mc,
                                                 int mouseX, int mouseY,
                                                 int screenWidth, int screenHeight,
                                                 String explanation) {

        int tooltipWidth = 180;
        int maxTextWidth = tooltipWidth - 12;

        List<FormattedCharSequence> formattedLines = new ArrayList<>();

        if (explanation != null && !explanation.isBlank()) {
            formattedLines.add(Component.literal("Explicação:").withStyle(ChatFormatting.AQUA).getVisualOrderText());
            formattedLines.addAll(mc.font.split(Component.literal(explanation).withStyle(ChatFormatting.GRAY), maxTextWidth));
        }

        if (formattedLines.isEmpty()) return;

        int totalTextHeight = formattedLines.size() * (mc.font.lineHeight + 2);
        int tooltipHeight = totalTextHeight + 12;

        int tooltipX = mouseX + 10;
        int tooltipY = mouseY - 10;

        if (tooltipX + tooltipWidth > screenWidth) {
            tooltipX = mouseX - tooltipWidth - 5;
        }
        if (tooltipY + tooltipHeight > screenHeight) {
            tooltipY = screenHeight - tooltipHeight - 5;
        }

        graphics.fill(tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + tooltipHeight, 0xF00A0A0A);

        int borderColor = 0xFF5555FF;
        graphics.fill(tooltipX - 1, tooltipY - 1, tooltipX + tooltipWidth + 1, tooltipY, borderColor);
        graphics.fill(tooltipX - 1, tooltipY + tooltipHeight, tooltipX + tooltipWidth + 1, tooltipY + tooltipHeight + 1, borderColor);
        graphics.fill(tooltipX - 1, tooltipY, tooltipX, tooltipY + tooltipHeight, borderColor);
        graphics.fill(tooltipX + tooltipWidth, tooltipY, tooltipX + tooltipWidth + 1, tooltipY + tooltipHeight, borderColor);

        int renderY = tooltipY + 6;
        for (FormattedCharSequence line : formattedLines) {
            graphics.text(mc.font, line, tooltipX + 6, renderY, 0xFFFFFFFF, true);
            renderY += mc.font.lineHeight + 2;
        }
    }

    /**
     * Handles mouse click inputs to prevent clicks from leaking into the background inventory.
     *
     * @return true if the event was consumed by this overlay.
     */
    public static boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!DictionaryLookupService.isOpen()) return false;
        if (button != 0) return true;

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int width = 240;
        int height = 145;
        int x = (screenWidth - width) / 2;
        int y = (screenHeight - height) / 2;

        // Silabário button hitbox ("あ")
        int sylBtnWidth = 18;
        int sylBtnHeight = 18;
        int sylBtnX = x + width - sylBtnWidth - 10;
        int sylBtnY = y + 3;

        if (mouseX >= sylBtnX && mouseX <= (sylBtnX + sylBtnWidth) && mouseY >= sylBtnY && mouseY <= (sylBtnY + sylBtnHeight)) {
            mc.setScreen(new SyllabaryScreen(mc.screen));
            return true;
        }

        // Toggle button dynamic hitbox ("👁")
        List<Word> wordsToRender = new ArrayList<>();
        CompoundWord compoundObj = DictionaryLookupService.getCurrentCompWord();
        if (compoundObj != null) {
            for (String compToken : compoundObj.components()) {
                if (!compToken.equals(" ")) {
                    Word compWord = DictionaryLoader.getDictionary().get(compToken);
                    if (compWord != null) wordsToRender.add(compWord);
                }
            }
        } else {
            Word simpleWord = DictionaryLookupService.getCurrentWord();
            if (simpleWord != null) wordsToRender.add(simpleWord);
        }

        if (!wordsToRender.isEmpty()) {
            Word word = wordsToRender.get(currentPageIndex);

            String mainText = word.kanji();

            String currentReading = showRomaji ? word.romaji() : word.hiragana();
            if (currentReading == null || currentReading.isBlank()) currentReading = "-";
            String formattedReading = "(" + currentReading + ")";

            int currentY = y + 30;
            int maxLineWidth = width - 24;

            float kanjiScale = 1.5f;
            int kanjiWidth = (int) (mc.font.width(mainText) * kanjiScale);

            float readingScale = 1.1f;
            int readingWidth = (int) (mc.font.width(formattedReading) * readingScale);
            int eyeBtnWidth = 14;
            int eyeBtnHeight = 14;

            boolean fitsInOneLine = (kanjiWidth + 8 + readingWidth + 6 + eyeBtnWidth) <= maxLineWidth;

            int eyeBtnX;
            int eyeBtnY;

            if (fitsInOneLine) {
                int readingX = x + 12 + kanjiWidth + 8;
                eyeBtnX = readingX + readingWidth + 6;
                eyeBtnY = currentY;
            } else {
                currentY += 18;
                int readingX = x + 12;
                eyeBtnX = readingX + readingWidth + 6;
                eyeBtnY = currentY - 1;
            }

            if (mouseX >= eyeBtnX && mouseX <= (eyeBtnX + eyeBtnWidth) && mouseY >= eyeBtnY && mouseY <= (eyeBtnY + eyeBtnHeight)) {
                showRomaji = !showRomaji;
                return true;
            }
        }

        // Don't check click if there's only one page
        if (currentTotalPages > 1) {
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