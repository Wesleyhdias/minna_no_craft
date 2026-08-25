package com.wesleyhdias.minnanocraft.config.modmenu;

import com.wesleyhdias.minnanocraft.config.data.ModConfig;

import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

public class ConfigListWidget extends ContainerObjectSelectionList<ConfigListEntry> {

    public ConfigListWidget(Minecraft minecraft, int width, int height, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
        loadConfigs(minecraft.font, width);
    }

    private void loadConfigs(Font font, int screenWidth) {
        int center = screenWidth / 2;
        int rowWidth = 310;
        int leftX = center - (rowWidth / 2);
        int rightX = center + (rowWidth / 2);

        // --- Adicionando as configurações ---

        this.addEntry(new ConfigListEntry(adder -> {
            Component titleText = Component.literal("--- Configurações gerais do SRS ---");

            // Descobre exatamente qual o tamanho da frase em pixels
            int textWidth = font.width(titleText);

            // Calcula a posição X para ficar no centro exato da tela
            int startX = (screenWidth - textWidth) / 2;

            // Cria o widget passando o X, Y, Largura, Altura, Texto e Fonte
            StringWidget title = new StringWidget(startX, 0, textWidth, 20, titleText, font);

            adder.accept(title);
        }));

        this.addEntry(ConfigListEntry.createInt(
                font, leftX, rightX,
                Component.literal("Máximo de Palavras Ativas:"),
                ModConfig.getConfig().getMaxActiveWords(), 30,
                val -> ModConfig.getConfig().setMaxActiveWords(val)
        ));

        long defaultInactivityMs = 1000L * 60 * 60 * 24;
        this.addEntry(ConfigListEntry.createInt(
                font, leftX, rightX,
                Component.literal("Inatividade para perder XP (h):"),
                (int) (ModConfig.getConfig().getInactivityTimeThreshold() / (1000L * 60 * 60)),
                (int) (defaultInactivityMs / (1000L * 60 * 60)),
                val -> ModConfig.getConfig().setInactivityTimeThreshold(val * 1000L * 60 * 60)
        ));

        long defaultDemotionMs = 1000L * 60 * 60 * 24 * 3;
        this.addEntry(ConfigListEntry.createInt(
                font, leftX, rightX,
                Component.literal("Tempo para rebaixamento (h):"),
                Component.literal("Quando uma palavra sai do estado de ACTIVE (sendo aprendida) e volta para a fila de espera"),
                (int) (ModConfig.getConfig().getDemotionTimeThreshold() / (1000L * 60 * 60)),
                (int) (defaultDemotionMs / (1000L * 60 * 60)),
                val -> ModConfig.getConfig().setDemotionTimeThreshold(val * 1000L * 60 * 60)
        ));

        this.addEntry(ConfigListEntry.createFloat(
                font, leftX, rightX,
                Component.literal("Perda de XP por inatividade:"),
                ModConfig.getConfig().getExpLossPerInactivityCycle(), 5.0f,
                val -> ModConfig.getConfig().setExpLossPerInactivityCycle(val)
        ));

        this.addEntry(ConfigListEntry.createFloat(
                font, leftX, rightX,
                Component.literal("Multiplicador ao reaprender:"),
                ModConfig.getConfig().getRelearnMultiplier(), 2.5f,
                val -> ModConfig.getConfig().setRelearnMultiplier(val)
        ));

        this.addEntry(ConfigListEntry.createSlider(
                font, leftX, rightX,
                Component.literal("Limite Máx. de perda de XP:"),
                ModConfig.getConfig().getMaxExpLossPercentage(),
                0.7f,
                val -> ModConfig.getConfig().setMaxExpLossPercentage(val)
        ));

        // 1. Linha vazia para dar respiro
        this.addEntry(new ConfigListEntry(adder -> {}));

        // 2. Linha de Título
        this.addEntry(new ConfigListEntry(adder -> {
            Component titleText = Component.literal("--- Configurações de nivel ---");

            // Descobre exatamente qual o tamanho da frase em pixels
            int textWidth = font.width(titleText);

            // Calcula a posição X para ficar no centro exato da tela
            int startX = (screenWidth - textWidth) / 2;

            // Cria o widget passando o X, Y, Largura, Altura, Texto e Fonte
            StringWidget title = new StringWidget(startX, 0, textWidth, 20, titleText, font);

            adder.accept(title);
        }));

        this.addEntry(ConfigListEntry.createDouble(
                font, leftX, rightX,
                Component.literal("EXP para atingir Lv.1 (Romaji):"),
                ModConfig.getConfig().getExpLevel1(),
                15.0,
                val -> ModConfig.getConfig().setExpLevel1(val)
        ));

        this.addEntry(ConfigListEntry.createDouble(
                font, leftX, rightX,
                Component.literal("EXP para atingir Lv.2 (Romaji Inv.):"),
                ModConfig.getConfig().getExpLevel2(),
                30.0,
                val -> ModConfig.getConfig().setExpLevel2(val)
        ));

        this.addEntry(ConfigListEntry.createDouble(
                font, leftX, rightX,
                Component.literal("EXP para atingir Lv.3 (Hiragana):"),
                ModConfig.getConfig().getExpLevel3(),
                45.0,
                val -> ModConfig.getConfig().setExpLevel3(val)
        ));

        this.addEntry(ConfigListEntry.createDouble(
                font, leftX, rightX,
                Component.literal("EXP para atingir Lv.4 (Kanji):"),
                ModConfig.getConfig().getExpLevel4(),
                100.0,
                val -> ModConfig.getConfig().setExpLevel4(val)
        ));

        this.addEntry(ConfigListEntry.createDouble(
                font, leftX, rightX,
                Component.literal("EXP para atingir MASTERED:"),
                ModConfig.getConfig().getMasteryExposure(),
                115.0,
                val -> ModConfig.getConfig().setMasteryExposure(val)
        ));

        // 1. Linha vazia para dar respiro
        this.addEntry(new ConfigListEntry(adder -> {}));

        // 2. Linha de Título
        this.addEntry(new ConfigListEntry(adder -> {
            Component titleText = Component.literal("--- Configurações de ganho de EXP ---");

            // Descobre exatamente qual o tamanho da frase em pixels
            int textWidth = font.width(titleText);

            // Calcula a posição X para ficar no centro exato da tela
            int startX = (screenWidth - textWidth) / 2;

            // Cria o widget passando o X, Y, Largura, Altura, Texto e Fonte
            StringWidget title = new StringWidget(startX, 0, textWidth, 20, titleText, font);

            adder.accept(title);
        }));

        this.addEntry(ConfigListEntry.createDouble(
                font, leftX, rightX,
                Component.literal("Ganho de EXP para hover::"),
                Component.literal("EXP concedido ao parar o mouse sobre um item por um tempo"),
                ModConfig.getConfig().getEventHover(),
                2.0,
                val -> ModConfig.getConfig().setEventHover(val)
        ));
        this.addEntry(ConfigListEntry.createDouble(
                font, leftX, rightX,
                Component.literal("Ganho de EXP pela tela no hud:"),
                Component.literal("EXP concedido ao olhar para um bloco ou entidade no mundo por um tempo (o seu nome pode ser lido pela tela no hud)"),
                ModConfig.getConfig().getEventHudSeen(),
                1.0,
                val -> ModConfig.getConfig().setEventHudSeen(val)
        ));
        this.addEntry(ConfigListEntry.createDouble(
                font, leftX, rightX,
                Component.literal("Ganho de EXP por segurar um item:"),
                ModConfig.getConfig().getEventSeen(),
                0.5,
                val -> ModConfig.getConfig().setEventSeen(val)
        ));
        this.addEntry(ConfigListEntry.createDouble(
                font, leftX, rightX,
                Component.literal("Perda de EXP para hover no tooltip:"),
                Component.literal("Passar o mouse sobre uma palavra do nome de um item no Tooltip congelado ao pressionar a tecla ALT:"),
                ModConfig.getConfig().getEventHoverLookup(),
                2.0,
                val -> ModConfig.getConfig().setEventHoverLookup(val)
        ));
        this.addEntry(ConfigListEntry.createDouble(
                font, leftX, rightX,
                Component.literal("Perda de EXP para abrir o dicionário:"),
                Component.literal("Ao clicar em uma palavra no tooltip congelado ao pressionar a tecla ALT"),
                ModConfig.getConfig().getEventLookup(),
                5.0,
                val -> ModConfig.getConfig().setEventLookup(val)
        ));
    }

    @Override
    public int getRowWidth() {
        return 320;
    }
}