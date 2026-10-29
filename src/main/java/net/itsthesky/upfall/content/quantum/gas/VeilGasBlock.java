package net.itsthesky.upfall.content.quantum.gas;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.UpfallConfig;
import net.itsthesky.upfall.compat.mc.McEntities;
import net.itsthesky.upfall.compat.mc.McEnv;
import net.itsthesky.upfall.content.quantum.QuantumParticles;
import net.itsthesky.upfall.content.quantum.QuantumDamageTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VeilGasBlock extends Block {
    public static final MapCodec<VeilGasBlock> CODEC = simpleCodec(VeilGasBlock::new);

    public VeilGasBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    //? if >=26.1 {
    /*@Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }
    *///?} else {
    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
    //?}

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacent, Direction direction) {
        return adjacent.is(this) || super.skipRendering(state, adjacent, direction);
    }

    @Override
    protected boolean canBeReplaced(BlockState state, Fluid fluid) {
        return true;
    }

    //? if >=26.1 {
    /*@Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, net.minecraft.world.entity.InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        gasInside(level, pos, entity);
    }
    *///?} else {
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        gasInside(level, pos, entity);
    }
    //?}

    private static void gasInside(Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide() || !(entity instanceof LivingEntity living) || !living.isAlive()) return;
        if (!touchesHead(living, pos) || VeilProtection.isProtected(living)) return;
        int interval = UpfallConfig.getInt(UpfallConfig.GAS_DAMAGE_INTERVAL);
        if (living.tickCount % interval != 0) return;
        float damage = (float) UpfallConfig.getDouble(UpfallConfig.GAS_DAMAGE);
        if (damage > 0.0F) {
            McEntities.hurt(living, level.damageSources().source(QuantumDamageTypes.VEIL_GAS), damage);
        }
    }

    private static boolean touchesHead(LivingEntity entity, BlockPos pos) {
        return BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ()).equals(pos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) != 0) return;
        Player player = McEnv.isClient() ? VeilGasClientHooks.localPlayer() : null;
        if (player == null || player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 25.0) return;
        boolean visible = VeilProtection.isProtected(player);
        if (!visible && random.nextInt(3) != 0) return;
        level.addParticle(QuantumParticles.VEIL_MOTE.get(), pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(),
                pos.getZ() + random.nextDouble(), (random.nextDouble() - 0.5) * 0.01, 0.003, (random.nextDouble() - 0.5) * 0.01);
    }
}
