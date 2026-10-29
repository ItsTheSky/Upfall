package net.itsthesky.upfall.content.world;

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
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

final class WorldModels {
    private static final TextureSlot BASE = TextureSlot.create("base");
    private static final TextureSlot PEARLS = TextureSlot.create("pearls");

    private WorldModels() {}

    static void blockStates(ModBlockStateProvider states) {
        pearledCloud(states);
        skyreed(states);
        skylily(states);
        cloudbloom(states);
    }

    private static void pearledCloud(ModBlockStateProvider states) {
        ModelTemplate template = ExtendedModelTemplateBuilder.builder()
                .parent(states.mcLoc("block/block"))
                .requiredTextureSlot(BASE)
                .requiredTextureSlot(PEARLS)
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .element(element -> element.from(0, 0, 0).to(16, 16, 16).cube(BASE))
                .element(element -> element.from(0, 0, 0).to(16, 16, 16).shade(false).ambientOcclusion(false).lightEmission(15).cube(PEARLS))
                .build();
        Material base = states.blockMaterial("dense_cloud");
        Identifier model = template.create(states.modLoc("block/pearled_cloud"), new TextureMapping()
                .put(BASE, base)
                .put(PEARLS, states.blockMaterial("pearled_cloud_pearls"))
                .put(TextureSlot.PARTICLE, base), states.modelOutput());
        states.simpleBlockWithItem(WorldBlocks.PEARLED_CLOUD.get(), model);
    }

    private static void skyreed(ModBlockStateProvider states) {
        Identifier lower = ModelTemplates.CROSS.create(states.modLoc("block/skyreed_bottom"),
                TextureMapping.cross(states.blockMaterial("skyreed_bottom")), states.modelOutput());
        Identifier upper = ModelTemplates.CROSS.create(states.modLoc("block/skyreed_top"),
                TextureMapping.cross(states.blockMaterial("skyreed_top")), states.modelOutput());
        states.blockStateOutput().accept(MultiVariantGenerator.dispatch(WorldBlocks.SKYREED.get()).with(PropertyDispatch.initial(DoublePlantBlock.HALF)
                .select(DoubleBlockHalf.LOWER, BlockModelGenerators.plainVariant(lower))
                .select(DoubleBlockHalf.UPPER, BlockModelGenerators.plainVariant(upper))));
        flatItem(states, WorldBlocks.SKYREED_ITEM.get(), "skyreed_top");
    }

    private static void skylily(ModBlockStateProvider states) {
        ModelTemplate template = ExtendedModelTemplateBuilder.builder()
                .parent(states.mcLoc("block/block"))
                .requiredTextureSlot(TextureSlot.TEXTURE)
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .ambientOcclusion(false)
                .element(element -> element.from(0, 0.25F, 0).to(16, 0.25F, 16).lightEmission(6)
                        .face(Direction.UP, face -> face.texture(TextureSlot.TEXTURE).uvs(0, 0, 16, 16))
                        .face(Direction.DOWN, face -> face.texture(TextureSlot.TEXTURE).uvs(0, 16, 16, 0)))
                .build();
        Material lily = states.blockMaterial("skylily");
        Identifier model = template.create(states.modLoc("block/skylily"), new TextureMapping()
                .put(TextureSlot.TEXTURE, lily)
                .put(TextureSlot.PARTICLE, lily), states.modelOutput());
        states.blockStateOutput().accept(BlockModelGenerators.createSimpleBlock(WorldBlocks.SKYLILY.get(),
                BlockModelGenerators.createRotatedVariants(BlockModelGenerators.plainModel(model))));
        flatItem(states, WorldBlocks.SKYLILY_ITEM.get(), "skylily");
    }

    private static void cloudbloom(ModBlockStateProvider states) {
        ModelTemplate template = ExtendedModelTemplateBuilder.builder()
                .parent(states.mcLoc("block/block"))
                .requiredTextureSlot(TextureSlot.SIDE)
                .requiredTextureSlot(TextureSlot.TOP)
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .ambientOcclusion(false)
                .element(element -> element.from(3, 1, 3).to(13, 8, 13).shade(false).lightEmission(12)
                        .allFaces((dir, face) -> face.texture(dir.getAxis().isVertical() ? TextureSlot.TOP : TextureSlot.SIDE)))
                .element(element -> element.from(5, 8, 5).to(11, 11, 11).shade(false).lightEmission(12)
                        .allFaces((dir, face) -> face.texture(dir.getAxis().isVertical() ? TextureSlot.TOP : TextureSlot.SIDE)))
                .element(element -> element.from(7, 0, 7).to(9, 1, 9)
                        .allFaces((dir, face) -> face.texture(TextureSlot.SIDE)))
                .build();
        Material side = states.blockMaterial("cloudbloom_side");
        Identifier model = template.create(states.modLoc("block/cloudbloom"), new TextureMapping()
                .put(TextureSlot.SIDE, side)
                .put(TextureSlot.TOP, states.blockMaterial("cloudbloom_top"))
                .put(TextureSlot.PARTICLE, side), states.modelOutput());
        states.simpleBlock(WorldBlocks.CLOUDBLOOM.get(), model);
        states.blockItem(WorldBlocks.CLOUDBLOOM.get());
    }

    private static void flatItem(ModBlockStateProvider states, Item item, String texture) {
        states.blockModels().registerSimpleItemModel(item, ModelTemplates.FLAT_ITEM.create(item,
                TextureMapping.layer0(states.blockMaterial(texture)), states.modelOutput()));
    }
}
