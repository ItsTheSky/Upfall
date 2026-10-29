package net.itsthesky.upfall.content.aerie.block;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.content.quantum.QuantumSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class WeatheredBricksBlock extends Block {
    public static final MapCodec<WeatheredBricksBlock> CODEC = simpleCodec(WeatheredBricksBlock::new);
    private static final int DELAY = 6;

    public WeatheredBricksBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide() && entity instanceof Player player && !player.isSpectator() && !player.isCreative()
                && !level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, DELAY);
            level.playSound(null, pos, QuantumSounds.FLOOR_CRUMBLE.get(), SoundSource.BLOCKS, 0.4F, 1.4F);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                16, 0.3, 0.3, 0.3, 0.05);
        level.playSound(null, pos, QuantumSounds.FLOOR_CRUMBLE.get(), SoundSource.BLOCKS, 1.0F, 0.8F + random.nextFloat() * 0.2F);
        for (BlockPos other : BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, 0, 1))) {
            if (level.getBlockState(other).is(this) && !level.getBlockTicks().hasScheduledTick(other, this)) {
                level.scheduleTick(other.immutable(), this, 2 + random.nextInt(3));
            }
        }
    }
}
