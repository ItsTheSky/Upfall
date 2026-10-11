package net.itsthesky.upfall.content.world.feature;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
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

public class CloudletFeature extends Feature<NoneFeatureConfiguration> {
    private static final int CLEARANCE = 3;

    public CloudletFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int lobes = 2 + random.nextInt(4);
        float[][] lobe = new float[lobes][5];
        int reach = 0;
        for (int i = 0; i < lobes; i++) {
            float rx = 2.5F + random.nextFloat() * 3.5F;
            float rz = 2.5F + random.nextFloat() * 3.5F;
            lobe[i][0] = i == 0 ? 0 : (random.nextFloat() * 2 - 1) * 5;
            lobe[i][1] = i == 0 ? 0 : (random.nextFloat() * 2 - 1) * 5;
            lobe[i][2] = rx;
            lobe[i][3] = rz;
            lobe[i][4] = 1.5F + Math.min(rx, rz) * 0.55F;
            reach = Math.max(reach, (int) Math.ceil(Math.max(Math.abs(lobe[i][0]) + rx, Math.abs(lobe[i][1]) + rz)));
        }
        reach = Math.min(reach, 11);
        LongSet cells = new LongOpenHashSet();
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                float height = -1;
                for (float[] l : lobe) {
                    float x = (dx - l[0]) / l[2];
                    float z = (dz - l[1]) / l[3];
                    float d = 1 - x * x - z * z;
                    if (d > 0) height = Math.max(height, l[4] * (float) Math.sqrt(d));
                }
                if (height < 0) continue;
                int top = Math.round(height);
                int base = -Math.round(height * 0.35F);
                for (int y = base; y <= top; y++) {
                    cells.add(BlockPos.asLong(origin.getX() + dx, origin.getY() + y, origin.getZ() + dz));
                }
            }
        }
        if (cells.size() < 12 || !clear(level, origin, reach)) {
            return false;
        }
        BlockState cloud = ModBlocks.DENSE_CLOUD.get().defaultBlockState();
        BlockState mist = ModBlocks.MIST.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (long cell : cells) {
            pos.set(cell);
            boolean surface = !cells.contains(BlockPos.asLong(pos.getX(), pos.getY() + 1, pos.getZ()));
            level.setBlock(pos, surface && pos.getY() > origin.getY() - 1 ? mist : cloud, Block.UPDATE_CLIENTS);
        }
        if (random.nextInt(3) == 0) {
            level.setBlock(origin.above(), WorldBlocks.PEARLED_CLOUD.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        if (random.nextInt(3) == 0) {
            CloudstalkPatchFeature.grow(level, random, origin.above(8));
        }
        return true;
    }

    private static boolean clear(WorldGenLevel level, BlockPos origin, int reach) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int r = reach + CLEARANCE;
        for (int dx = -r; dx <= r; dx += 2) {
            for (int dz = -r; dz <= r; dz += 2) {
                for (int dy = -CLEARANCE; dy <= 8 + CLEARANCE; dy += 2) {
                    if (!level.getBlockState(pos.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz)).isAir()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
