package net.itsthesky.upfall.content.hamlet;

import com.mojang.math.Quadrant;
import net.itsthesky.upfall.content.hamlet.block.RopeAnchorBlock;
import net.itsthesky.upfall.content.hamlet.block.RopeBlock;
import net.itsthesky.upfall.content.hamlet.block.RopeBridgeBlock;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.itsthesky.upfall.datagen.ModItemModelProvider;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

final class HamletModels {
    private static final TextureSlot MAIN = TextureSlot.create("main");
    private static final TextureSlot ROPE_TEXTURE = TextureSlot.create("rope");

    private HamletModels() {}

    static void blockStates(ModBlockStateProvider states) {
        BlockModelGenerators blocks = states.blockModels();
        blocks.family(HamletBlocks.WOVEN_PLANKS.get()).stairs(HamletBlocks.WOVEN_STAIRS.get()).slab(HamletBlocks.WOVEN_SLAB.get());
        for (HamletBlocks.CanopySet canopy : HamletBlocks.CANOPIES) {
            blocks.family(canopy.block().get()).stairs(canopy.stairs().get()).slab(canopy.slab().get());
        }

        Block spool = HamletBlocks.ROPE_SPOOL.get();
        TextureMapping spoolTextures = TextureMapping.column(states.blockMaterial("rope_spool_side"), states.blockMaterial("rope_spool_end"));
        states.blockStateOutput().accept(BlockModelGenerators.createRotatedPillarWithHorizontalVariant(spool,
                BlockModelGenerators.plainVariant(ModelTemplates.CUBE_COLUMN.create(spool, spoolTextures, states.modelOutput())),
                BlockModelGenerators.plainVariant(ModelTemplates.CUBE_COLUMN_HORIZONTAL.create(spool, spoolTextures, states.modelOutput()))));

        rope(states);
        ropeBridge(states);
        ropeAnchor(states);
        ziplineAnchor(states);

        LanternBlock lantern = HamletBlocks.CLOUD_LANTERN.get();
        TextureMapping lanternTextures = new TextureMapping().put(TextureSlot.LANTERN, states.blockMaterial("cloud_lantern"));
        Identifier standing = ModelTemplates.LANTERN.create(states.modLoc("block/cloud_lantern"), lanternTextures, states.modelOutput());
        Identifier hanging = ModelTemplates.HANGING_LANTERN.create(states.modLoc("block/cloud_lantern_hanging"), lanternTextures, states.modelOutput());
        states.blockStateOutput().accept(MultiVariantGenerator.dispatch(lantern).with(PropertyDispatch.initial(LanternBlock.HANGING)
                .select(true, BlockModelGenerators.plainVariant(hanging))
                .select(false, BlockModelGenerators.plainVariant(standing))));
        states.itemModels().generateFlatItem(lantern.asItem(), ModelTemplates.FLAT_ITEM);
    }

    static void itemModels(ModItemModelProvider models) {
        models.basicItem(HamletItems.LOFTKIN_SPAWN_EGG.get());
    }

    private static ExtendedModelTemplateBuilder part(ModBlockStateProvider states) {
        return ExtendedModelTemplateBuilder.builder()
                .parent(states.mcLoc("block/block"))
                .requiredTextureSlot(MAIN)
                .requiredTextureSlot(TextureSlot.PARTICLE);
    }

    private static Identifier create(ModBlockStateProvider states, String name, ExtendedModelTemplateBuilder part, String texture) {
        Material material = states.blockMaterial(texture);
        return part.build().create(states.modLoc("block/" + name),
                new TextureMapping().put(MAIN, material).put(TextureSlot.PARTICLE, material), states.modelOutput());
    }

    private static void rope(ModBlockStateProvider states) {
        Identifier rope = create(states, "rope", part(states)
                .element(e -> e.from(6, 0, 6).to(10, 16, 10)
                        .allFaces((dir, face) -> face.texture(MAIN).uvs(0, 0, 4, dir.getAxis() == Direction.Axis.Y ? 4 : 16))), "rope");
        Identifier end = create(states, "rope_end", part(states)
                .element(e -> e.from(6, 5, 6).to(10, 16, 10)
                        .allFaces((dir, face) -> face.texture(MAIN).uvs(0, 0, 4, dir.getAxis() == Direction.Axis.Y ? 4 : 11)))
                .element(e -> e.from(5, 1, 5).to(11, 6, 11)
                        .allFaces((dir, face) -> face.texture(MAIN).uvs(5, dir.getAxis() == Direction.Axis.Y ? 6 : 0, 11, dir.getAxis() == Direction.Axis.Y ? 12 : 5)))
                .element(e -> e.from(6.5F, 0, 6.5F).to(9.5F, 1, 9.5F)
                        .allFaces((dir, face) -> face.texture(MAIN).uvs(12, 0, 15, 1))), "rope");
        states.blockStateOutput().accept(MultiVariantGenerator.dispatch(HamletBlocks.ROPE.get())
                .with(PropertyDispatch.initial(RopeBlock.END)
                        .select(true, BlockModelGenerators.plainVariant(end))
                        .select(false, BlockModelGenerators.plainVariant(rope))));
        states.itemModels().generateFlatItem(HamletBlocks.ROPE_ITEM.get(), ModelTemplates.FLAT_ITEM);
    }

