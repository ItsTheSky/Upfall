package net.itsthesky.upfall.content.engine;

import net.itsthesky.upfall.content.engine.block.DrainRegulatorBlock;
import net.itsthesky.upfall.content.engine.block.DrainValveBlock;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;

final class EngineModels {
    private EngineModels() {}

    static void blockStates(ModBlockStateProvider states) {
        valve(states);
        regulator(states);
    }

    private static void valve(ModBlockStateProvider states) {
        ModelFile closed = valveModel(states, "drain_valve", 0);
        ModelFile open = valveModel(states, "drain_valve_open", 45);
        states.getVariantBuilder(EngineBlocks.DRAIN_VALVE.get()).forAllStates(state -> {
            Direction facing = state.getValue(DrainValveBlock.FACING);
            int x = facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90;
            int y = facing.getAxis().isHorizontal() ? ((int) facing.toYRot() + 180) % 360 : 0;
            return ConfiguredModel.builder().modelFile(state.getValue(DrainValveBlock.OPEN) ? open : closed).rotationX(x).rotationY(y).build();
        });
        states.blockItem(EngineBlocks.DRAIN_VALVE.get());
    }

    private static ModelFile valveModel(ModBlockStateProvider states, String name, float angle) {
        BlockModelBuilder model = states.models().withExistingParent(name, states.mcLoc("block/block"))
                .texture("wheel", states.blockTexture("drain_valve")).texture("stem", states.blockTexture("rusty_pipe_side"))
                .texture("particle", states.blockTexture("drain_valve")).renderType("cutout");
        model.element().from(6, 0, 6).to(10, 6, 10).allFaces((dir, face) -> face.texture("#stem")
                .uvs(6, dir.getAxis() == Direction.Axis.Y ? 6 : 0, 10, dir.getAxis() == Direction.Axis.Y ? 10 : 6)).end();
        var wheel = model.element().from(2, 6, 2).to(14, 8, 14);
        if (angle != 0) {
            wheel.rotation().angle(angle).axis(Direction.Axis.Y).origin(8, 7, 8).end();
        }
        wheel.allFaces((dir, face) -> {
                    face.texture("#wheel");
                    if (dir.getAxis() == Direction.Axis.Y) {
                        face.uvs(2, 2, 14, 14);
                    } else {
                        face.uvs(2, 2, 14, 4);
                    }
                }).end();
        return model;
    }

    private static void regulator(ModBlockStateProvider states) {
        ModelFile sealed = states.models().orientable("drain_regulator", states.blockTexture("rusty_plating"),
                states.blockTexture("drain_regulator_front"), states.blockTexture("rusty_plating"));
        ModelFile drained = states.models().orientable("drain_regulator_drained", states.blockTexture("rusty_plating"),
                states.blockTexture("drain_regulator_front_drained"), states.blockTexture("rusty_plating"));
        states.getVariantBuilder(EngineBlocks.DRAIN_REGULATOR.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(DrainRegulatorBlock.DRAINED) ? drained : sealed)
                .rotationY(((int) state.getValue(DrainRegulatorBlock.FACING).toYRot() + 180) % 360).build());
    }
}
