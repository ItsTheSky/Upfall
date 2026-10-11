package net.itsthesky.upfall.worldgen.feature;

import com.mojang.serialization.Codec;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModTags;
import net.itsthesky.upfall.worldgen.ModLootTables;
import net.itsthesky.upfall.worldgen.UpfallTerrain;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class InvertedObservatoryFeature extends Feature<NoneFeatureConfiguration> {
    private static final int RADIUS = 6;

    public InvertedObservatoryFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos anchor = context.origin();
        if (!level.getBlockState(anchor.above()).is(ModTags.Blocks.SKY_PLANTABLE)) return false;
        int shaft = 4 + random.nextInt(4);
        BlockPos center = anchor.below(shaft + RADIUS);
        if (center.getY() - RADIUS < UpfallTerrain.CLOUD_BAND_MAX + 4) return false;
        if (!enoughSpace(level, center)) return false;

        BlockState bricks = ModBlocks.DUSKSLATE_BRICKS.get().defaultBlockState();
        BlockState polished = ModBlocks.POLISHED_DUSKSLATE.get().defaultBlockState();
        BlockState gilded = ModBlocks.GILDED_DUSKSLATE.get().defaultBlockState();
        BlockState crystal = ModBlocks.MISTGLOW_CRYSTAL.get().defaultBlockState();
        BlockState floor = ModBlocks.POLISHED_PEARLSTONE.get().defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        for (int y = 0; y <= shaft + 1; y++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    level.setBlock(anchor.offset(dx, -y, dz), y % 3 == 2 ? gilded : polished, Block.UPDATE_CLIENTS);
                }
            }
        }

        int floorY = -3;
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (d > RADIUS) continue;
                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState state;
                    if (d > RADIUS - 1) {
                        boolean equator = dy == 0 || dy == 1;
                        boolean window = equator && (dx == 0 || dz == 0 || Math.abs(dx) == Math.abs(dz));
                        state = window ? crystal : dy == 2 ? gilded : bricks;
                    } else if (dy == floorY) {
                        state = floor;
                    } else if (dy < floorY) {
                        state = polished;
                    } else {
                        state = air;
                    }
                    level.setBlock(pos, state, Block.UPDATE_CLIENTS);
                }
            }
        }

        Direction door = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        for (Direction side : new Direction[]{door, door.getOpposite()}) {
            for (int depth = RADIUS - 2; depth <= RADIUS; depth++) {
                for (int h = 1; h <= 2; h++) {
                    level.setBlock(center.relative(side, depth).above(floorY + h), air, Block.UPDATE_CLIENTS);
                }
            }
        }

        level.setBlock(center.above(floorY + 1), ModBlocks.NIMBUSITE_BLOCK.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        level.setBlock(center.above(floorY + 2), gilded, Block.UPDATE_CLIENTS);
        level.setBlock(center.above(floorY + 3), ModBlocks.POLISHED_DUSKSLATE_VARIANTS.wall().get().defaultBlockState(), Block.UPDATE_CLIENTS);
        for (int y = floorY; y > -RADIUS; y--) {
            level.setBlock(center.above(y), y == -RADIUS + 1 ? crystal : air, Block.UPDATE_CLIENTS);
        }
        level.setBlock(center.below(RADIUS), crystal, Block.UPDATE_CLIENTS);

        Direction cacheSide = door.getClockWise();
        for (Direction side : new Direction[]{cacheSide, cacheSide.getOpposite()}) {
            BlockPos cache = center.relative(side, RADIUS - 3).above(floorY + 1);
            level.setBlock(cache, ModBlocks.SKY_CACHE.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            RandomizableContainer.setBlockEntityLootTable(level, random, cache, ModLootTables.INVERTED_OBSERVATORY);
        }

        for (int i = 0; i < 5; i++) {
            BlockPos start = center.offset(random.nextInt(7) - 3, -RADIUS - 1, random.nextInt(7) - 3);
            while (start.getY() < center.getY() && !level.getBlockState(start.above()).isSolid()) {
                start = start.above();
            }
            if (level.getBlockState(start).isAir()) {
                GildrootTreeFeature.hangVine(level, start, 2 + random.nextInt(5), random);
            }
        }
        return true;
    }

    private static boolean enoughSpace(WorldGenLevel level, BlockPos center) {
        int solid = 0;
        int samples = 0;
        for (int dx = -RADIUS; dx <= RADIUS; dx += 3) {
            for (int dy = -RADIUS; dy <= RADIUS; dy += 3) {
                for (int dz = -RADIUS; dz <= RADIUS; dz += 3) {
                    samples++;
                    if (!level.getBlockState(center.offset(dx, dy, dz)).isAir()) solid++;
                }
            }
        }
        return solid <= samples / 8;
    }
}
