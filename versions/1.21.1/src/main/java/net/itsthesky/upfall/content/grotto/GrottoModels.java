package net.itsthesky.upfall.content.grotto;

import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;

final class GrottoModels {
    private GrottoModels() {}

    static void blockStates(ModBlockStateProvider states) {
        ModelFile pool = states.models().getBuilder("prism_pool")
                .texture("particle", states.blockTexture("prism_pool")).texture("all", states.blockTexture("prism_pool"))
                .renderType("translucent")
                .element().from(0, 0, 0).to(16, 14, 16)
                .allFaces((dir, face) -> {
                    face.texture("#all");
                    if (dir != Direction.UP) face.cullface(dir);
                }).end();
        states.simpleBlockWithItem(GrottoBlocks.PRISM_POOL.get(), pool);

        for (GrottoBlocks.CrystalSet set : GrottoBlocks.CRYSTALS) {
            for (var cluster : set.clusters()) {
                clusterStates(states, cluster.get());
            }
        }

        Block lantern = GrottoBlocks.MISTGLOW_LANTERN.get();
        ModelFile standing = states.models().withExistingParent("mistglow_lantern", states.mcLoc("block/template_lantern"))
                .texture("lantern", states.blockTexture("mistglow_lantern")).renderType("cutout");
        ModelFile hanging = states.models().withExistingParent("mistglow_lantern_hanging", states.mcLoc("block/template_hanging_lantern"))
                .texture("lantern", states.blockTexture("mistglow_lantern")).renderType("cutout");
        states.getVariantBuilder(lantern).forAllStatesExcept(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(LanternBlock.HANGING) ? hanging : standing).build(), LanternBlock.WATERLOGGED);
        states.itemModels().withExistingParent("mistglow_lantern", states.mcLoc("item/generated"))
                .texture("layer0", states.modLoc("item/mistglow_lantern"));
    }

    private static void clusterStates(ModBlockStateProvider states, AmethystClusterBlock block) {
        String name = ModBlockStateProvider.name(block);
        ModelFile model = states.models().cross(name, states.blockTexture(name)).renderType("cutout");
        states.getVariantBuilder(block).forAllStatesExcept(state -> {
            ConfiguredModel.Builder<?> builder = ConfiguredModel.builder().modelFile(model);
            return switch (state.getValue(AmethystClusterBlock.FACING)) {
                case DOWN -> builder.rotationX(180).build();
                case NORTH -> builder.rotationX(90).build();
                case SOUTH -> builder.rotationX(90).rotationY(180).build();
                case EAST -> builder.rotationX(90).rotationY(90).build();
                case WEST -> builder.rotationX(90).rotationY(270).build();
                default -> builder.build();
            };
        }, AmethystClusterBlock.WATERLOGGED);
        states.itemModels().withExistingParent(name, states.mcLoc("item/generated")).texture("layer0", states.blockTexture(name));
    }
}
