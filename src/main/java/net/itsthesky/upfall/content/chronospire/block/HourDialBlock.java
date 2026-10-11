package net.itsthesky.upfall.content.chronospire.block;

import net.itsthesky.upfall.content.chronospire.ChronoClock;
import net.itsthesky.upfall.content.chronospire.ChronoPhase;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public class HourDialBlock extends PhaseTrackingBlock {
    public HourDialBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PHASE, ChronoPhase.DAWN));
    }

    @Override
    public BlockState stateAt(BlockState state, long time) {
        return state.setValue(PHASE, ChronoClock.phaseAt(time));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PHASE);
    }

    public static Component describe(Level level) {
        long time = ChronoClock.time(level);
        ChronoPhase phase = ChronoClock.phaseAt(time);
        return Component.translatable("message.upfall.hour_dial",
                phase.displayName().withColor(phase.color()),
                Component.literal(ChronoClock.formatTicks(ChronoClock.ticksUntilNextPhase(time))).withStyle(ChatFormatting.WHITE),
                phase.next().displayName().withColor(phase.next().color()));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            player.displayClientMessage(describe(level), true);
            level.playSound(null, pos, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 0.5F, 1.4F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("block.upfall.hour_dial.desc").withStyle(ChatFormatting.GRAY));
    }
}
