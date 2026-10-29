package net.itsthesky.upfall.content.reliquary;

import net.itsthesky.upfall.content.reliquary.block.FoldSocketBlock;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.itsthesky.upfall.datagen.ModItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;

final class ReliquaryModels {
    private ReliquaryModels() {}

    static void blockStates(ModBlockStateProvider states) {
        states.axisBlock(ReliquaryBlocks.LATTICE_BEAM.get(), states.blockTexture("lattice_beam"), states.blockTexture("lattice_beam_end"));
        states.blockItem(ReliquaryBlocks.LATTICE_BEAM.get());

        ModelFile empty = states.cubeAll(ReliquaryBlocks.FOLD_SOCKET.get());
        ModelFile filled = states.glowingCube("fold_socket_filled", "solid");
        states.getVariantBuilder(ReliquaryBlocks.FOLD_SOCKET.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(FoldSocketBlock.FILLED) ? filled : empty).build());
    }

    static void itemModels(ModItemModelProvider models) {
        models.withExistingParent(ReliquaryItems.FOLD_WARDEN_SPAWN_EGG.getId().getPath(), models.mcLoc("item/template_spawn_egg"));
    }
}
