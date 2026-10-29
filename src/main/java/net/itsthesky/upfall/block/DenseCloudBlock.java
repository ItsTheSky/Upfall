package net.itsthesky.upfall.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class DenseCloudBlock extends Block {
    public static final MapCodec<DenseCloudBlock> CODEC = simpleCodec(DenseCloudBlock::new);

    public DenseCloudBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    //? if >=26.1 {
    /*@Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        entity.causeFallDamage(fallDistance, 0.5F, level.damageSources().fall());
    }

    @Override
    public void updateEntityMovementAfterFallOn(BlockGetter level, Entity entity) {
        if (!bounce(entity)) {
            super.updateEntityMovementAfterFallOn(level, entity);
        }
    }
    *///?} else {
    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        entity.causeFallDamage(fallDistance, 0.5F, level.damageSources().fall());
    }

    @Override
    public void updateEntityAfterFallOn(BlockGetter level, Entity entity) {
        if (!bounce(entity)) {
            super.updateEntityAfterFallOn(level, entity);
        }
    }
    //?}

    private static boolean bounce(Entity entity) {
        Vec3 motion = entity.getDeltaMovement();
        if (entity.isSuppressingBounce() || motion.y > -0.25) {
            return false;
        }
        double factor = entity instanceof LivingEntity ? 0.45 : 0.35;
        entity.setDeltaMovement(motion.x, -motion.y * factor, motion.z);
        return true;
    }
}
