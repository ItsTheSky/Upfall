package net.itsthesky.upfall.content.rustworks.block;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.itsthesky.upfall.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WirevineBlock extends GrowingPlantHeadBlock {
    public static final MapCodec<WirevineBlock> CODEC = simpleCodec(WirevineBlock::new);
    private static final VoxelShape SHAPE = Block.box(4.0, 6.0, 4.0, 12.0, 16.0, 12.0);

    public WirevineBlock(Properties properties) {
        super(properties, Direction.DOWN, SHAPE, false, 0.06);
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
        return RustworksBlocks.WIREVINE_PLANT.get();
    }

    @Override
    protected boolean canGrowInto(BlockState state) {
        return state.isAir();
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return super.canSurvive(state, level, pos) || WirevinePlantBlock.hangsFromPipe(level, pos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(14) == 0) {
            level.addParticle(ModParticles.RUST_FLAKE.get(), pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.3,
                    pos.getY() + 0.3, pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.3, 0.0, -0.005, 0.0);
        }
    }
}
