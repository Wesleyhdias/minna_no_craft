package com.wesleyhdias.minnanocraft.language.dictionary;

import net.minecraft.client.Minecraft;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public record CompoundWord(
        Map<String, List<String>> local_translations,
        List<String> components
) {
    /**
     * Retrieves the list of translations based on the current language of the Minecraft client.
     * If the player's language is not available in the JSON dictionary returns null, to avoid NullPointerException.
     *
     * @param currentLang language code from the Minecraft client (e.g., "pt_br", "en_us", "es_es")
     * @return A list of translated strings for the current or fallback language.
     */
    public List<String> getLocalTranslations(String currentLang) {

        if (local_translations == null || !local_translations.containsKey(currentLang)) {
            return Collections.emptyList();
        }
        return local_translations.get(currentLang);
    }

    public List<String> getLocalTranslations() {
        String currentLang = Minecraft.getInstance().getLanguageManager().getSelected();

        return getLocalTranslations(currentLang);
    }
}