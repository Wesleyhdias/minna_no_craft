package com.wesleyhdias.minnanocraft;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinnaNoCraft implements ModInitializer {
	public static final String MOD_ID = "minnanocraft";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static long clientTicks = 0;

	public static long getClientTicks() {
		return clientTicks;
	}

	public static void incrementClientTicks() {
		clientTicks++;
	}

	@Override
	public void onInitialize() {
		LOGGER.info("MinnaNoCraft (Common) inicializado!");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}