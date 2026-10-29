package net.itsthesky.upfall.content.quantum.client;

import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;

final class VeilGasFaces {
    static final float INSET = 0.004F;

    private VeilGasFaces() {}

    static int exposedMask(BlockGetter level, BlockPos pos) {
        int mask = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()) {
            if (!level.getBlockState(cursor.setWithOffset(pos, direction)).is(QuantumBlocks.VEIL_GAS.get())) {
                mask |= 1 << direction.ordinal();
            }
        }
        return mask;
    }
}
