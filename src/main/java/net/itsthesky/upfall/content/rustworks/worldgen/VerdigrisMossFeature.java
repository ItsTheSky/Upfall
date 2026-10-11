package net.itsthesky.upfall.content.rustworks.worldgen;

import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class VerdigrisMossFeature extends RustFeature {
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        FeatureRegion region = new FeatureRegion(context.level(), context.origin());
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int radius = 2 + random.nextInt(3);
        int placed = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > radius || random.nextFloat() > 1.15F - dist / (radius + 1.0F)) continue;
                BlockPos pos = surface(region, origin.offset(dx, 0, dz));
                if (pos == null) continue;
                BlockPos ground = pos.below();
                BlockState below = region.get(ground);
                boolean soil = below.is(RustworksBlocks.OXIDE_TURF.get()) || below.is(RustworksBlocks.RUSTSOIL.get());
                if (soil && dist <= radius * 0.45 && random.nextInt(5) < 2) {
                    region.set(ground, RustworksBlocks.VERDIGRIS_MOSS_BLOCK.get().defaultBlockState());
                    region.setIfOpen(pos, RustworksBlocks.VERDIGRIS_MOSS.get().defaultBlockState());
                } else {
                    region.set(pos, RustworksBlocks.VERDIGRIS_MOSS.get().defaultBlockState());
                }
                placed++;
            }
        }
        return placed > 0;
    }

    private static BlockPos surface(FeatureRegion region, BlockPos column) {
        for (int dy = 2; dy >= -2; dy--) {
            BlockPos pos = column.above(dy);
            if (region.isOpen(pos) && isGround(region.get(pos.below()))) return pos;
        }
        return null;
    }

    private static boolean isGround(BlockState state) {
        return state.is(RustworksBlocks.OXIDE_TURF.get()) || state.is(RustworksBlocks.RUSTSOIL.get())
                || state.is(RustworksBlocks.RUSTY_PLATING.get()) || state.is(RustworksBlocks.COG_PLATING.get());
    }
}
