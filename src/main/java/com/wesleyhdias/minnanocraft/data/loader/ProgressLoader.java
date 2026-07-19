package com.wesleyhdias.minnanocraft.data.loader;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import com.wesleyhdias.minnanocraft.MinnaNoCraft;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.io.InputStream;
import java.util.HashMap;
import java.io.Reader;
import java.util.Map;

public class ProgressLoader {

    private static Map<String, Integer> progress;

    public static Map<String, Integer> getProgress() {

        if (progress == null) {
            load();
        }

        return progress;
    }

    private static void load() {

        try (InputStream is = ProgressLoader.class
                .getClassLoader()
                .getResourceAsStream("assets/lang/player_progress_test.json")
        ) {
            if (is == null) {
                MinnaNoCraft.LOGGER.error("player progress file not found! ");
                progress = new HashMap<>();
                return;
            }

            Reader reader = new InputStreamReader(is);

            Type type =
                    new TypeToken<Map<String, Integer>>() {}.getType();

            progress = new Gson().fromJson(reader, type);
            MinnaNoCraft.LOGGER.info("player progress file loaded successfully! ");

        } catch (Exception e) {
            MinnaNoCraft.LOGGER.error("progress loader found an error! ", e);
            progress = new HashMap<>();
        }
    }

    public static int getLevel(String token) {

        return getProgress().getOrDefault(token, 0);
    }
}