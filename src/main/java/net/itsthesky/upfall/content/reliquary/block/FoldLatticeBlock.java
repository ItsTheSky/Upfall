package net.itsthesky.upfall.content.reliquary.block;

import net.itsthesky.upfall.content.quantum.QuantumParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FoldLatticeBlock extends Block {
    public static final int LIFETIME = 160;

    public FoldLatticeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide() && !oldState.is(this)) {
            level.scheduleTick(pos, this, LIFETIME + level.getRandom().nextInt(20));
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.sendParticles(QuantumParticles.QUANTUM_SPARK.get(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.35, 0.35, 0.35, 0.03);
        if (random.nextInt(3) == 0) {
            level.playSound(null, pos, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.BLOCKS, 0.6F, 1.2F + random.nextFloat() * 0.3F);
        }
        level.removeBlock(pos, false);
    }
}
