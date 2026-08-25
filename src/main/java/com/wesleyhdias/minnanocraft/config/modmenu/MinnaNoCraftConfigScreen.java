package com.wesleyhdias.minnanocraft.config.modmenu;

import com.wesleyhdias.minnanocraft.config.data.ModConfig;
import com.wesleyhdias.minnanocraft.config.modmenu.progress.PlayerProgressScreen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MinnaNoCraftConfigScreen extends Screen {

    private final Screen parent;
    private ConfigListWidget configList;

    public MinnaNoCraftConfigScreen(Screen parent) {
        super(Component.literal("MinnaNoCraft - Configurações"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int center = this.width / 2;

        // Inicializa a lista rolável
        int listY = 40;
        int listHeight = this.height - 80;
        this.configList = new ConfigListWidget(this.minecraft, this.width, listHeight, listY, 25);
        this.addRenderableWidget(this.configList);

        // --- BOTÕES INFERIORES LADO A LADO ---
        int buttonWidth = 150;
        int buttonHeight = 20;
        int bottomY = this.height - 28;

        // Botão da Esquerda: Dicionário e Progresso
        this.addRenderableWidget(
                Button.builder(Component.literal("Dicionário e Progresso"), button -> {
                            this.minecraft.setScreen(new PlayerProgressScreen(this));
                        })
                        // x: centro - largura do botão - 5 pixels de margem
                        .bounds(center - buttonWidth - 5, bottomY, buttonWidth, buttonHeight)
                        .build()
        );

        // Botão da Direita: Concluído
        this.addRenderableWidget(
                Button.builder(Component.translatable("gui.done"), button -> {
                            ModConfig.save();
                            this.minecraft.setScreen(this.parent);
                        })
                        // x: centro + 5 pixels de margem
                        .bounds(center + 5, bottomY, buttonWidth, buttonHeight)
                        .build()
        );
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Fundo semitransparente padrão
        guiGraphics.fill(0, 0, this.width, this.height, 0x99000000);

        // Renderiza os componentes (incluindo a lista)
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        // Título fixo no topo
        guiGraphics.centeredText(this.font, this.title, this.width / 2, 10, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        ModConfig.save();
        this.minecraft.setScreen(this.parent);
    }
}