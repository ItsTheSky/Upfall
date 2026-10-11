package net.itsthesky.upfall.block;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GlowvineBlock extends GrowingPlantHeadBlock {
    public static final MapCodec<GlowvineBlock> CODEC = simpleCodec(GlowvineBlock::new);
    private static final VoxelShape SHAPE = Block.box(4.0, 6.0, 4.0, 12.0, 16.0, 12.0);

    public GlowvineBlock(Properties properties) {
        super(properties, Direction.DOWN, SHAPE, false, 0.08);
    }

    @Override
    protected MapCodec<? extends GrowingPlantHeadBlock> codec() {
        return CODEC;
    }

    @Override
    protected int getBlocksToGrowWhenBonemealed(RandomSource random) {
        return 1 + random.nextInt(3);
    }

    @Override
    protected Block getBodyBlock() {
        return ModBlocks.GLOWVINE_PLANT.get();
    }

    @Override
    protected boolean canGrowInto(BlockState state) {
        return state.isAir();
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(12) == 0) {
            level.addParticle(ModParticles.GILDED_DUST.get(), pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4,
                    pos.getY() + 0.2, pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4, 0.0, -0.01, 0.0);
        }
    }
}
