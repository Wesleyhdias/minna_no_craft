package com.wesleyhdias.minnanocraft.config.modmenu;

import com.wesleyhdias.minnanocraft.config.data.ModConfig;

import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

/**
 * Scrollable container widget holding configurable options, controls, and category headers
 * for the SRS (Spaced Repetition System) configuration menu.
 */
public class ConfigListWidget extends ContainerObjectSelectionList<ConfigListEntry> {

    /**
     * Constructs the configuration list widget and populates it with interactive options.
     *
     * @param minecraft  The Minecraft client instance.
     * @param width      Widget total width.
     * @param height     Widget total height.
     * @param y          Vertical Y-position of the widget.
     * @param itemHeight Height allocated per row entry.
     */
    public ConfigListWidget(Minecraft minecraft, int width, int height, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
        loadConfigs(minecraft.font, width);
    }

    /**
     * Populates the widget entries grouped by functional categories:
     * SRS General Settings, Script Level Thresholds, and Exposure Gain/Loss Events.
     *
     * @param font        Font renderer for text layout calculations.
     * @param screenWidth Screen width used for horizontal centering.
     */
    private void loadConfigs(Font font, int screenWidth) {
        int center = screenWidth / 2;
        int rowWidth = 310;
        int leftX = center - (rowWidth / 2);
        int rightX = center + (rowWidth / 2);

        // --- Category: General SRS Settings ---

        this.addEntry(new ConfigListEntry(adder -> {
            Component titleText = Component.translatable("config.minnanocraft.title.srs_general_settings");

            // Calculate exact width of the title string in pixels
            int textWidth = font.width(titleText);

            // Calculate starting X position to achieve horizontal center alignment
            int startX = (screenWidth - textWidth) / 2;

            // Instantiate header StringWidget with calculated position and bounds
            StringWidget title = new StringWidget(startX, 0, textWidth, 20, titleText, font);

            adder.accept(title);
        }));

        this.addEntry(ConfigListEntry.createInt(
                font, leftX, rightX,
                Component.translatable("config.minnanocraft.srs_max_words"),
                ModConfig.getConfig().getMaxActiveWords(), 30,
                val -> ModConfig.getConfig().setMaxActiveWords(val)
        ));

        long defaultInactivityMs = 1000L * 60 * 60 * 24;
        this.addEntry(ConfigListEntry.createInt(
                font, leftX, rightX,
                Component.translatable("config.minnanocraft.srs_inactivity_time"),
                (int) (ModConfig.getConfig().getInactivityTimeThreshold() / (1000L * 60 * 60)),
                (int) (defaultInactivityMs / (1000L * 60 * 60)),
                val -> ModConfig.getConfig().setInactivityTimeThreshold(val * 1000L * 60 * 60)
        ));

        long defaultDemotionMs = 1000L * 60 * 60 * 24 * 3;
        this.addEntry(ConfigListEntry.createInt(
                font, leftX, rightX,
                Component.translatable("config.minnanocraft.srs_demotion_time"),
                Component.translatable("config.minnanocraft.hover.srs_demotion_time"),
                (int) (ModConfig.getConfig().getDemotionTimeThreshold() / (1000L * 60 * 60)),
                (int) (defaultDemotionMs / (1000L * 60 * 60)),
                val -> ModConfig.getConfig().setDemotionTimeThreshold(val * 1000L * 60 * 60)
        ));

        this.addEntry(ConfigListEntry.createFloat(
                font, leftX, rightX,
                Component.translatable("config.minnanocraft.srs_exp_loss_per_cycle"),
                ModConfig.getConfig().getExpLossPerInactivityCycle(), 5.0f,
                val -> ModConfig.getConfig().setExpLossPerInactivityCycle(val)
        ));

        this.addEntry(ConfigListEntry.createFloat(
                font, leftX, rightX,
                Component.translatable("config.minnanocraft.srs_relearn_multiplier"),
                ModConfig.getConfig().getRelearnMultiplier(), 2.5f,
                val -> ModConfig.getConfig().setRelearnMultiplier(val)
        ));

        this.addEntry(ConfigListEntry.createSlider(
                font, leftX, rightX,
                Component.translatable("config.minnanocraft.srs_max_exp_loss_percentage"),
                ModConfig.getConfig().getMaxExpLossPercentage(),
                0.7f,
                val -> ModConfig.getConfig().setMaxExpLossPercentage(val)
        ));

        // Empty row entry for visual spacing padding
        this.addEntry(new ConfigListEntry(adder -> {}));

        // --- Category: Script Level Thresholds ---
        this.addEntry(new ConfigListEntry(adder -> {
            Component titleText = Component.translatable("config.minnanocraft.title.srs_level_settings");

            // Calculate exact width of the title string in pixels
            int textWidth = font.width(titleText);

            // Calculate starting X position to achieve horizontal center alignment
            int startX = (screenWidth - textWidth) / 2;

            // Instantiate header StringWidget with calculated position and bounds
            StringWidget title = new StringWidget(startX, 0, textWidth, 20, titleText, font);

            adder.accept(title);
        }));

        this.addEntry(ConfigListEntry.createDouble(
                font, leftX, rightX,
                Component.translatable("config.minnanocraft.srs_exp_level_1"),
                ModConfig.getConfig().getExpLevel1(),
                15.0,
                val -> ModConfig.getConfig().setExpLevel1(val)
        ));

        this.addEntry(ConfigListEntry.createDouble(
                font, leftX, rightX,
                Component.translatable("config.minnanocraft.srs_exp_level_2"),
                ModConfig.getConfig().getExpLevel2(),
                30.0,
                val -> ModConfig.getConfig().setExpLevel2(val)
        ));

        this.addEntry(ConfigListEntry.createDouble(
                font, leftX, rightX,
                Component.translatable("config.minnanocraft.srs_exp_level_3"),
                ModConfig.getConfig().getExpLevel3(),
                45.0,
                val -> ModConfig.getConfig().setExpLevel3(val)
        ));

        this.addEntry(ConfigListEntry.createDouble(
                font, leftX, rightX,
                Component.translatable("config.minnanocraft.srs_exp_level_4"),
                ModConfig.getConfig().getExpLevel4(),
                100.0,
                val -> ModConfig.getConfig().setExpLevel4(val)
        ));

        this.addEntry(ConfigListEntry.createDouble(
                font, leftX, rightX,
                Component.translatable("config.minnanocraft.srs_exp_level_mastered"),
                ModConfig.getConfig().getMasteryExposure(),
                115.0,
                val -> ModConfig.getConfig().setMasteryExposure(val)
        ));

        // Empty row entry for visual spacing padding
        this.addEntry(new ConfigListEntry(adder -> {}));

        // --- Category: EXP Gain / Loss Rules ---
        this.addEntry(new ConfigListEntry(adder -> {
            Component titleText = Component.translatable("config.minnanocraft.title.srs_exp_gain_settings");

            // Calculate exact width of the title string in pixels
            int textWidth = font.width(titleText);

            // Calculate starting X position to achieve horizontal center alignment
            int startX = (screenWidth - textWidth) / 2;

            // Instantiate header StringWidget with calculated position and bounds
            StringWidget title = new StringWidget(startX, 0, textWidth, 20, titleText, font);

            adder.accept(title);
        }));

        this.addEntry(ConfigListEntry.createDouble(
                font, leftX, rightX,
                Component.translatable("config.minnanocraft.srs_exp_gain_per_hover"),
                Component.translatable("config.minnanocraft.hover.srs_exp_gain_per_hover"),
                ModConfig.getConfig().getEventHover(),
                2.0,
                val -> ModConfig.getConfig().setEventHover(val)
        ));
        this.addEntry(ConfigListEntry.createDouble(
                font, leftX, rightX,
                Component.translatable("config.minnanocraft.srs_exp_gain_per_hud_look"),
                Component.translatable("config.minnanocraft.hover.srs_exp_gain_per_hud_look"),
                ModConfig.getConfig().getEventHudSeen(),
                1.0,
                val -> ModConfig.getConfig().setEventHudSeen(val)
        ));
        this.addEntry(ConfigListEntry.createDouble(
                font, leftX, rightX,
                Component.translatable("config.minnanocraft.srs_exp_gain_per_seen"),
                ModConfig.getConfig().getEventSeen(),
                0.5,
                val -> ModConfig.getConfig().setEventSeen(val)
        ));
        this.addEntry(ConfigListEntry.createDouble(
                font, leftX, rightX,
                Component.translatable("config.minnanocraft.srs_exp_gain_per_lookup"),
                Component.translatable("config.minnanocraft.hover.srs_exp_gain_per_lookup"),
                ModConfig.getConfig().getEventHoverLookup(),
                2.0,
                val -> ModConfig.getConfig().setEventHoverLookup(val)
        ));
        this.addEntry(ConfigListEntry.createDouble(
                font, leftX, rightX,
                Component.translatable("config.minnanocraft.srs_exp_gain_per_dictionary_lookup"),
                Component.translatable("config.minnanocraft.hover.srs_exp_gain_per_dictionary_lookup"),
                ModConfig.getConfig().getEventLookup(),
                5.0,
                val -> ModConfig.getConfig().setEventLookup(val)
        ));
    }

    /**
     * Gets the custom width allocated for row rendering inside the selection list.
     *
     * @return The row width in pixels.
     */
    @Override
    public int getRowWidth() {
        return 320;
    }
}