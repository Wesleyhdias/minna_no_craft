package com.wesleyhdias.minnanocraft.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class TargetItemResolver {

    public static ItemStack resolve(String translationKey) {
        if (translationKey == null || translationKey.isBlank()) return ItemStack.EMPTY;

        try {
            if (!translationKey.startsWith("entity.minecraft.")) {
                return resolveItem(translationKey);
            }

            String entityName = translationKey.replace("entity.minecraft.", "");

            switch (entityName) {
                case "ender_dragon" -> { return resolveItem("block.minecraft.dragon_head"); }
                case "wither" -> { return resolveItem("block.minecraft.wither_skeleton_skull"); }
                case "chicken", "rabbit", "salmon", "cod", "pufferfish", "tropical_fish" -> {
                    return resolveItem("item.minecraft." + entityName + "_spawn_egg");
                }
            }

            ItemStack baseItem = resolveItem("item.minecraft." + entityName);
            if (!baseItem.isEmpty()) return baseItem;

            ItemStack spawnEgg = resolveItem("item.minecraft." + entityName + "_spawn_egg");
            if (!spawnEgg.isEmpty()) return spawnEgg;

        } catch (Exception ignored) {}

        return ItemStack.EMPTY;
    }

    private static ItemStack resolveItem(String fullKey) {
        String[] parts = fullKey.split("\\.");
        if (parts.length >= 3) {
            Identifier id = Identifier.tryParse(parts[1] + ":" + parts[2]);
            if (id != null) {
                var optionalHolder = BuiltInRegistries.ITEM.get(id);
                if (optionalHolder.isPresent()) {
                    Item item = optionalHolder.get().value();
                    if (item != Items.AIR) return new ItemStack(item);
                }
            }
        }
        return ItemStack.EMPTY;
    }
}