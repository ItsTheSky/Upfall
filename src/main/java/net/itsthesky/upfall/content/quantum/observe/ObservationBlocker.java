package net.itsthesky.upfall.content.quantum.observe;

import net.minecraft.world.level.block.state.BlockState;

public interface ObservationBlocker {
    boolean blocksObservation(BlockState state);
}
