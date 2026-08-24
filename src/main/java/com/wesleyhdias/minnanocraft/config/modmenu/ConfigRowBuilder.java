package com.wesleyhdias.minnanocraft.config.modmenu;

import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.Font;

import java.util.function.Consumer;
import java.util.function.Function;

public class ConfigRowBuilder {

    /**
     * MÉTODO CORE (Privado): O motor que desenha a interface.
     */
    private static <T> EditBox buildCoreRow(
            Consumer<AbstractWidget> widgetAdder, Font font, int leftX, int rightX, int y,
            Component labelComponent, T currentValue, T defaultValue,
            Function<String, T> parser, Consumer<T> onSave
    ) {
        int resetButtonWidth = 60;
        int boxWidth = 35;

        int resetX = rightX - resetButtonWidth;
        int boxX = resetX - 6 - boxWidth;

        int textWidth = font.width(labelComponent);
        StringWidget label = new StringWidget(leftX, y, textWidth, 20, labelComponent, font);

        EditBox editBox = new EditBox(font, boxX, y, boxWidth, 20, labelComponent);
        editBox.setValue(String.valueOf(currentValue));

        Button resetBtn = Button.builder(Component.translatable("controls.reset"), button -> {
            editBox.setValue(String.valueOf(defaultValue));
            onSave.accept(defaultValue);
            button.active = false;
        }).bounds(resetX, y, resetButtonWidth, 20).build();

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

        return editBox;
    }

    /**
     * Para números inteiros (int)
     */
    public static EditBox buildIntRow(
            Consumer<AbstractWidget> widgetAdder, Font font, int leftX, int rightX, int y,
            Component labelComponent, int currentValue, int defaultValue, Consumer<Integer> onSave) {

        return buildCoreRow(widgetAdder, font, leftX, rightX, y, labelComponent, currentValue, defaultValue, Integer::parseInt, onSave);
    }

    /**
     * Para números decimais (float)
     */
    public static EditBox buildFloatRow(
            Consumer<AbstractWidget> widgetAdder, Font font, int leftX, int rightX, int y,
            Component labelComponent, float currentValue, float defaultValue, Consumer<Float> onSave) {

        return buildCoreRow(widgetAdder, font, leftX, rightX, y, labelComponent, currentValue, defaultValue, Float::parseFloat, onSave);
    }

    /**
     * Constrói e adiciona uma linha com um Slider (Barra Deslizante) para Porcentagens (0 a 100)
     */
    public static void buildSliderRow(
            Consumer<AbstractWidget> widgetAdder, Font font, int leftX, int rightX, int y,
            Component labelComponent, float currentValue, float defaultValue, Consumer<Float> onSave) {

        int resetButtonWidth = 60;
        int sliderWidth = 100;

        int resetX = rightX - resetButtonWidth;
        int sliderX = resetX - 6 - sliderWidth;

        int textWidth = font.width(labelComponent);
        StringWidget label = new StringWidget(leftX, y, textWidth, 20, labelComponent, font);

        Button[] resetBtnHolder = new Button[1];

        class PercentSlider extends AbstractSliderButton {
            public PercentSlider(int x, int y, int width, int height, double value) {
                // value já entra entre 0.0 e 1.0
                super(x, y, width, height, Component.empty(), value);
                this.updateMessage();
            }

            @Override
            protected void updateMessage() {
                // Multiplica por 100 Apenas para o visual ("70%")
                this.setMessage(Component.literal(Math.round(this.value * 100.0) + "%"));
            }

            @Override
            protected void applyValue() {
                // Salva o valor exato no intervalo 0.0f a 1.0f
                float savedValue = (float) this.value;
                onSave.accept(savedValue);

                if (resetBtnHolder[0] != null) {
                    // Multiplica por 100 para comparar com segurança e ignorar micro-diferenças do float
                    resetBtnHolder[0].active = Math.round(savedValue * 100) != Math.round(defaultValue * 100);
                }
            }

            public void forceValue(double newValue) {
                this.value = newValue;
                this.updateMessage();
                this.applyValue();
            }
        }

        // Instancia o slider passando o valor direto (ex: 0.7)
        PercentSlider slider = new PercentSlider(sliderX, y, sliderWidth, 20, currentValue);

        resetBtnHolder[0] = Button.builder(Component.translatable("controls.reset"), button -> {
            slider.forceValue(defaultValue);
            button.active = false;
        }).bounds(resetX, y, resetButtonWidth, 20).build();

        resetBtnHolder[0].active = Math.round(currentValue * 100) != Math.round(defaultValue * 100);

        widgetAdder.accept(label);
        widgetAdder.accept(slider);
        widgetAdder.accept(resetBtnHolder[0]);

    }
}