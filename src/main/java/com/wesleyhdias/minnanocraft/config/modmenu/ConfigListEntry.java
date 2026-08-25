package com.wesleyhdias.minnanocraft.config.modmenu;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.Font;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.NonNull;

/**
 * Represents a single row entry within the {@link ConfigListWidget}.
 * Manages rendering, interaction events, and value binding for labels,
 * text input boxes, sliders, and reset buttons.
 */
public class ConfigListEntry extends ContainerObjectSelectionList.Entry<ConfigListEntry> {

    /** Internal collection of UI widgets contained within this row entry. */
    private final List<AbstractWidget> widgets = new ArrayList<>();

    /**
     * Flexible constructor allowing custom widget layout construction.
     * Useful for section titles or blank spacing entries.
     *
     * @param builder Consumer function accepting widget instances to add to this entry.
     */
    public ConfigListEntry(Consumer<Consumer<AbstractWidget>> builder) {
        builder.accept(this.widgets::add);
    }

    /**
     * Renders each child widget within the entry row, updating its vertical position dynamically.
     *
     * @param graphics    The graphics extractor context.
     * @param mouseX      Current mouse cursor X position.
     * @param mouseY      Current mouse cursor Y position.
     * @param hovered     True if the entry is hovered by the mouse.
     * @param partialTick Render tick delta time.
     */
    @Override
    public void extractContent(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
        int y = this.getY();
        for (AbstractWidget widget : this.widgets) {
            widget.setY(y);
            widget.extractRenderState(graphics, mouseX, mouseY, partialTick);
        }
    }

    /**
     * Helper factory method to build standard numeric config entry rows containing a label, an EditBox, and a reset Button.
     *
     * @param <T>            Numeric data type (Integer, Float, etc.).
     * @param widgetAdder    Consumer receiving the constructed widgets.
     * @param font           Font renderer for label width calculation.
     * @param leftX          Left boundary X position for the label.
     * @param rightX         Right boundary X position for controls alignment.
     * @param labelComponent Text component displayed on the left.
     * @param tooltip        Optional tooltip component shown on hover.
     * @param currentValue   Active value from configuration data.
     * @param defaultValue   Default value used for reset action.
     * @param parser         Function mapping string inputs to value type T.
     * @param onSave         Callback executed when a valid new value is set.
     */
    private static <T> void buildCoreRow(
            Consumer<AbstractWidget> widgetAdder, Font font, int leftX, int rightX,
            Component labelComponent, Component tooltip, T currentValue, T defaultValue,
            Function<String, T> parser, Consumer<T> onSave
    ) {
        int resetButtonWidth = 60;
        int boxWidth = 35;

        // Calculate control positioning alignment relative to the right boundary
        int resetX = rightX - resetButtonWidth;
        int boxX = resetX - 6 - boxWidth;

        int textWidth = font.width(labelComponent);
        StringWidget label = new StringWidget(leftX, 0, textWidth, 20, labelComponent, font);

        EditBox editBox = new EditBox(font, boxX, 0, boxWidth, 20, labelComponent);
        editBox.setValue(String.valueOf(currentValue));

        // Attach tooltips to label and edit box if provided
        if (tooltip != null) {
            Tooltip mcTooltip = Tooltip.create(tooltip);
            label.setTooltip(mcTooltip);
            editBox.setTooltip(mcTooltip);
        }

        // Construct reset button to restore default configuration value
        Button resetBtn = Button.builder(Component.translatable("controls.reset"), button -> {
            editBox.setValue(String.valueOf(defaultValue));
            onSave.accept(defaultValue);
            button.active = false;
        }).bounds(resetX, 0, resetButtonWidth, 20).build();

        // Enable reset button only if current value differs from default
        resetBtn.active = !String.valueOf(currentValue).equals(String.valueOf(defaultValue));

        // Attach text change listener to validate and save input on the fly
        editBox.setResponder(text -> {
            try {
                T val = parser.apply(text.replace(",", "."));
                onSave.accept(val);
                resetBtn.active = !String.valueOf(val).equals(String.valueOf(defaultValue));
            } catch (Exception ignored) {}
        });

        widgetAdder.accept(label);
        widgetAdder.accept(editBox);
        widgetAdder.accept(resetBtn);
    }

    // --- INTEGER ENTRIES ---

    /** Factory method for creating an Integer configuration row entry without a tooltip. */
    public static ConfigListEntry createInt(Font font, int leftX, int rightX, Component label, int current, int def, Consumer<Integer> onSave) {
        return createInt(font, leftX, rightX, label, null, current, def, onSave);
    }

    /** Factory method for creating an Integer configuration row entry with a tooltip. */
    public static ConfigListEntry createInt(Font font, int leftX, int rightX, Component label, Component tooltip, int current, int def, Consumer<Integer> onSave) {
        return new ConfigListEntry(adder -> buildCoreRow(adder, font, leftX, rightX, label, tooltip, current, def, Integer::parseInt, onSave));
    }

