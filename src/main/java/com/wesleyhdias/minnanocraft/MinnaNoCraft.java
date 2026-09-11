package com.wesleyhdias.minnanocraft;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.List;

/**
 * Main common entrypoint for the MinnaNoCraft mod.
 * Handles common initialization tasks, global constants, and helper utilities.
 */
public class MinnaNoCraft implements ModInitializer {

	/** The unique namespace identifier for the mod. */
	public static final String MOD_ID = "minnanocraft";

	/** Global logger instance for cross-environment logging. */
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/** Immutable list of supported native/target language codes available in the mod. */
	public static final List<String> SUPPORTED_LANGUAGES = List.of("pt_br", "en_us");

	/**
	 * Executes common mod initialization logic when Fabric loads the mod.
	 */
	@Override
	public void onInitialize() {
		LOGGER.info("MinnaNoCraft (Common) initialized successfully!");
	}

	/**
	 * Helper method to create namespaced identifiers under the mod's ID.
	 *
	 * @param path The resource path within the namespace.
	 * @return A new {@link Identifier} under the {@value MOD_ID} namespace.
	 */
	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}