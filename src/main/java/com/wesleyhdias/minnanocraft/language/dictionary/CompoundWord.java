package com.wesleyhdias.minnanocraft.language.dictionary;

import net.minecraft.client.Minecraft;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public record CompoundWord(
        Map<String, List<String>> local_translations,
        List<String> components,
        String separator
) {
    /**
     * Retorna a lista de traduções para o idioma solicitado.
     * Retorna uma lista vazia caso não exista, evitando NullPointerException.
     */
    public List<String> getLocalTranslations() {

        String currentLang = Minecraft.getInstance().getLanguageManager().getSelected();

        if (local_translations == null || !local_translations.containsKey(currentLang)) {
            return Collections.emptyList();
        }
        return local_translations.get(currentLang);
    }

    /**
     * Garante que o separador nunca seja nulo.
     * Se não for definido no JSON, assume string vazia (sem espaço).
     */
    public String getSafeSeparator() {
        return separator != null ? separator : "";
    }
}