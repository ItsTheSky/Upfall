package net.itsthesky.upfall.content.grotto;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.grotto.block.LumenSproutsBlock;
import net.itsthesky.upfall.content.grotto.block.PrismPoolBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public final class GrottoBlocks {
    private GrottoBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Upfall.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Upfall.MODID);
    public static final List<DeferredItem<? extends Item>> CREATIVE_ORDER = new ArrayList<>();

    public record CrystalSet(String name, DeferredBlock<Block> block, DeferredBlock<AmethystClusterBlock> cluster,
                             DeferredBlock<AmethystClusterBlock> large, DeferredBlock<AmethystClusterBlock> medium,
                             DeferredBlock<AmethystClusterBlock> small) {
        public List<DeferredBlock<AmethystClusterBlock>> clusters() {
            return List.of(small, medium, large, cluster);
        }
    }

    public static final DeferredItem<Item> MISTGLOW_DUST = item("mistglow_dust");
    public static final DeferredItem<Item> LUMEN_SHARD = item("lumen_shard");

    public static final DeferredBlock<Block> LUMEN_ROCK = block("lumen_rock", Block::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BLUE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
            .strength(3.0F, 6.0F).sound(SoundType.DEEPSLATE));
    public static final DeferredBlock<Block> MOONSHELL = block("moonshell", Block::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
            .strength(1.8F, 4.0F).sound(SoundType.CALCITE));
    public static final DeferredBlock<Block> LUMEN_DRUSE = block("lumen_druse", Block::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_LIGHT_BLUE).instrument(NoteBlockInstrument.CHIME).requiresCorrectToolForDrops()
            .strength(1.5F).sound(SoundType.AMETHYST).lightLevel(s -> 6));
    public static final DeferredBlock<Block> LUMEN_MOSS = block("lumen_moss", Block::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_CYAN).strength(0.2F).sound(SoundType.MOSS).lightLevel(s -> 3));
    public static final DeferredBlock<LumenSproutsBlock> LUMEN_SPROUTS = block("lumen_sprouts", LumenSproutsBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_LIGHT_BLUE).noCollission().instabreak().sound(SoundType.SPORE_BLOSSOM)
            .offsetType(BlockBehaviour.OffsetType.XZ).lightLevel(s -> 7).pushReaction(PushReaction.DESTROY).replaceable());
    public static final DeferredBlock<PrismPoolBlock> PRISM_POOL = block("prism_pool", PrismPoolBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_LIGHT_BLUE).strength(0.5F).sound(SoundType.GLASS).noOcclusion().lightLevel(s -> 9)
            .isRedstoneConductor((s, l, p) -> false).isViewBlocking((s, l, p) -> false).isSuffocating((s, l, p) -> false));

    public static final CrystalSet AZURE = crystals("azure", MapColor.COLOR_LIGHT_BLUE);
    public static final CrystalSet ROSE = crystals("rose", MapColor.COLOR_PINK);
    public static final CrystalSet VERDANT = crystals("verdant", MapColor.COLOR_LIGHT_GREEN);
    public static final List<CrystalSet> CRYSTALS = List.of(AZURE, ROSE, VERDANT);

    public static final DeferredBlock<LanternBlock> MISTGLOW_LANTERN = block("mistglow_lantern", LanternBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_LIGHT_BLUE).forceSolidOn().requiresCorrectToolForDrops().strength(3.5F)
            .sound(SoundType.LANTERN).lightLevel(s -> 15).noOcclusion().pushReaction(PushReaction.DESTROY));

    private static DeferredItem<Item> item(String name) {
        DeferredItem<Item> item = ITEMS.registerSimpleItem(name);
        CREATIVE_ORDER.add(item);
        return item;
    }

    private static <B extends Block> DeferredBlock<B> block(String name, java.util.function.Function<BlockBehaviour.Properties, ? extends B> factory,
                                                            BlockBehaviour.Properties props) {
        DeferredBlock<B> block = BLOCKS.registerBlock(name, factory, props);
        CREATIVE_ORDER.add(ITEMS.registerSimpleBlockItem(block));
        return block;
    }

    private static CrystalSet crystals(String name, MapColor color) {
        DeferredBlock<Block> block = block(name + "_crystal_block", Block::new, BlockBehaviour.Properties.of()
                .mapColor(color).instrument(NoteBlockInstrument.CHIME).requiresCorrectToolForDrops().strength(1.5F)
                .sound(SoundType.AMETHYST).lightLevel(s -> 10));
        return new CrystalSet(name, block,
                cluster(name + "_cluster", color, 7.0F, 3.0F, 11, SoundType.AMETHYST_CLUSTER),
                cluster("large_" + name + "_bud", color, 5.0F, 3.0F, 9, SoundType.LARGE_AMETHYST_BUD),
                cluster("medium_" + name + "_bud", color, 4.0F, 3.0F, 7, SoundType.MEDIUM_AMETHYST_BUD),
                cluster("small_" + name + "_bud", color, 3.0F, 4.0F, 5, SoundType.SMALL_AMETHYST_BUD));
    }

    private static DeferredBlock<AmethystClusterBlock> cluster(String name, MapColor color, float height, float offset, int light, SoundType sound) {
        return block(name, props -> new AmethystClusterBlock(height, offset, props), BlockBehaviour.Properties.of()
                .mapColor(color).forceSolidOn().noOcclusion().sound(sound).strength(1.5F).lightLevel(s -> light)
                .pushReaction(PushReaction.DESTROY));
    }
}
