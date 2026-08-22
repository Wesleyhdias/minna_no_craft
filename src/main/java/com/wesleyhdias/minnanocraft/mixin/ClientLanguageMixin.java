package com.wesleyhdias.minnanocraft.mixin;


import com.wesleyhdias.minnanocraft.language.builder.PortugueseItemNameBuilder;
import com.wesleyhdias.minnanocraft.language.resolver.TranslationModeResolver;
import net.minecraft.client.resources.language.ClientLanguage;
import com.wesleyhdias.minnanocraft.language.builder.ItemNameBuilder;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

/* do codigo para extrair os lang
import org.spongepowered.asm.mixin.Unique;

import com.google.gson.GsonBuilder;
import com.google.gson.Gson;
 */

@Mixin(ClientLanguage.class)
public class ClientLanguageMixin {

    /* do codigo para extrair os lang
    @Unique
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    */


    @Inject(method = "getOrDefault", at = @At("RETURN"), cancellable = true)
    private void onGetOrDefault(String key, String defaultValue, CallbackInfoReturnable<String> cir) {

        String originalText = cir.getReturnValue();
        String customText;

        if (TranslationModeResolver.useJapanese(key)) {

            customText = ItemNameBuilder.build(key);

        }else {

            customText = PortugueseItemNameBuilder.build(
                    key,
                    originalText
            );
        }

        if(customText != null) {
            cir.setReturnValue(customText);
        }
    }


    /* código usado para extrair todas as chaves e traduções do jogo
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
     */
}
