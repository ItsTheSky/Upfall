package net.itsthesky.upfall.content.world.feature;

import com.mojang.serialization.Codec;
import net.itsthesky.upfall.content.world.WorldBlocks;
import net.itsthesky.upfall.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class RaincloudPatchFeature extends Feature<NoneFeatureConfiguration> {
    public RaincloudPatchFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos center = context.origin().above();
        if (!level.getBlockState(center).is(ModBlocks.DENSE_CLOUD.get())) {
            return false;
        }
        float rx = 4.0F + random.nextFloat() * 5.0F;
        float rz = 4.0F + random.nextFloat() * 5.0F;
        float ry = 3.0F + random.nextFloat() * 2.0F;
        int reach = (int) Math.ceil(Math.max(rx, rz));
        BlockState rain = WorldBlocks.RAINCLOUD.get().defaultBlockState();
        BlockState pearl = WorldBlocks.PEARLED_CLOUD.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int changed = 0;
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                for (int dy = -2; dy <= (int) ry + 2; dy++) {
                    float x = dx / rx;
                    float z = dz / rz;
                    float y = dy / ry;
                    if (x * x + z * z + y * y > 1.0F - random.nextFloat() * 0.25F) continue;
                    pos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    if (!level.getBlockState(pos).is(ModBlocks.DENSE_CLOUD.get()) || !isCloud(level.getBlockState(pos.above(2)))) continue;
                    boolean underside = level.getBlockState(pos.below()).isAir();
                    level.setBlock(pos, underside && random.nextInt(9) == 0 ? pearl : rain, Block.UPDATE_CLIENTS);
                    changed++;
                }
            }
        }
        return changed > 0;
    }

    private static boolean isCloud(BlockState state) {
        return state.is(ModBlocks.DENSE_CLOUD.get()) || state.is(WorldBlocks.RAINCLOUD.get()) || state.is(WorldBlocks.PEARLED_CLOUD.get());
    }
}
