package com.wesleyhdias.minnanocraft.services;

import com.wesleyhdias.minnanocraft.data.loader.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.data.provider.DictionaryProvider;
import com.wesleyhdias.minnanocraft.data.provider.MorphemeProvider;
import com.wesleyhdias.minnanocraft.data.provider.TokenProvider;

import java.util.List;

public class ItemNameBuilder {

    private static final List<TokenProvider> PROVIDERS = List.of(
            new DictionaryProvider(),
            new MorphemeProvider()
    );

    public static String build(String translationKey) {

        List<String> structure = ItemStructureLoader.getStructures().get(translationKey);

        // Não existe estrutura -> devolve a própria chave.
        if (structure == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();

        for (String token : structure) {

            result.append(resolve(token));
        }
        return result.toString();
    }

    private static String resolve(String token) {

        for (TokenProvider provider : PROVIDERS) {

            String value = provider.resolve(token);

            if (value != null) {
                return value;
            }
        }
        // Token desconhecido:
        // mantém exatamente como veio.
        return token;
    }

}