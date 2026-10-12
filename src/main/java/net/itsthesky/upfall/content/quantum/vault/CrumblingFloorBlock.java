package net.itsthesky.upfall.content.quantum.vault;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.UpfallConfig;
import net.itsthesky.upfall.content.quantum.QuantumSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CrumblingFloorBlock extends Block {
    public static final MapCodec<CrumblingFloorBlock> CODEC = simpleCodec(CrumblingFloorBlock::new);
    public static final BooleanProperty CRUMBLED = BooleanProperty.create("crumbled");
    private static final int DELAY = 5;

    public CrumblingFloorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(CRUMBLED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CRUMBLED);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(CRUMBLED) ? Shapes.empty() : Shapes.block();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(CRUMBLED) ? Shapes.empty() : Shapes.block();
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return state.getValue(CRUMBLED) ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(CRUMBLED);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && entity instanceof Player player && !player.isSpectator() && !state.getValue(CRUMBLED)
                && !level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, DELAY);
            level.playSound(null, pos, QuantumSounds.FLOOR_CRUMBLE.get(), SoundSource.BLOCKS, 0.4F, 1.6F);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(CRUMBLED)) {
            for (BlockPos other : BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, 0, 1))) {
                BlockState neighbor = level.getBlockState(other);
                if (neighbor.is(this) && !neighbor.getValue(CRUMBLED)) {
                    level.setBlock(other, neighbor.setValue(CRUMBLED, true), Block.UPDATE_ALL);
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, neighbor), other.getX() + 0.5, other.getY() + 0.5,
                            other.getZ() + 0.5, 12, 0.3, 0.2, 0.3, 0.05);
                    level.scheduleTick(other.immutable(), this, UpfallConfig.getInt(UpfallConfig.VAULT_FLOOR_REGROW_TICKS));
                }
            }
            level.playSound(null, pos, QuantumSounds.FLOOR_CRUMBLE.get(), SoundSource.BLOCKS, 1.0F, 0.9F);
        } else if (level.getEntitiesOfClass(Entity.class, new AABB(pos).inflate(0.1, 1.0, 0.1)).isEmpty()) {
            level.setBlock(pos, state.setValue(CRUMBLED, false), Block.UPDATE_ALL);
        } else {
            level.scheduleTick(pos, this, 40);
        }
    }
}
