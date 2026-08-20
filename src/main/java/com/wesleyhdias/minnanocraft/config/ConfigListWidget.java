package com.wesleyhdias.minnanocraft.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

public class ConfigListWidget extends ContainerObjectSelectionList<ConfigListWidget.Entry> {

    public ConfigListWidget(Minecraft minecraft, int width, int height, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
    }

    public void addConfigWidget(AbstractWidget widget) {
        this.addEntry(new Entry(widget));
    }

    @Override
    public int getRowWidth() {
        return 300;
    }

    public class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        private final AbstractWidget widget;

        public Entry(AbstractWidget widget) {
            this.widget = widget;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int top, int left, boolean isMouseOver, float partialTick) {
            // Centraliza o botão dentro da área reservada para a linha
            this.widget.setX(left + (ConfigListWidget.this.getRowWidth() - this.widget.getWidth()) / 2);
            this.widget.setY(top);

            this.widget.extractRenderState(graphics, 0, 0, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(this.widget);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(this.widget);
        }
    }
}