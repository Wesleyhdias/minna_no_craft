package com.wesleyhdias.minnanocraft.language.kana;

import java.util.ArrayList;
import java.util.List;

public class RomajiSyllableParser {

    // Record holding the matched Kana-Romaji pair
    public record SyllablePair(String kana, String romaji) {}

    public static List<SyllablePair> parse(String text) {
        List<SyllablePair> result = new ArrayList<>();
        boolean hasSmallTsu = false;

        for (int i = 0; i < text.length(); ) {
            char c = text.charAt(i);

            // 1. Detect Sokuon (Small 'tsu' in Hiragana or Katakana)
            if (c == 'っ' || c == 'ッ') {
                hasSmallTsu = true;
                i++;
                continue; // Skip iteration to merge 'tsu' with the upcoming syllable
            }

            String currentKana = "";
            String currentRomaji = "";

            // 2. Try matching 2 characters first (Yoon/Compound sounds like きゃ, チュ)
            if (i + 1 < text.length()) {
                String twoChars = text.substring(i, i + 2);
                currentRomaji = findRomaji(twoChars);
                if (currentRomaji != null) {
                    currentKana = twoChars;
                    i += 2;
                }
            }

            // 3. Fallback to single character lookup
            if (currentKana.isEmpty()) {
                currentKana = String.valueOf(c);
                currentRomaji = findRomaji(currentKana);

                // Safety fallback for punctuation, numbers, or unmapped kanji
                if (currentRomaji == null) {
                    currentRomaji = currentKana;
                }
                i++;
            }

            // 4. Apply small 'tsu' consonant doubling rule
            if (hasSmallTsu) {
                char tsuChar = isKatakana(currentKana) ? 'ッ' : 'っ';
                currentKana = tsuChar + currentKana;

                // Double the first consonant of the Romaji syllable (e.g., 'ko' -> 'kko')
                if (!currentRomaji.isEmpty()) {
                    currentRomaji = currentRomaji.charAt(0) + currentRomaji;
                }
                hasSmallTsu = false;
            }

            result.add(new SyllablePair(currentKana, currentRomaji));
        }

        // Edge case: Trailing small 'tsu' at the end of a string
        if (hasSmallTsu) {
            result.add(new SyllablePair("っ", "-"));
        }

        return result;
    }

    private static String findRomaji(String kana) {
        if (KanaLoader.getHiraganaMap().containsKey(kana)) {
            return KanaLoader.getHiraganaMap().get(kana);
        }
        if (KanaLoader.getKatakanaMap().containsKey(kana)) {
            return KanaLoader.getKatakanaMap().get(kana);
        }

        // Handle Katakana long vowel mark
        if (kana.equals("ー")) return "-";

        return null;
    }

    private static boolean isKatakana(String text) {
        if (text.isEmpty()) return false;
        char c = text.charAt(0);
        return c >= '\u30A0' && c <= '\u30FF'; // Unicode range for Katakana
    }
}