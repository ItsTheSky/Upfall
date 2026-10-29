package net.itsthesky.upfall.content.hamlet.block;

import net.itsthesky.upfall.content.hamlet.rope.RopeLines;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractRopeAnchorBlock extends BaseEntityBlock {
    protected AbstractRopeAnchorBlock(Properties properties) {
        super(properties);
    }

    public abstract Vec3 attachPoint(BlockPos pos, BlockState state);

    public abstract int maxLinks();

    public abstract boolean isZipline();

    public abstract double maxLength();

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RopeAnchorBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    //? if <26.1 {
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()) {
            RopeLines.unlinkAll(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
    //?}
}
