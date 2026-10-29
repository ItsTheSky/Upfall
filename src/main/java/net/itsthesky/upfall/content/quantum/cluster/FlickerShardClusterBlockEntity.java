package net.itsthesky.upfall.content.quantum.cluster;

import net.itsthesky.upfall.UpfallConfig;
import net.itsthesky.upfall.compat.mc.DataIn;
import net.itsthesky.upfall.compat.mc.DataOut;
import net.itsthesky.upfall.compat.mc.UpfallBlockEntity;
import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.itsthesky.upfall.content.quantum.QuantumParticles;
import net.itsthesky.upfall.content.quantum.QuantumSounds;
import net.itsthesky.upfall.content.quantum.observe.QuantumObservation;
import net.itsthesky.upfall.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class FlickerShardClusterBlockEntity extends UpfallBlockEntity {
    private static final int TRIES = 24;

    private BlockState replaced = Blocks.AIR.defaultBlockState();
    private @Nullable UUID miner;

    public FlickerShardClusterBlockEntity(BlockPos pos, BlockState state) {
        super(QuantumBlocks.FLICKER_SHARD_CLUSTER_ENTITY.get(), pos, state);
    }

    public BlockState replaced() {
        return replaced;
    }

    public void setReplaced(BlockState state) {
        this.replaced = state;
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FlickerShardClusterBlockEntity entity) {
        if (!(level instanceof ServerLevel server) || !QuantumObservation.shouldCheck(level, pos)) return;
        if (!QuantumObservation.anyPlayerNear(server, pos.getCenter())) {
            entity.miner = null;
            return;
        }
        if (QuantumObservation.isObserved(server, pos)) return;
        boolean mined = entity.miner != null;
        if (mined || level.getRandom().nextDouble() < UpfallConfig.getDouble(UpfallConfig.CLUSTER_JUMP_CHANCE)) {
            jump(server, pos, state, entity);
        }
    }

    public static @Nullable BlockPos jump(ServerLevel level, BlockPos pos, BlockState state, FlickerShardClusterBlockEntity entity) {
        RandomSource random = level.getRandom();
        int radius = UpfallConfig.getInt(UpfallConfig.CLUSTER_JUMP_RADIUS);
        for (int i = 0; i < TRIES; i++) {
            BlockPos target = pos.offset(random.nextInt(radius * 2 + 1) - radius, random.nextInt(radius * 2 + 1) - radius,
                    random.nextInt(radius * 2 + 1) - radius);
            if (target.equals(pos)) continue;
            BlockState there = level.getBlockState(target);
            if (!(there.isAir() || there.is(QuantumBlocks.VEIL_GAS.get())) || !there.getFluidState().isEmpty()) continue;
            Direction facing = wallFacing(level, target, random);
            if (facing == null || QuantumObservation.isObserved(level, target)) continue;
            BlockState moved = state.setValue(FlickerShardClusterBlock.FACING, facing).setValue(FlickerShardClusterBlock.WATERLOGGED, false);
            BlockState left = entity.replaced;
            level.setBlock(pos, left, Block.UPDATE_ALL);
            level.setBlock(target, moved, Block.UPDATE_ALL);
            if (level.getBlockEntity(target) instanceof FlickerShardClusterBlockEntity next) {
                next.setReplaced(there);
            }
            level.playSound(null, pos, QuantumSounds.CLUSTER_SHIFT.get(), SoundSource.BLOCKS, 0.6F, 0.9F + random.nextFloat() * 0.3F);
            level.sendParticles(QuantumParticles.QUANTUM_SPARK.get(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8, 0.25, 0.25, 0.25, 0.01);
            return target;
        }
        return null;
    }

    private static @Nullable Direction wallFacing(Level level, BlockPos pos, RandomSource random) {
        Direction[] directions = Direction.values();
        int start = random.nextInt(directions.length);
        for (int i = 0; i < directions.length; i++) {
            Direction facing = directions[(start + i) % directions.length];
            BlockPos support = pos.relative(facing.getOpposite());
            BlockState wall = level.getBlockState(support);
            if (wall.is(ModTags.Blocks.FLICKER_CLUSTER_WALLS) && wall.isFaceSturdy(level, support, facing)) return facing;
        }
        return null;
    }

    public static void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel().isClientSide() || !(event.getLevel().getBlockEntity(event.getPos()) instanceof FlickerShardClusterBlockEntity entity)) return;
        switch (event.getAction()) {
            case START -> entity.miner = event.getEntity().getUUID();
            case ABORT, STOP -> entity.miner = null;
            default -> {}
        }
    }

    @Override
    protected void saveData(DataOut out) {
        out.store("Replaced", BlockState.CODEC, replaced);
    }

    @Override
    protected void loadData(DataIn in) {
        in.read("Replaced", BlockState.CODEC).ifPresent(state -> replaced = state);
    }
}
