package net.itsthesky.upfall.content.reliquary;

import net.itsthesky.upfall.content.reliquary.block.FoldSocketBlock;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.itsthesky.upfall.datagen.ModItemModelProvider;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

final class ReliquaryModels {
    private ReliquaryModels() {}

    static void blockStates(ModBlockStateProvider states) {
        axisBlock(states, ReliquaryBlocks.LATTICE_BEAM.get(), "lattice_beam", "lattice_beam_end");
        states.blockItem(ReliquaryBlocks.LATTICE_BEAM.get());

        Block socket = ReliquaryBlocks.FOLD_SOCKET.get();
        Identifier empty = states.cubeAll(socket);
        Identifier filled = states.glowingCube("fold_socket_filled", "solid");
        states.blockStateOutput().accept(MultiVariantGenerator.dispatch(socket).with(PropertyDispatch.initial(FoldSocketBlock.FILLED)
                .select(true, BlockModelGenerators.plainVariant(filled))
                .select(false, BlockModelGenerators.plainVariant(empty))));
    }

    static void itemModels(ModItemModelProvider models) {
        models.basicItem(ReliquaryItems.FOLD_WARDEN_SPAWN_EGG.get());
    }

    private static void axisBlock(ModBlockStateProvider states, Block block, String side, String end) {
        String name = ModBlockStateProvider.name(block);
        TextureMapping textures = TextureMapping.column(states.blockMaterial(side), states.blockMaterial(end));
        Identifier vertical = ModelTemplates.CUBE_COLUMN.create(states.modLoc("block/" + name), textures, states.modelOutput());
        Identifier horizontal = ModelTemplates.CUBE_COLUMN_HORIZONTAL.create(states.modLoc("block/" + name + "_horizontal"), textures, states.modelOutput());
        states.blockStateOutput().accept(BlockModelGenerators.createRotatedPillarWithHorizontalVariant(block,
                BlockModelGenerators.plainVariant(vertical), BlockModelGenerators.plainVariant(horizontal)));
    }
}
