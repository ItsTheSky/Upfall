package net.itsthesky.upfall.datagen;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.UpfallModules;
import net.itsthesky.upfall.block.HaloPortalBlock;
import net.itsthesky.upfall.block.SkyCacheBlock;
import net.itsthesky.upfall.block.SkygrassBlock;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModFluids;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, Upfall.MODID, helper);
    }

    @Override
    protected void registerStatesAndModels() {
        stone(ModBlocks.PEARLSTONE, ModBlocks.PEARLSTONE_VARIANTS);
        stone(ModBlocks.COBBLED_PEARLSTONE, ModBlocks.COBBLED_PEARLSTONE_VARIANTS);
        stone(ModBlocks.POLISHED_PEARLSTONE, ModBlocks.POLISHED_PEARLSTONE_VARIANTS);
        stone(ModBlocks.PEARLSTONE_BRICKS, ModBlocks.PEARLSTONE_BRICK_VARIANTS);
        stone(ModBlocks.DUSKSLATE, ModBlocks.DUSKSLATE_VARIANTS);
        stone(ModBlocks.COBBLED_DUSKSLATE, ModBlocks.COBBLED_DUSKSLATE_VARIANTS);
        stone(ModBlocks.POLISHED_DUSKSLATE, ModBlocks.POLISHED_DUSKSLATE_VARIANTS);
        stone(ModBlocks.DUSKSLATE_BRICKS, ModBlocks.DUSKSLATE_BRICK_VARIANTS);

        for (DeferredBlock<?> block : new DeferredBlock<?>[]{ModBlocks.GILDED_DUSKSLATE, ModBlocks.SKYSOIL, ModBlocks.DENSE_CLOUD,
                ModBlocks.NIMBUSITE_ORE, ModBlocks.DUSKSLATE_NIMBUSITE_ORE, ModBlocks.LEVITITE_ORE,
                ModBlocks.RAW_NIMBUSITE_BLOCK, ModBlocks.NIMBUSITE_BLOCK, ModBlocks.LEVITITE_BLOCK, ModBlocks.HALO_FRAME}) {
            simpleBlockWithItem(block.get(), cubeAll(block.get()));
        }

        simpleBlockWithItem(ModBlocks.MISTGLOW_CRYSTAL.get(), glowingCube("mistglow_crystal", "solid"));
        skygrass();
        ModelFile cacheClosed = models().cubeBottomTop("sky_cache", blockTexture("sky_cache_side"), blockTexture("sky_cache_bottom"),
                blockTexture("sky_cache_top"));
        ModelFile cacheOpen = models().cubeBottomTop("sky_cache_open", blockTexture("sky_cache_side"), blockTexture("sky_cache_bottom"),
                blockTexture("sky_cache_top_open"));
        getVariantBuilder(ModBlocks.SKY_CACHE.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(SkyCacheBlock.OPEN) ? cacheOpen : cacheClosed).build());
        blockItem(ModBlocks.SKY_CACHE.get());
        translucentCube(ModBlocks.MIST, true);
        simpleBlock(ModBlocks.UPDRAFT_MIST.get(), glowingCube("updraft_mist", "translucent"));
        simpleBlock(ModBlocks.UPFALL_CASCADE.get(), glowingCube("upfall_cascade", "translucent"));

        ModelFile vent = models().cubeBottomTop(name(ModBlocks.UPDRAFT_VENT.get()), blockTexture("updraft_vent_side"),
                blockTexture("pearlstone"), blockTexture("updraft_vent_top"));
        simpleBlockWithItem(ModBlocks.UPDRAFT_VENT.get(), vent);

        wood(ModBlocks.SKYREACH, false);
        wood(ModBlocks.GILDROOT, true);

        crossPlant(ModBlocks.DUSKBLOOM.get(), "duskbloom");
        crossPlant(ModBlocks.CLOUD_FERN.get(), "cloud_fern");
        crossPlant(ModBlocks.GILDED_TUFT.get(), "gilded_tuft");
        crossPlant(ModBlocks.GLOWVINE.get(), "glowvine");
        simpleBlock(ModBlocks.GLOWVINE_PLANT.get(), models().cross("glowvine_plant", blockTexture("glowvine_plant")).renderType("cutout"));

        pottedPlant(ModBlocks.POTTED_DUSKBLOOM.get(), "duskbloom");
        pottedPlant(ModBlocks.POTTED_CLOUD_FERN.get(), "cloud_fern");
        pottedPlant(ModBlocks.POTTED_SKYREACH_SAPLING.get(), "skyreach_sapling");

        ModelFile portalNs = models().withExistingParent("halo_portal_ns", mcLoc("block/nether_portal_ns"))
                .texture("portal", blockTexture("halo_portal")).texture("particle", blockTexture("halo_portal")).renderType("translucent");
        ModelFile portalEw = models().withExistingParent("halo_portal_ew", mcLoc("block/nether_portal_ew"))
                .texture("portal", blockTexture("halo_portal")).texture("particle", blockTexture("halo_portal")).renderType("translucent");
        getVariantBuilder(ModBlocks.HALO_PORTAL.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(HaloPortalBlock.AXIS) == Direction.Axis.X ? portalNs : portalEw).build());

        simpleBlock(ModFluids.SKYWATER_BLOCK.get(), models().getBuilder("skywater").texture("particle", blockTexture("skywater_still")));
        UpfallModules.forEachData(data -> data.blockStates(this));
    }

    public static String name(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).getPath();
    }

    public ResourceLocation blockTexture(String name) {
        return modLoc("block/" + name);
    }

    public void blockItem(Block block) {
        itemModels().withExistingParent(name(block), modLoc("block/" + name(block)));
    }

    public void stone(DeferredBlock<Block> base, ModBlocks.StoneVariants variants) {
        ResourceLocation texture = blockTexture(name(base.get()));
        simpleBlockWithItem(base.get(), cubeAll(base.get()));
        stairsBlock(variants.stairs().get(), texture);
        slabBlock(variants.slab().get(), modLoc("block/" + name(base.get())), texture);
        wallBlock(variants.wall().get(), texture);
        blockItem(variants.stairs().get());
        blockItem(variants.slab().get());
        itemModels().wallInventory(name(variants.wall().get()), texture);
    }

    private void skygrass() {
        ModelFile model = models().withExistingParent("skygrass_block", mcLoc("block/grass_block"))
                .texture("particle", blockTexture("skysoil"))
                .texture("bottom", blockTexture("skysoil"))
                .texture("top", blockTexture("skygrass_block_top"))
                .texture("side", blockTexture("skygrass_block_side"))
                .texture("overlay", blockTexture("skygrass_block_side_overlay"))
                .renderType("cutout_mipped");
        getVariantBuilder(ModBlocks.SKYGRASS_BLOCK.get()).forAllStates(state -> {
            boolean inverted = state.getValue(SkygrassBlock.INVERTED);
            return inverted
                    ? ConfiguredModel.builder().modelFile(model).rotationX(180).build()
                    : ConfiguredModel.allYRotations(model, 0, false);
        });
        blockItem(ModBlocks.SKYGRASS_BLOCK.get());
    }

    public ModelFile glowingCube(String name, String renderType) {
        ResourceLocation texture = blockTexture(name);
        return models().withExistingParent(name, mcLoc("block/block"))
                .texture("all", texture).texture("particle", texture).renderType(renderType)
                .element().from(0, 0, 0).to(16, 16, 16).shade(false).ao(false).emissivity(15, 15)
                .allFaces((dir, face) -> face.texture("#all").cullface(dir)).end();
    }

    public void translucentCube(DeferredBlock<?> block, boolean withItem) {
        ModelFile model = models().cubeAll(name(block.get()), blockTexture(name(block.get()))).renderType("translucent");
        simpleBlock(block.get(), model);
        if (withItem) {
            blockItem(block.get());
        }
    }

    private void wood(ModBlocks.WoodSet set, boolean hangingSapling) {
        String w = set.name();
        ResourceLocation log = blockTexture(w + "_log");
        ResourceLocation logTop = blockTexture(w + "_log_top");
        ResourceLocation stripped = blockTexture("stripped_" + w + "_log");
        ResourceLocation strippedTop = blockTexture("stripped_" + w + "_log_top");
        ResourceLocation planks = blockTexture(w + "_planks");

        axisBlock((RotatedPillarBlock) set.log().get(), log, logTop);
        axisBlock((RotatedPillarBlock) set.wood().get(), log, log);
        axisBlock(set.strippedLog().get(), stripped, strippedTop);
        axisBlock(set.strippedWood().get(), stripped, stripped);
        simpleBlock(set.planks().get(), models().cubeAll(w + "_planks", planks));
        stairsBlock(set.stairs().get(), planks);
        slabBlock(set.slab().get(), planks, planks);
        fenceBlock(set.fence().get(), planks);
        fenceGateBlock(set.fenceGate().get(), planks);
        doorBlockWithRenderType(set.door().get(), blockTexture(w + "_door_bottom"), blockTexture(w + "_door_top"), "cutout");
        trapdoorBlockWithRenderType(set.trapdoor().get(), blockTexture(w + "_trapdoor"), true, "cutout");
        buttonBlock(set.button().get(), planks);
        pressurePlateBlock(set.pressurePlate().get(), planks);
        simpleBlock(set.leaves().get(), models().cubeAll(w + "_leaves", blockTexture(w + "_leaves")).renderType("cutout_mipped"));

        ModelFile sapling = models().cross(w + "_sapling", blockTexture(w + "_sapling")).renderType("cutout");
        if (hangingSapling) {
            simpleBlock(set.sapling().get(), ConfiguredModel.builder().modelFile(sapling).rotationX(180).build());
        } else {
            simpleBlock(set.sapling().get(), sapling);
        }
        itemModels().withExistingParent(w + "_sapling", mcLoc("item/generated")).texture("layer0", blockTexture(w + "_sapling"));

        for (Block block : new Block[]{set.log().get(), set.wood().get(), set.strippedLog().get(), set.strippedWood().get(),
                set.planks().get(), set.stairs().get(), set.slab().get(), set.fenceGate().get(), set.pressurePlate().get(),
                set.leaves().get()}) {
            blockItem(block);
        }
        itemModels().fenceInventory(w + "_fence", planks);
        itemModels().buttonInventory(w + "_button", planks);
        itemModels().withExistingParent(w + "_trapdoor", modLoc("block/" + w + "_trapdoor_bottom"));
        itemModels().basicItem(set.door().get().asItem());
    }

    public void crossPlant(Block block, String texture) {
        simpleBlock(block, models().cross(name(block), blockTexture(texture)).renderType("cutout"));
        itemModels().withExistingParent(name(block), mcLoc("item/generated")).texture("layer0", blockTexture(texture));
    }

    public void pottedPlant(Block pot, String texture) {
        simpleBlock(pot, models().withExistingParent(name(pot), mcLoc("block/flower_pot_cross"))
                .texture("plant", blockTexture(texture)).renderType("cutout"));
    }
}
