package com.wesleyhdias.minnanocraft.srs;

import com.wesleyhdias.minnanocraft.language.dictionary.CompoundDictionaryLoader;
import com.wesleyhdias.minnanocraft.language.dictionary.CompoundWord;
import com.wesleyhdias.minnanocraft.srs.models.WordProgress;

import java.util.ArrayList;
import java.util.List;

public class TokenUpgradeSelector {

    /**
     * Determines which token in an item's structure should receive priority progression points.
     * Implements a "wave" system where content words progress first, and particles catch up.
     *
     * @param structure The list of tokens representing the item's name structure.
     * @return The priority token string to upgrade, or null if the structure is empty.
     */
    public static String getNextTokenToUpgrade(List<String> structure, PlayerVocabularyManager manager) {
        if (structure == null || structure.isEmpty()) return null;

        // 1. Expande a estrutura para desmembrar palavras compostas em seus componentes e separadores
        List<String> expandedStructure = new ArrayList<>();
        for (String token : structure) {
            CompoundWord compound = CompoundDictionaryLoader.getDictionary().get(token);
            if (compound != null) {
                // Adiciona os componentes
                expandedStructure.addAll(compound.components());

            } else {
                // Se for um token normal ou morfema comum, mantém
                expandedStructure.add(token);
            }
        }

        List<String> contentTokens = new ArrayList<>();
        List<String> particleTokens = new ArrayList<>();

        // 2. Classifica os tokens expandidos (agora focando nos pedaços reais)
        for (String token : expandedStructure) {
            if (manager.isParticle(token)) {
                particleTokens.add(token);
            } else {
                contentTokens.add(token);
            }
        }

        // Safety fallback if the item consists solely of particles
        if (contentTokens.isEmpty()) {
            return getLowestLevelToken(particleTokens, manager);
        }

        // Finds the lowest script level among content words
        int minContentLevel = 4;
        for (String token : contentTokens) {
            WordProgress p = manager.getProgress(token);
            int level = (p != null) ? p.getScriptLevel() : 0;
            if (level < minContentLevel) minContentLevel = level;
        }

        // NATIVE MODE (Level 0): Particles are invisible; focus 100% on content words
        if (minContentLevel == 0) {
            return getFirstTokenAtLevel(contentTokens, 0, manager);
        }

        // JAPANESE MODE ACTIVATED (Level >= 1): Particles have appeared on screen
        if (!particleTokens.isEmpty()) {
            int minParticleLevel = 4;
            for (String token : particleTokens) {
                WordProgress p = manager.getProgress(token);
                int level = (p != null) ? p.getScriptLevel() : 0;
                if (level < minParticleLevel) minParticleLevel = level;
            }

            // If a particle lags behind content level, give it top priority to catch up
            if (minParticleLevel < minContentLevel) {
                return getFirstTokenAtLevel(particleTokens, minParticleLevel, manager);
            }
        }

        // TIE-BREAKER: Content words always take priority when inaugurating a new level
        return getFirstTokenAtLevel(contentTokens, minContentLevel, manager);
    }

    /**
     * Helper fallback method to find the token with the lowest script level in a list.
     */
    private static String getLowestLevelToken(List<String> tokens, PlayerVocabularyManager manager) {
        int minLevel = 4;
        for (String token : tokens) {
            WordProgress p = manager.getProgress(token);
            int level = (p != null) ? p.getScriptLevel() : 0;
            if (level < minLevel) minLevel = level;
        }
        return getFirstTokenAtLevel(tokens, minLevel, manager);
    }

    /**
     * Helper method to find the first token matching a target script level (left-to-right).
     */
    private static String getFirstTokenAtLevel(List<String> tokens, int targetLevel, PlayerVocabularyManager manager) {
        for (String token : tokens) {
            WordProgress p = manager.getProgress(token);
            int level = (p != null) ? p.getScriptLevel() : 0;
            if (level == targetLevel) return token;
        }
        return tokens.getFirst();
    }
}