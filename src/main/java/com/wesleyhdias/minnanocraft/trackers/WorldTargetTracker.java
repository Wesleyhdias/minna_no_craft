package com.wesleyhdias.minnanocraft.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Utility tracker to resolve the translation key of the block or entity
 * the player is currently looking at in the world.
 */
public class WorldTargetTracker {

    /**
     * Inspects what the client player is currently looking at.
     *
     * @return The translation key of the target, or null if looking at nothing.
     */
    public static String getTargetTranslationKey() {
        Minecraft client = Minecraft.getInstance();
        HitResult hit = client.hitResult;

        if (hit == null || hit.getType() == HitResult.Type.MISS) {
            return null;
        }

        // 1. If looking at a Block
        if (hit instanceof BlockHitResult blockHit && client.level != null) {
            BlockState state = client.level.getBlockState(blockHit.getBlockPos());
            if (state.isAir()) return null;
            return state.getBlock().getDescriptionId();
        }

        // 2. If looking at an Entity (excluding dropped items)
        if (hit instanceof EntityHitResult entityHit) {
            Entity entity = entityHit.getEntity();
            if (entity instanceof ItemEntity) return null; // Ignores item drops on the ground
            return entity.getType().getDescriptionId();
        }

        return null;
    }
}