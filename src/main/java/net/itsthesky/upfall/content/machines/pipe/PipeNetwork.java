package net.itsthesky.upfall.content.machines.pipe;

import net.itsthesky.upfall.content.machines.block.PneumaticPipeBlock;
import net.itsthesky.upfall.content.machines.block.PneumaticPipeEntranceBlock;
import net.itsthesky.upfall.content.machines.block.PneumaticPipeExitBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public final class PipeNetwork {
    private PipeNetwork() {}

    public static final int MAX_SEGMENTS = 512;

    public static boolean isPipe(BlockState state) {
        return state.getBlock() instanceof PneumaticPipeBlock;
    }

    public static boolean isEntrance(BlockState state) {
        return state.getBlock() instanceof PneumaticPipeEntranceBlock;
    }

    public static boolean isExit(BlockState state) {
        return state.getBlock() instanceof PneumaticPipeExitBlock;
    }

    public static boolean opensToward(BlockState state, Direction dir) {
        if (isPipe(state)) return true;
        return (isEntrance(state) || isExit(state)) && state.getValue(BlockStateProperties.FACING) != dir;
    }

    public static boolean connected(BlockState from, BlockState to, Direction dir) {
        if (!opensToward(from, dir) || !opensToward(to, dir.getOpposite())) return false;
        return isPipe(from) || isPipe(to) || isEntrance(from) != isEntrance(to);
    }

    public static Optional<PipeRoute> findRoute(LevelReader level, BlockPos entrance, Predicate<BlockPos> acceptExit) {
        if (!isEntrance(level.getBlockState(entrance))) return Optional.empty();
        Map<BlockPos, BlockPos> parents = new HashMap<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        parents.put(entrance, entrance);
        queue.add(entrance);
        int expanded = 0;
        while (!queue.isEmpty() && expanded++ <= MAX_SEGMENTS) {
            BlockPos pos = queue.poll();
            BlockState state = level.getBlockState(pos);
            for (Direction dir : Direction.values()) {
                BlockPos next = pos.relative(dir);
                if (parents.containsKey(next) || !level.hasChunkAt(next)) continue;
                BlockState nextState = level.getBlockState(next);
                if (!connected(state, nextState, dir)) continue;
                parents.put(next, pos);
                if (isExit(nextState)) {
                    if (acceptExit.test(next)) {
                        return Optional.of(new PipeRoute(trace(parents, entrance, next), nextState.getValue(BlockStateProperties.FACING)));
                    }
                } else if (isPipe(nextState)) {
                    queue.add(next);
                }
            }
        }
        return Optional.empty();
    }

    private static List<BlockPos> trace(Map<BlockPos, BlockPos> parents, BlockPos entrance, BlockPos exit) {
        List<BlockPos> blocks = new ArrayList<>();
        for (BlockPos pos = exit; !pos.equals(entrance); pos = parents.get(pos)) {
            blocks.add(pos);
        }
        blocks.add(entrance);
        Collections.reverse(blocks);
        return List.copyOf(blocks);
    }

    public static boolean isIntact(LevelReader level, PipeRoute route) {
        List<BlockPos> blocks = route.blocks();
        for (int i = 0; i < blocks.size(); i++) {
            BlockState state = level.getBlockState(blocks.get(i));
            boolean ok = i == 0 ? isEntrance(state) : i == blocks.size() - 1 ? isExit(state) : isPipe(state);
            if (!ok) return false;
        }
        return true;
    }

    public static boolean exitClear(LevelReader level, BlockPos exit) {
        BlockState state = level.getBlockState(exit);
        if (!isExit(state)) return false;
        BlockPos front = exit.relative(state.getValue(BlockStateProperties.FACING));
        return level.getBlockState(front).getCollisionShape(level, front).isEmpty();
    }
}
