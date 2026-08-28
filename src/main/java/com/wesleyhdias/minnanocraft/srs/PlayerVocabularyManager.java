package com.wesleyhdias.minnanocraft.srs;

import com.wesleyhdias.minnanocraft.language.dictionary.DictionaryLoader;
import com.wesleyhdias.minnanocraft.srs.models.WordProgress;
import com.wesleyhdias.minnanocraft.srs.models.ExpEvents;

import java.util.concurrent.ConcurrentHashMap;


/**
 * Central manager for vocabulary progress.
 * Serves as an in-memory cache and orchestrates data persistence,
 * event handling, and target token selection algorithms.
 */
public class PlayerVocabularyManager {

    private static PlayerVocabularyManager instance;

    /**
     * In-memory cache holding word progress data for thread-safe access.
     */
    private ConcurrentHashMap<String, WordProgress> vocabularyCache = new ConcurrentHashMap<>();

    private final PlayerVocabularyRepository repository;
    private final ProgressionSystem progressionSystem;

    private PlayerVocabularyManager() {
        this.repository = PlayerVocabularyRepository.getInstance();
        this.progressionSystem = ProgressionSystem.getInstance();
    }

    public static PlayerVocabularyManager getInstance() {
        if (instance == null) instance = new PlayerVocabularyManager();
        return instance;
    }

    public static void setInstanceForTesting(PlayerVocabularyManager mockInstance) {
        instance = mockInstance;
    }

    /**
     * Loads the vocabulary data from disk into the memory cache.
     * Should be called during mod/world initialization.
     */
    public void load() { vocabularyCache = repository.loadAll(); }

    /**
     * Saves the current in-memory vocabulary state to disk.
     */
    public void save() { repository.saveAll(vocabularyCache); }

    /**
     * Retrieves the progress for a specific token.
     *
     * @param token The target token string.
     * @return The WordProgress instance, or null if not found.
     */
    public WordProgress getProgress(String token) { return vocabularyCache.get(token); }


    public ConcurrentHashMap<String, WordProgress> getVocabularyCache() { return vocabularyCache; }

    /**
     * Retrieves the progress for a token, creating a new instance if it doesn't exist.
     *
     * @param token The target token string.
     * @return The existing or newly created WordProgress instance.
     */
    public WordProgress getOrCreateProgress(String token) {
        return vocabularyCache.computeIfAbsent(token, WordProgress::new);
    }

    /**
     * Registers a learning event for a specific token using real-time timestamps.
     *
     * @param token The target word or particle.
     * @param expEvents The triggered event type.
     */
    public void registerEvent(String token, ExpEvents expEvents) {
        WordProgress progress = getOrCreateProgress(token);
        long now = System.currentTimeMillis();
        if ((now - progress.getLastSeen()) < 3000) return;
        progressionSystem.applyEvent(progress, expEvents);
    }

    /**
     * Checks if a token is a particle (morpheme) by verifying its absence in the dictionary.
     *
     * @param token The target token to verify.
     * @return true if the token is a particle, false if it is a content word.
     */
    public boolean isParticle(String token) {
        return !DictionaryLoader.getDictionary().containsKey(token);
    }

    public void updateProgression() {
        progressionSystem.updateStates(vocabularyCache);
    }
}