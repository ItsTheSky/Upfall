package net.itsthesky.upfall.content.chronospire.block;

import net.itsthesky.upfall.compat.mc.TooltipSource;
import net.itsthesky.upfall.content.chronospire.ChronoClock;
import net.itsthesky.upfall.content.chronospire.ChronoPhase;
import net.itsthesky.upfall.registry.ModParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.function.Consumer;

public class PhaseStoneBlock extends PhaseTrackingBlock implements TooltipSource {
    public static final EnumProperty<Mode> MODE = EnumProperty.create("mode", Mode.class);
    public static final BooleanProperty SLAB = BooleanProperty.create("slab");
    private static final VoxelShape SLAB_SHAPE = Block.box(0, 0, 0, 16, 8, 16);

    public PhaseStoneBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PHASE, ChronoPhase.DAWN).setValue(MODE, Mode.SOLID).setValue(SLAB, false));
    }

    public static Mode modeAt(ChronoPhase own, long time) {
        if (ChronoClock.phaseAt(time) != own) {
            return Mode.GHOST;
        }
        return ChronoClock.ticksUntilNextPhase(time) <= ChronoClock.FADE_TICKS ? Mode.FADING : Mode.SOLID;
    }

    @Override
    public BlockState stateAt(BlockState state, long time) {
        return state.setValue(MODE, modeAt(state.getValue(PHASE), time));
    }

    @Override
    protected int ticksUntilChange(BlockState state, long time) {
        int untilNext = ChronoClock.ticksUntilNextPhase(time);
        return state.getValue(MODE) == Mode.SOLID ? untilNext - ChronoClock.FADE_TICKS : untilNext;
    }

    @Override
    protected void onPhaseChange(BlockState state, ServerLevel level, BlockPos pos) {
        if (state.getValue(MODE) == Mode.SOLID && level.getRandom().nextInt(6) == 0) {
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.6F, 0.6F + level.getRandom().nextFloat() * 0.4F);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PHASE, MODE, SLAB);
    }

    private static VoxelShape solidShape(BlockState state) {
        return state.getValue(SLAB) ? SLAB_SHAPE : Shapes.block();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(MODE) == Mode.GHOST ? Shapes.empty() : solidShape(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(MODE) == Mode.GHOST ? Shapes.empty() : solidShape(state);
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacent, Direction direction) {
        if (!adjacent.is(this)) {
            return false;
        }
        boolean ghost = state.getValue(MODE) == Mode.GHOST;
        if (ghost != (adjacent.getValue(MODE) == Mode.GHOST)) {
            return false;
        }
        return ghost || !state.getValue(SLAB) && !adjacent.getValue(SLAB);
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
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(MODE) == Mode.FADING && random.nextInt(3) == 0) {
            level.addParticle(ModParticles.GILDED_DUST.get(), pos.getX() + random.nextDouble(),
                    pos.getY() + (state.getValue(SLAB) ? 0.55 : 1.05), pos.getZ() + random.nextDouble(), 0.0, -0.02, 0.0);
        }
    }

    @Override
    public void hoverText(ItemStack stack, Item.TooltipContext context, Consumer<Component> out, TooltipFlag flag) {
        out.accept(Component.translatable("block.upfall.phase_stone.desc").withStyle(ChatFormatting.GRAY));
    }

    //? if <26.1 {
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        hoverText(stack, context, tooltip::add, flag);
    }
    //?}

    public enum Mode implements StringRepresentable {
        SOLID("solid"),
        FADING("fading"),
        GHOST("ghost");

        private final String name;

        Mode(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
