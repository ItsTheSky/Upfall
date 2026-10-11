package net.itsthesky.upfall.content.rustworks.block;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.joml.Vector3f;

public class LeakingPipeBlock extends RustyPipeBlock {
    public static final MapCodec<LeakingPipeBlock> CODEC = simpleCodec(LeakingPipeBlock::new);
    public static final int MAX_DRIP_DISTANCE = 10;
    private static final float DRIP_CHANCE = 0.2F;
    private static final DustParticleOptions DRIP = new DustParticleOptions(new Vector3f(0.25F, 0.66F, 0.55F), 0.8F);

    public LeakingPipeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends RotatedPillarBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() < DRIP_CHANCE) {
            tryFillCauldron(level, pos);
        }
    }

    public static boolean tryFillCauldron(ServerLevel level, BlockPos pipe) {
        BlockPos.MutableBlockPos cursor = pipe.mutable();
        for (int i = 0; i < MAX_DRIP_DISTANCE; i++) {
            cursor.move(0, -1, 0);
            if (level.isOutsideBuildHeight(cursor)) {
                return false;
            }
            BlockState below = level.getBlockState(cursor);
            BlockState filled = null;
            if (below.is(Blocks.CAULDRON)) {
                filled = RustworksBlocks.STILLFLOW_CAULDRON.get().defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 1);
            } else if (below.is(RustworksBlocks.STILLFLOW_CAULDRON.get())) {
                int current = below.getValue(LayeredCauldronBlock.LEVEL);
                if (current >= LayeredCauldronBlock.MAX_FILL_LEVEL) {
                    return false;
                }
                filled = below.setValue(LayeredCauldronBlock.LEVEL, current + 1);
            }
            if (filled != null) {
                level.setBlockAndUpdate(cursor, filled);
                level.gameEvent(GameEvent.BLOCK_CHANGE, cursor, GameEvent.Context.of(filled));
                level.playSound(null, cursor, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON, SoundSource.BLOCKS, 0.8F, 0.7F);
                return true;
            }
            if (!below.getCollisionShape(level, cursor).isEmpty() || !below.getFluidState().isEmpty()) {
                return false;
            }
        }
        return false;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(12) != 0) {
            return;
        }
        Direction.Axis axis = state.getValue(AXIS);
        double x = pos.getX() + 0.5;
        double y = pos.getY();
        double z = pos.getZ() + 0.5;
        if (axis != Direction.Axis.Y) {
            y += 0.12;
            if (axis == Direction.Axis.X) {
                x = pos.getX() + random.nextDouble();
            } else {
                z = pos.getZ() + random.nextDouble();
            }
        }
        level.addParticle(DRIP, x + (random.nextDouble() - 0.5) * 0.2, y, z + (random.nextDouble() - 0.5) * 0.2, 0.0, -0.1, 0.0);
    }
}
