package com.wesleyhdias.minnanocraft.mixin;

import com.wesleyhdias.minnanocraft.builders.PortugueseItemNameBuilder;
import com.wesleyhdias.minnanocraft.data.loader.ItemStructureLoader;
import com.wesleyhdias.minnanocraft.utils.TranslationModeResolver;
import com.wesleyhdias.minnanocraft.builders.ItemNameBuilder;
import com.wesleyhdias.minnanocraft.config.ModConfig;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

/**
 * Mixin targeting ItemStack to dynamically modify item hover names
 * according to the player's vocabulary and language progression.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackNameMixin {

    /**
     * Intercepts the getHoverName method to replace item names with progressive
     * Japanese scripts or partial translations.
     *
     * @param cir Callback info returnable containing the item name component.
     */
    @Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
    private void onGetHoverName(CallbackInfoReturnable<Component> cir) {
        if (!ModConfig.isEnabled()) {
            return;
        }

        ItemStack stack = (ItemStack) (Object) this;

        // Skips if stack is empty or has a custom user-defined name (e.g., anvil rename)
        if (stack.isEmpty() || stack.has(DataComponents.CUSTOM_NAME)) {
            return;
        }

        String translationKey = stack.getItem().getDescriptionId();
        List<String> structure = ItemStructureLoader.getStructures().get(translationKey);

        if (structure != null && !structure.isEmpty()) {
            Component original = cir.getReturnValue();
            String originalText = original.getString();
            String customText;

            if (TranslationModeResolver.useJapanese(translationKey)) {
                customText = ItemNameBuilder.build(translationKey);
            } else {
                customText = PortugueseItemNameBuilder.build(translationKey, originalText);
            }

            if (customText != null) {
                cir.setReturnValue(Component.literal(customText).withStyle(original.getStyle()));
            }
        }
    }
}