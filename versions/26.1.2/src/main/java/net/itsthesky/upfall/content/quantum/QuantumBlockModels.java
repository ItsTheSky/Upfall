package net.itsthesky.upfall.content.quantum;

import net.itsthesky.upfall.content.quantum.vault.FrostGlassBlock;
import net.itsthesky.upfall.content.quantum.vault.VaultLampBlock;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.resources.Identifier;

final class QuantumBlockModels {
    private QuantumBlockModels() {}

    static void register(ModBlockStateProvider states) {
        states.simpleBlockWithItem(QuantumBlocks.VAULT_BRICKS.get(), states.cubeAll(QuantumBlocks.VAULT_BRICKS.get()));
        states.simpleBlockWithItem(QuantumBlocks.VAULT_TILES.get(), states.cubeAll(QuantumBlocks.VAULT_TILES.get()));
        states.simpleBlockWithItem(QuantumBlocks.VAULT_GLASS.get(), cube(states, "vault_glass", "vault_glass"));
        states.simpleBlock(QuantumBlocks.VAULT_CORE.get(), cube(states, "vault_core", "vault_bricks"));

        Identifier clear = cube(states, "frost_glass", "frost_glass");
        Identifier frosted = cube(states, "frost_glass_frosted", "frost_glass_frosted");
        states.blockStateOutput().accept(MultiVariantGenerator.dispatch(QuantumBlocks.FROST_GLASS.get()).with(PropertyDispatch.initial(FrostGlassBlock.FROSTED)
                .select(true, BlockModelGenerators.plainVariant(frosted))
                .select(false, BlockModelGenerators.plainVariant(clear))));
        states.blockItem(QuantumBlocks.FROST_GLASS.get());

        Identifier lit = cube(states, "vault_lamp", "vault_lamp");
        Identifier off = cube(states, "vault_lamp_off", "vault_lamp_off");
        states.blockStateOutput().accept(MultiVariantGenerator.dispatch(QuantumBlocks.VAULT_LAMP.get()).with(PropertyDispatch.initial(VaultLampBlock.LIT)
                .select(true, BlockModelGenerators.plainVariant(lit))
                .select(false, BlockModelGenerators.plainVariant(off))));
        states.blockItem(QuantumBlocks.VAULT_LAMP.get());

        states.simpleBlock(QuantumBlocks.CRUMBLING_FLOOR.get(), cube(states, "crumbling_floor", "crumbling_floor"));
        states.blockItem(QuantumBlocks.CRUMBLING_FLOOR.get());

        Identifier chest = ModelTemplates.CUBE_ORIENTABLE.create(states.modLoc("block/shifting_chest"), new TextureMapping()
                .put(TextureSlot.SIDE, states.blockMaterial("shifting_chest_side")).put(TextureSlot.FRONT, states.blockMaterial("shifting_chest_front"))
                .put(TextureSlot.TOP, states.blockMaterial("shifting_chest_top")), states.modelOutput());
        states.horizontalBlock(QuantumBlocks.SHIFTING_CHEST.get(), chest);
        states.blockItem(QuantumBlocks.SHIFTING_CHEST.get());

        QuantumPedestalModels.register(states);
    }

    private static Identifier cube(ModBlockStateProvider states, String model, String texture) {
        return ModelTemplates.CUBE_ALL.create(states.modLoc("block/" + model), TextureMapping.cube(states.blockMaterial(texture)), states.modelOutput());
    }
}
