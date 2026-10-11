package net.itsthesky.upfall.content.rustworks.worldgen;

import com.mojang.serialization.Codec;
import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class IronbarkTreeFeature extends Feature<NoneFeatureConfiguration> {
    private final boolean large;

    public IronbarkTreeFeature(Codec<NoneFeatureConfiguration> codec, boolean large) {
        super(codec);
        this.large = large;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int size = large ? 2 : 1;
        for (int dx = 0; dx < size; dx++) {
            for (int dz = 0; dz < size; dz++) {
                BlockState ground = level.getBlockState(origin.offset(dx, -1, dz));
                if (!ground.is(ModTags.Blocks.SKY_PLANTABLE) && !ground.is(BlockTags.DIRT)) return false;
            }
        }
        if (nearVent(level, origin)) return false;
        int height = large ? 13 + random.nextInt(5) : 6 + random.nextInt(6);
        int spacing = large ? 3 : 2;
        int maxDiscs = large ? 5 : 4;
        int discs = Math.min(maxDiscs, (height - 2) / spacing + 1);
        if (!large && discs > 3 && random.nextInt(3) == 0) discs = 3;
        for (int y = 0; y <= height; y++) {
            for (int dx = 0; dx < size; dx++) {
                for (int dz = 0; dz < size; dz++) {
                    if (!canReplace(level, origin.offset(dx, y, dz))) return false;
                }
            }
        }

        Map<BlockPos, Direction.Axis> logs = new LinkedHashMap<>();
        Set<BlockPos> leaves = new LinkedHashSet<>();
        for (int y = 0; y < height; y++) {
            for (int dx = 0; dx < size; dx++) {
                for (int dz = 0; dz < size; dz++) {
                    logs.put(origin.offset(dx, y, dz), Direction.Axis.Y);
                }
            }
        }

        double center = (size - 1) / 2.0;
        int top = height - 1;
        for (int k = 0; k < discs; k++) {
            int y = top - k * spacing;
            double radius = discRadius(k, discs, random);
            int teeth = large ? 12 : 8;
            double phase = random.nextDouble() * Math.PI * 2;
            int r = (int) Math.ceil(radius) + 1;
            for (int dx = -r; dx <= r + size - 1; dx++) {
                for (int dz = -r; dz <= r + size - 1; dz++) {
                    double ox = dx - center;
                    double oz = dz - center;
                    double dist = Math.sqrt(ox * ox + oz * oz) + random.nextFloat() * 0.35;
                    if (dist > radius + 0.4) continue;
                    if (dist > radius - 0.9 && radius > 2.5) {
                        double angle = Math.atan2(oz, ox) + phase;
                        int tooth = (int) Math.floor((angle / (Math.PI * 2) + 4) * teeth * 2);
                        if ((tooth & 1) == 1) continue;
                    }
                    BlockPos pos = origin.offset(dx, y, dz);
                    if (!logs.containsKey(pos) && canReplace(level, pos)) leaves.add(pos);
                }
            }
            if (k < discs - 1 && random.nextInt(large ? 2 : 3) == 0) {
                Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                int along = random.nextInt(size);
                int kx = dir == Direction.EAST ? size - 1 : dir == Direction.WEST ? 0 : along;
                int kz = dir == Direction.SOUTH ? size - 1 : dir == Direction.NORTH ? 0 : along;
                BlockPos base = origin.offset(kx, y - 1, kz);
                BlockPos knot = base.relative(dir);
                if (canReplace(level, knot)) logs.put(knot, dir.getAxis());
            }
        }

        BlockState logState = ModBlocks.IRONBARK.log().get().defaultBlockState();
        for (Map.Entry<BlockPos, Direction.Axis> entry : logs.entrySet()) {
            level.setBlock(entry.getKey(), logState.setValue(RotatedPillarBlock.AXIS, entry.getValue()), Block.UPDATE_CLIENTS);
        }
        Map<BlockPos, Integer> distance = new HashMap<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        for (BlockPos pos : logs.keySet()) {
            distance.put(pos, 0);
            queue.add(pos);
        }
        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            int d = distance.get(pos);
            if (d >= LeavesBlock.DECAY_DISTANCE - 1) continue;
            for (Direction dir : Direction.values()) {
                BlockPos next = pos.relative(dir);
                if (leaves.contains(next) && !distance.containsKey(next)) {
                    distance.put(next, d + 1);
                    queue.add(next);
                }
            }
        }
        BlockState leafState = ModBlocks.IRONBARK.leaves().get().defaultBlockState();
        for (BlockPos pos : leaves) {
            Integer d = distance.get(pos);
            level.setBlock(pos, d == null
                    ? leafState.setValue(LeavesBlock.PERSISTENT, true)
                    : leafState.setValue(LeavesBlock.DISTANCE, Math.max(1, d)), Block.UPDATE_CLIENTS);
        }
        return true;
    }

    private double discRadius(int fromTop, int discs, RandomSource random) {
        double max = large ? 6.5 : 4.0;
        double min = large ? 3.2 : 2.0;
        double t = discs == 1 ? 1.0 : (double) fromTop / (discs - 1);
        return min + (max - min) * t + (random.nextDouble() - 0.5) * 0.5;
    }

    private static boolean nearVent(WorldGenLevel level, BlockPos origin) {
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-7, -3, -7), origin.offset(7, 1, 7))) {
            if (level.getBlockState(pos).is(RustworksBlocks.EXHAUST_VENT.get())) return true;
        }
        return false;
    }

    private static boolean canReplace(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.is(ModTags.Blocks.FEATURE_REPLACEABLE) || state.canBeReplaced();
    }
}
