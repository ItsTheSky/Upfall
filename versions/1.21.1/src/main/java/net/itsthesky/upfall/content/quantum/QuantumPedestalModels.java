package net.itsthesky.upfall.content.quantum;

import net.itsthesky.upfall.content.machines.ModelBoxes;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
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
        ModelBoxes.apply(model, QuantumData.pedestalBoxes());
        states.simpleBlockWithItem(block, model);
    }
}
