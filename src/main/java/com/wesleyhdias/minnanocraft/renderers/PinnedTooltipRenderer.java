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

/**
 * Renderer responsible for drawing the pinned item tooltips and handling the
 * Spaced Repetition System (SRS) hover logic when the player interacts with
 * specific vocabulary tokens.
 */
public class PinnedTooltipRenderer {

    /**
     * Renders the pinned tooltip on the screen if one is currently active.
     * Captures the actual mouse position, formats the custom vocabulary lines,
     * and sets up a fixed positioner for the tooltip.
     *
     * @param graphics The GUI graphics extractor used for drawing.
     */
    public static void render(GuiGraphicsExtractor graphics) {
        if (!PinnedTooltipService.isPinned()) return;

        ItemStack stack = PinnedTooltipService.getPinnedStack();
        if (stack == null || stack.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();

        // Calculate actual GUI-scaled mouse coordinates
        int realMouseX = (int) (mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth());
        int realMouseY = (int) (mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight());

        List<Component> originalLines = Screen.getTooltipFromItem(mc, stack);
        if (originalLines.isEmpty()) return;

        List<Component> customLines = new ArrayList<>(originalLines);

        String translationKey = stack.getItem().getDescriptionId();
        String originalText = originalLines.getFirst().getString();

        // 1. Replaces the name only if the formatting returns a valid component
        Component formattedName = TooltipFormatter.formatItemName(translationKey, originalText);
        customLines.set(0, formattedName);

        // 2. Converts to ClientTooltipComponent, ignoring any null elements
        List<ClientTooltipComponent> clientLines = new ArrayList<>();
        for (Component comp : customLines) {
            if (comp != null) {
                clientLines.add(ClientTooltipComponent.create(comp.getVisualOrderText()));
            }
        }

        if (clientLines.isEmpty()) return;

        // Custom positioner to lock the tooltip in place based on when it was pinned
        ClientTooltipPositioner positioner = (screenWidth, screenHeight, x, y, width, height) -> {
            int boxX = x + 12;
            int boxY = y - 12;
            if (boxX + width > screenWidth) boxX = x - 16 - width;
            if (boxY + height > screenHeight) boxY = screenHeight - height;

            PinnedTooltipService.setTextPosition(boxX + 4, boxY + 4);

            return new Vector2i(boxX, boxY);
        };

        // 3. Renders the main frozen/pinned tooltip
        graphics.tooltip(mc.font, clientLines, PinnedTooltipService.getPinMouseX(), PinnedTooltipService.getPinMouseY(), positioner, null);

        // 4. Calculates hitboxes and processes mouse hover interactions
        HitboxCalculator.rebuildHitboxes(mc, translationKey, PinnedTooltipService.getTextX(), PinnedTooltipService.getTextY(), originalText);
        processHoverSRS(graphics, mc, realMouseX, realMouseY);
    }

    /**
     * Processes mouse hover interactions over interactive vocabulary tokens.
     * Renders sub-tooltips (definitions/previous texts) and triggers SRS penalty events
     * (HOVER_LOOKUP) if the player hovers over a word for too long.
     *
     * @param graphics The GUI graphics extractor used for drawing.
     * @param mc       The current Minecraft client instance.
     * @param mouseX   The current GUI-scaled X coordinate of the mouse.
     * @param mouseY   The current GUI-scaled Y coordinate of the mouse.
     */
    private static void processHoverSRS(GuiGraphicsExtractor graphics, Minecraft mc, int mouseX, int mouseY) {
        HitboxCalculator.TokenHitbox hoveredHitbox = HitboxCalculator.getHitboxAt(mouseX, mouseY);

        if (hoveredHitbox != null && hoveredHitbox.prevText() != null) {
            String token = hoveredHitbox.token();

            if (!token.equals(PinnedTooltipService.getCurrentHoveredToken())) {
                // Started hovering over a new token
                PinnedTooltipService.updateHoverState(token, System.currentTimeMillis(), false);
            } else if (!PinnedTooltipService.isHoverPunished() && (System.currentTimeMillis() - PinnedTooltipService.getHoverStartTime() >= 1000)) {
                // Punish the player if they hover for 1 second or more (triggering a lookup event)
                VocabularyManager.registerEvent(token, Event.HOVER_LOOKUP);
                PinnedTooltipService.updateHoverState(token, PinnedTooltipService.getHoverStartTime(), true);
            }

            // Render the sub-tooltip containing the definition or previous script level
            List<ClientTooltipComponent> subTooltipLines = List.of(
                    ClientTooltipComponent.create(Component.literal(hoveredHitbox.prevText()).getVisualOrderText())
            );
            graphics.tooltip(mc.font, subTooltipLines, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null);
        } else {
            // Mouse is not over any interactive token
            PinnedTooltipService.resetHoverState();
        }
    }
}