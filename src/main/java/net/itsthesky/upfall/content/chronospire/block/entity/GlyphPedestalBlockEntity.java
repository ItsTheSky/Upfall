package net.itsthesky.upfall.content.chronospire.block.entity;

import net.itsthesky.upfall.content.chronospire.ChronoBlocks;
import net.itsthesky.upfall.content.chronospire.ChronoPhase;
import net.itsthesky.upfall.content.chronospire.ChronoRegistries;
import net.itsthesky.upfall.content.chronospire.block.ChronoglyphBlock;
import net.itsthesky.upfall.content.chronospire.block.GlyphPedestalBlock.Mode;
import net.itsthesky.upfall.content.chronospire.block.GlyphPedestalBlock;
import net.itsthesky.upfall.content.chronospire.puzzle.PuzzleGates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GlyphPedestalBlockEntity extends BlockEntity {
    public static final int LENGTH = 5;
    public static final int SHOW_INTERVAL = 18;
    private static final int SHOW_LIT = 12;
    private static final int LISTEN_TIMEOUT = 1200;
    private static final int RANGE = 8;
    private static final int DEPTH = 6;
    private static final float[] PITCH = {0.707F, 0.891F, 1.059F, 1.335F};

    private int[] sequence = new int[0];
    private int step;
    private int timer;
    private final PuzzleGates gates = new PuzzleGates();

    public GlyphPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ChronoRegistries.GLYPH_PEDESTAL.get(), pos, state);
    }

    public PuzzleGates gates() {
        return gates;
    }

    public Mode mode() {
        return getBlockState().getValue(GlyphPedestalBlock.MODE);
    }

    public int[] sequence() {
        if (sequence.length != LENGTH) {
            sequence = generate(level != null ? level.random : RandomSource.create(), LENGTH);
            setChanged();
        }
        return sequence;
    }

    public static int[] generate(RandomSource random, int length) {
        int[] seq = new int[length];
        for (int i = 0; i < length; i++) {
            do {
                seq[i] = random.nextInt(ChronoPhase.values().length);
            } while (i > 0 && seq[i] == seq[i - 1]);
        }
        return seq;
    }

    private void setMode(Mode mode) {
        if (level != null && mode() != mode) {
            level.setBlock(worldPosition, getBlockState().setValue(GlyphPedestalBlock.MODE, mode), Block.UPDATE_ALL);
        }
        setChanged();
    }

    public void use(Player player) {
        switch (mode()) {
            case SOLVED -> tell(player, "message.upfall.glyph.at_rest");
            case SHOWING -> tell(player, "message.upfall.glyph.watch");
            case LISTENING -> {
                if (player.isShiftKeyDown()) {
                    setMode(Mode.IDLE);
                    tell(player, "message.upfall.glyph.reset");
                } else {
                    startShowing();
                    tell(player, "message.upfall.glyph.watch");
                }
            }
            case IDLE -> {
                startShowing();
                tell(player, "message.upfall.glyph.watch");
            }
        }
    }

    public void startShowing() {
        sequence();
        step = 0;
        timer = -10;
        setMode(Mode.SHOWING);
        level.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.0F, 0.8F);
    }

    public void onGlyph(ChronoPhase glyph, @Nullable Player player) {
        switch (mode()) {
            case SOLVED -> {
                chime(level, worldPosition, glyph);
            }
            case IDLE, SHOWING -> {
                light(glyph, ChronoglyphBlock.Light.ON, 10);
                chime(level, worldPosition, glyph);
                if (player != null && mode() == Mode.IDLE) {
                    tell(player, "message.upfall.glyph.idle");
                }
            }
            case LISTENING -> {
                timer = 0;
                if (sequence()[step] == glyph.ordinal()) {
                    step++;
                    light(glyph, ChronoglyphBlock.Light.ON, 10);
                    chime(level, worldPosition, glyph);
                    if (step >= sequence.length) {
                        solve();
                    } else if (player != null) {
                        player.displayClientMessage(Component.translatable("message.upfall.glyph.progress", step, sequence.length), true);
                    }
                } else {
                    step = 0;
                    for (ChronoPhase each : ChronoPhase.values()) {
                        light(each, ChronoglyphBlock.Light.ERROR, 24);
                    }
                    level.playSound(null, worldPosition, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 1.0F, 0.5F);
                    level.playSound(null, worldPosition, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 1.0F, 0.53F);
                    setMode(Mode.IDLE);
                    if (player != null) {
                        tell(player, "message.upfall.glyph.wrong");
                    }
                }
                setChanged();
            }
        }
    }

    private void solve() {
        setMode(Mode.SOLVED);
        for (BlockPos pos : glyphs(null)) {
            ChronoglyphBlock.setSolved(level, pos);
        }
        if (level instanceof ServerLevel server) {
            gates.open(server, worldPosition);
            server.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.3F);
            server.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.0F, 1.5F);
            for (ServerPlayer player : server.players()) {
                if (player.distanceToSqr(worldPosition.getCenter()) < 32 * 32) {
                    tell(player, "message.upfall.glyph.solved");
                }
            }
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GlyphPedestalBlockEntity pedestal) {
        Mode mode = state.getValue(GlyphPedestalBlock.MODE);
        if (mode == Mode.SHOWING) {
            pedestal.timer++;
            if (pedestal.timer >= 0 && pedestal.timer % SHOW_INTERVAL == 0) {
                int index = pedestal.timer / SHOW_INTERVAL;
                int[] seq = pedestal.sequence();
                if (index < seq.length) {
                    ChronoPhase glyph = ChronoPhase.byIndex(seq[index]);
                    pedestal.light(glyph, ChronoglyphBlock.Light.ON, SHOW_LIT);
                    chime(level, pos, glyph);
                } else {
                    pedestal.step = 0;
                    pedestal.timer = 0;
                    pedestal.setMode(Mode.LISTENING);
                }
            }
        } else if (mode == Mode.LISTENING && ++pedestal.timer > LISTEN_TIMEOUT) {
            pedestal.step = 0;
            pedestal.setMode(Mode.IDLE);
        }
    }

    private void light(ChronoPhase glyph, ChronoglyphBlock.Light light, int ticks) {
        for (BlockPos pos : glyphs(glyph)) {
            ChronoglyphBlock.flash(level, pos, light, ticks);
        }
    }

    public List<BlockPos> glyphs(@Nullable ChronoPhase glyph) {
        List<BlockPos> found = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(worldPosition.offset(-RANGE, -2, -RANGE), worldPosition.offset(RANGE, DEPTH, RANGE))) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof ChronoglyphBlock && (glyph == null || state.getValue(ChronoglyphBlock.GLYPH) == glyph)) {
                found.add(pos.immutable());
            }
        }
        return found;
    }

    public static @Nullable GlyphPedestalBlockEntity find(Level level, BlockPos glyphPos) {
        for (BlockPos pos : BlockPos.betweenClosed(glyphPos.offset(-RANGE, -DEPTH, -RANGE), glyphPos.offset(RANGE, 2, RANGE))) {
            if (level.getBlockState(pos).is(ChronoBlocks.GLYPH_PEDESTAL) && level.getBlockEntity(pos) instanceof GlyphPedestalBlockEntity pedestal) {
                return pedestal;
            }
        }
        return null;
    }

    public static void chime(Level level, BlockPos pos, ChronoPhase glyph) {
        level.playSound(null, pos, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 1.0F, PITCH[glyph.ordinal()]);
    }

    private static void tell(Player player, String key) {
        player.displayClientMessage(Component.translatable(key), true);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putIntArray("Sequence", sequence);
        tag.putInt("Step", step);
        tag.putInt("Timer", timer);
        gates.save(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        sequence = tag.getIntArray("Sequence");
        step = tag.getInt("Step");
        timer = tag.getInt("Timer");
        gates.load(tag);
    }
}
