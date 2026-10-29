package net.itsthesky.upfall.content.hamlet;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.compat.mc.McRegistration;
import net.itsthesky.upfall.content.hamlet.block.RopeAnchorBlock;
import net.itsthesky.upfall.content.hamlet.block.RopeAnchorBlockEntity;
import net.itsthesky.upfall.content.hamlet.block.RopeBlock;
import net.itsthesky.upfall.content.hamlet.block.RopeBridgeBlock;
import net.itsthesky.upfall.content.hamlet.block.ZiplineAnchorBlock;
import net.itsthesky.upfall.content.hamlet.item.RopeItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class HamletBlocks {
    private HamletBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Upfall.MODID);
    public static final DeferredRegister.Items BLOCK_ITEMS = DeferredRegister.createItems(Upfall.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Upfall.MODID);
    static final List<DeferredItem<? extends Item>> CREATIVE_ORDER = new ArrayList<>();

    public static final DeferredBlock<Block> WOVEN_PLANKS = block("woven_planks", Block::new, woven());
    public static final DeferredBlock<StairBlock> WOVEN_STAIRS = block("woven_stairs",
            p -> new StairBlock(WOVEN_PLANKS.get().defaultBlockState(), p), woven());
    public static final DeferredBlock<SlabBlock> WOVEN_SLAB = block("woven_slab", SlabBlock::new, woven());

    public record CanopySet(String color, DeferredBlock<Block> block, DeferredBlock<StairBlock> stairs, DeferredBlock<SlabBlock> slab) {}

    public static final CanopySet DUSK_CANOPY = canopy("dusk", MapColor.COLOR_PINK);
    public static final CanopySet PEARL_CANOPY = canopy("pearl", MapColor.SNOW);
    public static final CanopySet SKY_CANOPY = canopy("sky", MapColor.COLOR_LIGHT_BLUE);
    public static final List<CanopySet> CANOPIES = List.of(DUSK_CANOPY, PEARL_CANOPY, SKY_CANOPY);

    public static final DeferredBlock<RopeBlock> ROPE = McRegistration.block(BLOCKS, "rope", RopeBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.SAND).noCollission().strength(0.4F).sound(SoundType.WOOL).noOcclusion()
            .pushReaction(PushReaction.DESTROY).ignitedByLava());
    public static final DeferredItem<RopeItem> ROPE_ITEM = item(McRegistration.blockItem(BLOCK_ITEMS, "rope", p -> new RopeItem(ROPE.get(), p)));
    public static final DeferredBlock<RopeBridgeBlock> ROPE_BRIDGE = block("rope_bridge", RopeBridgeBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD).strength(0.8F).sound(SoundType.SCAFFOLDING).noOcclusion().ignitedByLava());
    public static final DeferredBlock<RopeAnchorBlock> ROPE_ANCHOR = block("rope_anchor", RopeAnchorBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD).strength(0.8F).sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.DESTROY));
    public static final DeferredBlock<ZiplineAnchorBlock> ZIPLINE_ANCHOR = block("zipline_anchor", ZiplineAnchorBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()
            .pushReaction(PushReaction.BLOCK));
    public static final DeferredBlock<LanternBlock> CLOUD_LANTERN = block("cloud_lantern", LanternBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.QUARTZ).forceSolidOn().strength(3.5F).sound(SoundType.LANTERN).lightLevel(s -> 15)
            .noOcclusion().pushReaction(PushReaction.DESTROY));
    public static final DeferredBlock<RotatedPillarBlock> ROPE_SPOOL = block("rope_spool", RotatedPillarBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASS).strength(1.5F).sound(SoundType.WOOD).ignitedByLava());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RopeAnchorBlockEntity>> ROPE_ANCHOR_ENTITY = BLOCK_ENTITIES.register("rope_anchor",
            () -> McRegistration.blockEntityType(RopeAnchorBlockEntity::new, ROPE_ANCHOR.get(), ZIPLINE_ANCHOR.get()));

    private static BlockBehaviour.Properties woven() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F)
                .sound(SoundType.BAMBOO_WOOD).ignitedByLava();
    }

    private static CanopySet canopy(String color, MapColor mapColor) {
        java.util.function.Supplier<BlockBehaviour.Properties> props = () -> BlockBehaviour.Properties.of().mapColor(mapColor)
                .instrument(NoteBlockInstrument.GUITAR).strength(0.8F).sound(SoundType.WOOL).ignitedByLava();
        DeferredBlock<Block> block = block(color + "_canopy", Block::new, props.get());
        DeferredBlock<StairBlock> stairs = block(color + "_canopy_stairs", p -> new StairBlock(block.get().defaultBlockState(), p), props.get());
        DeferredBlock<SlabBlock> slab = block(color + "_canopy_slab", SlabBlock::new, props.get());
        return new CanopySet(color, block, stairs, slab);
    }

    private static <B extends Block> DeferredBlock<B> block(String name, Function<BlockBehaviour.Properties, ? extends B> factory, BlockBehaviour.Properties props) {
        DeferredBlock<B> block = McRegistration.block(BLOCKS, name, factory, props);
        item(BLOCK_ITEMS.registerSimpleBlockItem(block));
        return block;
    }

    private static <I extends Item> DeferredItem<I> item(DeferredItem<I> item) {
        CREATIVE_ORDER.add(item);
        return item;
    }
}
