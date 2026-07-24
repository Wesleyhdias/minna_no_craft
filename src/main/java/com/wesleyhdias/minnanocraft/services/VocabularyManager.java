package com.wesleyhdias.minnanocraft.services;

import com.wesleyhdias.minnanocraft.data.loader.DictionaryLoader;
import com.wesleyhdias.minnanocraft.repository.VocabularyRepository;
import com.wesleyhdias.minnanocraft.data.models.WordProgress;
import com.wesleyhdias.minnanocraft.data.models.Event;

import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;

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


    // Salva o estado atual no disco (Autosave).
    public static void save() {
        repository.saveAll(vocabularyCache);
    }

    public static WordProgress getProgress(String token) {
        return vocabularyCache.get(token);
    }

    public static WordProgress getOrCreateProgress(String token) {
        return vocabularyCache.computeIfAbsent(token, WordProgress::new);
    }

    // GATILHOS DO JOGO (Mixins chamarão estes métodos)
    public static void registerEvent(String token, Event event, long currentTick) {
        WordProgress progress = getOrCreateProgress(token);
        long now = System.currentTimeMillis();

        // --- FILTRO ANTISPAM ---
        // Se a palavra já foi vista recentemente (1 segundo atrás), ignora!
        if (progress.getLastSeen() != null && (now- progress.getLastSeen()) < 1000) {
            return;
        }

        if (progress.getFirstSeen() == null) {
            progress.setFirstSeen(currentTick);
        }
        progress.setLastSeen(currentTick);

        progressionSystem.applyEvent(progress, event);
    }

    /**
     * Se o token NÃO existe no dicionário de traduções, significa que ele é um
     * morfema (partícula) e não aparece no modo português.
     */
    public static boolean isParticle(String token) {
        // Substitua 'DictionaryLoader.getDictionary()' pelo código real que você
        // usa no PortugueseItemNameBuilder para buscar a tradução da palavra.
        return !DictionaryLoader.getDictionary().containsKey(token);
    }

    /**
     * Descobre qual token da estrutura precisa de pontos primeiro.
     * Cria um sistema de "ondas": conteúdo evolui primeiro, partículas correm atrás para alcançar.
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

        // Segurança caso o item só tenha partículas (raro/impossível, mas evita crash)
        if (contentTokens.isEmpty()) {
            return getLowestLevelToken(particleTokens);
        }

        // 1. Descobre o nível em que a palavra de conteúdo MAIS ATRASADA está
        int minContentLevel = 3;
        for (String token : contentTokens) {
            WordProgress p = getProgress(token);
            int level = (p != null) ? p.getScriptLevel() : 0;
            if (level < minContentLevel) {
                minContentLevel = level;
            }
        }

        // 2. MODO PORTUGUÊS (Nível 0): Partículas estão invisíveis. Foca 100% no conteúdo.
        if (minContentLevel == 0) {
            return getFirstTokenAtLevel(contentTokens, 0);
        }

        // 3. MODO JAPONÊS ATIVADO (Nível >= 1): Partículas apareceram na tela!
        if (!particleTokens.isEmpty()) {
            int minParticleLevel = 3;
            for (String token : particleTokens) {
                WordProgress p = getProgress(token);
                int level = (p != null) ? p.getScriptLevel() : 0;
                if (level < minParticleLevel) {
                    minParticleLevel = level;
                }
            }

            // Se a partícula estiver com um nível MENOR que o do conteúdo (ex: Conteúdo 2, Partícula 1),
            // a partícula ganha prioridade máxima para "alcançar" a onda!
            if (minParticleLevel < minContentLevel) {
                return getFirstTokenAtLevel(particleTokens, minParticleLevel);
            }
        }

        // 4. SE HOUVER EMPATE (Ex: Todos no Nível 1 ou Todos no Nível 2):
        // As palavras de conteúdo sempre têm o privilégio de inaugurar o próximo nível!
        return getFirstTokenAtLevel(contentTokens, minContentLevel);
    }

    /**
     * Método auxiliar para buscar o menor nível de uma lista genérica (fallback).
     */
    private static String getLowestLevelToken(List<String> tokens) {
        int minLevel = 3;
        for (String token : tokens) {
            WordProgress p = getProgress(token);
            int level = (p != null) ? p.getScriptLevel() : 0;
            if (level < minLevel) minLevel = level;
        }
        return getFirstTokenAtLevel(tokens, minLevel);
    }

    /**
     * Método auxiliar para encontrar a primeira palavra que está no nível alvo (lê da esq -> dir).
     */
    private static String getFirstTokenAtLevel(List<String> tokens, int targetLevel) {
        for (String token : tokens) {
            WordProgress p = getProgress(token);
            int level = (p != null) ? p.getScriptLevel() : 0;
            if (level == targetLevel) {
                return token;
            }
        }
        return tokens.getFirst(); // Fallback
    }

    /**
     * Loop temporal do mod (chamado a cada ~10 segundos pelo relógio do jogo).
     * Atualiza quedas de esquecimento e avanço de níveis.
     */
    public static void updateProgression() {

         progressionSystem.updateStates(vocabularyCache);
    }
}