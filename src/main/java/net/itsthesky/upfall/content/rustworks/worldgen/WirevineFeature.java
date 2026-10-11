package net.itsthesky.upfall.content.rustworks.worldgen;

import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.itsthesky.upfall.content.rustworks.block.RustyPipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class WirevineFeature extends RustFeature {
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        FeatureRegion region = new FeatureRegion(context.level(), context.origin());
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        boolean placed = false;
        int vines = 1 + random.nextInt(4);
        for (int i = 0; i < vines; i++) {
            BlockPos column = i == 0 ? origin : origin.offset(random.nextInt(5) - 2, 0, random.nextInt(5) - 2);
            BlockPos start = anchor(region, column);
            if (start != null) {
                placed |= hang(region, start, 2 + random.nextInt(i == 0 ? 8 : 5), random);
            }
        }
        return placed;
    }

    private static BlockPos anchor(FeatureRegion region, BlockPos column) {
        for (int dy = 1; dy >= -2; dy--) {
            BlockPos pos = column.above(dy);
            if (region.get(pos).isAir() && canHang(region, pos.above())) return pos;
        }
        return null;
    }

    private static boolean canHang(FeatureRegion region, BlockPos support) {
        BlockState state = region.get(support);
        return state.getBlock() instanceof RustyPipeBlock || state.isFaceSturdy(region.level(), support, Direction.DOWN);
    }

    private static boolean hang(FeatureRegion region, BlockPos start, int length, RandomSource random) {
        int n = 0;
        while (n < length && region.get(start.below(n)).isAir()) {
            n++;
        }
        if (n == 0) return false;
        for (int i = 0; i < n - 1; i++) {
            region.set(start.below(i), RustworksBlocks.WIREVINE_PLANT.get().defaultBlockState());
        }
        region.set(start.below(n - 1), RustworksBlocks.WIREVINE.get().defaultBlockState()
                .setValue(GrowingPlantHeadBlock.AGE, 16 + random.nextInt(9)));
        return true;
    }
}
