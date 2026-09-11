package com.wesleyhdias.minnanocraft;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import org.jspecify.annotations.NonNull;

/**
 * Main Fabric Data Generator entrypoint for the MinnaNoCraft mod.
 * <p>
 * This class is invoked by Fabric during the data generation phase to register
 * custom data providers such as recipes, models, loot tables, and tags.
 */
public class MinnaNoCraftDataGenerator implements DataGeneratorEntrypoint {

	/**
	 * Initializes and registers mod-specific data providers with the Fabric Data Generator.
	 *
	 * @param fabricDataGenerator The {@link FabricDataGenerator} instance used to configure
	 *                            and attach data provider packs.
	 */
	@Override
	public void onInitializeDataGenerator(@NonNull FabricDataGenerator fabricDataGenerator) {

	}
}