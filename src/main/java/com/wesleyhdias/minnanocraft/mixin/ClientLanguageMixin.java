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
import java.io.Writer;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.List;
import java.util.Map;

@Mixin(ClientLanguage.class)
public class ClientLanguageMixin {

    @Unique
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();


    // código usado para extrair todas as chaves e traduções do jogo
    @Inject(method = "loadFrom", at = @At("RETURN"), remap = false)
    private static void dumpLanguage(
            ResourceManager resourceManager,
            List<String> languageStack,
            boolean defaultRightToLeft,
            CallbackInfoReturnable<ClientLanguage> cir
    ) {
        ClientLanguage language = cir.getReturnValue();

        Map<String, String> translations = ((ClientLanguageAccessor) language).getStorage();

        // Último idioma da pilha é o idioma realmente selecionado
        String languageCode = languageStack.getLast();

        Path file = FabricLoader.getInstance()
                .getGameDir()
                .resolve("lang_dump")
                .resolve(languageCode + ".json");

        try {
            Files.createDirectories(file.getParent());

            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(translations, writer);
            }

            MinnaNoCraft.LOGGER.info("idioma {} extraido para run/lang_dump", languageCode);

        } catch (IOException e) {
            MinnaNoCraft.LOGGER.error("Falha ao exportar idioma {}", languageCode, e);
        }
    }
}
