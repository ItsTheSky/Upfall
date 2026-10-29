package net.itsthesky.upfall.content.chronospire.block;

import net.itsthesky.upfall.content.chronospire.ChronoPhase;
import net.itsthesky.upfall.content.chronospire.block.entity.ChronoGnomonBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

public class PhaseWheelBlock extends Block {
    public PhaseWheelBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PhaseTrackingBlock.PHASE, ChronoPhase.DAWN));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PhaseTrackingBlock.PHASE);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ChronoGnomonBlockEntity gnomon = ChronoGnomonBlockEntity.find(level, pos);
        if (gnomon != null) {
            gnomon.press(pos, player);
        } else {
            level.setBlock(pos, state.setValue(PhaseTrackingBlock.PHASE, state.getValue(PhaseTrackingBlock.PHASE).next()), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.8F, 0.7F);
        }
        return InteractionResult.CONSUME;
    }
}
