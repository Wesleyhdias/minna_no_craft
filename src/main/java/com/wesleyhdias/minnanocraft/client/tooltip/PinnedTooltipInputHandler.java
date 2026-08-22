package com.wesleyhdias.minnanocraft.client.tooltip;

import com.wesleyhdias.minnanocraft.client.tooltip.lookup.DictionaryLookupService;
import com.wesleyhdias.minnanocraft.language.dictionary.DictionaryLoader;
import com.wesleyhdias.minnanocraft.srs.VocabularyManager;
import com.wesleyhdias.minnanocraft.srs.models.ExpEvents;
import com.wesleyhdias.minnanocraft.language.dictionary.Word;

import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;

import org.lwjgl.glfw.GLFW;

/**
 * Handler responsible for processing keyboard and mouse inputs related to the
 * pinned tooltip functionality.
 */
public class PinnedTooltipInputHandler {

    /**
     * Handles keyboard key presses to toggle or close the pinned tooltip.
     * Pressing ALT (Left or Right) pins or unpins the currently hovered item's tooltip.
     * Pressing ESCAPE closes the pinned tooltip if one is active.
     *
     * @param keyCode     The GLFW key code of the pressed key.
     * @param hoveredItem The {@link ItemStack} currently being hovered over in the inventory.
     * @return {@code true} if the key press was handled and should be consumed; {@code false} otherwise.
     */
    public static boolean handleKeyPress(int keyCode, ItemStack hoveredItem) {
        if (keyCode == GLFW.GLFW_KEY_LEFT_ALT || keyCode == GLFW.GLFW_KEY_RIGHT_ALT) {
            Minecraft mc = Minecraft.getInstance();

            // Calculate actual GUI-scaled mouse coordinates
            int mouseX = (int) (mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth());
            int mouseY = (int) (mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight());

            return PinnedTooltipService.togglePin(hoveredItem, mouseX, mouseY);

        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE && PinnedTooltipService.isPinned()) {
            PinnedTooltipService.unpin();
            return true;
        }
        return false;
    }

    /**
     * Handles mouse click events while a tooltip is pinned on the screen.
     * Intercepts left clicks to either register a dictionary lookup event (if clicking a word)
     * or unpin the tooltip (if clicking outside a word).
     *
     * @param mouseX The current GUI-scaled X coordinate of the mouse.
     * @param mouseY The current GUI-scaled Y coordinate of the mouse.
     * @param button The GLFW mouse button code (0 for left click).
     * @return {@code true} if the click was handled and should be consumed; {@code false} otherwise.
     */
    public static boolean handleMouseClick(double mouseX, double mouseY, int button) {
        if (!PinnedTooltipService.isPinned()) return false;

        if (button == 0) { // Left click
            HitboxCalculator.TokenHitbox clickedHitbox = HitboxCalculator.getHitboxAt((int) mouseX, (int) mouseY);

            if (clickedHitbox != null) {
                // The user clicked on a specific interactive vocabulary word
                VocabularyManager.registerEvent(clickedHitbox.token(), ExpEvents.LOOKUP);
                String token = clickedHitbox.token();

                // 1. Register the SRS event
                VocabularyManager.registerEvent(token, ExpEvents.LOOKUP);

                // 2. Fetch the Word object associated with this token from your manager/loader
                Word word = DictionaryLoader.getDictionary().get(token);

                // 3. Open the dictionary overlay if the word exists in the database
                if (word != null) {
                    DictionaryLookupService.open(word);
                }
            } else {
                // The user clicked outside of any interactive word, so unpin the tooltip
                PinnedTooltipService.unpin();
            }
            return true;
        }
        return false;
    }
}