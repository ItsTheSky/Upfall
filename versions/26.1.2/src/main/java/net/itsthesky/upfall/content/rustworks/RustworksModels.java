package net.itsthesky.upfall.content.rustworks;

import net.itsthesky.upfall.content.rustworks.block.ExhaustVentBlock;
import net.itsthesky.upfall.content.rustworks.block.KairiteOreBlock;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

final class RustworksModels {
    private static final TextureSlot WHEEL = TextureSlot.create("wheel");

    private RustworksModels() {}

    static void blockStates(ModBlockStateProvider states) {
        Identifier turf = states.cubeBottomTop("oxide_turf", "oxide_turf_side", "rustsoil", "oxide_turf_top");
        states.blockStateOutput().accept(BlockModelGenerators.createSimpleBlock(RustworksBlocks.OXIDE_TURF.get(),
                BlockModelGenerators.createRotatedVariants(BlockModelGenerators.plainModel(turf))));
        states.blockItem(RustworksBlocks.OXIDE_TURF.get());

        kairiteOre(states);

        pipe(states, RustworksBlocks.RUSTY_PIPE.get(), "rusty_pipe_side");
        pipe(states, RustworksBlocks.LEAKING_PIPE.get(), "leaking_pipe_side");
        valve(states);

        axisBlock(states, RustworksBlocks.RUSTY_COG.get(), "rusty_cog_side", "rusty_cog_end");
        states.blockItem(RustworksBlocks.RUSTY_COG.get());

        TextureMapping content = TextureMapping.cauldron(states.blockMaterial("stillflow_still"));
        Identifier level1 = ModelTemplates.CAULDRON_LEVEL1.create(states.modLoc("block/stillflow_cauldron_level1"), content, states.modelOutput());
        Identifier level2 = ModelTemplates.CAULDRON_LEVEL2.create(states.modLoc("block/stillflow_cauldron_level2"), content, states.modelOutput());
        Identifier full = ModelTemplates.CAULDRON_FULL.create(states.modLoc("block/stillflow_cauldron_full"), content, states.modelOutput());
        states.blockStateOutput().accept(MultiVariantGenerator.dispatch(RustworksBlocks.STILLFLOW_CAULDRON.get()).with(PropertyDispatch.initial(LayeredCauldronBlock.LEVEL)
                .select(1, BlockModelGenerators.plainVariant(level1))
                .select(2, BlockModelGenerators.plainVariant(level2))
                .select(3, BlockModelGenerators.plainVariant(full))));
    }

    static void flora(ModBlockStateProvider states) {
        Block carpet = RustworksBlocks.VERDIGRIS_MOSS.get();
        states.simpleBlock(carpet, ModelTemplates.CARPET.create(states.modLoc("block/verdigris_moss"),
                TextureMapping.wool(states.blockMaterial("verdigris_moss_block")), states.modelOutput()));
        states.blockItem(carpet);

        Identifier bottom = ModelTemplates.CROSS.create(states.modLoc("block/tall_rivetgrass_bottom"),
                TextureMapping.cross(states.blockMaterial("tall_rivetgrass_bottom")), states.modelOutput());
        Identifier top = ModelTemplates.CROSS.create(states.modLoc("block/tall_rivetgrass_top"),
                TextureMapping.cross(states.blockMaterial("tall_rivetgrass_top")), states.modelOutput());
        states.blockStateOutput().accept(MultiVariantGenerator.dispatch(RustworksBlocks.TALL_RIVETGRASS.get()).with(PropertyDispatch.initial(DoublePlantBlock.HALF)
                .select(DoubleBlockHalf.LOWER, BlockModelGenerators.plainVariant(bottom))
                .select(DoubleBlockHalf.UPPER, BlockModelGenerators.plainVariant(top))));

        states.simpleBlock(RustworksBlocks.WIREVINE_PLANT.get(), ModelTemplates.CROSS.create(states.modLoc("block/wirevine_plant"),
                TextureMapping.cross(states.blockMaterial("wirevine_plant")), states.modelOutput()));
    }

    static void vent(ModBlockStateProvider states) {
        Identifier idle = states.cubeBottomTop("exhaust_vent", "exhaust_vent_side", "rusty_plating", "exhaust_vent_top");
        Identifier active = states.cubeBottomTop("exhaust_vent_active", "exhaust_vent_side_active", "rusty_plating", "exhaust_vent_top_active");
        states.blockStateOutput().accept(MultiVariantGenerator.dispatch(RustworksBlocks.EXHAUST_VENT.get()).with(PropertyDispatch.initial(ExhaustVentBlock.ACTIVE)
                .select(true, BlockModelGenerators.plainVariant(active))
                .select(false, BlockModelGenerators.plainVariant(idle))));
        states.blockItem(RustworksBlocks.EXHAUST_VENT.get());
    }

