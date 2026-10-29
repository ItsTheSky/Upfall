package net.itsthesky.upfall.content.chronospire.block;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.content.chronospire.ChronoClock;
import net.itsthesky.upfall.content.chronospire.ChronoPhase;
import net.itsthesky.upfall.content.chronospire.ChronoRegistries;
import net.itsthesky.upfall.content.chronospire.block.entity.ChronoGnomonBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ChronoGnomonBlock extends BaseEntityBlock {
    public static final MapCodec<ChronoGnomonBlock> CODEC = simpleCodec(ChronoGnomonBlock::new);
    public static final BooleanProperty SOLVED = BooleanProperty.create("solved");

    public ChronoGnomonBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PhaseTrackingBlock.PHASE, ChronoPhase.DAWN).setValue(SOLVED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PhaseTrackingBlock.PHASE, SOLVED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(PhaseTrackingBlock.PHASE, ChronoClock.phase(context.getLevel()));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChronoGnomonBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, ChronoRegistries.CHRONO_GNOMON.get(), ChronoGnomonBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof ChronoGnomonBlockEntity gnomon) {
            gnomon.use(player);
        }
        return InteractionResult.CONSUME;
    }
}
