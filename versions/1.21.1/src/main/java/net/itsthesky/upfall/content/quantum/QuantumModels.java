package net.itsthesky.upfall.content.quantum;

import net.itsthesky.upfall.content.machines.ModelBoxes;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.itsthesky.upfall.datagen.ModItemModelProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;

final class QuantumModels {
    private QuantumModels() {}

    static void blockStates(ModBlockStateProvider states) {
        ModelFile gas = states.models().cubeAll("veil_gas", states.blockTexture("veil_gas")).renderType("translucent");
        states.simpleBlock(QuantumBlocks.VEIL_GAS.get(), gas);
        floorDeco(states, QuantumBlocks.ABANDONED_PICKAXE.get());
        floorDeco(states, QuantumBlocks.CRACKED_VEIL_MASK.get());
        floorDeco(states, QuantumBlocks.SCATTERED_BONES.get());
        ModelFile lantern = states.models().withExistingParent("spent_lantern", states.mcLoc("block/template_lantern"))
                .texture("lantern", states.blockTexture("spent_lantern")).renderType("cutout");
        ModelFile hanging = states.models().withExistingParent("spent_lantern_hanging", states.mcLoc("block/template_hanging_lantern"))
                .texture("lantern", states.blockTexture("spent_lantern")).renderType("cutout");
        states.getVariantBuilder(QuantumBlocks.SPENT_LANTERN.get()).forAllStatesExcept(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(net.minecraft.world.level.block.LanternBlock.HANGING) ? hanging : lantern).build(),
                net.minecraft.world.level.block.LanternBlock.WATERLOGGED);
        ModelFile cluster = states.models().cross("flicker_shard_cluster", states.blockTexture("flicker_shard_cluster")).renderType("cutout");
        states.directionalBlock(QuantumBlocks.FLICKER_SHARD_CLUSTER.get(), state -> cluster);
        states.itemModels().withExistingParent("flicker_shard_cluster", states.mcLoc("item/generated"))
                .texture("layer0", states.blockTexture("flicker_shard_cluster"));
        QuantumBlockModels.register(states);
    }

    private static void floorDeco(ModBlockStateProvider states, Block block) {
        String name = ModBlockStateProvider.name(block);
        var model = states.models().withExistingParent(name, states.mcLoc("block/block"))
                .texture("deco", states.blockTexture(name)).texture("particle", states.blockTexture(name)).renderType("cutout");
        ModelBoxes.apply(model, QuantumData.floorDecoBoxes());
        states.horizontalBlock(block, model);
        states.itemModels().withExistingParent(name, states.mcLoc("item/generated")).texture("layer0", states.blockTexture(name));
    }

    static void spentLanternItem(ModItemModelProvider models) {
        models.withExistingParent("spent_lantern", models.modLoc("block/spent_lantern"));
    }
}