    private static void ropeBridge(ModBlockStateProvider states) {
        Identifier[] levels = new Identifier[4];
        for (int level = 0; level < 4; level++) {
            int y = level * 4;
            ExtendedModelTemplateBuilder model = part(states).requiredTextureSlot(ROPE_TEXTURE);
            for (int slat = 0; slat < 4; slat++) {
                int x = slat * 4;
                model.element(e -> e.from(x, y, 0).to(x + 3, y + 2, 16).allFaces((dir, face) -> {
                    face.texture(MAIN);
                    switch (dir.getAxis()) {
                        case Y -> face.uvs(x, 0, x + 3, 16);
                        case Z -> face.uvs(x, 0, x + 3, 2);
                        default -> face.uvs(x, 0, x + 2, 16).rotation(Quadrant.R90);
                    }
                }));
            }
            for (int z : new int[]{1, 14}) {
                model.element(e -> e.from(0, y + 2, z).to(16, y + 3, z + 1).allFaces((dir, face) -> {
                    face.texture(ROPE_TEXTURE);
                    if (dir.getAxis() == Direction.Axis.X) {
                        face.uvs(0, 0, 1, 1);
                    } else {
                        face.uvs(1, 0, 2, 16).rotation(Quadrant.R90);
                    }
                }));
            }
            Material material = states.blockMaterial("rope_bridge");
            levels[level] = model.build().create(states.modLoc("block/rope_bridge_" + level), new TextureMapping()
                    .put(MAIN, material).put(TextureSlot.PARTICLE, material).put(ROPE_TEXTURE, states.blockMaterial("rope")), states.modelOutput());
        }
        states.blockStateOutput().accept(MultiVariantGenerator.dispatch(HamletBlocks.ROPE_BRIDGE.get())
                .with(PropertyDispatch.initial(RopeBridgeBlock.LEVEL, RopeBridgeBlock.AXIS).generate((level, axis) ->
                        BlockModelGenerators.plainVariant(levels[level]).with(rotation(0, axis == Direction.Axis.X ? 0 : 90)))));
        states.itemModels().generateFlatItem(HamletBlocks.ROPE_BRIDGE.get().asItem(), ModelTemplates.FLAT_ITEM);
    }

    private static void ropeAnchor(ModBlockStateProvider states) {
        Identifier model = create(states, "rope_anchor", part(states)
                .element(e -> e.from(6, 0, 6).to(10, 9, 10).allFaces((dir, face) -> face.texture(MAIN)
                        .uvs(dir.getAxis() == Direction.Axis.Y ? 11 : 0, 0, dir.getAxis() == Direction.Axis.Y ? 15 : 4, dir.getAxis() == Direction.Axis.Y ? 4 : 9)))
                .element(e -> e.from(5, 3, 5).to(11, 7, 11).allFaces((dir, face) -> face.texture(MAIN)
                        .uvs(5, dir.getAxis() == Direction.Axis.Y ? 5 : 0, 11, dir.getAxis() == Direction.Axis.Y ? 11 : 4)))
                .element(e -> e.from(5, 9, 5).to(11, 10, 11).allFaces((dir, face) -> face.texture(MAIN)
                        .uvs(dir.getAxis() == Direction.Axis.Y ? 6 : 0, 10, dir.getAxis() == Direction.Axis.Y ? 12 : 6, dir.getAxis() == Direction.Axis.Y ? 16 : 11))),
                "rope_anchor");
        states.blockStateOutput().accept(MultiVariantGenerator.dispatch(HamletBlocks.ROPE_ANCHOR.get())
                .with(PropertyDispatch.initial(RopeAnchorBlock.FACING).generate(facing -> {
                    int x = facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90;
                    int y = facing.getAxis().isHorizontal() ? ((int) facing.toYRot() + 180) % 360 : 0;
                    return BlockModelGenerators.plainVariant(model).with(rotation(x, y));
                })));
    }

    private static void ziplineAnchor(ModBlockStateProvider states) {
        Identifier model = create(states, "zipline_anchor", part(states)
                .element(e -> e.from(5, 0, 5).to(11, 9, 11).allFaces((dir, face) -> face.texture(MAIN)
                        .uvs(0, dir.getAxis() == Direction.Axis.Y ? 10 : 0, 6, dir.getAxis() == Direction.Axis.Y ? 16 : 9)))
                .element(e -> e.from(5, 9, 6).to(6, 15, 10).allFaces((dir, face) -> face.texture(MAIN)
                        .uvs(7, 0, dir.getAxis() == Direction.Axis.X ? 11 : 8, dir.getAxis() == Direction.Axis.Y ? 4 : 6)))
                .element(e -> e.from(10, 9, 6).to(11, 15, 10).allFaces((dir, face) -> face.texture(MAIN)
                        .uvs(7, 0, dir.getAxis() == Direction.Axis.X ? 11 : 8, dir.getAxis() == Direction.Axis.Y ? 4 : 6)))
                .element(e -> e.from(7, 9, 5).to(9, 15, 11).allFaces((dir, face) -> {
                    face.texture(MAIN);
                    if (dir.getAxis() == Direction.Axis.X) {
                        face.uvs(10, 10, 16, 16);
                    } else {
                        face.uvs(12, 0, 14, 6);
                    }
                }))
                .element(e -> e.from(4.5F, 11.5F, 7.5F).to(11.5F, 12.5F, 8.5F).allFaces((dir, face) -> face.texture(MAIN).uvs(7, 7, 14, 8))),
                "zipline_anchor");
        states.horizontalBlock(HamletBlocks.ZIPLINE_ANCHOR.get(), model);
    }

    private static VariantMutator rotation(int x, int y) {
        return VariantMutator.X_ROT.withValue(quadrant(x)).then(VariantMutator.Y_ROT.withValue(quadrant(y)));
    }

    private static Quadrant quadrant(int degrees) {
        return Quadrant.values()[Math.floorMod(degrees / 90, 4)];
    }
}
