package net.itsthesky.upfall.content.chronospire.block;

import net.itsthesky.upfall.content.chronospire.ChronoPhase;
import net.itsthesky.upfall.content.chronospire.block.entity.GlyphPedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public class ChronoglyphBlock extends Block {
    public static final EnumProperty<ChronoPhase> GLYPH = EnumProperty.create("glyph", ChronoPhase.class);
    public static final EnumProperty<Light> LIGHT = EnumProperty.create("light", Light.class);

    public ChronoglyphBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(GLYPH, ChronoPhase.DAWN).setValue(LIGHT, Light.OFF));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(GLYPH, LIGHT);
    }

    public static void flash(Level level, BlockPos pos, Light light, int ticks) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof ChronoglyphBlock) || state.getValue(LIGHT) == Light.SOLVED) return;
        level.setBlock(pos, state.setValue(LIGHT, light), Block.UPDATE_CLIENTS);
        level.scheduleTick(pos, state.getBlock(), ticks);
    }

    public static void setSolved(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof ChronoglyphBlock) {
            level.setBlock(pos, state.setValue(LIGHT, Light.SOLVED), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIGHT) == Light.ON || state.getValue(LIGHT) == Light.ERROR) {
            level.setBlock(pos, state.setValue(LIGHT, Light.OFF), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        GlyphPedestalBlockEntity pedestal = GlyphPedestalBlockEntity.find(level, pos);
        if (pedestal != null) {
            pedestal.onGlyph(state.getValue(GLYPH), player);
        } else {
            flash(level, pos, Light.ON, 10);
            GlyphPedestalBlockEntity.chime(level, pos, state.getValue(GLYPH));
        }
        return InteractionResult.CONSUME;
    }

    public enum Light implements StringRepresentable {
        OFF("off", 3),
        ON("on", 11),
        ERROR("error", 7),
        SOLVED("solved", 11);

        private final String name;
        private final int lightLevel;

        Light(String name, int lightLevel) {
            this.name = name;
            this.lightLevel = lightLevel;
        }

        public int lightLevel() {
            return lightLevel;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
