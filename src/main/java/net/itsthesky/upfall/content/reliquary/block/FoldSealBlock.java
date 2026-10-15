package net.itsthesky.upfall.content.reliquary.block;

import net.itsthesky.upfall.content.quantum.QuantumParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FoldSealBlock extends Block {
    public FoldSealBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.sendParticles(QuantumParticles.QUANTUM_SPARK.get(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.4, 0.4, 0.4, 0.04);
        if (random.nextInt(4) == 0) {
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, 0.8F, 0.7F + random.nextFloat() * 0.4F);
        }
        level.removeBlock(pos, false);
    }
}
