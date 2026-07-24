package com.wesleyhdias.minnanocraft.data.provider;

/**
 * Strategy interface for resolving a token string into its corresponding translation or script.
 */
public interface TokenProvider {

    /**
     * Resolves a token into its appropriate text representation.
     *
     * @param token The token string to resolve.
     * @return The resolved text, or null if this provider cannot process the given token.
     */
    String resolve(String token);
}
