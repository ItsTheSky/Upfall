package net.itsthesky.upfall.content.rustworks.worldgen;

import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.itsthesky.upfall.content.rustworks.block.PipeValveBlock;
import net.itsthesky.upfall.worldgen.UpfallTerrain;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

abstract class RustFeature extends Feature<NoneFeatureConfiguration> {
    static final int MIN_LAND = UpfallTerrain.CLOUD_BAND_MAX + 6;

    RustFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    static BlockState plating() {
        return RustworksBlocks.RUSTY_PLATING.get().defaultBlockState();
    }

    static BlockState cogPlating() {
        return RustworksBlocks.COG_PLATING.get().defaultBlockState();
    }

    static BlockState cog(Direction.Axis axis) {
        return RustworksBlocks.RUSTY_COG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    static BlockState pipe(Direction.Axis axis) {
        return RustworksBlocks.RUSTY_PIPE.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    static BlockState leakingPipe(Direction.Axis axis) {
        return RustworksBlocks.LEAKING_PIPE.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    static BlockState valve(Direction facing) {
        return RustworksBlocks.PIPE_VALVE.get().defaultBlockState().setValue(PipeValveBlock.FACING, facing);
    }

    static Direction.Axis lateral(Direction.Axis axis) {
        return axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
    }

    static BlockPos offset(BlockPos origin, Direction.Axis along, int t, int u, int v) {
        return along == Direction.Axis.X ? origin.offset(t, v, u) : origin.offset(u, v, t);
    }

    static double outer(int radius) {
        return (radius + 0.5) * (radius + 0.5);
    }

    static double inner(int radius) {
        return (radius - 0.5) * (radius - 0.5);
    }
}
