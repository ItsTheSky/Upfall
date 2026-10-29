package net.itsthesky.upfall.content.engine;

import com.mojang.math.Quadrant;
import net.itsthesky.upfall.content.engine.block.DrainRegulatorBlock;
import net.itsthesky.upfall.content.engine.block.DrainValveBlock;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

final class EngineModels {
    private static final TextureSlot WHEEL = TextureSlot.create("wheel");
    private static final TextureSlot STEM = TextureSlot.create("stem");

    private EngineModels() {}

    static void blockStates(ModBlockStateProvider states) {
        valve(states);
        regulator(states);
    }

    private static void valve(ModBlockStateProvider states) {
        Identifier closed = valveModel(states, "drain_valve", 0);
        Identifier open = valveModel(states, "drain_valve_open", 45);
        states.blockStateOutput().accept(MultiVariantGenerator.dispatch(EngineBlocks.DRAIN_VALVE.get())
                .with(PropertyDispatch.initial(DrainValveBlock.FACING, DrainValveBlock.OPEN).generate((facing, isOpen) -> {
                    int x = facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90;
                    int y = facing.getAxis().isHorizontal() ? ((int) facing.toYRot() + 180) % 360 : 0;
                    return BlockModelGenerators.plainVariant(isOpen ? open : closed).with(rotation(x, y));
                })));
        states.blockModels().registerSimpleItemModel(EngineBlocks.DRAIN_VALVE.get(), closed);
    }

    private static Identifier valveModel(ModBlockStateProvider states, String name, float angle) {
        ModelTemplate template = ExtendedModelTemplateBuilder.builder()
                .parent(states.mcLoc("block/block"))
                .requiredTextureSlot(WHEEL)
                .requiredTextureSlot(STEM)
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .element(stem -> stem.from(6, 0, 6).to(10, 6, 10).allFaces((dir, face) -> face.texture(STEM)
                        .uvs(6, dir.getAxis() == Direction.Axis.Y ? 6 : 0, 10, dir.getAxis() == Direction.Axis.Y ? 10 : 6)))
                .element(wheel -> {
                    wheel.from(2, 6, 2).to(14, 8, 14);
                    if (angle != 0) {
                        wheel.rotation(rotation -> rotation.origin(8, 7, 8).singleAxis(Direction.Axis.Y, angle));
                    }
                    wheel.allFaces((dir, face) -> {
                        face.texture(WHEEL);
                        if (dir.getAxis() == Direction.Axis.Y) {
                            face.uvs(2, 2, 14, 14);
                        } else {
                            face.uvs(2, 2, 14, 4);
                        }
                    });
                })
                .build();
        return template.create(states.modLoc("block/" + name), new TextureMapping()
                .put(WHEEL, states.blockMaterial("drain_valve"))
                .put(STEM, states.blockMaterial("rusty_pipe_side"))
                .put(TextureSlot.PARTICLE, states.blockMaterial("drain_valve")), states.modelOutput());
    }

    private static void regulator(ModBlockStateProvider states) {
        Identifier sealed = regulatorModel(states, "drain_regulator", "drain_regulator_front");
        Identifier drained = regulatorModel(states, "drain_regulator_drained", "drain_regulator_front_drained");
        states.blockStateOutput().accept(MultiVariantGenerator.dispatch(EngineBlocks.DRAIN_REGULATOR.get())
                .with(PropertyDispatch.initial(DrainRegulatorBlock.DRAINED, DrainRegulatorBlock.FACING).generate((isDrained, facing) ->
                        BlockModelGenerators.plainVariant(isDrained ? drained : sealed)
                                .with(rotation(0, ((int) facing.toYRot() + 180) % 360)))));
    }

    private static Identifier regulatorModel(ModBlockStateProvider states, String name, String front) {
        return ModelTemplates.CUBE_ORIENTABLE.create(states.modLoc("block/" + name), new TextureMapping()
                .put(TextureSlot.SIDE, states.blockMaterial("rusty_plating"))
                .put(TextureSlot.FRONT, states.blockMaterial(front))
                .put(TextureSlot.TOP, states.blockMaterial("rusty_plating")), states.modelOutput());
    }

    private static VariantMutator rotation(int x, int y) {
        return VariantMutator.X_ROT.withValue(quadrant(x)).then(VariantMutator.Y_ROT.withValue(quadrant(y)));
    }

    private static Quadrant quadrant(int degrees) {
        return Quadrant.values()[Math.floorMod(degrees / 90, 4)];
    }
}
