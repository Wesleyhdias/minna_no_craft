package com.wesleyhdias.minnanocraft;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main common initializer for MinnaNoCraft.
 */
public class MinnaNoCraft implements ModInitializer {

	public static final String MOD_ID = "minnanocraft";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("MinnaNoCraft (Common) initialized successfully!");
	}

	/**
	 * Helper method to create namespaced identifiers for the mod.
	 *
	 * @param path The resource path.
	 * @return A new Identifier under the minnanocraft namespace.
	 */
	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}