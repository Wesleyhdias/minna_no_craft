package com.wesleyhdias.minnanocraft.mixin;

import com.wesleyhdias.minnanocraft.language.builder.CurrentLangItemNameBuilder;
import com.wesleyhdias.minnanocraft.language.resolver.TranslationModeResolver;
import com.wesleyhdias.minnanocraft.language.builder.JapaneseItemNameBuilder;
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
import java.util.TreeMap;
import java.util.List;
import java.io.Writer;
import java.util.Map;

@Mixin(ClientLanguage.class)
public class ClientLanguageMixin {

    @Unique
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

//    @Inject(method = "getOrDefault", at = @At("RETURN"), cancellable = true)
//    private void onGetOrDefault(String key, String defaultValue, CallbackInfoReturnable<String> cir) {
//        String originalText = cir.getReturnValue();
//        String customText;
//
//        if (TranslationModeResolver.useJapanese(key)) {
//            customText = JapaneseItemNameBuilder.build(key);
//        } else {
//            customText = CurrentLangItemNameBuilder.build(
//                    key,
//                    originalText
//            );
//        }
//
//        if (customText != null) {
//            cir.setReturnValue(customText);
//        }
//    }

    @Inject(method = "loadFrom", at = @At("RETURN"), remap = false)
    private static void dumpLanguage(
            ResourceManager resourceManager,
            List<String> languageStack,
            boolean defaultRightToLeft,
            CallbackInfoReturnable<ClientLanguage> cir
    ) {
        if (!Boolean.parseBoolean(System.getProperty("minnanocraft.dump", "false"))) {
            return;
        }

        ClientLanguage language = cir.getReturnValue();
        Map<String, String> allTranslations = ((ClientLanguageAccessor) language).getStorage();

        // O último idioma da pilha é o idioma realmente selecionado
        String languageCode = languageStack.getLast();

        List<String> allowedPrefixes = List.of(
                "item.minecraft.",
                "block.minecraft.",
                "entity.minecraft.",
                "death.attack.",
                "container.",
                "menu."
        );

        List<String> blacklist = List.of(
                "entity.minecraft.ender_pearl",
                "entity.minecraft.potion",
                "entity.minecraft.experience_orb",
                "entity.minecraft.item",
                "entity.minecraft.falling_block"
        );

        Map<String, String> filteredTranslations = new TreeMap<>();

        for (Map.Entry<String, String> entry : allTranslations.entrySet()) {
            String key = entry.getKey();
            boolean isAllowed = allowedPrefixes.stream().anyMatch(key::startsWith);
            boolean isNotBlacklisted = blacklist.stream().noneMatch(key::equals);

            if (isAllowed && isNotBlacklisted) {
                filteredTranslations.put(key, entry.getValue());
            }
        }

        Path file = FabricLoader.getInstance()
                .getGameDir()
                .resolve("lang_dump")
                .resolve(languageCode + "_filtered.json");

        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(filteredTranslations, writer);
            }
            MinnaNoCraft.LOGGER.info("Idioma {} extraído e filtrado com sucesso! Total de chaves: {}", languageCode, filteredTranslations.size());
        } catch (IOException e) {
            MinnaNoCraft.LOGGER.error("Falha ao exportar idioma {}", languageCode, e);
        }
    }
}