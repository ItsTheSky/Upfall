package net.itsthesky.upfall.block;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.compat.mc.McLevel;
import net.itsthesky.upfall.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class UpdraftVentBlock extends Block {
    public static final MapCodec<UpdraftVentBlock> CODEC = simpleCodec(UpdraftVentBlock::new);
    public static final int PLAYER_COLUMN_HEIGHT = 48;

    public UpdraftVentBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    public static int fillColumn(LevelAccessor level, BlockPos ventPos, int maxHeight, int flags) {
        BlockState mist = ModBlocks.UPDRAFT_MIST.get().defaultBlockState();
        BlockPos.MutableBlockPos cursor = ventPos.mutable();
        int placed = 0;
        for (int i = 1; i <= maxHeight; i++) {
            cursor.setY(ventPos.getY() + i);
            if (cursor.getY() >= McLevel.maxYExclusive(level)) break;
            BlockState current = level.getBlockState(cursor);
            if (current.is(ModBlocks.UPDRAFT_MIST.get())) continue;
            if (!current.isAir()) break;
            level.setBlock(cursor, mist, flags);
            placed++;
        }
        return placed;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(state.getBlock())) {
            level.scheduleTick(pos, this, 4);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        fillColumn(level, pos, PLAYER_COLUMN_HEIGHT, Block.UPDATE_ALL);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockState(pos.above()).isAir()) {
            fillColumn(level, pos, PLAYER_COLUMN_HEIGHT, Block.UPDATE_ALL);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.WHITE_ASH, pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 1.05,
                    pos.getZ() + 0.3 + random.nextDouble() * 0.4, 0.0, 0.2, 0.0);
        }
    }
}
