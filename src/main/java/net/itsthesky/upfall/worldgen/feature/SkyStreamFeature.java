package net.itsthesky.upfall.worldgen.feature;

import com.mojang.serialization.Codec;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.LinkedHashSet;
import java.util.Set;

public class SkyStreamFeature extends Feature<NoneFeatureConfiguration> {
    public SkyStreamFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos start = context.origin().below();
        if (!level.getBlockState(start).is(ModBlocks.SKYGRASS_BLOCK.get())) {
            return false;
        }
        int y = start.getY();
        Set<BlockPos> cells = new LinkedHashSet<>();
        BlockPos.MutableBlockPos cursor = start.mutable();
        Direction heading = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int steps = 8 + random.nextInt(16);
        for (int i = 0; i < steps; i++) {
            if (Math.abs(cursor.getX() - start.getX()) > 14 || Math.abs(cursor.getZ() - start.getZ()) > 14) break;
            if (!isGround(level.getBlockState(cursor))) break;
            cells.add(cursor.immutable());
            if (random.nextInt(3) == 0) {
                BlockPos side = cursor.relative(heading.getClockWise());
                if (isGround(level.getBlockState(side))) cells.add(side.immutable());
            }
            if (random.nextInt(4) == 0) {
                heading = random.nextBoolean() ? heading.getClockWise() : heading.getCounterClockWise();
            }
            cursor.move(heading);
            cursor.setY(y);
        }
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (random.nextBoolean() && isGround(level.getBlockState(start.relative(dir)))) cells.add(start.relative(dir));
        }
        while (cells.removeIf(cell -> !enclosed(level, cell, cells))) {
        }
        if (cells.size() < 4) {
            return false;
        }
        BlockState water = ModFluids.SKYWATER_BLOCK.get().defaultBlockState();
        for (BlockPos cell : cells) {
            BlockState above = level.getBlockState(cell.above());
            if (!above.isAir() && !above.canBeReplaced()) continue;
            level.setBlock(cell.above(), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(cell, water, Block.UPDATE_CLIENTS);
            if (level.getBlockState(cell.below()).is(ModBlocks.SKYGRASS_BLOCK.get())) {
                level.setBlock(cell.below(), ModBlocks.SKYSOIL.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        return true;
    }

    private static boolean isGround(BlockState state) {
        return state.is(ModBlocks.SKYGRASS_BLOCK.get()) || state.is(ModBlocks.SKYSOIL.get());
    }

    private static boolean enclosed(WorldGenLevel level, BlockPos cell, Set<BlockPos> cells) {
        if (!level.getBlockState(cell.below()).isSolid()) return false;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos next = cell.relative(dir);
            if (!cells.contains(next) && !level.getBlockState(next).isSolid()) return false;
        }
        return true;
    }
}
