package net.itsthesky.upfall.content.chronospire;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.chronospire.block.ChronoCurrentBlock;
import net.itsthesky.upfall.content.chronospire.block.ChronoGnomonBlock;
import net.itsthesky.upfall.content.chronospire.block.ChronoSealBlock;
import net.itsthesky.upfall.content.chronospire.block.ChronoglyphBlock;
import net.itsthesky.upfall.content.chronospire.block.GlyphPedestalBlock;
import net.itsthesky.upfall.content.chronospire.block.HourDialBlock;
import net.itsthesky.upfall.content.chronospire.block.PhaseLanternBlock;
import net.itsthesky.upfall.content.chronospire.block.PhaseStoneBlock;
import net.itsthesky.upfall.content.chronospire.block.PhaseWheelBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class ChronoBlocks {
    private ChronoBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Upfall.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Upfall.MODID);
    public static final List<DeferredItem<? extends Item>> CREATIVE_ORDER = new ArrayList<>();

    public static final DeferredBlock<Block> CHRONOSTONE = block("chronostone", Block::new, stone(MapColor.COLOR_PURPLE, SoundType.DEEPSLATE));
    public static final DeferredBlock<Block> CHRONOSTONE_BRICKS = block("chronostone_bricks", Block::new,
            stone(MapColor.COLOR_PURPLE, SoundType.DEEPSLATE_BRICKS));
    public static final DeferredBlock<Block> CHISELED_CHRONOSTONE = block("chiseled_chronostone", Block::new,
            stone(MapColor.COLOR_PURPLE, SoundType.DEEPSLATE_BRICKS));
    public static final DeferredBlock<Block> GILDED_CHRONOSTONE = block("gilded_chronostone", Block::new,
            stone(MapColor.GOLD, SoundType.DEEPSLATE_BRICKS).lightLevel(s -> 2));
    public static final DeferredBlock<Block> WARDED_CHRONOSTONE = block("warded_chronostone", Block::new,
            warded(MapColor.COLOR_PURPLE, SoundType.DEEPSLATE_BRICKS).lightLevel(s -> 3));
    public static final DeferredBlock<TransparentBlock> CHRONOGLASS = block("chronoglass", TransparentBlock::new,
            warded(MapColor.COLOR_LIGHT_BLUE, SoundType.GLASS).noOcclusion().isRedstoneConductor((s, l, p) -> false)
                    .isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p) -> false));

    public static final DeferredBlock<PhaseStoneBlock> PHASE_STONE = block("phase_stone", PhaseStoneBlock::new,
            warded(MapColor.COLOR_LIGHT_BLUE, SoundType.AMETHYST).noOcclusion().isSuffocating((s, l, p) -> false)
                    .isViewBlocking((s, l, p) -> false).isRedstoneConductor((s, l, p) -> false)
                    .lightLevel(s -> switch (s.getValue(PhaseStoneBlock.MODE)) {
                        case SOLID -> 10;
                        case FADING -> 7;
                        case GHOST -> 2;
                    }));
    public static final DeferredBlock<PhaseLanternBlock> PHASE_LANTERN = block("phase_lantern", PhaseLanternBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).strength(1.5F).sound(SoundType.AMETHYST).lightLevel(s -> 15));
    public static final DeferredBlock<HourDialBlock> HOUR_DIAL = block("hour_dial", HourDialBlock::new,
            stone(MapColor.COLOR_PURPLE, SoundType.DEEPSLATE_BRICKS).strength(2.0F, 6.0F).lightLevel(s -> 6));

    public static final DeferredBlock<ChronoglyphBlock> CHRONOGLYPH = block("chronoglyph", ChronoglyphBlock::new,
            warded(MapColor.COLOR_PURPLE, SoundType.AMETHYST).lightLevel(s -> s.getValue(ChronoglyphBlock.LIGHT).lightLevel()));
    public static final DeferredBlock<GlyphPedestalBlock> GLYPH_PEDESTAL = block("glyph_pedestal", GlyphPedestalBlock::new,
            warded(MapColor.COLOR_PURPLE, SoundType.DEEPSLATE_BRICKS)
                    .lightLevel(s -> s.getValue(GlyphPedestalBlock.MODE) == GlyphPedestalBlock.Mode.SOLVED ? 12 : 5));
    public static final DeferredBlock<PhaseWheelBlock> PHASE_WHEEL = block("phase_wheel", PhaseWheelBlock::new,
            warded(MapColor.COLOR_PURPLE, SoundType.DEEPSLATE_BRICKS).lightLevel(s -> 6));
    public static final DeferredBlock<ChronoGnomonBlock> CHRONO_GNOMON = block("chrono_gnomon", ChronoGnomonBlock::new,
            warded(MapColor.GOLD, SoundType.AMETHYST).lightLevel(s -> s.getValue(ChronoGnomonBlock.SOLVED) ? 15 : 10));
    public static final DeferredBlock<ChronoSealBlock> CHRONO_SEAL = block("chrono_seal", ChronoSealBlock::new,
            warded(MapColor.COLOR_LIGHT_BLUE, SoundType.AMETHYST).noOcclusion().isSuffocating((s, l, p) -> false)
                    .isViewBlocking((s, l, p) -> false).isRedstoneConductor((s, l, p) -> false)
                    .lightLevel(s -> s.getValue(ChronoSealBlock.OPEN) ? 0 : 6));

    public static final DeferredBlock<ChronoCurrentBlock> CHRONO_CURRENT = BLOCKS.registerBlock("chrono_current", ChronoCurrentBlock::new,
            BlockBehaviour.Properties.of().noCollission().noOcclusion().noLootTable().replaceable().strength(-1.0F, 0.0F)
                    .isValidSpawn(Blocks::never).isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p) -> false)
                    .pushReaction(PushReaction.DESTROY));
    public static final DeferredBlock<Block> DRIFT_PLATE = BLOCKS.registerBlock("drift_plate", Block::new,
            BlockBehaviour.Properties.of().noLootTable().noOcclusion().strength(-1.0F, 3600000.0F));

    private static <B extends Block> DeferredBlock<B> block(String name, Function<BlockBehaviour.Properties, ? extends B> factory,
                                                           BlockBehaviour.Properties props) {
        DeferredBlock<B> block = BLOCKS.registerBlock(name, factory, props);
        CREATIVE_ORDER.add(ITEMS.registerSimpleBlockItem(block));
        return block;
    }

    private static BlockBehaviour.Properties stone(MapColor color, SoundType sound) {
        return BlockBehaviour.Properties.of().mapColor(color).instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops().strength(3.0F, 9.0F).sound(sound);
    }

    private static BlockBehaviour.Properties warded(MapColor color, SoundType sound) {
        return BlockBehaviour.Properties.of().mapColor(color).strength(-1.0F, 3600000.0F).noLootTable()
                .isValidSpawn(Blocks::never).pushReaction(PushReaction.BLOCK).sound(sound);
    }
}
