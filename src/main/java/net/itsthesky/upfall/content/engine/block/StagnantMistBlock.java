package net.itsthesky.upfall.content.engine.block;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StagnantMistBlock extends Block {
    public static final MapCodec<StagnantMistBlock> CODEC = simpleCodec(StagnantMistBlock::new);

    public StagnantMistBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacent, Direction direction) {
        return adjacent.is(this) || super.skipRendering(state, adjacent, direction);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(24) != 0) return;
        Direction side = Direction.getRandom(random);
        BlockPos next = pos.relative(side);
        if (level.getBlockState(next).is(this) || level.getBlockState(next).isSolidRender(level, next)) return;
        level.addParticle(ModParticles.MIST_MOTE.get(),
                pos.getX() + 0.5 + side.getStepX() * 0.55 + (random.nextDouble() - 0.5) * 0.8,
                pos.getY() + 0.5 + side.getStepY() * 0.55 + (random.nextDouble() - 0.5) * 0.8,
                pos.getZ() + 0.5 + side.getStepZ() * 0.55 + (random.nextDouble() - 0.5) * 0.8,
                side.getStepX() * 0.01, 0.003, side.getStepZ() * 0.01);
    }
}
