package net.itsthesky.upfall.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class HangingGlowvineFeature extends Feature<NoneFeatureConfiguration> {
    public HangingGlowvineFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        boolean placed = false;
        for (int i = 0; i < 6; i++) {
            BlockPos pos = context.origin().offset(random.nextInt(5) - 2, 0, random.nextInt(5) - 2);
            for (int step = 0; step < 3 && level.getBlockState(pos.above()).isAir(); step++) {
                pos = pos.above();
            }
            if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isFaceSturdy(level, pos.above(), Direction.DOWN)) {
                continue;
            }
            GildrootTreeFeature.hangVine(level, pos, 2 + random.nextInt(7), random);
            placed = true;
        }
        return placed;
    }
}
