package net.itsthesky.upfall.worldgen.feature;

import net.itsthesky.upfall.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

final class TreeBuilder {
    private final WorldGenLevel level;
    private final Map<BlockPos, Direction.Axis> logs = new LinkedHashMap<>();
    private final Set<BlockPos> leaves = new LinkedHashSet<>();

    TreeBuilder(WorldGenLevel level) {
        this.level = level;
    }

    static boolean canReplace(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.is(ModTags.Blocks.FEATURE_REPLACEABLE) || state.canBeReplaced();
    }

    boolean log(BlockPos pos, Direction.Axis axis) {
        if (!canReplace(level, pos) && !logs.containsKey(pos)) return false;
        leaves.remove(pos);
        logs.put(pos.immutable(), axis);
        return true;
    }

    void leaf(BlockPos pos) {
        if (logs.containsKey(pos) || !canReplace(level, pos)) return;
        leaves.add(pos.immutable());
    }

    boolean hasLog(BlockPos pos) {
        return logs.containsKey(pos);
    }

    void place(Block log, Block leafBlock) {
        for (var entry : logs.entrySet()) {
            level.setBlock(entry.getKey(), log.defaultBlockState().setValue(RotatedPillarBlock.AXIS, entry.getValue()), Block.UPDATE_CLIENTS);
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
        for (BlockPos pos : leaves) {
            Integer d = distance.get(pos);
            BlockState state = leafBlock.defaultBlockState();
            state = d == null
                    ? state.setValue(LeavesBlock.PERSISTENT, true)
                    : state.setValue(LeavesBlock.DISTANCE, Math.max(1, d));
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        }
    }
}
