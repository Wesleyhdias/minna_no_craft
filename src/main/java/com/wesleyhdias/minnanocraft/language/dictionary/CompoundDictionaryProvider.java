package com.wesleyhdias.minnanocraft.language.dictionary;

import com.wesleyhdias.minnanocraft.language.resolver.DifficultyResolver;
import com.wesleyhdias.minnanocraft.language.resolver.TokenProvider;

/**
 * Token provider implementation responsible for resolving compound word tokens.
 * <p>
 * Queries the {@link CompoundDictionaryLoader} for compound word definitions and delegates
 * script rendering to {@link DifficultyResolver#renderCompound(CompoundWord)}.
 */
public class CompoundDictionaryProvider implements TokenProvider {

    /**
     * Resolves a compound word token into its rendered Japanese script string.
     *
     * @param token The dictionary token key to resolve.
     * @return The rendered compound string, or {@code null} if the token is not a compound word.
     */
    @Override
    public String resolve(String token) {
        CompoundWord compound = CompoundDictionaryLoader.getDictionary().get(token);

        if (compound == null) {
            return null; // Token is not a compound word
        }

        return DifficultyResolver.renderCompound(compound);
    }
}