package com.wesleyhdias.minnanocraft.mixin;

import com.wesleyhdias.minnanocraft.language.builder.CurrentLangItemNameBuilder;
import com.wesleyhdias.minnanocraft.language.resolver.TranslationModeResolver;
import com.wesleyhdias.minnanocraft.language.builder.JapaneseItemNameBuilder;
import com.wesleyhdias.minnanocraft.language.ItemStructureLoader;
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
 * Mixin targeting Minecraft's {@link ItemStack} to dynamically modify item display names
 * based on the player's SRS vocabulary progression and active learning mode.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackNameMixin {

    /**
     * Intercepts the return value of {@code getHoverName()} to replace or modify item names
     * with progressive Japanese script representations or localized target strings.
     *
     * @param cir The {@link CallbackInfoReturnable} containing the original item name {@link Component}.
     */
    @Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
    private void onGetHoverName(CallbackInfoReturnable<Component> cir) {

        // Skips name processing if the mod is disabled in configuration
        if (!ModConfig.getConfig().isEnabled()) {
            return;
        }

        ItemStack stack = (ItemStack) (Object) this;

        // Ignores empty item stacks and items with custom anvil or user-defined names
        if (stack.isEmpty() || stack.has(DataComponents.CUSTOM_NAME)) {
            return;
        }

        String translationKey = stack.getItem().getDescriptionId();
        List<String> structure = ItemStructureLoader.getStructures().get(translationKey);

        if (structure != null && !structure.isEmpty()) {
            Component original = cir.getReturnValue();
            String originalText = original.getString();
            String customText;

            // Determines whether to build full Japanese rendering or native/progressive structure
            if (TranslationModeResolver.useJapanese(translationKey)) {
                customText = JapaneseItemNameBuilder.build(translationKey);
            } else {
                customText = CurrentLangItemNameBuilder.build(translationKey, originalText);
            }

            if (customText != null) {
                cir.setReturnValue(Component.literal(customText).withStyle(original.getStyle()));
            }
        }
    }
}