package net.itsthesky.upfall.content.quantum.pedestal;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.itsthesky.upfall.content.quantum.QuantumParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

public class QuantumPedestalBlock extends PedestalBlock {
    public static final MapCodec<QuantumPedestalBlock> CODEC = simpleCodec(QuantumPedestalBlock::new);

    public QuantumPedestalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new QuantumPedestalBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != QuantumBlocks.QUANTUM_PEDESTAL_ENTITY.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<QuantumPedestalBlockEntity>) QuantumPedestalBlockEntity::serverTick;
    }

    @Override
    protected boolean locked(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof QuantumPedestalBlockEntity pedestal && pedestal.active();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.isSecondaryUseActive()) return super.useWithoutItem(state, level, pos, player, hit);
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof QuantumPedestalBlockEntity pedestal) {
            Component refusal = pedestal.tryStart(serverPlayer);
            if (refusal != null) player.displayClientMessage(refusal, true);
        }
        return InteractionResult.CONSUME;
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (event.getHand() == InteractionHand.MAIN_HAND && player.isSecondaryUseActive() && player.getMainHandItem().isEmpty()
                && event.getLevel().getBlockState(event.getPos()).getBlock() instanceof QuantumPedestalBlock) {
            event.setUseBlock(TriState.TRUE);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) == 0) {
            level.addParticle(QuantumParticles.QUANTUM_SPARK.get(), pos.getX() + 0.2 + random.nextDouble() * 0.6, pos.getY() + 1.05,
                    pos.getZ() + 0.2 + random.nextDouble() * 0.6, 0.0, 0.02, 0.0);
        }
    }
}
