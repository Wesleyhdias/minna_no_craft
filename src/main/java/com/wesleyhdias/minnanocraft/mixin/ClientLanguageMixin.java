package com.wesleyhdias.minnanocraft.mixin;

import com.wesleyhdias.minnanocraft.mixin.acessor.ClientLanguageAccessor;
import com.wesleyhdias.minnanocraft.MinnaNoCraft;

import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.server.packs.resources.ResourceManager;
import net.fabricmc.loader.api.FabricLoader;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Mixin;

import com.google.gson.GsonBuilder;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeMap;
import java.util.List;
import java.io.Writer;
import java.util.Map;

/**
 * Mixin targeting Minecraft's {@link ClientLanguage} class to export filtered translation files.
 * <p>
 * Functions as a developer utility that extracts active language translation keys to
 * JSON files in the {@code lang_dump} directory when the system property {@code minnanocraft.dump} is enabled.
 */
@Mixin(ClientLanguage.class)
public class ClientLanguageMixin {

    @Unique
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    /**
     * Intercepts language loading to filter and dump relevant translation key-value pairs to disk.
     *
     * @param resourceManager    The active Minecraft resource manager instance.
     * @param languageStack      List of loaded language codes (the active language is at the end).
     * @param defaultRightToLeft Whether the default language text direction is right-to-left.
     * @param cir                The {@link CallbackInfoReturnable} containing the initialized {@link ClientLanguage}.
     */
    @Inject(method = "loadFrom", at = @At("RETURN"), remap = false)
    private static void dumpLanguage(
            ResourceManager resourceManager,
            List<String> languageStack,
            boolean defaultRightToLeft,
            CallbackInfoReturnable<ClientLanguage> cir
    ) {

        // Executes only if the dump property is explicitly set to true in system properties
        if (!Boolean.parseBoolean(System.getProperty("minnanocraft.dump", "false"))) {
            return;
        }

        ClientLanguage language = cir.getReturnValue();
        Map<String, String> allTranslations = ((ClientLanguageAccessor) language).getStorage();

        // The last language code in the stack represents the active selection
        String languageCode = languageStack.getLast();

        // Prefixes determining which translation keys should be captured
        Map<String, String> filteredTranslations = getFilteredTranslations(allTranslations);

        Path file = FabricLoader.getInstance()
                .getGameDir()
                .resolve("lang_dump")
                .resolve(languageCode + "_filtered.json");

        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(filteredTranslations, writer);
            }
            MinnaNoCraft.LOGGER.info("Successfully extracted and filtered language {}. Total keys: {}", languageCode, filteredTranslations.size());
        } catch (IOException e) {
            MinnaNoCraft.LOGGER.error("Failed to export language {}", languageCode, e);
        }
    }

    /**
     * Filters raw Minecraft translation mappings using pre-defined allowed key prefixes
     * and a blacklist of excluded keys.
     *
     * @param allTranslations The complete map of loaded translation keys and localized strings.
     * @return A sorted {@link Map} containing only the filtered vocabulary translation entries.
     */
    @Unique
    private static Map<String, String> getFilteredTranslations(Map<String, String> allTranslations) {
        // Namespace prefixes defining translation categories targeted for export
        List<String> allowedPrefixes = List.of(
                "item.minecraft.",
                "block.minecraft.",
                "entity.minecraft.",
                "death.attack.",
                "container.",
                "menu."
        );

        // Specific entity or item translation keys to explicitly exclude
        Set<String> blacklist = Set.of(
                "entity.minecraft.ender_pearl",
                "entity.minecraft.potion",
                "entity.minecraft.experience_orb",
                "entity.minecraft.item",
                "entity.minecraft.falling_block"
        );

        return createFilteredTranslations(allTranslations, blacklist, allowedPrefixes);
    }

    /**
     * Evaluates translation entries against blacklist and prefix rules, returning a
     * sorted map of entries that pass validation.
     *
     * @param allTranslations The complete map of loaded translation entries.
     * @param blacklist       Set of translation keys to strictly exclude.
     * @param allowedPrefixes List of key prefixes that candidates must start with.
     * @return A sorted {@link TreeMap} containing validated key-value translation pairs.
     */
    @Unique
    private static Map<String, String> createFilteredTranslations(
            Map<String, String> allTranslations,
            Set<String> blacklist,
            List<String> allowedPrefixes
    ) {
        // TreeMap ensures output JSON keys are sorted alphabetically
        Map<String, String> filteredTranslations = new TreeMap<>();

        for (Map.Entry<String, String> entry : allTranslations.entrySet()) {
            String key = entry.getKey();

            // Skip execution early if the key is explicitly blacklisted
            boolean isNotBlacklisted = !blacklist.contains(key);
            if (!isNotBlacklisted) continue;

            // Check if the key matches any allowed domain prefix
            boolean isAllowed = false;
            for (String prefix : allowedPrefixes) {
                if (key.startsWith(prefix)) {
                    isAllowed = true;
                    break;
                }
            }

            // Retain key if validation succeeded
            if (isAllowed) {
                filteredTranslations.put(key, entry.getValue());
            }
        }
        return filteredTranslations;
    }
}