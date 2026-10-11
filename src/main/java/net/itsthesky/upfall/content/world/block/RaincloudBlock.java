package net.itsthesky.upfall.content.world.block;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.block.DenseCloudBlock;
import net.itsthesky.upfall.content.world.WorldParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class RaincloudBlock extends DenseCloudBlock {
    public static final MapCodec<RaincloudBlock> CODEC = simpleCodec(RaincloudBlock::new);

    public RaincloudBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(2) == 0 && level.getBlockState(pos.below()).isAir()) {
            level.addParticle(WorldParticles.CLOUD_DRIP.get(), pos.getX() + 0.1 + random.nextDouble() * 0.8, pos.getY() - 0.05,
                    pos.getZ() + 0.1 + random.nextDouble() * 0.8, 0.0, 0.0, 0.0);
        }
    }
}
