package net.itsthesky.upfall.content.rustworks.block;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;

public class OxideTurfBlock extends Block {
    public static final MapCodec<OxideTurfBlock> CODEC = simpleCodec(OxideTurfBlock::new);

    public OxideTurfBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    private static boolean canBeTurf(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);
        if (aboveState.getFluidState().getAmount() == 8) {
            return false;
        }
        int opacity = LightEngine.getLightBlockInto(level, state, pos, aboveState, above, Direction.UP, aboveState.getLightBlock(level, above));
        return opacity < level.getMaxLightLevel();
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canBeTurf(state, level, pos)) {
            if (!level.isAreaLoaded(pos, 1)) return;
            level.setBlockAndUpdate(pos, RustworksBlocks.RUSTSOIL.get().defaultBlockState());
            return;
        }
        if (!level.isAreaLoaded(pos, 3)) return;
        for (int i = 0; i < 4; i++) {
            BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
            if (level.getBlockState(target).is(RustworksBlocks.RUSTSOIL.get()) && canBeTurf(defaultBlockState(), level, target)) {
                level.setBlockAndUpdate(target, defaultBlockState());
            }
        }
    }
}
