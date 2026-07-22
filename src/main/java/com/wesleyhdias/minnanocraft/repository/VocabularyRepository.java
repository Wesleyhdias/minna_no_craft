package com.wesleyhdias.minnanocraft.repository;

import com.wesleyhdias.minnanocraft.data.models.WordProgress;
import com.wesleyhdias.minnanocraft.MinnaNoCraft;

import com.google.gson.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import com.google.gson.Gson;

import java.util.concurrent.ConcurrentHashMap;
import java.lang.reflect.Type;
import java.io.*;

public class VocabularyRepository {

    // Gson com PrettyPrinting para o JSON ficar bonito e legível no arquivo
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Caminho onde o progresso será salvo (na pasta raiz do Minecraft: ./config/minnanocraft/player_progress.json)
    private static final File SAVE_FILE = new File("config/minnanocraft/player_progress.json");

    /**
     * Lê o arquivo de progresso. Retorna um mapa vazio se o jogador for novo.
     */
    public ConcurrentHashMap<String, WordProgress> loadAll() {
        if (!SAVE_FILE.exists()) {
            MinnaNoCraft.LOGGER.info("Arquivo de progresso não encontrado. Criando novo perfil para o jogador.");
            return new ConcurrentHashMap<>();
        }

        try (Reader reader = new FileReader(SAVE_FILE)) {
            Type type = new TypeToken<ConcurrentHashMap<String, WordProgress>>() {}.getType();
            ConcurrentHashMap<String, WordProgress> loaded = GSON.fromJson(reader, type);

            MinnaNoCraft.LOGGER.info("Progresso do jogador carregado com sucesso!");
            return loaded != null ? loaded : new ConcurrentHashMap<>();

        } catch (Exception e) {
            MinnaNoCraft.LOGGER.error("Falha ao carregar o progresso do jogador! ", e);
            return new ConcurrentHashMap<>();
        }
    }

    /**
     * Salva o estado atual do progresso no arquivo JSON.
     */
    public void saveAll(ConcurrentHashMap<String, WordProgress> progressMap) {
        try {
            // Garante que as pastas (config/minnanocraft) existam antes de salvar
            SAVE_FILE.getParentFile().mkdirs();

            try (Writer writer = new FileWriter(SAVE_FILE)) {
                GSON.toJson(progressMap, writer);
            }
        } catch (Exception e) {
            MinnaNoCraft.LOGGER.error("Falha ao salvar o progresso do jogador! ", e);
        }
    }
}
