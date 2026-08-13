package com.wesleyhdias.minnanocraft.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Utility class responsible for resolving a given translation key into a representative {@link ItemStack}.
 * This is particularly useful for converting entity translation keys (e.g., "entity.minecraft.pig")
 * into renderable items (like a spawn egg or a related block).
 */
public class TargetItemResolver {

    /**
     * Attempts to resolve a translation key into a valid {@link ItemStack}.
     * Handles specific mappings for entities, converting them to their respective
     * spawn eggs or representative items (e.g., Ender Dragon to Dragon Head).
     *
     * @param translationKey The translation key to resolve (e.g., "entity.minecraft.chicken" or "block.minecraft.stone").
     * @return An {@link ItemStack} representing the key, or {@link ItemStack#EMPTY} if it cannot be resolved.
     */
    public static ItemStack resolve(String translationKey) {
        if (translationKey == null || translationKey.isBlank()) return ItemStack.EMPTY;

        try {
            // If the key does not belong to an entity, attempt to resolve it as a standard item or block
            if (!translationKey.startsWith("entity.minecraft.")) {
                return resolveItem(translationKey);
            }

            // Extract the raw entity name by removing the prefix
            String entityName = translationKey.replace("entity.minecraft.", "");

            // Handle hardcoded visual representations for specific entities
            switch (entityName) {
                case "ender_dragon" -> { return resolveItem("block.minecraft.dragon_head"); }
                case "wither" -> { return resolveItem("block.minecraft.wither_skeleton_skull"); }
                case "chicken", "rabbit", "salmon", "cod", "pufferfish", "tropical_fish" -> {
                    return resolveItem("item.minecraft." + entityName + "_spawn_egg");
                }
            }

            // Fallback 1: Try to resolve the entity name directly as an item (e.g., "item.minecraft.boat")
            ItemStack baseItem = resolveItem("item.minecraft." + entityName);
            if (!baseItem.isEmpty()) return baseItem;

            // Fallback 2: Try to resolve the entity name as a spawn egg
            ItemStack spawnEgg = resolveItem("item.minecraft." + entityName + "_spawn_egg");
            if (!spawnEgg.isEmpty()) return spawnEgg;

        } catch (Exception ignored) {
            // Silently fail and return an empty ItemStack if any parsing or registry lookup throws an exception
        }

        return ItemStack.EMPTY;
    }

    /**
     * Parses a full translation key, constructs an identifier, and fetches the corresponding
     * item from the Minecraft registry.
     *
     * @param fullKey The full translation key (e.g., "item.minecraft.apple").
     * @return An {@link ItemStack} of the found item, or {@link ItemStack#EMPTY} if not found.
     */
    private static ItemStack resolveItem(String fullKey) {
        // Split the key into its components (e.g., ["item", "minecraft", "apple"])
        String[] parts = fullKey.split("\\.");

        if (parts.length >= 3) {
            // Attempt to build a standard Minecraft identifier (e.g., "minecraft:apple")
            Identifier id = Identifier.tryParse(parts[1] + ":" + parts[2]);

            if (id != null) {
                // Look up the item in the built-in item registry
                var optionalHolder = BuiltInRegistries.ITEM.get(id);
                if (optionalHolder.isPresent()) {
                    Item item = optionalHolder.get().value();

                    // Ensure we don't return Air blocks as valid items
                    if (item != Items.AIR) return new ItemStack(item);
                }
            }
        }
        return ItemStack.EMPTY;
    }
}