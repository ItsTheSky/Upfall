package net.itsthesky.upfall.content.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.itsthesky.upfall.content.world.WorldBlocks;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class SkyLakeFeature extends Feature<SkyLakeFeature.Config> {
    public record Config(int minRadius, int maxRadius, int maxDepth) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(2, 12).fieldOf("min_radius").forGetter(Config::minRadius),
                Codec.intRange(2, 12).fieldOf("max_radius").forGetter(Config::maxRadius),
                Codec.intRange(1, 8).fieldOf("max_depth").forGetter(Config::maxDepth)
        ).apply(instance, Config::new));
    }

    public SkyLakeFeature(Codec<Config> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        Config config = context.config();
        BlockPos origin = context.origin();
        if (!level.getBlockState(origin.below()).is(ModBlocks.SKYGRASS_BLOCK.get())) {
            return false;
        }
        int surface = origin.getY() - 1;
        int radius = Mth.randomBetweenInclusive(random, config.minRadius(), config.maxRadius());
        int extent = radius + 2;

        int lobes = 2 + random.nextInt(3);
        float[][] lobe = new float[lobes][4];
        for (int i = 0; i < lobes; i++) {
            float spread = i == 0 ? 0.0F : radius * 0.55F;
            lobe[i][0] = (random.nextFloat() * 2 - 1) * spread;
            lobe[i][1] = (random.nextFloat() * 2 - 1) * spread;
            lobe[i][2] = radius * (0.45F + random.nextFloat() * 0.55F);
            lobe[i][3] = radius * (0.45F + random.nextFloat() * 0.55F);
        }
        Long2IntMap depths = new Long2IntOpenHashMap();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -extent; dx <= extent; dx++) {
            for (int dz = -extent; dz <= extent; dz++) {
                float weight = 0.0F;
                for (float[] l : lobe) {
                    float x = (dx - l[0]) / l[2];
                    float z = (dz - l[1]) / l[3];
                    weight = Math.max(weight, 1.0F - x * x - z * z);
                }
                if (weight <= 0.0F) continue;
                int depth = Mth.clamp(1 + Math.round(weight * config.maxDepth() + random.nextFloat() * 0.6F - 0.3F), 1, config.maxDepth());
                pos.set(origin.getX() + dx, surface, origin.getZ() + dz);
                if (!isLakeGround(level.getBlockState(pos)) || !isOpenAbove(level, pos)) continue;
                depths.put(BlockPos.asLong(pos.getX(), surface, pos.getZ()), depth);
            }
        }
        int planned = depths.size();
        stabilize(level, depths, surface);
        if (depths.size() < 6 || depths.size() < planned * 0.6F) {
            return false;
        }

        BlockState water = ModFluids.SKYWATER_BLOCK.get().defaultBlockState();
        BlockState silt = WorldBlocks.SKYSILT.get().defaultBlockState();
        for (Long2IntMap.Entry entry : depths.long2IntEntrySet()) {
            pos.set(entry.getLongKey());
            int depth = entry.getIntValue();
            for (int y = 1; y <= 2; y++) {
                BlockPos above = pos.above(y);
                if (!level.getBlockState(above).isAir()) level.setBlock(above, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            for (int d = 0; d < depth; d++) {
                level.setBlock(pos.below(d), water, Block.UPDATE_CLIENTS);
            }
            BlockPos floor = pos.below(depth);
            if (random.nextInt(4) != 0 && isLakeGround(level.getBlockState(floor))) {
                level.setBlock(floor, silt, Block.UPDATE_CLIENTS);
            }
        }
        decorateShore(level, random, depths, surface, origin, extent + 1);
        return true;
    }

    private static void stabilize(WorldGenLevel level, Long2IntMap depths, int surface) {
        BlockPos.MutableBlockPos cell = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos next = new BlockPos.MutableBlockPos();
        boolean changed = true;
        while (changed && !depths.isEmpty()) {
            changed = false;
            var iterator = depths.long2IntEntrySet().iterator();
            while (iterator.hasNext()) {
                Long2IntMap.Entry entry = iterator.next();
                cell.set(entry.getLongKey());
                int depth = entry.getIntValue();
                int kept = depth;
                for (int d = 0; d < depth && kept == depth; d++) {
                    int y = surface - d;
                    cell.setY(y);
                    for (Direction dir : Direction.Plane.HORIZONTAL) {
                        next.setWithOffset(cell, dir);
                        int other = depths.getOrDefault(BlockPos.asLong(next.getX(), surface, next.getZ()), 0);
                        boolean water = other > surface - y;
                        if (!water && !isWall(level.getBlockState(next))) {
                            kept = d;
                            break;
                        }
                    }
                }
                if (kept > 0) {
                    cell.setY(surface - kept);
                    while (kept > 0 && !isWall(level.getBlockState(cell))) {
                        kept--;
                        cell.setY(surface - kept);
                    }
                }
                if (kept != depth) {
                    changed = true;
                    if (kept == 0) iterator.remove();
                    else entry.setValue(kept);
                }
            }
        }
    }

    private void decorateShore(WorldGenLevel level, RandomSource random, Long2IntMap depths, int surface, BlockPos origin, int extent) {
        BlockState silt = WorldBlocks.SKYSILT.get().defaultBlockState();
        BlockState lily = WorldBlocks.SKYLILY.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -extent; dx <= extent; dx++) {
            for (int dz = -extent; dz <= extent; dz++) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                if (depths.containsKey(BlockPos.asLong(x, surface, z))) {
                    if (random.nextInt(13) == 0) {
                        pos.set(x, surface + 1, z);
                        if (level.getBlockState(pos).isAir()) level.setBlock(pos, lily, Block.UPDATE_CLIENTS);
                    }
                    continue;
                }
                int distance = shoreDistance(depths, x, z, surface);
                if (distance > 2) continue;
                int top = groundTop(level, pos.set(x, surface + 2, z), surface - 1);
                if (top < 0) continue;
                pos.setY(top);
                BlockState ground = level.getBlockState(pos);
                if (!ground.is(ModBlocks.SKYGRASS_BLOCK.get()) && !ground.is(ModBlocks.SKYSOIL.get())) continue;
                if (random.nextFloat() < (distance == 1 ? 0.8F : 0.35F)) {
                    level.setBlock(pos, silt, Block.UPDATE_CLIENTS);
                }
                if (distance == 1 && random.nextInt(3) == 0) {
                    placeReed(level, pos.above());
                }
            }
        }
    }

    private static void placeReed(WorldGenLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()) return;
        BlockState reed = WorldBlocks.SKYREED.get().defaultBlockState();
        level.setBlock(pos, reed.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER), Block.UPDATE_CLIENTS);
        level.setBlock(pos.above(), reed.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_CLIENTS);
    }

    private static int shoreDistance(Long2IntMap depths, int x, int z, int surface) {
        int best = 3;
        for (int ox = -2; ox <= 2; ox++) {
            for (int oz = -2; oz <= 2; oz++) {
                if (depths.containsKey(BlockPos.asLong(x + ox, surface, z + oz))) {
                    best = Math.min(best, Math.max(Math.abs(ox), Math.abs(oz)));
                }
            }
        }
        return best;
    }

    private static int groundTop(WorldGenLevel level, BlockPos.MutableBlockPos pos, int minY) {
        for (int y = pos.getY(); y >= minY; y--) {
            pos.setY(y);
            if (level.getBlockState(pos).isSolid()) {
                return level.getBlockState(pos.above()).isAir() ? y : -1;
            }
        }
        return -1;
    }

    private static boolean isLakeGround(BlockState state) {
        return state.is(ModBlocks.SKYGRASS_BLOCK.get()) || state.is(ModBlocks.SKYSOIL.get()) || state.is(ModBlocks.PEARLSTONE.get());
    }

    private static boolean isWall(BlockState state) {
        return state.isSolid() && state.getFluidState().isEmpty();
    }

    private static boolean isOpenAbove(WorldGenLevel level, BlockPos pos) {
        BlockState one = level.getBlockState(pos.above());
        BlockState two = level.getBlockState(pos.above(2));
        boolean oneFree = one.isAir() || one.canBeReplaced() && one.getFluidState().isEmpty();
        boolean twoFree = two.isAir() || two.canBeReplaced() && two.getFluidState().isEmpty();
        if (oneFree && twoFree) return true;
        return twoFree && (one.is(ModBlocks.SKYGRASS_BLOCK.get()) || one.is(ModBlocks.SKYSOIL.get()));
    }
}
