package net.itsthesky.upfall.content.rustworks.block;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

//? if >=26.1 {
/*public class RivetgrassBlock extends net.minecraft.world.level.block.VegetationBlock implements BonemealableBlock {
*///?} else {
public class RivetgrassBlock extends BushBlock implements BonemealableBlock {
//?}
    public static final MapCodec<RivetgrassBlock> CODEC = simpleCodec(RivetgrassBlock::new);
    private static final VoxelShape SHAPE = BushBlock.box(2.0, 0.0, 2.0, 14.0, 12.0, 14.0);

    public RivetgrassBlock(Properties properties) {
        super(properties);
    }

    //? if >=26.1 {
    /*@Override
    protected MapCodec<? extends net.minecraft.world.level.block.VegetationBlock> codec() {
        return CODEC;
    }
    *///?} else {
    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }
    //?}

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        //? if >=26.1 {
        /*Vec3 offset = state.getOffset(pos);
        *///?} else {
        Vec3 offset = state.getOffset(level, pos);
        //?}
        return SHAPE.move(offset.x, offset.y, offset.z);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return level.getBlockState(pos.above()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        DoublePlantBlock tall = (DoublePlantBlock) RustworksBlocks.TALL_RIVETGRASS.get();
        if (tall.defaultBlockState().canSurvive(level, pos) && level.isEmptyBlock(pos.above())) {
            DoublePlantBlock.placeAt(level, tall.defaultBlockState(), pos, 2);
        }
    }
}
