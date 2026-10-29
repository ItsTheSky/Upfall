package net.itsthesky.upfall.content.quantum;

import net.itsthesky.upfall.content.machines.ModelBoxes;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.Map;

final class QuantumPedestalModels {
    private QuantumPedestalModels() {}

    static void register(ModBlockStateProvider states) {
        pedestal(states, QuantumBlocks.PEDESTAL.get(), "pedestal_top", "pedestal_side");
        pedestal(states, QuantumBlocks.QUANTUM_PEDESTAL.get(), "quantum_pedestal_top", "quantum_pedestal_side");
    }

    private static void pedestal(ModBlockStateProvider states, Block block, String top, String side) {
        String name = ModBlockStateProvider.name(block);
        Identifier model = ModelBoxes.create(states.modelOutput(), states.modLoc("block/" + name), Map.of(
                "top", states.blockMaterial(top), "side", states.blockMaterial(side),
                "bottom", states.blockMaterial("pedestal_bottom"), "particle", states.blockMaterial(side)), QuantumData.pedestalBoxes());
        states.simpleBlockWithItem(block, model);
    }
}
