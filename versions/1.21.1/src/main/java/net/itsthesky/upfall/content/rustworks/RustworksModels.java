package net.itsthesky.upfall.content.rustworks;

import net.itsthesky.upfall.content.rustworks.block.ExhaustVentBlock;
import net.itsthesky.upfall.content.rustworks.block.KairiteOreBlock;
import net.itsthesky.upfall.content.rustworks.block.PipeValveBlock;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;

final class RustworksModels {
    private RustworksModels() {}

    static void blockStates(ModBlockStateProvider states) {
        ModelFile turf = states.models().cubeBottomTop("oxide_turf", states.blockTexture("oxide_turf_side"),
                states.blockTexture("rustsoil"), states.blockTexture("oxide_turf_top"));
        states.simpleBlock(RustworksBlocks.OXIDE_TURF.get(), ConfiguredModel.allYRotations(turf, 0, false));
        states.blockItem(RustworksBlocks.OXIDE_TURF.get());

        kairiteOre(states);

        pipe(states, RustworksBlocks.RUSTY_PIPE.get(), "rusty_pipe_side");
        pipe(states, RustworksBlocks.LEAKING_PIPE.get(), "leaking_pipe_side");
        valve(states);

        states.axisBlock(RustworksBlocks.RUSTY_COG.get(), states.blockTexture("rusty_cog_side"), states.blockTexture("rusty_cog_end"));
        states.blockItem(RustworksBlocks.RUSTY_COG.get());

        ResourceLocation content = states.blockTexture("stillflow_still");
        ModelFile[] levels = {
                states.models().withExistingParent("stillflow_cauldron_level1", states.mcLoc("block/template_cauldron_level1")).texture("content", content),
                states.models().withExistingParent("stillflow_cauldron_level2", states.mcLoc("block/template_cauldron_level2")).texture("content", content),
                states.models().withExistingParent("stillflow_cauldron_full", states.mcLoc("block/template_cauldron_full")).texture("content", content)};
        states.getVariantBuilder(RustworksBlocks.STILLFLOW_CAULDRON.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(levels[state.getValue(LayeredCauldronBlock.LEVEL) - 1]).build());
    }

    static void flora(ModBlockStateProvider states) {
        Block carpet = RustworksBlocks.VERDIGRIS_MOSS.get();
        states.simpleBlock(carpet, states.models().carpet("verdigris_moss", states.blockTexture("verdigris_moss_block")));
        states.blockItem(carpet);

        ModelFile bottom = states.models().cross("tall_rivetgrass_bottom", states.blockTexture("tall_rivetgrass_bottom")).renderType("cutout");
        ModelFile top = states.models().cross("tall_rivetgrass_top", states.blockTexture("tall_rivetgrass_top")).renderType("cutout");
        states.getVariantBuilder(RustworksBlocks.TALL_RIVETGRASS.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER ? bottom : top).build());

        states.simpleBlock(RustworksBlocks.WIREVINE_PLANT.get(),
                states.models().cross("wirevine_plant", states.blockTexture("wirevine_plant")).renderType("cutout"));
    }

    static void vent(ModBlockStateProvider states) {
        ModelFile idle = states.models().cubeBottomTop("exhaust_vent", states.blockTexture("exhaust_vent_side"),
                states.blockTexture("rusty_plating"), states.blockTexture("exhaust_vent_top"));
        ModelFile active = states.models().cubeBottomTop("exhaust_vent_active", states.blockTexture("exhaust_vent_side_active"),
                states.blockTexture("rusty_plating"), states.blockTexture("exhaust_vent_top_active"));
        states.getVariantBuilder(RustworksBlocks.EXHAUST_VENT.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(ExhaustVentBlock.ACTIVE) ? active : idle).build());
        states.blockItem(RustworksBlocks.EXHAUST_VENT.get());
    }

    private static void kairiteOre(ModBlockStateProvider states) {
        Block ore = RustworksBlocks.KAIRITE_ORE.get();
        String[] names = {"kairite_ore", "kairite_ore_warm", "kairite_ore_peak"};
        ModelFile[] models = new ModelFile[3];
        for (int i = 0; i < 3; i++) {
            models[i] = states.models().cubeAll(names[i], states.blockTexture(names[i]));
        }
        states.getVariantBuilder(ore).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(models[state.getValue(KairiteOreBlock.GLOW)]).build());
        states.blockItem(ore);
    }

    private static void pipe(ModBlockStateProvider states, RotatedPillarBlock block, String side) {
        String name = ModBlockStateProvider.name(block);
        BlockModelBuilder model = states.models().withExistingParent(name, states.mcLoc("block/block"))
                .texture("side", states.blockTexture(side)).texture("end", states.blockTexture("rusty_pipe_end"))
                .texture("particle", states.blockTexture(side)).renderType("cutout");
        model.element().from(2, 0, 2).to(14, 16, 14).allFaces((dir, face) -> {
            if (dir.getAxis() == Direction.Axis.Y) {
                face.texture("#end").uvs(2, 2, 14, 14).cullface(dir);
            } else {
                face.texture("#side").uvs(2, 0, 14, 16);
            }
        }).end();
        states.getVariantBuilder(block).forAllStates(state -> switch (state.getValue(RotatedPillarBlock.AXIS)) {
            case Y -> ConfiguredModel.builder().modelFile(model).build();
            case Z -> ConfiguredModel.builder().modelFile(model).rotationX(90).build();
            case X -> ConfiguredModel.builder().modelFile(model).rotationX(90).rotationY(90).build();
        });
        states.blockItem(block);
    }

    private static void valve(ModBlockStateProvider states) {
        BlockModelBuilder model = states.models().withExistingParent("pipe_valve", states.mcLoc("block/block"))
                .texture("wheel", states.blockTexture("pipe_valve")).texture("stem", states.blockTexture("rusty_pipe_side"))
                .texture("particle", states.blockTexture("pipe_valve")).renderType("cutout");
        model.element().from(6, 0, 6).to(10, 6, 10).allFaces((dir, face) -> face.texture("#stem")
                .uvs(6, dir.getAxis() == Direction.Axis.Y ? 6 : 0, 10, dir.getAxis() == Direction.Axis.Y ? 10 : 6)).end();
        model.element().from(2, 6, 2).to(14, 8, 14).allFaces((dir, face) -> {
            face.texture("#wheel");
            if (dir.getAxis() == Direction.Axis.Y) {
                face.uvs(2, 2, 14, 14);
            } else {
                face.uvs(2, 2, 14, 4);
            }
        }).end();
        states.getVariantBuilder(RustworksBlocks.PIPE_VALVE.get()).forAllStates(state -> {
            Direction facing = state.getValue(PipeValveBlock.FACING);
            int x = facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90;
            int y = facing.getAxis().isHorizontal() ? ((int) facing.toYRot() + 180) % 360 : 0;
            return ConfiguredModel.builder().modelFile(model).rotationX(x).rotationY(y).build();
        });
        states.blockItem(RustworksBlocks.PIPE_VALVE.get());
    }
}
