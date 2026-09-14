package com.wesleyhdias.minnanocraft.srs;

import com.wesleyhdias.minnanocraft.language.dictionary.CompoundDictionaryLoader;
import com.wesleyhdias.minnanocraft.language.dictionary.DictionaryLoader;
import com.wesleyhdias.minnanocraft.language.dictionary.CompoundWord;
import com.wesleyhdias.minnanocraft.srs.models.WordProgress;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility selector responsible for determining token progression priority during item vocabulary upgrades.
 * <p>
 * Implements a "wave" progression system where content/meaning words are prioritized first,
 * allowing grammar particles to catch up once content words reach a minimum script level.
 */
public class TokenUpgradeSelector {

    /**
     * Determines which token in an item's structure should receive priority progression points.
     * <p>
     * Expands compound tokens into sub-components, separates content words from particles,
     * and evaluates script levels to balance progression left-to-right.
     *
     * @param structure The list of token keys representing the item's name structure.
     * @param manager   The {@link PlayerVocabularyManager} instance tracking player progress.
     * @return The priority token string to upgrade, or {@code null} if the structure is empty.
     */
    public static String getNextTokenToUpgrade(List<String> structure, PlayerVocabularyManager manager) {
        if (structure == null || structure.isEmpty()) return null;

        // Expands the structure to break down compound words into their components
        List<String> expandedStructure = new ArrayList<>();
        for (String token : structure) {
            CompoundWord compound = CompoundDictionaryLoader.getDictionary().get(token);
            if (compound != null) {
                // Add all the components
                expandedStructure.addAll(compound.components());
            } else {
                // If it's a normal token, keep it
                expandedStructure.add(token);
            }
        }

        List<String> contentTokens = new ArrayList<>();
        List<String> particleTokens = new ArrayList<>();

        // Categorizes expanded tokens, focusing on their individual components
        for (String token : expandedStructure) {
            if (manager.isParticle(token)) {
                particleTokens.add(token);
            } else if (DictionaryLoader.getDictionary().containsKey(token)){
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
     *
     * @param tokens  The list of token keys to evaluate.
     * @param manager The {@link PlayerVocabularyManager} instance tracking player progress.
     * @return The token string with the lowest script level.
     */
    private static String getLowestLevelToken(List<String> tokens, PlayerVocabularyManager manager) {
        if (tokens == null || tokens.isEmpty()) return null;

        String lowestToken = tokens.getFirst();
        int minLevel = 4;

        for (String token : tokens) {
            WordProgress p = manager.getProgress(token);
            int level = (p != null) ? p.getScriptLevel() : 0;
            if (level < minLevel) {
                minLevel = level;
                lowestToken = token;
            }
        }
        return lowestToken;
    }

    /**
     * Helper method to find the first token matching a target script level evaluating left-to-right.
     *
     * @param tokens      The list of token keys to search through.
     * @param targetLevel The target script level to match.
     * @param manager     The {@link PlayerVocabularyManager} instance tracking player progress.
     * @return The first matching token string, or the first token in the list as fallback.
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