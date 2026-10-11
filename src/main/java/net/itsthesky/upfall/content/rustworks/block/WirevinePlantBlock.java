package net.itsthesky.upfall.content.rustworks.block;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantBodyBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WirevinePlantBlock extends GrowingPlantBodyBlock {
    public static final MapCodec<WirevinePlantBlock> CODEC = simpleCodec(WirevinePlantBlock::new);
    private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);

    public WirevinePlantBlock(Properties properties) {
        super(properties, Direction.DOWN, SHAPE, false);
    }

    @Override
    protected MapCodec<? extends GrowingPlantBodyBlock> codec() {
        return CODEC;
    }

    @Override
    protected GrowingPlantHeadBlock getHeadBlock() {
        return RustworksBlocks.WIREVINE.get();
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return super.canSurvive(state, level, pos) || hangsFromPipe(level, pos);
    }

    static boolean hangsFromPipe(LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.above()).getBlock() instanceof RustyPipeBlock;
    }
}
