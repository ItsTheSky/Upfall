package net.itsthesky.upfall.content.rustworks.block;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.content.rustworks.RustworksSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ExhaustVentBlock extends Block {
    public static final MapCodec<ExhaustVentBlock> CODEC = simpleCodec(ExhaustVentBlock::new);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final int BURST = 40;
    public static final int MIN_IDLE = 80;
    public static final int MAX_COLUMN = 18;
    public static final double LAUNCH_SPEED = 1.05;

    public ExhaustVentBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    public static int light(BlockState state) {
        return state.getValue(ACTIVE) ? 7 : 0;
    }

    public static int period(BlockPos pos) {
        return BURST + MIN_IDLE + (int) Math.floorMod(Mth.getSeed(pos), 41L);
    }

    public static int phase(long gameTime, BlockPos pos) {
        int period = period(pos);
        return (int) Math.floorMod(gameTime + Math.floorMod(Mth.getSeed(pos) >> 16, (long) period), (long) period);
    }

    public static int column(BlockGetter level, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = pos.mutable();
        for (int i = 1; i <= MAX_COLUMN; i++) {
            cursor.setY(pos.getY() + i);
            if (level.isOutsideBuildHeight(cursor) || !level.getBlockState(cursor).getCollisionShape(level, cursor).isEmpty()) {
                return i - 1;
            }
        }
        return MAX_COLUMN;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide() && !oldState.is(this)) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int phase = phase(level.getGameTime(), pos);
        boolean active = phase < BURST;
        if (active != state.getValue(ACTIVE)) {
            level.setBlock(pos, state.setValue(ACTIVE, active), Block.UPDATE_CLIENTS);
        }
        if (!active) {
            level.scheduleTick(pos, this, period(pos) - phase);
            return;
        }
        int height = column(level, pos);
        if (height > 0) {
            if (phase == 0 || phase == BURST / 2) {
                level.playSound(null, pos, RustworksSounds.EXHAUST_VENT_HISS.get(), SoundSource.BLOCKS, 1.4F, 0.85F + random.nextFloat() * 0.3F);
            }
            vent(level, pos, random, phase);
            launch(level, pos, height);
        }
        level.scheduleTick(pos, this, 1);
    }

    private static void vent(ServerLevel level, BlockPos pos, RandomSource random, int phase) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.05;
        double z = pos.getZ() + 0.5;
        double fade = phase > BURST - 8 ? 0.5 : 1.0;
        for (int i = 0; i < 3; i++) {
            level.sendParticles(ParticleTypes.CLOUD, x + random.nextGaussian() * 0.15, y, z + random.nextGaussian() * 0.15, 0,
                    random.nextGaussian() * 0.04, 1.0, random.nextGaussian() * 0.04, (0.55 + random.nextDouble() * 0.6) * fade);
        }
        level.sendParticles(ParticleTypes.WHITE_SMOKE, x, y, z, 2, 0.2, 0.05, 0.2, 0.05);
        if (random.nextInt(3) == 0) {
            level.sendParticles(ParticleTypes.POOF, x, y + 0.2, z, 1, 0.25, 0.1, 0.25, 0.04);
        }
    }

    private static void launch(ServerLevel level, BlockPos pos, int height) {
        AABB box = new AABB(pos.getX() - 0.2, pos.getY() + 1.0, pos.getZ() - 0.2, pos.getX() + 1.2, pos.getY() + 1.0 + height, pos.getZ() + 1.2);
        for (Entity entity : level.getEntities((Entity) null, box, ExhaustVentBlock::canLaunch)) {
            Vec3 motion = entity.getDeltaMovement();
            double target = entity.isShiftKeyDown() ? LAUNCH_SPEED * 0.5 : LAUNCH_SPEED;
            if (motion.y < target) {
                entity.setDeltaMovement(motion.x, target, motion.z);
                entity.hurtMarked = true;
            }
            entity.resetFallDistance();
        }
    }

    private static boolean canLaunch(Entity entity) {
        if (entity.isSpectator() || entity.noPhysics || entity.isPassenger()) return false;
        return !(entity instanceof Player player) || !player.getAbilities().flying;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()) return;
        double x = pos.getX() + 0.3 + random.nextDouble() * 0.4;
        double z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;
        if (state.getValue(ACTIVE)) {
            level.addParticle(ParticleTypes.CLOUD, x, pos.getY() + 1.1, z, 0.0, 0.6 + random.nextDouble() * 0.4, 0.0);
        } else if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.SMOKE, x, pos.getY() + 1.05, z, 0.0, 0.04, 0.0);
        }
    }
}
