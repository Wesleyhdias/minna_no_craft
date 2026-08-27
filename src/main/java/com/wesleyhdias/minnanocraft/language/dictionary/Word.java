package com.wesleyhdias.minnanocraft.language.dictionary;

import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.Map;

public record Word(Map<String, List<String>> translations, String kanji, String hiragana, String romaji) {

    /**
     * Retrieves the list of translations based on the current language of the Minecraft client.
     * If the player's language is not available in the JSON dictionary, it defaults to "en_us".
     *
     * @return A list of translated strings for the current or fallback language.
     */
    public List<String> getLocalTranslations() {
        // 1. Get the current language code from the Minecraft client (e.g., "pt_br", "en_us", "es_es")
        String currentLang = Minecraft.getInstance().getLanguageManager().getSelected();

        // 2. Attempt to fetch translations for the active language
        List<String> localList = this.translations.get(currentLang);

        // 3. Primary Fallback: If not found default to "en_us"
        if (localList == null || localList.isEmpty()) {
            localList = this.translations.get("en_us");
        }

        // 4. Secondary Fallback: If "en_us" is missing for some reason, try "pt_br"
        if (localList == null || localList.isEmpty()) {
            localList = this.translations.get("pt_br");
        }

        return localList;
    }
}
