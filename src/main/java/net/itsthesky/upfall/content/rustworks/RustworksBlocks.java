package net.itsthesky.upfall.content.rustworks;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.compat.mc.McRegistration;
import net.itsthesky.upfall.content.rustworks.block.ExhaustVentBlock;
import net.itsthesky.upfall.content.rustworks.block.KairiteOreBlock;
import net.itsthesky.upfall.content.rustworks.block.LeakingPipeBlock;
import net.itsthesky.upfall.content.rustworks.block.OxideTurfBlock;
import net.itsthesky.upfall.content.rustworks.block.PipeValveBlock;
import net.itsthesky.upfall.content.rustworks.block.RivetgrassBlock;
import net.itsthesky.upfall.content.rustworks.block.RustyPipeBlock;
import net.itsthesky.upfall.content.rustworks.block.StillflowCauldronBlock;
import net.itsthesky.upfall.content.rustworks.block.TallRivetgrassBlock;
import net.itsthesky.upfall.content.rustworks.block.WirevineBlock;
import net.itsthesky.upfall.content.rustworks.block.WirevinePlantBlock;
import net.itsthesky.upfall.registry.ModBlocks;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
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
import java.util.function.Supplier;

public final class RustworksBlocks {
    private RustworksBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Upfall.MODID);
    public static final DeferredRegister.Items BLOCK_ITEMS = DeferredRegister.createItems(Upfall.MODID);
    static final List<DeferredItem<? extends Item>> CREATIVE_ORDER = new ArrayList<>();

    public static final DeferredBlock<OxideTurfBlock> OXIDE_TURF = block("oxide_turf", OxideTurfBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_RED).randomTicks().strength(0.6F).sound(SoundType.GRASS));
    public static final DeferredBlock<Block> RUSTSOIL = block("rustsoil", Block::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_PURPLE).strength(0.5F).sound(SoundType.ROOTED_DIRT));

    public static final DeferredBlock<Block> RUSTSLATE = block("rustslate", Block::new, stone(3.0F, SoundType.DEEPSLATE));
    public static final ModBlocks.StoneVariants RUSTSLATE_VARIANTS = variants("rustslate", RUSTSLATE);
    public static final DeferredBlock<Block> COBBLED_RUSTSLATE = block("cobbled_rustslate", Block::new, stone(3.5F, SoundType.DEEPSLATE));
    public static final ModBlocks.StoneVariants COBBLED_RUSTSLATE_VARIANTS = variants("cobbled_rustslate", COBBLED_RUSTSLATE);
    public static final DeferredBlock<Block> POLISHED_RUSTSLATE = block("polished_rustslate", Block::new, stone(3.5F, SoundType.POLISHED_DEEPSLATE));
    public static final ModBlocks.StoneVariants POLISHED_RUSTSLATE_VARIANTS = variants("polished_rustslate", POLISHED_RUSTSLATE);
    public static final DeferredBlock<Block> RUSTSLATE_BRICKS = block("rustslate_bricks", Block::new, stone(3.5F, SoundType.DEEPSLATE_BRICKS));
    public static final ModBlocks.StoneVariants RUSTSLATE_BRICK_VARIANTS = variants("rustslate_brick", RUSTSLATE_BRICKS);

    public static final DeferredBlock<KairiteOreBlock> KAIRITE_ORE = block("kairite_ore", KairiteOreBlock::new,
            stone(4.0F, SoundType.DEEPSLATE).mapColor(MapColor.WARPED_NYLIUM).lightLevel(KairiteOreBlock::light));

    public static final DeferredBlock<Block> RUSTY_PLATING = block("rusty_plating", Block::new, metal(MapColor.TERRACOTTA_ORANGE, 4.0F));
    public static final DeferredBlock<Block> COG_PLATING = block("cog_plating", Block::new, metal(MapColor.COLOR_GRAY, 4.0F));
    public static final DeferredBlock<RustyPipeBlock> RUSTY_PIPE = block("rusty_pipe", RustyPipeBlock::new,
            metal(MapColor.TERRACOTTA_ORANGE, 3.0F).noOcclusion());
    public static final DeferredBlock<LeakingPipeBlock> LEAKING_PIPE = block("leaking_pipe", LeakingPipeBlock::new,
            metal(MapColor.TERRACOTTA_ORANGE, 3.0F).noOcclusion());
    public static final DeferredBlock<PipeValveBlock> PIPE_VALVE = block("pipe_valve", PipeValveBlock::new,
            metal(MapColor.COLOR_RED, 3.0F).noOcclusion());
    public static final DeferredBlock<RotatedPillarBlock> RUSTY_COG = block("rusty_cog", RotatedPillarBlock::new,
            metal(MapColor.TERRACOTTA_ORANGE, 4.0F));
    public static final DeferredBlock<ExhaustVentBlock> EXHAUST_VENT = block("exhaust_vent", ExhaustVentBlock::new,
            metal(MapColor.TERRACOTTA_ORANGE, 3.5F).lightLevel(ExhaustVentBlock::light));
    public static final DeferredBlock<Block> HEARTGEAR = block("heartgear", Block::new,
            metal(MapColor.WARPED_NYLIUM, 25.0F).explosionResistance(1200.0F).lightLevel(s -> 4));

    public static final DeferredBlock<StillflowCauldronBlock> STILLFLOW_CAULDRON = McRegistration.block(BLOCKS, "stillflow_cauldron",
            StillflowCauldronBlock::new, BlockBehaviour.Properties.ofLegacyCopy(Blocks.CAULDRON).lightLevel(s -> 3));

    public static final DeferredBlock<Block> VERDIGRIS_MOSS_BLOCK = block("verdigris_moss_block", Block::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.WARPED_NYLIUM).strength(0.1F).sound(SoundType.MOSS).pushReaction(PushReaction.DESTROY));
    public static final DeferredBlock<CarpetBlock> VERDIGRIS_MOSS = block("verdigris_moss", CarpetBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.WARPED_NYLIUM).strength(0.1F).sound(SoundType.MOSS_CARPET).pushReaction(PushReaction.DESTROY));
    public static final DeferredBlock<RivetgrassBlock> RIVETGRASS = block("rivetgrass", RivetgrassBlock::new, groundCover().replaceable());
    public static final DeferredBlock<TallRivetgrassBlock> TALL_RIVETGRASS = McRegistration.block(BLOCKS, "tall_rivetgrass", TallRivetgrassBlock::new, groundCover().replaceable());
    public static final DeferredBlock<FlowerBlock> VERDIGRIS_BLOOM = block("verdigris_bloom", props -> new FlowerBlock(MobEffects.NIGHT_VISION, 5.0F, props),
            groundCover().lightLevel(s -> 4));
    public static final DeferredBlock<FlowerPotBlock> POTTED_VERDIGRIS_BLOOM = McRegistration.block(BLOCKS, "potted_verdigris_bloom",
            props -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, VERDIGRIS_BLOOM, props),
            BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY).lightLevel(s -> 4));
    public static final DeferredBlock<WirevineBlock> WIREVINE = block("wirevine", WirevineBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_ORANGE).randomTicks().noCollission().instabreak().sound(SoundType.CAVE_VINES)
            .lightLevel(s -> 6).pushReaction(PushReaction.DESTROY));
    public static final DeferredBlock<WirevinePlantBlock> WIREVINE_PLANT = McRegistration.block(BLOCKS, "wirevine_plant", WirevinePlantBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).noCollission().instabreak().sound(SoundType.CAVE_VINES)
                    .lightLevel(s -> 2).pushReaction(PushReaction.DESTROY));

    public static List<ModBlocks.StoneVariants> allVariants() {
        return List.of(RUSTSLATE_VARIANTS, COBBLED_RUSTSLATE_VARIANTS, POLISHED_RUSTSLATE_VARIANTS, RUSTSLATE_BRICK_VARIANTS);
    }

    private static BlockBehaviour.Properties groundCover() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).noCollission().instabreak()
                .sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties stone(float hardness, SoundType sound) {
        return BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BLACK).instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops().strength(hardness, 6.0F).sound(sound);
    }

    private static BlockBehaviour.Properties metal(MapColor color, float hardness) {
        return BlockBehaviour.Properties.of().mapColor(color).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                .requiresCorrectToolForDrops().strength(hardness, 6.0F).sound(SoundType.COPPER);
    }

    private static ModBlocks.StoneVariants variants(String prefix, DeferredBlock<Block> base) {
        Supplier<BlockBehaviour.Properties> props = () -> BlockBehaviour.Properties.ofFullCopy(base.get());
        DeferredBlock<StairBlock> stairs = block(prefix + "_stairs", p -> new StairBlock(base.get().defaultBlockState(), p), props);
        DeferredBlock<SlabBlock> slab = block(prefix + "_slab", SlabBlock::new, props);
        DeferredBlock<WallBlock> wall = block(prefix + "_wall", WallBlock::new, () -> props.get().forceSolidOn());
        return new ModBlocks.StoneVariants(stairs, slab, wall);
    }

    private static <B extends Block> DeferredBlock<B> block(String name, Function<BlockBehaviour.Properties, ? extends B> factory, BlockBehaviour.Properties props) {
        DeferredBlock<B> block = McRegistration.block(BLOCKS, name, factory, props);
        CREATIVE_ORDER.add(BLOCK_ITEMS.registerSimpleBlockItem(block));
        return block;
    }

    private static <B extends Block> DeferredBlock<B> block(String name, Function<BlockBehaviour.Properties, ? extends B> factory, Supplier<BlockBehaviour.Properties> props) {
        DeferredBlock<B> block = McRegistration.block(BLOCKS, name, factory, props);
        CREATIVE_ORDER.add(BLOCK_ITEMS.registerSimpleBlockItem(block));
        return block;
    }
}
