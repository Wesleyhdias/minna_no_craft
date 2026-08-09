package com.wesleyhdias.minnanocraft.utils;

import com.wesleyhdias.minnanocraft.builders.PortugueseItemNameBuilder;
import com.wesleyhdias.minnanocraft.data.loader.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.data.loader.DictionaryLoader;
import com.wesleyhdias.minnanocraft.renderers.DifficultyRenderer;
import com.wesleyhdias.minnanocraft.services.VocabularyManager;
import com.wesleyhdias.minnanocraft.data.models.WordProgress;
import com.wesleyhdias.minnanocraft.builders.ItemNameBuilder;
import com.wesleyhdias.minnanocraft.data.models.Word;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;

public class TooltipFormatter {

    private static final Style INTERACTIVE_STYLE = Style.EMPTY.withUnderlined(true).withColor(0xFFFF55);
    private static final Style DEFAULT_STYLE = Style.EMPTY.withColor(0xFFFFFF);

    // Classe auxiliar para mapear a frase do builder
    public static class ParsedWord {
        public String text;
        public boolean isInteractive;
        public String tokenId;
        public String prevText;
    }

    // Pega a frase do builder e descobre quem são os tokens interativos dentro dela
    public static List<ParsedWord> parseBuilderText(String translationKey, String originalName) {
        List<ParsedWord> result = new ArrayList<>();

        // Pega a string final exata, não importa qual builder foi usado ("Espada de Tetsu")
        String fullText;
        if (TranslationModeResolver.useJapanese(translationKey)) {
            fullText = ItemNameBuilder.build(translationKey);
        } else {
            fullText = PortugueseItemNameBuilder.build(translationKey, originalName);
        }

        if (fullText == null) fullText = originalName;
        if (fullText == null) return result;

        List<String> structure = ItemStructureLoader.getStructures().get(translationKey);
        String[] words = fullText.split(" "); // Quebra a frase em palavras

        for (String word : words) {
            ParsedWord pw = new ParsedWord();
            pw.text = word;
            pw.isInteractive = false;

            // Verifica se essa palavra é um dos nossos tokens (ignorando partículas)
            if (structure != null) {
                for (String token : structure) {
                    if (VocabularyManager.isParticle(token)) continue;

                    WordProgress progress = VocabularyManager.getProgress(token);
                    int level = (progress != null) ? progress.getScriptLevel() : 0;
                    String expectedText = getWordAtLevel(token, level); // ex: "Tetsu"

                    // Se a palavra bater com o texto esperado pelo SRS, achamos o token!
                    if (word.equalsIgnoreCase(expectedText) || word.equalsIgnoreCase(token)) {
                        pw.isInteractive = true;
                        pw.tokenId = token;
                        pw.prevText = getWordAtLevel(token, Math.max(0, level - 1));
                        break;
                    }
                }
            }
            result.add(pw);
        }
        return result;
    }

    public static Component formatItemName(String translationKey, String originalName) {
        List<ParsedWord> parsedWords = parseBuilderText(translationKey, originalName);
        MutableComponent finalName = Component.empty();

        for (int i = 0; i < parsedWords.size(); i++) {
            ParsedWord pw = parsedWords.get(i);

            // Aplica estilo APENAS se for uma palavra do dicionário mapeada
            if (pw.isInteractive) {
                finalName.append(Component.literal(pw.text).withStyle(INTERACTIVE_STYLE));
            } else {
                finalName.append(Component.literal(pw.text).withStyle(DEFAULT_STYLE));
            }

            if (i < parsedWords.size() - 1) {
                finalName.append(Component.literal(" "));
            }
        }

        return finalName;
    }

    public static String getWordAtLevel(String token, int level) {
        Word word = DictionaryLoader.getDictionary().get(token);
        if (word == null) return token;

        String renderedText = DifficultyRenderer.render(word, level);
        if (renderedText != null) {
            return renderedText;
        }

        return (word.translations() != null && !word.translations().isEmpty())
                ? word.translations().getFirst()
                : token;
    }
}