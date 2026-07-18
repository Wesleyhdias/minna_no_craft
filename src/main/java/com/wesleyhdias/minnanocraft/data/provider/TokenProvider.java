package com.wesleyhdias.minnanocraft.data.provider;

public interface TokenProvider {

    /**
     * Retorna a tradução do token.
     * Se não conhecer o token, retorna null.
     */
    String resolve(String token);

}
