package com.wesleyhdias.minnanocraft.srs;

import com.wesleyhdias.minnanocraft.language.dictionary.CompoundDictionaryLoader;
import com.wesleyhdias.minnanocraft.language.dictionary.CompoundWord;
import com.wesleyhdias.minnanocraft.language.morpheme.MorphemeLoader;
import com.wesleyhdias.minnanocraft.srs.models.WordProgress;
import com.wesleyhdias.minnanocraft.srs.models.ExpEvents;

import java.util.concurrent.ConcurrentHashMap;


/**
 * Central manager for player vocabulary progress.
 * <p>
 * Serves as an in-memory thread-safe cache, orchestrates data persistence through
 * {@link PlayerVocabularyRepository}, handles compound word decomposition, and dispatches
 * interaction events to {@link ProgressionSystem}.
 */
public class PlayerVocabularyManager {

    private static PlayerVocabularyManager instance;

    /** In-memory cache holding active word progress data for thread-safe access. */
    private ConcurrentHashMap<String, WordProgress> vocabularyCache = new ConcurrentHashMap<>();

    private final PlayerVocabularyRepository repository;
    private final ProgressionSystem progressionSystem;

    /**
     * Private constructor enforcing singleton pattern and initializing core dependencies.
     */
    protected PlayerVocabularyManager() {
        this.repository = PlayerVocabularyRepository.getInstance();
        this.progressionSystem = ProgressionSystem.getInstance();
    }

    /**
     * Retrieves the global singleton instance of the manager.
     *
     * @return The active {@link PlayerVocabularyManager} instance.
     */
    public static PlayerVocabularyManager getInstance() {
        if (instance == null) instance = new PlayerVocabularyManager();
        return instance;
    }

    /**
     * Replaces the singleton instance with a custom or mock instance for unit testing.
     *
     * @param mockInstance The instance to inject for testing scenarios.
     */
    public static void setInstanceForTesting(PlayerVocabularyManager mockInstance) {
        instance = mockInstance;
    }

    /**
     * Loads saved vocabulary progress data from disk into the in-memory cache.
     * Should be called during mod or client initialization.
     */
    public void load() {
        vocabularyCache = repository.loadAll();
    }

    /**
     * Saves the current in-memory vocabulary cache state to disk.
     */
    public void save() {
        repository.saveAll(vocabularyCache);
    }

    /**
     * Retrieves the progress data for a specific token key.
     *
     * @param token The target token string key.
     * @return The {@link WordProgress} instance, or {@code null} if untracked.
     */
    public WordProgress getProgress(String token) {
        return vocabularyCache.get(token);
    }

    /**
     * Gets the full in-memory cache mapping token keys to progress objects.
     *
     * @return The underlying {@link ConcurrentHashMap} cache.
     */
    public ConcurrentHashMap<String, WordProgress> getVocabularyCache() {
        return vocabularyCache;
    }

    /**
     * Retrieves existing progress for a token key, or atomically creates a new
     * {@link WordProgress} instance if it is not already tracked.
     *
     * @param token The target token string key.
     * @return The existing or newly created {@link WordProgress} instance.
     */
    public WordProgress getOrCreateProgress(String token) {
        return vocabularyCache.computeIfAbsent(token, WordProgress::new);
    }

    /**
     * Registers a learning event for a token. If the token represents a compound word,
     * the event is unpacked and experience is applied individually to each component.
     *
     * @param token     The target word, compound token, or particle key.
     * @param expEvents The triggered interaction event type.
     */
    public void registerEvent(String token, ExpEvents expEvents) {
        // Checks if the token is a registered compound word
        CompoundWord compound = CompoundDictionaryLoader.getDictionary().get(token);

        if (compound != null) {
            // Unpacks compound word and grants experience to each underlying component
            for (String compToken : compound.components()) {
                applyExpToToken(compToken, expEvents);
            }
            return;
        }
        // Standard token or single morpheme
        applyExpToToken(token, expEvents);
    }

    /**
     * Helper method to enforce interaction cooldowns and apply experience to a single token.
     *
     * @param token     The target token key.
     * @param expEvents The experience event to apply.
     */
    private void applyExpToToken(String token, ExpEvents expEvents) {
        WordProgress progress = getOrCreateProgress(token);
        long now = System.currentTimeMillis();

        // Enforces a 5-second cooldown guard before passing the event to the progression system
        if ((now - progress.getLastSeen()) < 5000) return;

        progressionSystem.applyEvent(progress, expEvents);
    }

    /**
     * Determines whether a token is a grammatical particle/morpheme by checking
     * its absence from the main content word dictionary.
     *
     * @param token The target token string key to verify.
     * @return {@code true} if the token represents a particle, {@code false} if it is a content word.
     */
    public boolean isParticle(String token) {
        return MorphemeLoader.getMorphemes().containsKey(token);
    }

    /**
     * Triggers periodic SRS state calculations, inactivity decay, and active queue updates.
     */
    public void updateProgression() {
        progressionSystem.updateStates(vocabularyCache);
    }
}