    private static void kairiteOre(ModBlockStateProvider states) {
        Block ore = RustworksBlocks.KAIRITE_ORE.get();
        String[] names = {"kairite_ore", "kairite_ore_warm", "kairite_ore_peak"};
        PropertyDispatch.C1<MultiVariant, Integer> glow = PropertyDispatch.initial(KairiteOreBlock.GLOW);
        for (int i = 0; i < 3; i++) {
            Identifier model = ModelTemplates.CUBE_ALL.create(states.modLoc("block/" + names[i]),
                    TextureMapping.cube(states.blockMaterial(names[i])), states.modelOutput());
            glow.select(i, BlockModelGenerators.plainVariant(model));
        }
        states.blockStateOutput().accept(MultiVariantGenerator.dispatch(ore).with(glow));
        states.blockItem(ore);
    }

    private static void pipe(ModBlockStateProvider states, RotatedPillarBlock block, String side) {
        ModelTemplate template = ExtendedModelTemplateBuilder.builder()
                .parent(states.mcLoc("block/block"))
                .requiredTextureSlot(TextureSlot.SIDE)
                .requiredTextureSlot(TextureSlot.END)
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .element(element -> element.from(2, 0, 2).to(14, 16, 14).allFaces((dir, face) -> {
                    if (dir.getAxis() == Direction.Axis.Y) {
                        face.texture(TextureSlot.END).uvs(2, 2, 14, 14).cullface(dir);
                    } else {
                        face.texture(TextureSlot.SIDE).uvs(2, 0, 14, 16);
                    }
                }))
                .build();
        Material sideMaterial = states.blockMaterial(side);
        Identifier model = template.create(states.modLoc("block/" + ModBlockStateProvider.name(block)), new TextureMapping()
                .put(TextureSlot.SIDE, sideMaterial)
                .put(TextureSlot.END, states.blockMaterial("rusty_pipe_end"))
                .put(TextureSlot.PARTICLE, sideMaterial), states.modelOutput());
        states.blockStateOutput().accept(BlockModelGenerators.createAxisAlignedPillarBlock(block, BlockModelGenerators.plainVariant(model)));
        states.blockItem(block);
    }

    private static void valve(ModBlockStateProvider states) {
        ModelTemplate template = ExtendedModelTemplateBuilder.builder()
                .parent(states.mcLoc("block/block"))
                .requiredTextureSlot(WHEEL)
                .requiredTextureSlot(TextureSlot.STEM)
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .element(element -> element.from(6, 0, 6).to(10, 6, 10).allFaces((dir, face) -> face.texture(TextureSlot.STEM)
                        .uvs(6, dir.getAxis() == Direction.Axis.Y ? 6 : 0, 10, dir.getAxis() == Direction.Axis.Y ? 10 : 6)))
                .element(element -> element.from(2, 6, 2).to(14, 8, 14).allFaces((dir, face) -> {
                    face.texture(WHEEL);
                    if (dir.getAxis() == Direction.Axis.Y) {
                        face.uvs(2, 2, 14, 14);
                    } else {
                        face.uvs(2, 2, 14, 4);
                    }
                }))
                .build();
        Material wheel = states.blockMaterial("pipe_valve");
        Identifier model = template.create(states.modLoc("block/pipe_valve"), new TextureMapping()
                .put(WHEEL, wheel)
                .put(TextureSlot.STEM, states.blockMaterial("rusty_pipe_side"))
                .put(TextureSlot.PARTICLE, wheel), states.modelOutput());
        states.blockStateOutput().accept(MultiVariantGenerator.dispatch(RustworksBlocks.PIPE_VALVE.get(), BlockModelGenerators.plainVariant(model))
                .with(BlockModelGenerators.ROTATIONS_COLUMN_WITH_FACING));
        states.blockItem(RustworksBlocks.PIPE_VALVE.get());
    }

    private static void axisBlock(ModBlockStateProvider states, Block block, String side, String end) {
        String name = ModBlockStateProvider.name(block);
        TextureMapping textures = TextureMapping.column(states.blockMaterial(side), states.blockMaterial(end));
        Identifier vertical = ModelTemplates.CUBE_COLUMN.create(states.modLoc("block/" + name), textures, states.modelOutput());
        Identifier horizontal = ModelTemplates.CUBE_COLUMN_HORIZONTAL.create(states.modLoc("block/" + name + "_horizontal"), textures, states.modelOutput());
        states.blockStateOutput().accept(BlockModelGenerators.createRotatedPillarWithHorizontalVariant(block,
                BlockModelGenerators.plainVariant(vertical), BlockModelGenerators.plainVariant(horizontal)));
    }
}
