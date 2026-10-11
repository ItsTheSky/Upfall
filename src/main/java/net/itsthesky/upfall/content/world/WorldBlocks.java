package net.itsthesky.upfall.content.world;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.world.block.CloudbloomBlock;
import net.itsthesky.upfall.content.world.block.CloudstalkBlock;
import net.itsthesky.upfall.content.world.block.FadingCloudBlock;
import net.itsthesky.upfall.content.world.block.RaincloudBlock;
import net.itsthesky.upfall.content.world.block.SkylilyBlock;
import net.itsthesky.upfall.content.world.block.SkyreedBlock;
import net.itsthesky.upfall.block.DenseCloudBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
import java.util.function.Function;

public final class WorldBlocks {
    private WorldBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Upfall.MODID);
    public static final DeferredRegister.Items BLOCK_ITEMS = DeferredRegister.createItems(Upfall.MODID);
    static final List<DeferredItem<? extends Item>> CREATIVE_ORDER = new ArrayList<>();

    public static final DeferredBlock<Block> SKYSILT = block("skysilt", Block::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_WHITE).strength(0.5F).sound(SoundType.SAND).instrument(NoteBlockInstrument.SNARE));
    public static final DeferredBlock<SkyreedBlock> SKYREED = BLOCKS.registerBlock("skyreed", SkyreedBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_LIGHT_BLUE).noCollission().instabreak().sound(SoundType.GRASS)
            .offsetType(BlockBehaviour.OffsetType.XZ).ignitedByLava().pushReaction(PushReaction.DESTROY));
    public static final DeferredItem<DoubleHighBlockItem> SKYREED_ITEM = item("skyreed", props -> new DoubleHighBlockItem(SKYREED.get(), props));
    public static final DeferredBlock<SkylilyBlock> SKYLILY = BLOCKS.registerBlock("skylily", SkylilyBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PINK).instabreak().sound(SoundType.LILY_PAD).noOcclusion().lightLevel(s -> 6)
            .pushReaction(PushReaction.DESTROY));
    public static final DeferredItem<PlaceOnWaterBlockItem> SKYLILY_ITEM = item("skylily", props -> new PlaceOnWaterBlockItem(SKYLILY.get(), props));

    public static final DeferredBlock<DenseCloudBlock> PEARLED_CLOUD = block("pearled_cloud", DenseCloudBlock::new, cloud(MapColor.SNOW)
            .strength(0.8F).lightLevel(s -> 9));
    public static final DeferredBlock<RaincloudBlock> RAINCLOUD = block("raincloud", RaincloudBlock::new, cloud(MapColor.COLOR_LIGHT_GRAY));
    public static final DeferredBlock<CloudstalkBlock> CLOUDSTALK = block("cloudstalk", CloudstalkBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_LIGHT_BLUE).noCollission().instabreak().sound(SoundType.WOOL).pushReaction(PushReaction.DESTROY));
    public static final DeferredBlock<CloudbloomBlock> CLOUDBLOOM = block("cloudbloom", CloudbloomBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_YELLOW).instabreak().sound(SoundType.WOOL).noOcclusion().lightLevel(s -> 13)
            .pushReaction(PushReaction.DESTROY));
    public static final DeferredBlock<Block> CLOUD_LAMP = block("cloud_lamp", Block::new, cloud(MapColor.SNOW).strength(0.6F)
            .lightLevel(s -> 15));
    public static final DeferredBlock<FadingCloudBlock> FADING_CLOUD = BLOCKS.registerBlock("fading_cloud", FadingCloudBlock::new,
            cloud(MapColor.SNOW).strength(0.2F).noOcclusion().noLootTable().isValidSpawn(Blocks::never)
                    .isViewBlocking((s, l, p) -> false).isSuffocating((s, l, p) -> false).pushReaction(PushReaction.DESTROY));

    private static BlockBehaviour.Properties cloud(MapColor color) {
        return BlockBehaviour.Properties.of().mapColor(color).strength(0.4F).sound(SoundType.WOOL).instrument(NoteBlockInstrument.FLUTE);
    }

    private static <B extends Block> DeferredBlock<B> block(String name, Function<BlockBehaviour.Properties, ? extends B> factory,
                                                           BlockBehaviour.Properties props) {
        DeferredBlock<B> block = BLOCKS.registerBlock(name, factory, props);
        CREATIVE_ORDER.add(BLOCK_ITEMS.registerSimpleBlockItem(block));
        return block;
    }

    private static <I extends BlockItem> DeferredItem<I> item(String name, Function<Item.Properties, I> factory) {
        DeferredItem<I> item = BLOCK_ITEMS.registerItem(name, factory);
        CREATIVE_ORDER.add(item);
        return item;
    }
}
