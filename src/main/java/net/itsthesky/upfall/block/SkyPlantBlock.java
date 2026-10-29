package net.itsthesky.upfall.block;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

//? if >=26.1 {
/*public class SkyPlantBlock extends net.minecraft.world.level.block.VegetationBlock {
*///?} else {
public class SkyPlantBlock extends BushBlock {
//?}
    public static final MapCodec<SkyPlantBlock> CODEC = simpleCodec(SkyPlantBlock::new);
    private static final VoxelShape SHAPE = BushBlock.box(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);

    public SkyPlantBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<SkyPlantBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(ModTags.Blocks.SKY_PLANTABLE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        //? if >=26.1 {
        /*net.minecraft.world.phys.Vec3 offset = state.getOffset(pos);
        *///?} else {
        net.minecraft.world.phys.Vec3 offset = state.getOffset(level, pos);
        //?}
        return SHAPE.move(offset.x, offset.y, offset.z);
    }
}
