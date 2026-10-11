package net.itsthesky.upfall.worldgen.feature;

import com.mojang.serialization.Codec;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.worldgen.ModLootTables;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class DriftingRuinsFeature extends Feature<NoneFeatureConfiguration> {
    public DriftingRuinsFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos floorCenter = context.origin().below();
        int radius = 3 + random.nextInt(2);
        int supported = 0;
        int cells = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius + 1) continue;
                cells++;
                if (level.getBlockState(floorCenter.offset(dx, -1, dz)).isSolid()) supported++;
            }
        }
        if (supported < cells * 0.75) return false;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius + 1) continue;
                BlockPos pos = floorCenter.offset(dx, 0, dz);
                for (int y = 1; y <= 3; y++) {
                    if (level.getBlockState(pos.above(y)).canBeReplaced()) {
                        level.setBlock(pos.above(y), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
                if (random.nextFloat() < 0.18F) continue;
                level.setBlock(pos, floorBlock(random), Block.UPDATE_CLIENTS);
            }
        }

        int pillars = 4 + random.nextInt(3);
        double phase = random.nextDouble() * Math.PI * 2;
        for (int i = 0; i < pillars; i++) {
            double angle = phase + i * Math.PI * 2 / pillars;
            BlockPos base = floorCenter.offset((int) Math.round(Math.cos(angle) * radius), 1, (int) Math.round(Math.sin(angle) * radius));
            int height = 1 + random.nextInt(5);
            for (int y = 0; y < height; y++) {
                level.setBlock(base.above(y), y == height - 1 && random.nextBoolean()
                        ? ModBlocks.PEARLSTONE_BRICK_VARIANTS.wall().get().defaultBlockState()
                        : ModBlocks.PEARLSTONE_BRICKS.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            if (height < 4 && random.nextInt(3) > 0) {
                BlockPos drift = base.above(height + 2 + random.nextInt(3));
                level.setBlock(drift, ModBlocks.PEARLSTONE_BRICKS.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                if (random.nextBoolean()) {
                    level.setBlock(drift.above(), ModBlocks.PEARLSTONE_BRICK_VARIANTS.slab().get().defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
            BlockPos fallen = base.relative(Direction.Plane.HORIZONTAL.getRandomDirection(random));
            if (level.getBlockState(fallen).isAir() && level.getBlockState(fallen.below()).isSolid()) {
                level.setBlock(fallen, ModBlocks.PEARLSTONE_BRICK_VARIANTS.stairs().get().defaultBlockState()
                        .setValue(StairBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random)), Block.UPDATE_CLIENTS);
            }
        }

        BlockPos pedestal = floorCenter.above();
        level.setBlock(floorCenter, ModBlocks.POLISHED_PEARLSTONE.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        level.setBlock(pedestal, ModBlocks.SKY_CACHE.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        RandomizableContainer.setBlockEntityLootTable(level, random, pedestal, ModLootTables.DRIFTING_RUINS);
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (random.nextBoolean()) {
                level.setBlock(pedestal.relative(dir), ModBlocks.POLISHED_PEARLSTONE_VARIANTS.slab().get().defaultBlockState()
                        .setValue(SlabBlock.TYPE, SlabType.BOTTOM), Block.UPDATE_CLIENTS);
            }
        }
        return true;
    }

    private static BlockState floorBlock(RandomSource random) {
        int roll = random.nextInt(10);
        if (roll < 5) return ModBlocks.PEARLSTONE_BRICKS.get().defaultBlockState();
        if (roll < 8) return ModBlocks.COBBLED_PEARLSTONE.get().defaultBlockState();
        return ModBlocks.POLISHED_PEARLSTONE.get().defaultBlockState();
    }
}
