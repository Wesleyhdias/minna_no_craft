package com.wesleyhdias.minnanocraft.config.modmenu;

import com.wesleyhdias.minnanocraft.config.data.ModConfig;

import com.wesleyhdias.minnanocraft.config.modmenu.progress.PlayerProgressScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MinnaNoCraftConfigScreen extends Screen {

    private final Screen parent;

    public MinnaNoCraftConfigScreen(Screen parent) {
        super(Component.literal("MinnaNoCraft - Configurações"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int center = this.width / 2;
        int buttonWidth = 200;
        int buttonHeight = 20;

        int rowWidth = 310;
        int leftX = center - (rowWidth / 2);
        int rightX = center + (rowWidth / 2);

        // 1. Botão do Dicionário (Mantido no topo)
        this.addRenderableWidget(
                Button.builder(Component.literal("Dicionário e Progresso"), button -> {
                            this.minecraft.setScreen(new PlayerProgressScreen(this));
                        })
                        .bounds(center - (buttonWidth / 2), 10, buttonWidth, buttonHeight)
                        .build()
        );

        // ==========================================
        // OPÇÃO 1: MÁXIMO DE PALAVRAS ATIVAS
        // ==========================================
        ConfigRowBuilder.buildIntRow(
                this::addRenderableWidget, this.font, leftX, rightX, 50,
                Component.literal("Máximo de Palavras Ativas:"),
                ModConfig.getConfig().getMaxActiveWords(),
                30,
                val -> ModConfig.getConfig().setMaxActiveWords(val)
        );


        // ==========================================
        // OPÇÃO 2: TEMPO DE INATIVIDADE
        // ==========================================
        long defaultInactivityMs = 1000L * 60 * 60 * 24; // 24 horas

        // Converte os milissegundos atuais para Horas (int) para aparecer na caixinha
        // Converte o padrão de 24h para aparecer se o jogador clicar no botão de reset
        // Quando o jogador digita um valor e salva, multiplica de volta para milissegundos (long)
        ConfigRowBuilder.buildIntRow(
                this::addRenderableWidget, this.font, leftX, rightX, 75,
                Component.literal("Inatividade para perder XP (horas):"),
                (int) (ModConfig.getConfig().getInactivityTimeThreshold() / (1000L * 60 * 60)),
                (int) (defaultInactivityMs / (1000L * 60 * 60)),
                val -> ModConfig.getConfig().setInactivityTimeThreshold(val * 1000L * 60 * 60)
        );

        // ==========================================
        // OPÇÃO 3: TEMPO PARA REBAIXAMENTO (HORAS)
        // ==========================================
        long defaultDemotionMs = 1000L * 60 * 60 * 24 * 3;

        ConfigRowBuilder.buildIntRow(
                this::addRenderableWidget, this.font, leftX, rightX, 100,
                Component.literal("Tempo p/ rebaixamento (horas):"),
                (int) (ModConfig.getConfig().getDemotionTimeThreshold() / (1000L * 60 * 60)),
                (int) (defaultDemotionMs / (1000L * 60 * 60)),
                val -> ModConfig.getConfig().setDemotionTimeThreshold(val * 1000L * 60 * 60)
        );

        // ==========================================
        // OPÇÃO 4: PERDA DE XP POR INATIVIDADE
        // ==========================================
        ConfigRowBuilder.buildFloatRow(
                this::addRenderableWidget, this.font, leftX, rightX, 125,
                Component.literal("Perda de XP por inatividade:"),
                ModConfig.getConfig().getExpLossPerInactivityCycle(),
                5.0f,
                val -> ModConfig.getConfig().setExpLossPerInactivityCycle(val)
        );

        // ==========================================
        // OPÇÃO 5: MULTIPLICADOR AO REAPRENDER
        // ==========================================
       ConfigRowBuilder.buildFloatRow(
                this::addRenderableWidget, this.font, leftX, rightX, 150,
                Component.literal("Multiplicador de XP ao reaprender:"),
                ModConfig.getConfig().getRelearnMultiplier(),
                2.5f,
                val -> ModConfig.getConfig().setRelearnMultiplier(val)
        );

        // ==========================================
        // OPÇÃO 6: LIMITE MÁXIMO DE PERDA DE XP (%)
        // ==========================================
        ConfigRowBuilder.buildSliderRow(
                this::addRenderableWidget, this.font, leftX, rightX, 175,
                Component.literal("Limite Máx. de perda de XP:"),
                ModConfig.getConfig().getMaxExpLossPercentage(),
                0.7f, // O seu padrão de 0.7
                val -> ModConfig.getConfig().setMaxExpLossPercentage(val)
        );

        // Botão de Rodapé: Concluído
        this.addRenderableWidget(
                Button.builder(Component.translatable("gui.done"), button -> {
                            // Salva tudo no disco rígido quando o jogador termina!
                            ModConfig.save();

                            this.minecraft.setScreen(this.parent);
                        })
                        .bounds(center - (buttonWidth / 2), this.height - 28, buttonWidth, buttonHeight)
                        .build()
        );
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x99000000);

        // Desenha os botões e o resto da tela normalmente por cima do fundo escuro
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        // Título centralizado no topo
        guiGraphics.centeredText(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        // Se o jogador fechar com 'ESC', garantimos que o progresso seja salvo também
        ModConfig.save();
        this.minecraft.setScreen(this.parent);
    }
}