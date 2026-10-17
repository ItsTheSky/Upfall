package net.itsthesky.upfall.content.reliquary.block;

import net.itsthesky.upfall.content.quantum.QuantumParticles;
import net.itsthesky.upfall.content.reliquary.ReliquaryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class FoldSealBlock extends Block {
    public static final int NOT_FLOOR = 0;
    public static final int FLOOR = 1;
    public static final int FLOOR_EDGE = 2;
    public static final IntegerProperty FLOOR_KIND = IntegerProperty.create("floor", NOT_FLOOR, FLOOR_EDGE);

    public FoldSealBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FLOOR_KIND, NOT_FLOOR));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FLOOR_KIND);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.sendParticles(QuantumParticles.QUANTUM_SPARK.get(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.4, 0.4, 0.4, 0.04);
        if (random.nextInt(4) == 0) {
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, 0.8F, 0.7F + random.nextFloat() * 0.4F);
        }
        switch (state.getValue(FLOOR_KIND)) {
            case FLOOR -> level.setBlock(pos, ReliquaryBlocks.LATTICE_STONE.get().defaultBlockState(), Block.UPDATE_ALL);
            case FLOOR_EDGE -> level.setBlock(pos, ReliquaryBlocks.CARVED_LATTICE_STONE.get().defaultBlockState(), Block.UPDATE_ALL);
            default -> level.removeBlock(pos, false);
        }
    }
}
