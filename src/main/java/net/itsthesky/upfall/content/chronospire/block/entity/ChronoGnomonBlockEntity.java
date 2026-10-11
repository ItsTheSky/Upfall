package net.itsthesky.upfall.content.chronospire.block.entity;

import net.itsthesky.upfall.content.chronospire.ChronoBlocks;
import net.itsthesky.upfall.content.chronospire.ChronoClock;
import net.itsthesky.upfall.content.chronospire.ChronoPhase;
import net.itsthesky.upfall.content.chronospire.ChronoRegistries;
import net.itsthesky.upfall.content.chronospire.block.ChronoGnomonBlock;
import net.itsthesky.upfall.content.chronospire.block.PhaseTrackingBlock;
import net.itsthesky.upfall.content.chronospire.block.PhaseWheelBlock;
import net.itsthesky.upfall.content.chronospire.puzzle.PuzzleGates;
import net.itsthesky.upfall.content.chronospire.puzzle.WheelRing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChronoGnomonBlockEntity extends BlockEntity {
    private static final int RANGE = 7;
    private static final int DEPTH = 6;
    private static final float[] PITCH = {0.707F, 0.891F, 1.059F, 1.335F};

    private final PuzzleGates gates = new PuzzleGates();

    public ChronoGnomonBlockEntity(BlockPos pos, BlockState state) {
        super(ChronoRegistries.CHRONO_GNOMON.get(), pos, state);
    }

    public PuzzleGates gates() {
        return gates;
    }

    public boolean solved() {
        return getBlockState().getValue(ChronoGnomonBlock.SOLVED);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ChronoGnomonBlockEntity gnomon) {
        if ((level.getGameTime() + pos.asLong()) % 20 != 0) return;
        ChronoPhase phase = ChronoClock.phase(level);
        if (state.getValue(PhaseTrackingBlock.PHASE) != phase) {
            level.setBlock(pos, state.setValue(PhaseTrackingBlock.PHASE, phase), Block.UPDATE_CLIENTS);
        }
        if (!gnomon.solved()) {
            gnomon.checkSolved(null);
        }
    }

    public List<BlockPos> wheels() {
        List<BlockPos> found = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(worldPosition.offset(-RANGE, -DEPTH, -RANGE), worldPosition.offset(RANGE, 1, RANGE))) {
            if (level.getBlockState(pos).getBlock() instanceof PhaseWheelBlock) {
                found.add(pos.immutable());
            }
        }
        found.sort(Comparator.comparingDouble(p -> Math.atan2(p.getZ() - worldPosition.getZ(), p.getX() - worldPosition.getX())));
        return found;
    }

    public int[] readWheels(List<BlockPos> wheels) {
        int[] values = new int[wheels.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = level.getBlockState(wheels.get(i)).getValue(PhaseTrackingBlock.PHASE).ordinal();
        }
        return values;
    }

    private void writeWheels(List<BlockPos> wheels, int[] values) {
        for (int i = 0; i < values.length; i++) {
            BlockState state = level.getBlockState(wheels.get(i));
            ChronoPhase phase = ChronoPhase.byIndex(values[i]);
            if (state.getValue(PhaseTrackingBlock.PHASE) != phase) {
                level.setBlock(wheels.get(i), state.setValue(PhaseTrackingBlock.PHASE, phase), Block.UPDATE_ALL);
            }
        }
    }

    public void press(BlockPos wheel, @Nullable Player player) {
        if (solved()) {
            if (player != null) tell(player, "message.upfall.wheel.locked");
            return;
        }
        List<BlockPos> wheels = wheels();
        int index = wheels.indexOf(wheel);
        if (index < 0) return;
        int[] values = readWheels(wheels);
        WheelRing.press(values, index);
        writeWheels(wheels, values);
        level.playSound(null, wheel, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.8F, 0.7F);
        level.playSound(null, wheel, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 0.6F, PITCH[values[index]]);
        checkSolved(player);
    }

    public void use(Player player) {
        if (!solved() && player.isShiftKeyDown()) {
            scramble();
            tell(player, "message.upfall.wheel.scrambled");
            return;
        }
        ChronoPhase phase = ChronoClock.phase(level);
        player.displayClientMessage(Component.translatable(solved() ? "message.upfall.wheel.at_rest" : "message.upfall.wheel.hint",
                phase.displayName().withColor(phase.color())), true);
    }

    public void scramble() {
        List<BlockPos> wheels = wheels();
        if (wheels.isEmpty()) return;
        int target = ChronoClock.phase(level).ordinal();
        int[] values = new int[wheels.size()];
        do {
            for (int i = 0; i < values.length; i++) {
                values[i] = level.random.nextInt(WheelRing.STATES);
            }
        } while (WheelRing.solved(values, target));
        writeWheels(wheels, values);
        level.playSound(null, worldPosition, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.8F, 0.7F);
    }

    private void checkSolved(@Nullable Player player) {
        List<BlockPos> wheels = wheels();
        if (wheels.isEmpty() || !WheelRing.solved(readWheels(wheels), ChronoClock.phase(level).ordinal())) return;
        level.setBlock(worldPosition, getBlockState().setValue(ChronoGnomonBlock.SOLVED, true), Block.UPDATE_ALL);
        setChanged();
        if (level instanceof ServerLevel server) {
            gates.open(server, worldPosition);
            server.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.5F, 1.0F);
            server.playSound(null, worldPosition, SoundEvents.BELL_RESONATE, SoundSource.BLOCKS, 1.5F, 1.2F);
            for (ServerPlayer nearby : server.players()) {
                if (nearby.distanceToSqr(worldPosition.getCenter()) < 48 * 48) {
                    tell(nearby, "message.upfall.wheel.solved");
                }
            }
        }
    }

    public static @Nullable ChronoGnomonBlockEntity find(Level level, BlockPos wheelPos) {
        for (BlockPos pos : BlockPos.betweenClosed(wheelPos.offset(-RANGE, -1, -RANGE), wheelPos.offset(RANGE, DEPTH, RANGE))) {
            if (level.getBlockState(pos).is(ChronoBlocks.CHRONO_GNOMON) && level.getBlockEntity(pos) instanceof ChronoGnomonBlockEntity gnomon) {
                return gnomon;
            }
        }
        return null;
    }

    private static void tell(Player player, String key) {
        player.displayClientMessage(Component.translatable(key), true);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        gates.save(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        gates.load(tag);
    }
}
