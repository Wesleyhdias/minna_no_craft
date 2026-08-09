package com.wesleyhdias.minnanocraft.renderers;

import com.wesleyhdias.minnanocraft.services.PinnedTooltipService;
import com.wesleyhdias.minnanocraft.services.VocabularyManager;
import com.wesleyhdias.minnanocraft.utils.HitboxCalculator;
import com.wesleyhdias.minnanocraft.utils.TooltipFormatter;
import com.wesleyhdias.minnanocraft.data.models.Event;

import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class PinnedTooltipRenderer {

    public static void render(GuiGraphicsExtractor graphics) {
        if (!PinnedTooltipService.isPinned()) return;

        ItemStack stack = PinnedTooltipService.getPinnedStack();
        if (stack == null || stack.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();

        int realMouseX = (int) (mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth());
        int realMouseY = (int) (mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight());


        List<Component> originalLines = Screen.getTooltipFromItem(mc, stack);
        List<Component> customLines = new ArrayList<>(originalLines);

        String translationKey = stack.getItem().getDescriptionId();
        String originalText = originalLines.isEmpty() ? null : originalLines.getFirst().getString();

        if (!customLines.isEmpty()) {
            customLines.set(0, TooltipFormatter.formatItemName(translationKey, originalText));
        }

        List<ClientTooltipComponent> clientLines = new ArrayList<>();
        for (Component comp : customLines) {
            clientLines.add(ClientTooltipComponent.create(comp.getVisualOrderText()));
        }

        ClientTooltipPositioner positioner = (screenWidth, screenHeight, x, y, width, height) -> {
            int boxX = x + 12;
            int boxY = y - 12;
            if (boxX + width > screenWidth) boxX = x - 16 - width;
            if (boxY + height > screenHeight) boxY = screenHeight - height;

            PinnedTooltipService.setTextPosition(boxX + 4, boxY + 4);

            return new Vector2i(boxX, boxY);
        };

        graphics.tooltip(mc.font, clientLines, PinnedTooltipService.getPinMouseX(), PinnedTooltipService.getPinMouseY(), positioner, null);


        HitboxCalculator.rebuildHitboxes(mc, translationKey, PinnedTooltipService.getTextX(), PinnedTooltipService.getTextY(), originalText);
        processHoverSRS(graphics, mc, realMouseX, realMouseY);
    }

    private static void processHoverSRS(GuiGraphicsExtractor graphics, Minecraft mc, int mouseX, int mouseY) {
        HitboxCalculator.TokenHitbox hoveredHitbox = HitboxCalculator.getHitboxAt(mouseX, mouseY);

        if (hoveredHitbox != null && hoveredHitbox.prevText() != null) {
            String token = hoveredHitbox.token();

            if (!token.equals(PinnedTooltipService.getCurrentHoveredToken())) {
                PinnedTooltipService.updateHoverState(token, System.currentTimeMillis(), false);
            } else if (!PinnedTooltipService.isHoverPunished() && (System.currentTimeMillis() - PinnedTooltipService.getHoverStartTime() >= 1000)) {
                VocabularyManager.registerEvent(token, Event.HOVER_LOOKUP);
                PinnedTooltipService.updateHoverState(token, PinnedTooltipService.getHoverStartTime(), true);
            }

            List<ClientTooltipComponent> subTooltipLines = List.of(
                    ClientTooltipComponent.create(Component.literal(hoveredHitbox.prevText()).getVisualOrderText())
            );
            graphics.tooltip(mc.font, subTooltipLines, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null);
        } else {
            PinnedTooltipService.resetHoverState();
        }
    }
}