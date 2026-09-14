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

    public static String toHiragana(String romajiText) {
        if (romajiText == null || romajiText.isEmpty()) return "";

        StringBuilder result = new StringBuilder();
        String lowerRomaji = romajiText.toLowerCase();

        for (int i = 0; i < lowerRomaji.length(); ) {
            boolean matched = false;

            // Use a Greedy Match, starting with 3 characters
            for (int len = Math.min(3, lowerRomaji.length() - i); len > 0; len--) {
                String chunk = lowerRomaji.substring(i, i + len);
                String hiragana = findHiragana(chunk);

                if (hiragana != null) {
                    result.append(hiragana);
                    i += len;
                    matched = true;
                    break;
                }
            }

            // Resolve the different kana
            if (!matched) {
                char currentChar = lowerRomaji.charAt(i);

                // Apply the Sokuon (small tsu): For any double consonant diferente than 'n' (ex: 'tt' em 'chotto')
                if (i + 1 < lowerRomaji.length() && currentChar == lowerRomaji.charAt(i + 1) && isConsonant(currentChar)) {
                    result.append("っ");
                } else {
                    // Fallback: for spaces or number, just ignore
                    result.append(romajiText.charAt(i));
                }
                i++; // jumps the character
            }
        }
        return result.toString();
    }

    private static boolean isConsonant(char c) {
        return c >= 'a' && c <= 'z' && c != 'a' && c != 'e' && c != 'i' && c != 'o' && c != 'u' && c != 'n';
    }

    private static String findRomaji(String kana) {

        if (KanaLoader.getKanaToRomajiMap().containsKey(kana)) {
            return KanaLoader.getKanaToRomajiMap().get(kana);
        }

        // Handle Katakana long vowel mark
        if (kana.equals("ー")) return "-";

        return null;
    }

    private static String findHiragana(String romaji) {

        Kana kanaObj = KanaLoader.getRomajiMap().get(romaji);
        if (kanaObj != null) {
            return kanaObj.hiragana();
        }
        return null;
    }

    private static boolean isKatakana(String text) {
        if (text.isEmpty()) return false;
        char c = text.charAt(0);
        return c >= '\u30A0' && c <= '\u30FF';
    }
}