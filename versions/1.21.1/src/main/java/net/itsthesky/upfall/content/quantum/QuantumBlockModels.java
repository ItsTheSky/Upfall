package net.itsthesky.upfall.content.quantum;

import net.itsthesky.upfall.content.quantum.vault.CrumblingFloorBlock;
import net.itsthesky.upfall.content.quantum.vault.FrostGlassBlock;
import net.itsthesky.upfall.content.quantum.vault.ShiftingChestBlock;
import net.itsthesky.upfall.content.quantum.vault.VaultLampBlock;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;

final class QuantumBlockModels {
    private QuantumBlockModels() {}

    static void register(ModBlockStateProvider states) {
        states.simpleBlockWithItem(QuantumBlocks.VAULT_BRICKS.get(), states.cubeAll(QuantumBlocks.VAULT_BRICKS.get()));
        states.simpleBlockWithItem(QuantumBlocks.VAULT_TILES.get(), states.cubeAll(QuantumBlocks.VAULT_TILES.get()));
        states.simpleBlockWithItem(QuantumBlocks.VAULT_GLASS.get(),
                states.models().cubeAll("vault_glass", states.blockTexture("vault_glass")).renderType("cutout"));
        states.simpleBlockWithItem(QuantumBlocks.VAULT_CORE.get(), states.models().cubeAll("vault_core", states.blockTexture("vault_bricks")));

        ModelFile clear = states.models().cubeAll("frost_glass", states.blockTexture("frost_glass")).renderType("cutout");
        ModelFile frosted = states.models().cubeAll("frost_glass_frosted", states.blockTexture("frost_glass_frosted")).renderType("cutout");
        states.getVariantBuilder(QuantumBlocks.FROST_GLASS.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(FrostGlassBlock.FROSTED) ? frosted : clear).build());
        states.blockItem(QuantumBlocks.FROST_GLASS.get());

        ModelFile lit = states.models().cubeAll("vault_lamp", states.blockTexture("vault_lamp"));
        ModelFile off = states.models().cubeAll("vault_lamp_off", states.blockTexture("vault_lamp_off"));
        states.getVariantBuilder(QuantumBlocks.VAULT_LAMP.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(VaultLampBlock.LIT) ? lit : off).build());
        states.blockItem(QuantumBlocks.VAULT_LAMP.get());

        ModelFile floor = states.models().cubeAll("crumbling_floor", states.blockTexture("crumbling_floor"));
        states.getVariantBuilder(QuantumBlocks.CRUMBLING_FLOOR.get()).forAllStates(state -> ConfiguredModel.builder().modelFile(floor).build());
        states.blockItem(QuantumBlocks.CRUMBLING_FLOOR.get());

        ModelFile chest = states.models().orientable("shifting_chest", states.blockTexture("shifting_chest_side"),
                states.blockTexture("shifting_chest_front"), states.blockTexture("shifting_chest_top"));
        states.horizontalBlock(QuantumBlocks.SHIFTING_CHEST.get(), chest);
        states.blockItem(QuantumBlocks.SHIFTING_CHEST.get());

        QuantumPedestalModels.register(states);
    }
}
