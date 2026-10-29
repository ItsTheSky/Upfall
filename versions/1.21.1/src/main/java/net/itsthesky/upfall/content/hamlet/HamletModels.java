package net.itsthesky.upfall.content.hamlet;

import net.itsthesky.upfall.content.hamlet.block.RopeAnchorBlock;
import net.itsthesky.upfall.content.hamlet.block.RopeBlock;
import net.itsthesky.upfall.content.hamlet.block.RopeBridgeBlock;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.itsthesky.upfall.datagen.ModItemModelProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;

final class HamletModels {
    private HamletModels() {}

    static void blockStates(ModBlockStateProvider states) {
        ResourceLocation woven = states.blockTexture("woven_planks");
        states.simpleBlockWithItem(HamletBlocks.WOVEN_PLANKS.get(), states.models().cubeAll("woven_planks", woven));
        stairsAndSlab(states, HamletBlocks.WOVEN_STAIRS.get(), HamletBlocks.WOVEN_SLAB.get(), "woven_planks", woven);
        for (HamletBlocks.CanopySet canopy : HamletBlocks.CANOPIES) {
            String name = canopy.color() + "_canopy";
            ResourceLocation texture = states.blockTexture(name);
            states.simpleBlockWithItem(canopy.block().get(), states.models().cubeAll(name, texture));
            stairsAndSlab(states, canopy.stairs().get(), canopy.slab().get(), name, texture);
        }

        states.axisBlock(HamletBlocks.ROPE_SPOOL.get(), states.blockTexture("rope_spool_side"), states.blockTexture("rope_spool_end"));
        states.blockItem(HamletBlocks.ROPE_SPOOL.get());

        rope(states);
        ropeBridge(states);
        ropeAnchor(states);
        ziplineAnchor(states);

        ModelFile lantern = states.models().withExistingParent("cloud_lantern", states.mcLoc("block/template_lantern"))
                .texture("lantern", states.blockTexture("cloud_lantern")).renderType("cutout");
        ModelFile hanging = states.models().withExistingParent("cloud_lantern_hanging", states.mcLoc("block/template_hanging_lantern"))
                .texture("lantern", states.blockTexture("cloud_lantern")).renderType("cutout");
        states.getVariantBuilder(HamletBlocks.CLOUD_LANTERN.get()).forAllStatesExcept(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(LanternBlock.HANGING) ? hanging : lantern).build(), LanternBlock.WATERLOGGED);
        flatItem(states, "cloud_lantern");
    }

    private static void stairsAndSlab(ModBlockStateProvider states, StairBlock stairs, SlabBlock slab, String full, ResourceLocation texture) {
        states.stairsBlock(stairs, texture);
        states.slabBlock(slab, states.modLoc("block/" + full), texture);
        states.blockItem(stairs);
        states.blockItem(slab);
    }

    private static void flatItem(ModBlockStateProvider states, String name) {
        states.itemModels().withExistingParent(name, states.mcLoc("item/generated")).texture("layer0", states.modLoc("item/" + name));
    }

    private static BlockModelBuilder part(ModBlockStateProvider states, String name, String texture) {
        return states.models().withExistingParent(name, states.mcLoc("block/block"))
                .texture("main", states.blockTexture(texture)).texture("particle", states.blockTexture(texture)).renderType("cutout");
    }

    private static void rope(ModBlockStateProvider states) {
        BlockModelBuilder rope = part(states, "rope", "rope");
        rope.element().from(6, 0, 6).to(10, 16, 10)
                .allFaces((dir, face) -> face.texture("#main").uvs(0, 0, 4, dir.getAxis() == Direction.Axis.Y ? 4 : 16)).end();
        BlockModelBuilder end = part(states, "rope_end", "rope");
        end.element().from(6, 5, 6).to(10, 16, 10)
                .allFaces((dir, face) -> face.texture("#main").uvs(0, 0, 4, dir.getAxis() == Direction.Axis.Y ? 4 : 11)).end();
        end.element().from(5, 1, 5).to(11, 6, 11)
                .allFaces((dir, face) -> face.texture("#main").uvs(5, dir.getAxis() == Direction.Axis.Y ? 6 : 0, 11, dir.getAxis() == Direction.Axis.Y ? 12 : 5)).end();
        end.element().from(6.5F, 0, 6.5F).to(9.5F, 1, 9.5F)
                .allFaces((dir, face) -> face.texture("#main").uvs(12, 0, 15, 1)).end();
        states.getVariantBuilder(HamletBlocks.ROPE.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(RopeBlock.END) ? end : rope).build());
        flatItem(states, "rope");
    }

    private static void ropeBridge(ModBlockStateProvider states) {
        ModelFile[] levels = new ModelFile[4];
        for (int level = 0; level < 4; level++) {
            int y = level * 4;
            BlockModelBuilder model = part(states, "rope_bridge_" + level, "rope_bridge").texture("rope", states.blockTexture("rope"));
            for (int slat = 0; slat < 4; slat++) {
                int x = slat * 4;
                model.element().from(x, y, 0).to(x + 3, y + 2, 16).allFaces((dir, face) -> {
                    face.texture("#main");
                    switch (dir.getAxis()) {
                        case Y -> face.uvs(x, 0, x + 3, 16);
                        case Z -> face.uvs(x, 0, x + 3, 2);
                        default -> face.uvs(x, 0, x + 2, 16).rotation(net.neoforged.neoforge.client.model.generators.ModelBuilder.FaceRotation.CLOCKWISE_90);
                    }
                }).end();
            }
            for (int z : new int[]{1, 14}) {
                model.element().from(0, y + 2, z).to(16, y + 3, z + 1).allFaces((dir, face) -> {
                    face.texture("#rope");
                    if (dir.getAxis() == Direction.Axis.X) {
                        face.uvs(0, 0, 1, 1);
                    } else {
                        face.uvs(1, 0, 2, 16).rotation(net.neoforged.neoforge.client.model.generators.ModelBuilder.FaceRotation.CLOCKWISE_90);
                    }
                }).end();
            }
            levels[level] = model;
        }
        states.getVariantBuilder(HamletBlocks.ROPE_BRIDGE.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(levels[state.getValue(RopeBridgeBlock.LEVEL)])
                .rotationY(state.getValue(RopeBridgeBlock.AXIS) == Direction.Axis.X ? 0 : 90).build());
        flatItem(states, "rope_bridge");
    }

    private static void ropeAnchor(ModBlockStateProvider states) {
        BlockModelBuilder model = part(states, "rope_anchor", "rope_anchor");
        model.element().from(6, 0, 6).to(10, 9, 10).allFaces((dir, face) -> face.texture("#main")
                .uvs(dir.getAxis() == Direction.Axis.Y ? 11 : 0, 0, dir.getAxis() == Direction.Axis.Y ? 15 : 4, dir.getAxis() == Direction.Axis.Y ? 4 : 9)).end();
        model.element().from(5, 3, 5).to(11, 7, 11).allFaces((dir, face) -> face.texture("#main")
                .uvs(5, dir.getAxis() == Direction.Axis.Y ? 5 : 0, 11, dir.getAxis() == Direction.Axis.Y ? 11 : 4)).end();
        model.element().from(5, 9, 5).to(11, 10, 11).allFaces((dir, face) -> face.texture("#main")
                .uvs(dir.getAxis() == Direction.Axis.Y ? 6 : 0, 10, dir.getAxis() == Direction.Axis.Y ? 12 : 6, dir.getAxis() == Direction.Axis.Y ? 16 : 11)).end();
        states.getVariantBuilder(HamletBlocks.ROPE_ANCHOR.get()).forAllStates(state -> {
            Direction facing = state.getValue(RopeAnchorBlock.FACING);
            int x = facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90;
            int y = facing.getAxis().isHorizontal() ? ((int) facing.toYRot() + 180) % 360 : 0;
            return ConfiguredModel.builder().modelFile(model).rotationX(x).rotationY(y).build();
        });
        states.blockItem(HamletBlocks.ROPE_ANCHOR.get());
    }

    private static void ziplineAnchor(ModBlockStateProvider states) {
        BlockModelBuilder model = part(states, "zipline_anchor", "zipline_anchor");
        model.element().from(5, 0, 5).to(11, 9, 11).allFaces((dir, face) -> face.texture("#main")
                .uvs(0, dir.getAxis() == Direction.Axis.Y ? 10 : 0, 6, dir.getAxis() == Direction.Axis.Y ? 16 : 9)).end();
        for (int x : new int[]{5, 10}) {
            model.element().from(x, 9, 6).to(x + 1, 15, 10).allFaces((dir, face) -> face.texture("#main")
                    .uvs(7, 0, dir.getAxis() == Direction.Axis.X ? 11 : 8, dir.getAxis() == Direction.Axis.Y ? 4 : 6)).end();
        }
        model.element().from(7, 9, 5).to(9, 15, 11).allFaces((dir, face) -> {
            face.texture("#main");
            if (dir.getAxis() == Direction.Axis.X) {
                face.uvs(10, 10, 16, 16);
            } else {
                face.uvs(12, 0, 14, 6);
            }
        }).end();
        model.element().from(4.5F, 11.5F, 7.5F).to(11.5F, 12.5F, 8.5F).allFaces((dir, face) -> face.texture("#main").uvs(7, 7, 14, 8)).end();
        states.horizontalBlock(HamletBlocks.ZIPLINE_ANCHOR.get(), model);
        states.blockItem(HamletBlocks.ZIPLINE_ANCHOR.get());
    }

    static void itemModels(ModItemModelProvider models) {
        models.withExistingParent(HamletItems.LOFTKIN_SPAWN_EGG.getId().getPath(), models.mcLoc("item/template_spawn_egg"));
    }
}
