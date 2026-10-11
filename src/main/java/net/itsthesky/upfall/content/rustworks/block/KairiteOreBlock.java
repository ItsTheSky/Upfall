package net.itsthesky.upfall.content.rustworks.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.joml.Vector3f;

public class KairiteOreBlock extends DropExperienceBlock {
    public static final MapCodec<KairiteOreBlock> CODEC = simpleCodec(KairiteOreBlock::new);
    public static final IntegerProperty GLOW = IntegerProperty.create("glow", 0, 2);
    public static final int PERIOD = 200;
    public static final int PEAK_LENGTH = 40;
    public static final int FADE_LENGTH = 20;
    private static final DustParticleOptions SPARK = new DustParticleOptions(new Vector3f(0.47F, 0.92F, 0.76F), 0.6F);

    public KairiteOreBlock(Properties properties) {
        super(UniformInt.of(1, 3), properties);
        registerDefaultState(stateDefinition.any().setValue(GLOW, 0));
    }

    @Override
    public MapCodec<? extends DropExperienceBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(GLOW);
    }

    public static int light(BlockState state) {
        return switch (state.getValue(GLOW)) {
            case 2 -> 12;
            case 1 -> 6;
            default -> 2;
        };
    }

    public static int phase(long gameTime, BlockPos pos) {
        int cell = (pos.getX() >> 3) * 73856093 ^ (pos.getY() >> 3) * 19349663 ^ (pos.getZ() >> 3) * 83492791;
        return (int) Math.floorMod(gameTime + Math.floorMod(cell, PERIOD), (long) PERIOD);
    }

    public static int glowAt(int phase) {
        if (phase < PEAK_LENGTH) {
            return 2;
        }
        return phase < PEAK_LENGTH + FADE_LENGTH || phase >= PERIOD - FADE_LENGTH ? 1 : 0;
    }

    private static int ticksToNextStage(int phase) {
        int next = phase < PEAK_LENGTH ? PEAK_LENGTH
                : phase < PEAK_LENGTH + FADE_LENGTH ? PEAK_LENGTH + FADE_LENGTH
                : phase < PERIOD - FADE_LENGTH ? PERIOD - FADE_LENGTH : PERIOD;
        return Math.max(1, next - phase);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide && !oldState.is(this)) {
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
        int glow = glowAt(phase);
        if (state.getValue(GLOW) != glow) {
            level.setBlock(pos, state.setValue(GLOW, glow), Block.UPDATE_ALL);
            if (glow == 2 && random.nextInt(4) == 0 && level.hasNearbyAlivePlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 12.0)) {
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 0.4F, 1.4F);
            }
        }
        level.scheduleTick(pos, this, ticksToNextStage(phase));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(GLOW) == 2 && random.nextInt(6) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 1.05;
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(SPARK, x, y, z, 0.0, 0.02, 0.0);
        }
    }
}
