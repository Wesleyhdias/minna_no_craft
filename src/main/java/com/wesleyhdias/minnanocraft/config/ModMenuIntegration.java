package com.wesleyhdias.minnanocraft.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.wesleyhdias.minnanocraft.config.modmenu.MinnaNoCraftConfigScreen;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return MinnaNoCraftConfigScreen::new;
    }
}