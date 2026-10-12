package net.itsthesky.upfall.content.quantum;

import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;

final class QuantumPedestalModels {
    private QuantumPedestalModels() {}

    static void register(ModBlockStateProvider states) {
        pedestal(states, QuantumBlocks.PEDESTAL.get(), "pedestal_top", "pedestal_side");
        pedestal(states, QuantumBlocks.QUANTUM_PEDESTAL.get(), "quantum_pedestal_top", "quantum_pedestal_side");
    }

    private static void pedestal(ModBlockStateProvider states, Block block, String top, String side) {
        String name = ModBlockStateProvider.name(block);
        BlockModelBuilder model = states.models().withExistingParent(name, states.mcLoc("block/block"))
                .texture("top", states.blockTexture(top)).texture("side", states.blockTexture(side))
                .texture("bottom", states.blockTexture("pedestal_bottom")).texture("particle", states.blockTexture(side));
        part(model, 0, 0, 16, 3, Direction.DOWN);
        part(model, 3, 3, 13, 13, null);
        part(model, 1, 13, 15, 16, Direction.UP);
        states.simpleBlockWithItem(block, model);
    }

    private static void part(BlockModelBuilder model, float min, float minY, float max, float maxY, Direction culled) {
        model.element().from(min, minY, min).to(max, maxY, max).allFaces((direction, face) -> {
            face.texture(direction == Direction.UP ? "#top" : direction == Direction.DOWN ? "#bottom" : "#side");
            if (direction == culled || direction.getAxis().isHorizontal() && min == 0) face.cullface(direction);
        }).end();
    }
}
