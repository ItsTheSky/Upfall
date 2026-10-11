package net.itsthesky.upfall.content.rustworks.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RustyPipeBlock extends RotatedPillarBlock {
    public static final MapCodec<RustyPipeBlock> CODEC = simpleCodec(RustyPipeBlock::new);
    private static final VoxelShape SHAPE_X = Block.box(0, 2, 2, 16, 14, 14);
    private static final VoxelShape SHAPE_Y = Block.box(2, 0, 2, 14, 16, 14);
    private static final VoxelShape SHAPE_Z = Block.box(2, 2, 0, 14, 14, 16);

    public RustyPipeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends RotatedPillarBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(AXIS)) {
            case X -> SHAPE_X;
            case Y -> SHAPE_Y;
            case Z -> SHAPE_Z;
        };
    }
}