    // --- FLOAT ENTRIES ---

    /** Factory method for creating a Float configuration row entry without a tooltip. */
    public static ConfigListEntry createFloat(Font font, int leftX, int rightX, Component label, float current, float def, Consumer<Float> onSave) {
        return createFloat(font, leftX, rightX, label, null, current, def, onSave);
    }

    /** Factory method for creating a Float configuration row entry with a tooltip. */
    public static ConfigListEntry createFloat(Font font, int leftX, int rightX, Component label, Component tooltip, float current, float def, Consumer<Float> onSave) {
        return new ConfigListEntry(adder -> buildCoreRow(adder, font, leftX, rightX, label, tooltip, current, def, Float::parseFloat, onSave));
    }

    // --- DOUBLE ENTRIES ---

    /** Factory method for creating a Double configuration row entry without a tooltip. */
    public static ConfigListEntry createDouble(Font font, int leftX, int rightX, Component label, double current, double def, Consumer<Double> onSave) {
        return createDouble(font, leftX, rightX, label, null, current, def, onSave);
    }

    /** Factory method for creating a Double configuration row entry with a tooltip. */
    public static ConfigListEntry createDouble(Font font, int leftX, int rightX, Component label, Component tooltip, double current, double def, Consumer<Double> onSave) {
        return new ConfigListEntry(adder -> buildCoreRow(
                adder, font, leftX, rightX, label, tooltip, (float) current, (float) def,
                Float::parseFloat, val -> onSave.accept((double) val)
        ));
    }

    // --- SLIDER ENTRIES (PERCENTAGE) ---

    /** Factory method for creating a percentage slider configuration entry without a tooltip. */
    public static ConfigListEntry createSlider(Font font, int leftX, int rightX, Component label, float current, float def, Consumer<Float> onSave) {
        return createSlider(font, leftX, rightX, label, null, current, def, onSave);
    }

    /** Factory method for creating a percentage slider configuration entry with a tooltip. */
    public static ConfigListEntry createSlider(Font font, int leftX, int rightX, Component label, Component tooltip, float current, float def, Consumer<Float> onSave) {
        return new ConfigListEntry(adder -> {
            int resetButtonWidth = 60;
            int sliderWidth = 100;

            int resetX = rightX - resetButtonWidth;
            int sliderX = resetX - 6 - sliderWidth;

            int textWidth = font.width(label);
            StringWidget labelWidget = new StringWidget(leftX, 0, textWidth, 20, label, font);

            // Holder array to allow referencing the button inside the slider class
            Button[] resetBtnHolder = new Button[1];

            class PercentSlider extends AbstractSliderButton {
                public PercentSlider(int x, int y, int width, int height, double value) {
                    super(x, y, width, height, Component.empty(), value);
                    this.updateMessage();
                }

                @Override
                protected void updateMessage() {
                    this.setMessage(Component.literal(Math.round(this.value * 100.0) + "%"));
                }

                @Override
                protected void applyValue() {
                    float savedValue = (float) this.value;
                    onSave.accept(savedValue);

                    if (resetBtnHolder[0] != null) {
                        resetBtnHolder[0].active = Math.round(savedValue * 100) != Math.round(def * 100);
                    }
                }

                public void forceValue(double newValue) {
                    this.value = newValue;
                    this.updateMessage();
                    this.applyValue();
                }
            }

            PercentSlider slider = new PercentSlider(sliderX, 0, sliderWidth, 20, current);

            if (tooltip != null) {
                Tooltip mcTooltip = Tooltip.create(tooltip);
                labelWidget.setTooltip(mcTooltip);
                slider.setTooltip(mcTooltip);
            }

            resetBtnHolder[0] = Button.builder(Component.translatable("controls.reset"), button -> {
                slider.forceValue(def);
                button.active = false;
            }).bounds(resetX, 0, resetButtonWidth, 20).build();

            resetBtnHolder[0].active = Math.round(current * 100) != Math.round(def * 100);

            adder.accept(labelWidget);
            adder.accept(slider);
            adder.accept(resetBtnHolder[0]);
        });
    }

    /**
     * Visitor callback to register internal widgets with the UI system.
     *
     * @param widgetVisitor Consumer receiving each child widget.
     */
    @Override
    public void visitWidgets(@NonNull Consumer<AbstractWidget> widgetVisitor) {
        for (AbstractWidget widget : this.widgets) {
            widgetVisitor.accept(widget);
        }
    }

    /**
     * Provides child GUI event listeners for click and key event propagation.
     *
     * @return List of interactive widgets.
     */
    @Override
    public @NonNull List<? extends GuiEventListener> children() {
        return this.widgets;
    }

    /**
     * Provides narratable entries for Minecraft accessibility features.
     *
     * @return List of narratable widgets.
     */
    @Override
    public @NonNull List<? extends NarratableEntry> narratables() {
        return this.widgets;
    }
}