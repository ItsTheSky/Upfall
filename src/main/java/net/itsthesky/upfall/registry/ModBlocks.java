package net.itsthesky.upfall.registry;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.block.DenseCloudBlock;
import net.itsthesky.upfall.block.GlowvineBlock;
import net.itsthesky.upfall.block.HaloPortalBlock;
import net.itsthesky.upfall.block.GlowvinePlantBlock;
import net.itsthesky.upfall.block.HangingSaplingBlock;
import net.itsthesky.upfall.block.MistBlock;
import net.itsthesky.upfall.block.SkyCacheBlock;
import net.itsthesky.upfall.block.SkyPlantBlock;
import net.itsthesky.upfall.block.SkygrassBlock;
import net.itsthesky.upfall.block.UpdraftMistBlock;
import net.itsthesky.upfall.block.UpdraftVentBlock;
import net.itsthesky.upfall.block.UpfallCascadeBlock;
import net.itsthesky.upfall.block.UpfallLogBlock;
import net.itsthesky.upfall.worldgen.ModFeatureKeys;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ModBlocks {
    private ModBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Upfall.MODID);
    public static final DeferredRegister.Items BLOCK_ITEMS = DeferredRegister.createItems(Upfall.MODID);
    public static final List<DeferredItem<? extends Item>> CREATIVE_ORDER = new ArrayList<>();

    public static final BlockSetType SKYREACH_SET = BlockSetType.register(new BlockSetType("upfall:skyreach"));
    public static final BlockSetType GILDROOT_SET = BlockSetType.register(new BlockSetType("upfall:gildroot"));
    public static final WoodType SKYREACH_WOOD_TYPE = WoodType.register(new WoodType("upfall:skyreach", SKYREACH_SET));
    public static final WoodType GILDROOT_WOOD_TYPE = WoodType.register(new WoodType("upfall:gildroot", GILDROOT_SET));

    public static final DeferredBlock<Block> PEARLSTONE = block("pearlstone", Block::new, stone(MapColor.QUARTZ, 1.5F, SoundType.CALCITE));
    public static final StoneVariants PEARLSTONE_VARIANTS = variants("pearlstone", PEARLSTONE);
    public static final DeferredBlock<Block> COBBLED_PEARLSTONE = block("cobbled_pearlstone", Block::new, stone(MapColor.QUARTZ, 2.0F, SoundType.CALCITE));
    public static final StoneVariants COBBLED_PEARLSTONE_VARIANTS = variants("cobbled_pearlstone", COBBLED_PEARLSTONE);
    public static final DeferredBlock<Block> POLISHED_PEARLSTONE = block("polished_pearlstone", Block::new, stone(MapColor.QUARTZ, 1.5F, SoundType.CALCITE));
    public static final StoneVariants POLISHED_PEARLSTONE_VARIANTS = variants("polished_pearlstone", POLISHED_PEARLSTONE);
    public static final DeferredBlock<Block> PEARLSTONE_BRICKS = block("pearlstone_bricks", Block::new, stone(MapColor.QUARTZ, 1.5F, SoundType.CALCITE));
    public static final StoneVariants PEARLSTONE_BRICK_VARIANTS = variants("pearlstone_brick", PEARLSTONE_BRICKS);

    public static final DeferredBlock<Block> DUSKSLATE = block("duskslate", Block::new, stone(MapColor.COLOR_BLUE, 3.0F, SoundType.DEEPSLATE));
    public static final StoneVariants DUSKSLATE_VARIANTS = variants("duskslate", DUSKSLATE);
    public static final DeferredBlock<Block> COBBLED_DUSKSLATE = block("cobbled_duskslate", Block::new, stone(MapColor.COLOR_BLUE, 3.5F, SoundType.DEEPSLATE));
    public static final StoneVariants COBBLED_DUSKSLATE_VARIANTS = variants("cobbled_duskslate", COBBLED_DUSKSLATE);
    public static final DeferredBlock<Block> POLISHED_DUSKSLATE = block("polished_duskslate", Block::new, stone(MapColor.COLOR_BLUE, 3.5F, SoundType.POLISHED_DEEPSLATE));
    public static final StoneVariants POLISHED_DUSKSLATE_VARIANTS = variants("polished_duskslate", POLISHED_DUSKSLATE);
    public static final DeferredBlock<Block> DUSKSLATE_BRICKS = block("duskslate_bricks", Block::new, stone(MapColor.COLOR_BLUE, 3.5F, SoundType.DEEPSLATE_BRICKS));
    public static final StoneVariants DUSKSLATE_BRICK_VARIANTS = variants("duskslate_brick", DUSKSLATE_BRICKS);
    public static final DeferredBlock<Block> GILDED_DUSKSLATE = block("gilded_duskslate", Block::new, stone(MapColor.GOLD, 3.0F, SoundType.DEEPSLATE));

    public static final DeferredBlock<Block> SKYSOIL = block("skysoil", Block::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_LIGHT_GRAY).strength(0.5F).sound(SoundType.ROOTED_DIRT));
    public static final DeferredBlock<SkygrassBlock> SKYGRASS_BLOCK = block("skygrass_block", SkygrassBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_CYAN).randomTicks().strength(0.6F).sound(SoundType.GRASS));
    public static final DeferredBlock<DenseCloudBlock> DENSE_CLOUD = block("dense_cloud", DenseCloudBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.SNOW).strength(0.4F).sound(SoundType.WOOL).instrument(NoteBlockInstrument.FLUTE));
    public static final DeferredBlock<MistBlock> MIST = block("mist", MistBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.SNOW).strength(0.1F).sound(SoundType.WOOL).noOcclusion().noLootTable().replaceable()
            .isViewBlocking((s, l, p) -> false).isSuffocating((s, l, p) -> false).isRedstoneConductor((s, l, p) -> false)
            .isValidSpawn(Blocks::never).pushReaction(PushReaction.DESTROY));
    public static final DeferredBlock<UpdraftVentBlock> UPDRAFT_VENT = block("updraft_vent", UpdraftVentBlock::new,
            stone(MapColor.QUARTZ, 2.0F, SoundType.CALCITE).randomTicks().lightLevel(s -> 6));
    public static final DeferredBlock<UpdraftMistBlock> UPDRAFT_MIST = BLOCKS.registerBlock("updraft_mist", UpdraftMistBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.GOLD).noCollission().noOcclusion().noLootTable().replaceable().strength(-1.0F, 0.0F)
            .lightLevel(s -> 3).isViewBlocking((s, l, p) -> false).isSuffocating((s, l, p) -> false)
            .isValidSpawn(Blocks::never).pushReaction(PushReaction.DESTROY).sound(SoundType.WOOL));
    public static final DeferredBlock<UpfallCascadeBlock> UPFALL_CASCADE = BLOCKS.registerBlock("upfall_cascade", UpfallCascadeBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.WATER).noCollission().noOcclusion().noLootTable().replaceable().strength(-1.0F, 0.0F).lightLevel(s -> 5)
            .isViewBlocking((s, l, p) -> false).isSuffocating((s, l, p) -> false)
            .isValidSpawn(Blocks::never).pushReaction(PushReaction.DESTROY).sound(SoundType.WOOL));

    public static final WoodSet SKYREACH = wood("skyreach", SKYREACH_SET, SKYREACH_WOOD_TYPE, MapColor.COLOR_LIGHT_BLUE, MapColor.QUARTZ,
            props -> new SaplingBlock(new TreeGrower("upfall_skyreach", Optional.empty(), Optional.of(ModFeatureKeys.SKYREACH_TREE), Optional.empty()), props));
    public static final WoodSet GILDROOT = wood("gildroot", GILDROOT_SET, GILDROOT_WOOD_TYPE, MapColor.GOLD, MapColor.COLOR_YELLOW,
            props -> new HangingSaplingBlock(new TreeGrower("upfall_gildroot", Optional.empty(), Optional.of(ModFeatureKeys.GILDROOT_TREE), Optional.empty()), props));

    public static final DeferredBlock<FlowerBlock> DUSKBLOOM = block("duskbloom", props -> new FlowerBlock(MobEffects.SLOW_FALLING, 7.0F, props), plant(MapColor.COLOR_PINK));
    public static final DeferredBlock<SkyPlantBlock> CLOUD_FERN = block("cloud_fern", SkyPlantBlock::new, plant(MapColor.COLOR_CYAN).replaceable());
    public static final DeferredBlock<SkyPlantBlock> GILDED_TUFT = block("gilded_tuft", SkyPlantBlock::new, plant(MapColor.GOLD).replaceable());
    public static final DeferredBlock<GlowvineBlock> GLOWVINE = block("glowvine", GlowvineBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.GOLD).randomTicks().noCollission().instabreak().sound(SoundType.CAVE_VINES)
            .lightLevel(s -> 10).pushReaction(PushReaction.DESTROY));
    public static final DeferredBlock<GlowvinePlantBlock> GLOWVINE_PLANT = BLOCKS.registerBlock("glowvine_plant", GlowvinePlantBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.GOLD).noCollission().instabreak().sound(SoundType.CAVE_VINES)
            .lightLevel(s -> 7).pushReaction(PushReaction.DESTROY));
    public static final DeferredBlock<Block> MISTGLOW_CRYSTAL = block("mistglow_crystal", Block::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_LIGHT_BLUE).strength(0.6F).sound(SoundType.AMETHYST).lightLevel(s -> 15));

    public static final DeferredBlock<FlowerPotBlock> POTTED_DUSKBLOOM = pot("potted_duskbloom", DUSKBLOOM);
    public static final DeferredBlock<FlowerPotBlock> POTTED_CLOUD_FERN = pot("potted_cloud_fern", CLOUD_FERN);
    public static final DeferredBlock<FlowerPotBlock> POTTED_SKYREACH_SAPLING = pot("potted_skyreach_sapling", SKYREACH.sapling());

    public static final DeferredBlock<DropExperienceBlock> NIMBUSITE_ORE = block("nimbusite_ore",
            props -> new DropExperienceBlock(UniformInt.of(3, 7), props), stone(MapColor.QUARTZ, 6.0F, SoundType.CALCITE).explosionResistance(9.0F));
    public static final DeferredBlock<DropExperienceBlock> DUSKSLATE_NIMBUSITE_ORE = block("duskslate_nimbusite_ore",
            props -> new DropExperienceBlock(UniformInt.of(3, 7), props), stone(MapColor.COLOR_BLUE, 7.5F, SoundType.DEEPSLATE).explosionResistance(9.0F));
    public static final DeferredBlock<DropExperienceBlock> LEVITITE_ORE = block("levitite_ore",
            props -> new DropExperienceBlock(UniformInt.of(6, 12), props), stone(MapColor.COLOR_PURPLE, 25.0F, SoundType.ANCIENT_DEBRIS).explosionResistance(1200.0F));
    public static final DeferredBlock<Block> RAW_NIMBUSITE_BLOCK = block("raw_nimbusite_block", Block::new, stone(MapColor.COLOR_LIGHT_BLUE, 5.0F, SoundType.STONE).explosionResistance(6.0F));
    public static final DeferredBlock<Block> NIMBUSITE_BLOCK = block("nimbusite_block", Block::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_LIGHT_BLUE).requiresCorrectToolForDrops().strength(50.0F, 1200.0F).sound(SoundType.NETHERITE_BLOCK));
    public static final DeferredBlock<Block> LEVITITE_BLOCK = block("levitite_block", Block::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PURPLE).requiresCorrectToolForDrops().strength(55.0F, 1200.0F).sound(SoundType.NETHERITE_BLOCK).lightLevel(s -> 4));

    public static final DeferredBlock<Block> HALO_FRAME = block("halo_frame", Block::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.QUARTZ).requiresCorrectToolForDrops().strength(25.0F, 1200.0F).sound(SoundType.AMETHYST).lightLevel(s -> 3));

    public static final DeferredBlock<SkyCacheBlock> SKY_CACHE = block("sky_cache", SkyCacheBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.CHERRY_WOOD).ignitedByLava());

    public static final DeferredBlock<HaloPortalBlock> HALO_PORTAL = BLOCKS.registerBlock("halo_portal", HaloPortalBlock::new, BlockBehaviour.Properties.of()
            .noCollission().strength(-1.0F).sound(SoundType.GLASS).lightLevel(s -> 12).noLootTable()
            .pushReaction(PushReaction.BLOCK));

    public record StoneVariants(DeferredBlock<StairBlock> stairs, DeferredBlock<SlabBlock> slab, DeferredBlock<WallBlock> wall) {}

    public record WoodSet(String name, DeferredBlock<UpfallLogBlock> log, DeferredBlock<UpfallLogBlock> wood,
                          DeferredBlock<RotatedPillarBlock> strippedLog, DeferredBlock<RotatedPillarBlock> strippedWood,
                          DeferredBlock<Block> planks, DeferredBlock<StairBlock> stairs, DeferredBlock<SlabBlock> slab,
                          DeferredBlock<FenceBlock> fence, DeferredBlock<FenceGateBlock> fenceGate, DeferredBlock<DoorBlock> door,
                          DeferredBlock<TrapDoorBlock> trapdoor, DeferredBlock<ButtonBlock> button,
                          DeferredBlock<PressurePlateBlock> pressurePlate, DeferredBlock<LeavesBlock> leaves,
                          DeferredBlock<? extends SaplingBlock> sapling) {
        public List<DeferredBlock<? extends Block>> all() {
            return List.of(log, wood, strippedLog, strippedWood, planks, stairs, slab, fence, fenceGate, door, trapdoor,
                    button, pressurePlate, leaves, sapling);
        }
    }

    private static <B extends Block> DeferredBlock<B> block(String name, Function<BlockBehaviour.Properties, ? extends B> factory, BlockBehaviour.Properties props) {
        DeferredBlock<B> block = BLOCKS.registerBlock(name, factory, props);
        CREATIVE_ORDER.add(BLOCK_ITEMS.registerSimpleBlockItem(block));
        return block;
    }

    private static BlockBehaviour.Properties stone(MapColor color, float hardness, SoundType sound) {
        return BlockBehaviour.Properties.of().mapColor(color).instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops().strength(hardness, 6.0F).sound(sound);
    }

    private static BlockBehaviour.Properties plant(MapColor color) {
        return BlockBehaviour.Properties.of().mapColor(color).noCollission().instabreak().sound(SoundType.GRASS)
                .offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY);
    }

    private static StoneVariants variants(String prefix, DeferredBlock<Block> base) {
        Supplier<BlockBehaviour.Properties> props = () -> BlockBehaviour.Properties.ofFullCopy(base.get());
        DeferredBlock<StairBlock> stairs = block(prefix + "_stairs", p -> new StairBlock(base.get().defaultBlockState(), props.get()), BlockBehaviour.Properties.of());
        DeferredBlock<SlabBlock> slab = block(prefix + "_slab", p -> new SlabBlock(props.get()), BlockBehaviour.Properties.of());
        DeferredBlock<WallBlock> wall = block(prefix + "_wall", p -> new WallBlock(props.get().forceSolidOn()), BlockBehaviour.Properties.of());
        return new StoneVariants(stairs, slab, wall);
    }

    private static DeferredBlock<FlowerPotBlock> pot(String name, DeferredBlock<? extends Block> plant) {
        return BLOCKS.registerBlock(name, props -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, plant, props),
                BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY));
    }

    private static BlockBehaviour.Properties wooden(MapColor color) {
        return BlockBehaviour.Properties.of().mapColor(color).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F)
                .sound(SoundType.CHERRY_WOOD).ignitedByLava();
    }

    private static WoodSet wood(String name, BlockSetType setType, WoodType woodType, MapColor planksColor, MapColor barkColor,
                                Function<BlockBehaviour.Properties, ? extends SaplingBlock> saplingFactory) {
        DeferredBlock<RotatedPillarBlock> strippedLog = BLOCKS.registerBlock("stripped_" + name + "_log", RotatedPillarBlock::new, wooden(planksColor));
        DeferredBlock<RotatedPillarBlock> strippedWood = BLOCKS.registerBlock("stripped_" + name + "_wood", RotatedPillarBlock::new, wooden(planksColor));
        DeferredBlock<UpfallLogBlock> log = block(name + "_log", p -> new UpfallLogBlock(strippedLog, p), wooden(barkColor));
        DeferredBlock<UpfallLogBlock> wood = block(name + "_wood", p -> new UpfallLogBlock(strippedWood, p), wooden(barkColor));
        CREATIVE_ORDER.add(BLOCK_ITEMS.registerSimpleBlockItem(strippedLog));
        CREATIVE_ORDER.add(BLOCK_ITEMS.registerSimpleBlockItem(strippedWood));
        DeferredBlock<Block> planks = block(name + "_planks", Block::new, wooden(planksColor));
        DeferredBlock<StairBlock> stairs = block(name + "_stairs", p -> new StairBlock(planks.get().defaultBlockState(), p), wooden(planksColor));
        DeferredBlock<SlabBlock> slab = block(name + "_slab", SlabBlock::new, wooden(planksColor));
        DeferredBlock<FenceBlock> fence = block(name + "_fence", FenceBlock::new, wooden(planksColor).forceSolidOn());
        DeferredBlock<FenceGateBlock> fenceGate = block(name + "_fence_gate", p -> new FenceGateBlock(woodType, p), wooden(planksColor).forceSolidOn());
        DeferredBlock<DoorBlock> door = BLOCKS.registerBlock(name + "_door", p -> new DoorBlock(setType, p),
                wooden(planksColor).strength(3.0F).noOcclusion().pushReaction(PushReaction.DESTROY));
        CREATIVE_ORDER.add(BLOCK_ITEMS.registerItem(name + "_door", p -> new DoubleHighBlockItem(door.get(), p)));
        DeferredBlock<TrapDoorBlock> trapdoor = block(name + "_trapdoor", p -> new TrapDoorBlock(setType, p),
                wooden(planksColor).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never));
        DeferredBlock<ButtonBlock> button = block(name + "_button", p -> new ButtonBlock(setType, 30, p),
                BlockBehaviour.Properties.of().noCollission().strength(0.5F).pushReaction(PushReaction.DESTROY));
        DeferredBlock<PressurePlateBlock> plate = block(name + "_pressure_plate", p -> new PressurePlateBlock(setType, p),
                wooden(planksColor).forceSolidOn().noCollission().strength(0.5F).pushReaction(PushReaction.DESTROY));
        DeferredBlock<LeavesBlock> leaves = block(name + "_leaves", LeavesBlock::new, BlockBehaviour.Properties.of()
                .mapColor(barkColor == MapColor.GOLD ? MapColor.COLOR_YELLOW : MapColor.COLOR_CYAN)
                .strength(0.2F).randomTicks().sound(SoundType.AZALEA_LEAVES).noOcclusion()
                .isValidSpawn(Blocks::ocelotOrParrot).isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p) -> false)
                .ignitedByLava().pushReaction(PushReaction.DESTROY).isRedstoneConductor((s, l, p) -> false));
        DeferredBlock<? extends SaplingBlock> sapling = block(name + "_sapling", saplingFactory, BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY));
        return new WoodSet(name, log, wood, strippedLog, strippedWood, planks, stairs, slab, fence, fenceGate, door, trapdoor,
                button, plate, leaves, sapling);
    }
}
