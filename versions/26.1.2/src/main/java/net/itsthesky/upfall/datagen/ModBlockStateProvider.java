package net.itsthesky.upfall.datagen;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.block.HaloPortalBlock;
import net.itsthesky.upfall.block.SkyCacheBlock;
import net.itsthesky.upfall.block.SkygrassBlock;
import net.itsthesky.upfall.client.UpfallClient;
import net.itsthesky.upfall.content.UpfallModules;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModFluids;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ModBlockStateProvider {
    private static final TextureSlot PORTAL = TextureSlot.create("portal");
    private static final TextureSlot OVERLAY = TextureSlot.create("overlay");

    private final BlockModelGenerators blocks;
    private final ItemModelGenerators items;

    public ModBlockStateProvider(BlockModelGenerators blocks, ItemModelGenerators items) {
        this.blocks = blocks;
        this.items = items;
    }

    public BlockModelGenerators blockModels() {
        return blocks;
    }

    public ItemModelGenerators itemModels() {
        return items;
    }

    public BiConsumer<Identifier, ModelInstance> modelOutput() {
        return blocks.modelOutput;
    }

    public Consumer<BlockModelDefinitionGenerator> blockStateOutput() {
        return blocks.blockStateOutput;
    }

    public void register() {
        stone(ModBlocks.PEARLSTONE, ModBlocks.PEARLSTONE_VARIANTS);
        stone(ModBlocks.COBBLED_PEARLSTONE, ModBlocks.COBBLED_PEARLSTONE_VARIANTS);
        stone(ModBlocks.POLISHED_PEARLSTONE, ModBlocks.POLISHED_PEARLSTONE_VARIANTS);
        stone(ModBlocks.PEARLSTONE_BRICKS, ModBlocks.PEARLSTONE_BRICK_VARIANTS);
        stone(ModBlocks.MOSSY_PEARLSTONE_BRICKS, ModBlocks.MOSSY_PEARLSTONE_BRICK_VARIANTS);
        stone(ModBlocks.DUSKSLATE, ModBlocks.DUSKSLATE_VARIANTS);
        stone(ModBlocks.COBBLED_DUSKSLATE, ModBlocks.COBBLED_DUSKSLATE_VARIANTS);
        stone(ModBlocks.POLISHED_DUSKSLATE, ModBlocks.POLISHED_DUSKSLATE_VARIANTS);
        stone(ModBlocks.DUSKSLATE_BRICKS, ModBlocks.DUSKSLATE_BRICK_VARIANTS);

        for (DeferredBlock<?> block : new DeferredBlock<?>[]{ModBlocks.CRACKED_PEARLSTONE_BRICKS, ModBlocks.CHISELED_PEARLSTONE_BRICKS, ModBlocks.GILDED_DUSKSLATE, ModBlocks.SKYSOIL, ModBlocks.DENSE_CLOUD,
                ModBlocks.NIMBUSITE_ORE, ModBlocks.DUSKSLATE_NIMBUSITE_ORE, ModBlocks.LEVITITE_ORE,
                ModBlocks.RAW_NIMBUSITE_BLOCK, ModBlocks.NIMBUSITE_BLOCK, ModBlocks.LEVITITE_BLOCK, ModBlocks.HALO_FRAME}) {
            blocks.createTrivialCube(block.get());
        }

        simpleBlock(ModBlocks.MISTGLOW_CRYSTAL.get(), glowingCube("mistglow_crystal", "solid"));
        skygrass();
        Identifier cacheClosed = cubeBottomTop("sky_cache", "sky_cache_side", "sky_cache_bottom", "sky_cache_top");
        Identifier cacheOpen = cubeBottomTop("sky_cache_open", "sky_cache_side", "sky_cache_bottom", "sky_cache_top_open");
        blockStateOutput().accept(MultiVariantGenerator.dispatch(ModBlocks.SKY_CACHE.get()).with(PropertyDispatch.initial(SkyCacheBlock.OPEN)
                .select(true, BlockModelGenerators.plainVariant(cacheOpen))
                .select(false, BlockModelGenerators.plainVariant(cacheClosed))));
        translucentCube(ModBlocks.MIST, true);
        simpleBlock(ModBlocks.UPDRAFT_MIST.get(), glowingCube("updraft_mist", "translucent"));
        simpleBlock(ModBlocks.UPFALL_CASCADE.get(), glowingCube("upfall_cascade", "translucent"));

        simpleBlock(ModBlocks.UPDRAFT_VENT.get(), cubeBottomTop(name(ModBlocks.UPDRAFT_VENT.get()), "updraft_vent_side", "pearlstone", "updraft_vent_top"));

        wood(ModBlocks.SKYREACH, false);
        wood(ModBlocks.GILDROOT, true);
        wood(ModBlocks.IRONBARK, false);

        crossPlant(ModBlocks.DUSKBLOOM.get(), "duskbloom");
        crossPlant(ModBlocks.CLOUD_FERN.get(), "cloud_fern");
        crossPlant(ModBlocks.GILDED_TUFT.get(), "gilded_tuft");
        crossPlant(ModBlocks.GLOWVINE.get(), "glowvine");
        simpleBlock(ModBlocks.GLOWVINE_PLANT.get(), ModelTemplates.CROSS.create(modLoc("block/glowvine_plant"),
                TextureMapping.cross(blockMaterial("glowvine_plant")), modelOutput()));

        pottedPlant(ModBlocks.POTTED_DUSKBLOOM.get(), "duskbloom");
        pottedPlant(ModBlocks.POTTED_CLOUD_FERN.get(), "cloud_fern");
        pottedPlant(ModBlocks.POTTED_SKYREACH_SAPLING.get(), "skyreach_sapling");
        pottedPlant(ModBlocks.POTTED_IRONBARK_SAPLING.get(), "ironbark_sapling");

        Material portal = blockMaterial("halo_portal").withForceTranslucent(true);
        TextureMapping portalTextures = new TextureMapping().put(PORTAL, portal).put(TextureSlot.PARTICLE, portal);
        Identifier portalNs = portalTemplate("block/nether_portal_ns").create(modLoc("block/halo_portal_ns"), portalTextures, modelOutput());
        Identifier portalEw = portalTemplate("block/nether_portal_ew").create(modLoc("block/halo_portal_ew"), portalTextures, modelOutput());
        blockStateOutput().accept(MultiVariantGenerator.dispatch(ModBlocks.HALO_PORTAL.get()).with(PropertyDispatch.initial(HaloPortalBlock.AXIS)
                .select(Direction.Axis.X, BlockModelGenerators.plainVariant(portalNs))
                .select(Direction.Axis.Z, BlockModelGenerators.plainVariant(portalEw))));

        simpleBlock(ModFluids.SKYWATER_BLOCK.get(), ModelTemplates.PARTICLE_ONLY.create(modLoc("block/skywater"),
                new TextureMapping().put(TextureSlot.PARTICLE, blockMaterial("skywater_still")), modelOutput()));
        UpfallModules.forEachData(data -> data.blockStates(this));
    }

    public static String name(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).getPath();
    }

    public Identifier modLoc(String path) {
        return Upfall.id(path);
    }

    public Identifier mcLoc(String path) {
        return Identifier.withDefaultNamespace(path);
    }

    public Identifier blockTexture(String name) {
        return modLoc("block/" + name);
    }

    public Material blockMaterial(String name) {
        return new Material(blockTexture(name));
    }

    public void simpleBlock(Block block, Identifier model) {
        blockStateOutput().accept(BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(model)));
    }

    public void simpleBlockWithItem(Block block, Identifier model) {
        simpleBlock(block, model);
        blocks.registerSimpleItemModel(block, model);
    }

    public void horizontalBlock(Block block, Identifier model) {
        blockStateOutput().accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model)).with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
    }

    public Identifier cubeAll(Block block) {
        return TexturedModel.CUBE.create(block, modelOutput());
    }

    public void blockItem(Block block) {
        blocks.registerSimpleItemModel(block, ModelLocationUtils.getModelLocation(block));
    }

    public Identifier cubeBottomTop(String model, String side, String bottom, String top) {
        return ModelTemplates.CUBE_BOTTOM_TOP.create(modLoc("block/" + model), new TextureMapping()
                .put(TextureSlot.SIDE, blockMaterial(side)).put(TextureSlot.BOTTOM, blockMaterial(bottom)).put(TextureSlot.TOP, blockMaterial(top)), modelOutput());
    }

    public void stone(DeferredBlock<Block> base, ModBlocks.StoneVariants variants) {
        blocks.family(base.get()).stairs(variants.stairs().get()).slab(variants.slab().get()).wall(variants.wall().get());
    }

    private void skygrass() {
        Identifier model = new ModelTemplate(Optional.of(mcLoc("block/grass_block")), Optional.empty(),
                TextureSlot.PARTICLE, TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE, OVERLAY)
                .create(modLoc("block/skygrass_block"), new TextureMapping()
                        .put(TextureSlot.PARTICLE, blockMaterial("skysoil"))
                        .put(TextureSlot.BOTTOM, blockMaterial("skysoil"))
                        .put(TextureSlot.TOP, blockMaterial("skygrass_block_top"))
                        .put(TextureSlot.SIDE, blockMaterial("skygrass_block_side"))
                        .put(OVERLAY, blockMaterial("skygrass_block_side_overlay")), modelOutput());
        MultiVariant plain = BlockModelGenerators.plainVariant(model);
        blockStateOutput().accept(MultiVariantGenerator.dispatch(ModBlocks.SKYGRASS_BLOCK.get()).with(PropertyDispatch.initial(SkygrassBlock.INVERTED)
                .select(true, plain.with(BlockModelGenerators.X_ROT_180))
                .select(false, BlockModelGenerators.createRotatedVariants(BlockModelGenerators.plainModel(model)))));
        blocks.registerSimpleTintedItemModel(ModBlocks.SKYGRASS_BLOCK.get(), model, ItemModelUtils.constantTint(UpfallClient.SKYGRASS_ITEM_COLOR));
    }

    public Identifier glowingCube(String name, String renderType) {
        Material texture = blockMaterial(name);
        if (renderType.equals("translucent")) {
            texture = texture.withForceTranslucent(true);
        }
        ModelTemplate template = ExtendedModelTemplateBuilder.builder()
                .parent(mcLoc("block/block"))
                .requiredTextureSlot(TextureSlot.ALL)
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .element(element -> element.from(0, 0, 0).to(16, 16, 16).shade(false).ambientOcclusion(false).lightEmission(15).cube(TextureSlot.ALL))
                .build();
        return template.create(modLoc("block/" + name), new TextureMapping().put(TextureSlot.ALL, texture).put(TextureSlot.PARTICLE, texture), modelOutput());
    }

    public void translucentCube(DeferredBlock<?> block, boolean withItem) {
        Identifier model = ModelTemplates.CUBE_ALL.create(modLoc("block/" + name(block.get())),
                TextureMapping.cube(blockMaterial(name(block.get())).withForceTranslucent(true)), modelOutput());
        simpleBlock(block.get(), model);
        if (withItem) {
            blocks.registerSimpleItemModel(block.get(), model);
        }
    }

    private void wood(ModBlocks.WoodSet set, boolean hangingSapling) {
        String w = set.name();
        blocks.woodProvider(set.log().get()).logWithHorizontal(set.log().get());
        blocks.woodProvider(set.strippedLog().get()).logWithHorizontal(set.strippedLog().get());
        woodWithHorizontal(set.wood().get(), w + "_log");
        woodWithHorizontal(set.strippedWood().get(), "stripped_" + w + "_log");
        blocks.family(set.planks().get())
                .stairs(set.stairs().get())
                .slab(set.slab().get())
                .fence(set.fence().get())
                .fenceGate(set.fenceGate().get())
                .button(set.button().get())
                .pressurePlate(set.pressurePlate().get())
                .door(set.door().get())
                .trapdoor(set.trapdoor().get());
        blocks.createTrivialCube(set.leaves().get());

        Block sapling = set.sapling().get();
        if (hangingSapling) {
            Identifier model = ModelTemplates.CROSS.create(sapling, TextureMapping.cross(blockMaterial(w + "_sapling")), modelOutput());
            blockStateOutput().accept(BlockModelGenerators.createSimpleBlock(sapling,
                    BlockModelGenerators.plainVariant(model).with(BlockModelGenerators.X_ROT_180)));
        } else {
            blocks.createCrossBlock(sapling, BlockModelGenerators.PlantType.NOT_TINTED, TextureMapping.cross(blockMaterial(w + "_sapling")));
        }
        blocks.registerSimpleFlatItemModel(sapling);
    }

    private void woodWithHorizontal(Block wood, String texture) {
        TextureMapping mapping = TextureMapping.column(blockMaterial(texture), blockMaterial(texture));
        Identifier model = ModelTemplates.CUBE_COLUMN.create(wood, mapping, modelOutput());
        blockStateOutput().accept(BlockModelGenerators.createRotatedPillarWithHorizontalVariant(wood, BlockModelGenerators.plainVariant(model),
                BlockModelGenerators.plainVariant(ModelTemplates.CUBE_COLUMN_HORIZONTAL.create(wood, mapping, modelOutput()))));
        blocks.registerSimpleItemModel(wood, model);
    }

    public void crossPlant(Block block, String texture) {
        Material material = blockMaterial(texture);
        blocks.createCrossBlock(block, BlockModelGenerators.PlantType.NOT_TINTED, TextureMapping.cross(material));
        blocks.registerSimpleItemModel(block.asItem(), ModelTemplates.FLAT_ITEM.create(block.asItem(), TextureMapping.layer0(material), modelOutput()));
    }

    public void pottedPlant(Block pot, String texture) {
        simpleBlock(pot, ModelTemplates.FLOWER_POT_CROSS.create(pot, TextureMapping.plant(blockMaterial(texture)), modelOutput()));
    }

    private static ModelTemplate portalTemplate(String parent) {
        return new ModelTemplate(Optional.of(Identifier.withDefaultNamespace(parent)), Optional.empty(), PORTAL, TextureSlot.PARTICLE);
    }
}
