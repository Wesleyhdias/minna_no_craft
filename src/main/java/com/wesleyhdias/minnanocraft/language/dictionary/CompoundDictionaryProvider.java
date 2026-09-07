package com.wesleyhdias.minnanocraft.language.dictionary;

import com.wesleyhdias.minnanocraft.language.resolver.DifficultyResolver;
import com.wesleyhdias.minnanocraft.language.resolver.TokenProvider;

public class CompoundDictionaryProvider implements TokenProvider {

    @Override
    public String resolve(String token) {
        CompoundWord compound = CompoundDictionaryLoader.getDictionary().get(token);

        if (compound == null) {
            return null; // Não é uma palavra composta
        }

        // Delega toda a lógica de construção e checagem de nível para o DifficultyResolver
        return DifficultyResolver.renderCompound(compound);
    }
}