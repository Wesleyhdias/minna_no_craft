package com.wesleyhdias.minnanocraft.events;

import com.wesleyhdias.minnanocraft.services.PinnedTooltipService;
import com.wesleyhdias.minnanocraft.services.VocabularyManager;
import com.wesleyhdias.minnanocraft.utils.HitboxCalculator;
import com.wesleyhdias.minnanocraft.data.models.Event;

import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;

import org.lwjgl.glfw.GLFW;

public class PinnedTooltipInputHandler {

    public static boolean handleKeyPress(int keyCode, ItemStack hoveredItem) {
        if (keyCode == GLFW.GLFW_KEY_LEFT_ALT || keyCode == GLFW.GLFW_KEY_RIGHT_ALT) {
            Minecraft mc = Minecraft.getInstance();
            int mouseX = (int) (mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth());
            int mouseY = (int) (mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight());

            return PinnedTooltipService.togglePin(hoveredItem, mouseX, mouseY);
        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE && PinnedTooltipService.isPinned()) {
            PinnedTooltipService.unpin();
            return true;
        }
        return false;
    }

    public static boolean handleMouseClick(double mouseX, double mouseY, int button) {
        if (!PinnedTooltipService.isPinned()) return false;

        if (button == 0) { // Clique esquerdo
            HitboxCalculator.TokenHitbox clickedHitbox = HitboxCalculator.getHitboxAt((int) mouseX, (int) mouseY);
            if (clickedHitbox != null) {
                VocabularyManager.registerEvent(clickedHitbox.token(), Event.LOOKUP);
            } else {
                PinnedTooltipService.unpin();
            }
            return true;
        }
        return false;
    }
}