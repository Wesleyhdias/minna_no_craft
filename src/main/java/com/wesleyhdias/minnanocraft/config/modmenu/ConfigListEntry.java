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

public class ConfigListEntry extends ContainerObjectSelectionList.Entry<ConfigListEntry> {

    private final List<AbstractWidget> widgets = new ArrayList<>();

    // Construtor original (mantido para espaçamentos vazios ou títulos manuais)
    public ConfigListEntry(Consumer<Consumer<AbstractWidget>> builder) {
        builder.accept(this.widgets::add);
    }

    @Override
    public void extractContent(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
        int y = this.getY();
        for (AbstractWidget widget : this.widgets) {
            widget.setY(y);
            widget.extractRenderState(graphics, mouseX, mouseY, partialTick);
        }
    }

    private static <T> void buildCoreRow(
            Consumer<AbstractWidget> widgetAdder, Font font, int leftX, int rightX,
            Component labelComponent, Component tooltip, T currentValue, T defaultValue,
            Function<String, T> parser, Consumer<T> onSave
    ) {
        int resetButtonWidth = 60;
        int boxWidth = 35;

        int resetX = rightX - resetButtonWidth;
        int boxX = resetX - 6 - boxWidth;

        int textWidth = font.width(labelComponent);
        StringWidget label = new StringWidget(leftX, 0, textWidth, 20, labelComponent, font);

        EditBox editBox = new EditBox(font, boxX, 0, boxWidth, 20, labelComponent);
        editBox.setValue(String.valueOf(currentValue));

        if (tooltip != null) {
            Tooltip mcTooltip = Tooltip.create(tooltip);
            label.setTooltip(mcTooltip);
            editBox.setTooltip(mcTooltip);
        }

        Button resetBtn = Button.builder(Component.translatable("controls.reset"), button -> {
            editBox.setValue(String.valueOf(defaultValue));
            onSave.accept(defaultValue);
            button.active = false;
        }).bounds(resetX, 0, resetButtonWidth, 20).build();

        resetBtn.active = !String.valueOf(currentValue).equals(String.valueOf(defaultValue));

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

    // --- INTEIROS (INT) ---
    public static ConfigListEntry createInt(Font font, int leftX, int rightX, Component label, int current, int def, Consumer<Integer> onSave) {
        return createInt(font, leftX, rightX, label, null, current, def, onSave);
    }

    public static ConfigListEntry createInt(Font font, int leftX, int rightX, Component label, Component tooltip, int current, int def, Consumer<Integer> onSave) {
        return new ConfigListEntry(adder -> buildCoreRow(adder, font, leftX, rightX, label, tooltip, current, def, Integer::parseInt, onSave));
    }

    // --- DECIMAIS (FLOAT) ---
    public static ConfigListEntry createFloat(Font font, int leftX, int rightX, Component label, float current, float def, Consumer<Float> onSave) {
        return createFloat(font, leftX, rightX, label, null, current, def, onSave);
    }

    public static ConfigListEntry createFloat(Font font, int leftX, int rightX, Component label, Component tooltip, float current, float def, Consumer<Float> onSave) {
        return new ConfigListEntry(adder -> buildCoreRow(adder, font, leftX, rightX, label, tooltip, current, def, Float::parseFloat, onSave));
    }

    // --- DECIMAIS (DOUBLE - O seu antigo createDoubleEntry agora mora aqui!) ---
    public static ConfigListEntry createDouble(Font font, int leftX, int rightX, Component label, double current, double def, Consumer<Double> onSave) {
        return createDouble(font, leftX, rightX, label, null, current, def, onSave);
    }

    public static ConfigListEntry createDouble(Font font, int leftX, int rightX, Component label, Component tooltip, double current, double def, Consumer<Double> onSave) {
        return new ConfigListEntry(adder -> buildCoreRow(
                adder, font, leftX, rightX, label, tooltip, (float) current, (float) def,
                Float::parseFloat, val -> onSave.accept((double) val)
        ));
    }

    // --- SLIDERS (PORCENTAGEM) ---
    public static ConfigListEntry createSlider(Font font, int leftX, int rightX, Component label, float current, float def, Consumer<Float> onSave) {
        return createSlider(font, leftX, rightX, label, null, current, def, onSave);
    }

    public static ConfigListEntry createSlider(Font font, int leftX, int rightX, Component label, Component tooltip, float current, float def, Consumer<Float> onSave) {
        return new ConfigListEntry(adder -> {
            int resetButtonWidth = 60;
            int sliderWidth = 100;

            int resetX = rightX - resetButtonWidth;
            int sliderX = resetX - 6 - sliderWidth;

            int textWidth = font.width(label);
            StringWidget labelWidget = new StringWidget(leftX, 0, textWidth, 20, label, font);

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

    @Override
    public void visitWidgets(@NonNull Consumer<AbstractWidget> widgetVisitor) {
        for (AbstractWidget widget : this.widgets) {
            widgetVisitor.accept(widget);
        }
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.widgets; // Retorna seus botões, sliders e editboxes
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return this.widgets; // Para o narrador do jogo ler os botões
    }
}