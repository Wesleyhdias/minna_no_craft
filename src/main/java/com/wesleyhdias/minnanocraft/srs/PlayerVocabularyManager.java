package com.wesleyhdias.minnanocraft.srs;

import com.wesleyhdias.minnanocraft.language.dictionary.DictionaryLoader;
import com.wesleyhdias.minnanocraft.srs.models.WordProgress;
import com.wesleyhdias.minnanocraft.srs.models.ExpEvents;

import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;

/**
 * Central manager for vocabulary progress.
 * Serves as an in-memory cache and orchestrates data persistence,
 * event handling, and target token selection algorithms.
 */
public class PlayerVocabularyManager {

    /**
     * In-memory cache holding word progress data for thread-safe access.
     */
    private static ConcurrentHashMap<String, WordProgress> vocabularyCache = new ConcurrentHashMap<>();

    // Dependencies
    private static final PlayerVocabularyRepository repository = new PlayerVocabularyRepository();
    private static final ProgressionSystem progressionSystem = new ProgressionSystem();

    /**
     * Loads the vocabulary data from disk into the memory cache.
     * Should be called during mod/world initialization.
     */
    public static void load() {
        vocabularyCache = repository.loadAll();
    }

    /**
     * Saves the current in-memory vocabulary state to disk.
     */
    public static void save() {
        repository.saveAll(vocabularyCache);
    }

    /**
     * Retrieves the progress for a specific token.
     *
     * @param token The target token string.
     * @return The WordProgress instance, or null if not found.
     */
    public static WordProgress getProgress(String token) {
        return vocabularyCache.get(token);
    }

    /**
     * Retrieves the progress for a token, creating a new instance if it doesn't exist.
     *
     * @param token The target token string.
     * @return The existing or newly created WordProgress instance.
     */
    public static WordProgress getOrCreateProgress(String token) {
        return vocabularyCache.computeIfAbsent(token, WordProgress::new);
    }

    // =========================================================
    // GAMEPLAY TRIGGERS
    // =========================================================

    /**
     * Registers a learning event for a specific token using real-time timestamps.
     *
     * @param token The target word or particle.
     * @param expEvents The triggered event type.
     */
    public static void registerEvent(String token, ExpEvents expEvents) {
        WordProgress progress = getOrCreateProgress(token);
        long now = System.currentTimeMillis();

        // --- ANTISPAM FILTER ---
        // Ignores event triggers if the token was interacted with less than 3 second (3000 ms) ago
        if ((now - progress.getLastSeen()) < 3000) {
            return;
        }

        progressionSystem.applyEvent(progress, expEvents);
    }

    /**
     * Checks if a token is a particle (morpheme) by verifying its absence in the dictionary.
     *
     * @param token The target token to verify.
     * @return true if the token is a particle, false if it is a content word.
     */
    public static boolean isParticle(String token) {
        return !DictionaryLoader.getDictionary().containsKey(token);
    }

    /**
     * Determines which token in an item's structure should receive priority progression points.
     * Implements a "wave" system where content words progress first, and particles catch up.
     *
     * @param structure The list of tokens representing the item's name structure.
     * @return The priority token string to upgrade, or null if the structure is empty.
     */
    public static String getNextTokenToUpgrade(List<String> structure) {
        if (structure == null || structure.isEmpty()) return null;

        List<String> contentTokens = new ArrayList<>();
        List<String> particleTokens = new ArrayList<>();

        for (String token : structure) {
            if (isParticle(token)) {
                particleTokens.add(token);
            } else {
                contentTokens.add(token);
            }
        }

        // Safety fallback if the item consists solely of particles
        if (contentTokens.isEmpty()) {
            return getLowestLevelToken(particleTokens);
        }

        // 1. Finds the lowest script level among content words
        int minContentLevel = 4;
        for (String token : contentTokens) {
            WordProgress p = getProgress(token);
            int level = (p != null) ? p.getScriptLevel() : 0;
            if (level < minContentLevel) {
                minContentLevel = level;
            }
        }

        // 2. NATIVE MODE (Level 0): Particles are invisible; focus 100% on content words
        if (minContentLevel == 0) {
            return getFirstTokenAtLevel(contentTokens, 0);
        }

        // 3. JAPANESE MODE ACTIVATED (Level >= 1): Particles have appeared on screen
        if (!particleTokens.isEmpty()) {
            int minParticleLevel = 4;
            for (String token : particleTokens) {
                WordProgress p = getProgress(token);
                int level = (p != null) ? p.getScriptLevel() : 0;
                if (level < minParticleLevel) {
                    minParticleLevel = level;
                }
            }

            // If a particle lags behind content level, give it top priority to catch up
            if (minParticleLevel < minContentLevel) {
                return getFirstTokenAtLevel(particleTokens, minParticleLevel);
            }
        }

        // 4. TIE-BREAKER: Content words always take priority when inaugurating a new level
        return getFirstTokenAtLevel(contentTokens, minContentLevel);
    }

    /**
     * Helper fallback method to find the token with the lowest script level in a list.
     */
    private static String getLowestLevelToken(List<String> tokens) {
        int minLevel = 4;
        for (String token : tokens) {
            WordProgress p = getProgress(token);
            int level = (p != null) ? p.getScriptLevel() : 0;
            if (level < minLevel) minLevel = level;
        }
        return getFirstTokenAtLevel(tokens, minLevel);
    }

    /**
     * Helper method to find the first token matching a target script level (left-to-right).
     */
    private static String getFirstTokenAtLevel(List<String> tokens, int targetLevel) {
        for (String token : tokens) {
            WordProgress p = getProgress(token);
            int level = (p != null) ? p.getScriptLevel() : 0;
            if (level == targetLevel) {
                return token;
            }
        }
        return tokens.getFirst();
    }

    /**
     * Real-time progression decay loop (called during auto-save ticks every ~5 minutes).
     * Processes inactivity decays and updates word learning states.
     */
    public static void updateProgression() {
        progressionSystem.updateStates(vocabularyCache);
    }
}