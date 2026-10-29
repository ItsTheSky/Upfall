package net.itsthesky.upfall.content.world;

import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;

final class WorldModels {
    private WorldModels() {}

    static void blockStates(ModBlockStateProvider states) {
        ModelFile pearled = states.models().withExistingParent("pearled_cloud", states.mcLoc("block/block"))
                .texture("base", states.blockTexture("dense_cloud")).texture("pearls", states.blockTexture("pearled_cloud_pearls"))
                .texture("particle", states.blockTexture("dense_cloud")).renderType("cutout")
                .element().from(0, 0, 0).to(16, 16, 16).allFaces((dir, face) -> face.texture("#base").cullface(dir)).end()
                .element().from(0, 0, 0).to(16, 16, 16).shade(false).ao(false).emissivity(15, 15)
                .allFaces((dir, face) -> face.texture("#pearls").cullface(dir)).end();
        states.simpleBlockWithItem(WorldBlocks.PEARLED_CLOUD.get(), pearled);

        ModelFile reedLower = states.models().cross("skyreed_bottom", states.blockTexture("skyreed_bottom")).renderType("cutout");
        ModelFile reedUpper = states.models().cross("skyreed_top", states.blockTexture("skyreed_top")).renderType("cutout");
        states.getVariantBuilder(WorldBlocks.SKYREED.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER ? reedLower : reedUpper).build());
        states.itemModels().withExistingParent("skyreed", states.mcLoc("item/generated")).texture("layer0", states.blockTexture("skyreed_top"));

        ModelFile lily = states.models().withExistingParent("skylily", states.mcLoc("block/block"))
                .texture("texture", states.blockTexture("skylily")).texture("particle", states.blockTexture("skylily"))
                .renderType("cutout").ao(false)
                .element().from(0, 0.25F, 0).to(16, 0.25F, 16).emissivity(6, 6)
                .face(Direction.UP).texture("#texture").uvs(0, 0, 16, 16).end()
                .face(Direction.DOWN).texture("#texture").uvs(0, 16, 16, 0).end().end();
        states.getVariantBuilder(WorldBlocks.SKYLILY.get()).forAllStates(state -> ConfiguredModel.allYRotations(lily, 0, false));
        states.itemModels().withExistingParent("skylily", states.mcLoc("item/generated")).texture("layer0", states.blockTexture("skylily"));

        ModelFile bloom = states.models().withExistingParent("cloudbloom", states.mcLoc("block/block"))
                .texture("side", states.blockTexture("cloudbloom_side")).texture("top", states.blockTexture("cloudbloom_top"))
                .texture("particle", states.blockTexture("cloudbloom_side")).renderType("cutout").ao(false)
                .element().from(3, 1, 3).to(13, 8, 13).shade(false).emissivity(12, 12)
                .allFaces((dir, face) -> face.texture(dir.getAxis().isVertical() ? "#top" : "#side")).end()
                .element().from(5, 8, 5).to(11, 11, 11).shade(false).emissivity(12, 12)
                .allFaces((dir, face) -> face.texture(dir.getAxis().isVertical() ? "#top" : "#side")).end()
                .element().from(7, 0, 7).to(9, 1, 9)
                .allFaces((dir, face) -> face.texture("#side")).end();
        states.simpleBlock(WorldBlocks.CLOUDBLOOM.get(), bloom);
        states.blockItem(WorldBlocks.CLOUDBLOOM.get());
    }
}
