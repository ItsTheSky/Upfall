package net.itsthesky.upfall.content.grotto;

import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

final class GrottoModels {
    private GrottoModels() {}

    static void blockStates(ModBlockStateProvider states) {
        Material pool = states.blockMaterial("prism_pool").withForceTranslucent(true);
        ModelTemplate poolTemplate = ExtendedModelTemplateBuilder.builder()
                .requiredTextureSlot(TextureSlot.ALL)
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .element(element -> element.from(0, 0, 0).to(16, 14, 16).allFaces((dir, face) -> {
                    face.texture(TextureSlot.ALL);
                    if (dir != Direction.UP) face.cullface(dir);
                }))
                .build();
        states.simpleBlockWithItem(GrottoBlocks.PRISM_POOL.get(), poolTemplate.create(states.modLoc("block/prism_pool"),
                new TextureMapping().put(TextureSlot.ALL, pool).put(TextureSlot.PARTICLE, pool), states.modelOutput()));

        for (GrottoBlocks.CrystalSet set : GrottoBlocks.CRYSTALS) {
            for (var cluster : set.clusters()) {
                clusterStates(states, cluster.get());
            }
        }

        LanternBlock lantern = GrottoBlocks.MISTGLOW_LANTERN.get();
        TextureMapping lanternTextures = new TextureMapping().put(TextureSlot.LANTERN, states.blockMaterial("mistglow_lantern"));
        Identifier standing = ModelTemplates.LANTERN.create(states.modLoc("block/mistglow_lantern"), lanternTextures, states.modelOutput());
        Identifier hanging = ModelTemplates.HANGING_LANTERN.create(states.modLoc("block/mistglow_lantern_hanging"), lanternTextures, states.modelOutput());
        states.blockStateOutput().accept(MultiVariantGenerator.dispatch(lantern).with(PropertyDispatch.initial(LanternBlock.HANGING)
                .select(true, BlockModelGenerators.plainVariant(hanging))
                .select(false, BlockModelGenerators.plainVariant(standing))));
        states.itemModels().generateFlatItem(lantern.asItem(), ModelTemplates.FLAT_ITEM);
    }

    private static void clusterStates(ModBlockStateProvider states, AmethystClusterBlock block) {
        String name = ModBlockStateProvider.name(block);
        Material texture = states.blockMaterial(name);
        Identifier model = ModelTemplates.CROSS.create(states.modLoc("block/" + name), TextureMapping.cross(texture), states.modelOutput());
        states.blockStateOutput().accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model))
                .with(PropertyDispatch.modify(AmethystClusterBlock.FACING)
                        .select(Direction.UP, BlockModelGenerators.NOP)
                        .select(Direction.DOWN, BlockModelGenerators.X_ROT_180)
                        .select(Direction.NORTH, BlockModelGenerators.X_ROT_90)
                        .select(Direction.SOUTH, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_180))
                        .select(Direction.EAST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90))
                        .select(Direction.WEST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_270))));
        states.blockModels().registerSimpleItemModel(block.asItem(),
                ModelTemplates.FLAT_ITEM.create(block.asItem(), TextureMapping.layer0(texture), states.modelOutput()));
    }
}
