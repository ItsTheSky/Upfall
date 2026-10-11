package net.itsthesky.upfall.content.rustworks.worldgen;

import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.itsthesky.upfall.content.rustworks.block.RustyPipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class VerdigrisBloomFeature extends RustFeature {
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        FeatureRegion region = new FeatureRegion(context.level(), context.origin());
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int count;
        if (nearMetal(region, origin)) {
            count = 4 + random.nextInt(5);
        } else if (random.nextInt(4) == 0) {
            count = 1 + random.nextInt(3);
        } else {
            return false;
        }
        int placed = 0;
        for (int i = 0; i < count * 3 && placed < count; i++) {
            BlockPos pos = origin.offset(random.nextInt(7) - 3, random.nextInt(3) - 1, random.nextInt(7) - 3);
            BlockState below = region.get(pos.below());
            if (region.isOpen(pos) && (below.is(RustworksBlocks.OXIDE_TURF.get()) || below.is(RustworksBlocks.RUSTSOIL.get()))) {
                region.set(pos, RustworksBlocks.VERDIGRIS_BLOOM.get().defaultBlockState());
                placed++;
            }
        }
        return placed > 0;
    }

    private static boolean nearMetal(FeatureRegion region, BlockPos origin) {
        for (int dx = -5; dx <= 5; dx += 2) {
            for (int dy = -2; dy <= 3; dy++) {
                for (int dz = -5; dz <= 5; dz += 2) {
                    BlockState state = region.get(origin.offset(dx, dy, dz));
                    if (state.is(RustworksBlocks.RUSTY_PLATING.get()) || state.is(RustworksBlocks.COG_PLATING.get())
                            || state.is(RustworksBlocks.RUSTY_COG.get()) || state.getBlock() instanceof RustyPipeBlock) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
