package com.wesleyhdias.minnanocraft.mixin.acessor;

import net.minecraft.client.resources.language.ClientLanguage;

import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Map;

/**
 * Accessor interface targeting Minecraft's {@link ClientLanguage} class.
 * <p>
 * Exposes the private {@code storage} map containing active key-value
 * translation mappings loaded by the client language manager.
 */
@Mixin(ClientLanguage.class)
public interface ClientLanguageAccessor {

    /**
     * Retrieves the underlying map holding all loaded translation key-value pairs.
     *
     * @return The internal {@link Map} of translation keys to localized strings.
     */
    @Accessor("storage")
    Map<String, String> getStorage();
}