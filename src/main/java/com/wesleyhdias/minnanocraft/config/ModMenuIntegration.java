package com.wesleyhdias.minnanocraft.config;

import com.wesleyhdias.minnanocraft.config.modmenu.MinnaNoCraftConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * ModMenu integration entrypoint for the mod.
 * <p>
 * Provides ModMenu with a factory to construct and display the mod's
 * configuration interface screen ({@link MinnaNoCraftConfigScreen}).
 */
public class ModMenuIntegration implements ModMenuApi {

    /**
     * Supplies the factory used by ModMenu to create the mod settings screen.
     *
     * @return A {@link ConfigScreenFactory} constructing {@link MinnaNoCraftConfigScreen} instances.
     */
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return MinnaNoCraftConfigScreen::new;
    }
}