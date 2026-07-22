package com.wesleyhdias.minnanocraft.services;

import com.wesleyhdias.minnanocraft.repository.VocabularyRepository;
import com.wesleyhdias.minnanocraft.data.models.WordProgress;
import com.wesleyhdias.minnanocraft.data.models.Event;

import java.util.concurrent.ConcurrentHashMap;

public class VocabularyManager {

    // Cache em memória que substitui o "self.data" do Python
    private static ConcurrentHashMap<String, WordProgress> vocabularyCache = new ConcurrentHashMap<>();

    // Dependências
    private static final VocabularyRepository repository = new VocabularyRepository();
    private static final ProgressionSystem progressionSystem = new ProgressionSystem();

    // Carrega os dados do disco ao iniciar o mod ou entrar no mundo.
    public static void load() {
        vocabularyCache = repository.loadAll();
    }


    // Salva o estado atual no disco (Auto-save).
    public static void save() {
        repository.saveAll(vocabularyCache);
    }


    public static boolean exists(String token) {
        return vocabularyCache.containsKey(token);
    }

    public static WordProgress getProgress(String token) {
        return vocabularyCache.get(token);
    }

    public static WordProgress getOrCreateProgress(String token) {
        return vocabularyCache.computeIfAbsent(token, WordProgress::new);
    }

    public static ConcurrentHashMap<String, WordProgress> getAll() {
        return vocabularyCache;
    }

    // =========================================================
    // GATILHOS DO JOGO (Mixins chamarão estes métodos)
    // =========================================================
    public static void registerEvent(String token, Event event, long currentTick) {
        WordProgress progress = getOrCreateProgress(token);

        // --- FILTRO ANTI-SPAM ---
        // Se a palavra já foi vista recentemente (menos de 20 ticks / 1 segundo atrás), ignora!
        if (progress.getLastSeen() != null && (currentTick - progress.getLastSeen()) < 20) {
            return;
        }

        if (progress.getFirstSeen() == null) {
            progress.setFirstSeen(currentTick);
        }
        progress.setLastSeen(currentTick);

        progressionSystem.applyEvent(progress, event, currentTick);
    }

    /**
     * Loop temporal do mod (chamado a cada ~10 segundos pelo relógio do jogo).
     * Atualiza quedas de esquecimento e avanço de níveis.
     */
    public static void updateProgression(long currentTick) {

        // Descomente quando criarmos o ProgressionSystem
         progressionSystem.updateStates(vocabularyCache, currentTick);

        // Dispara o Auto-Save a cada ciclo de atualização
        save();
    }
}