package net.itsthesky.upfall.content.machines;

import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.itsthesky.upfall.datagen.ModLanguageProvider;
import net.itsthesky.upfall.content.machines.block.HasteningGearBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;

import java.util.Set;

final class ClockworkData {
    private ClockworkData() {}

    static final String COG_MODEL = "hastening_gear_cog";

    static boolean owns(Block block) {
        return Set.of(MachinesBlocks.VOID_TETHER.get(), MachinesBlocks.REWINDER.get(), MachinesBlocks.HASTENING_GEAR.get()).contains(block);
    }

    static void lang(ModLanguageProvider lang) {
        lang.add("message.upfall.rewinder.fuel", "Kairite: %s%%");
        lang.add("message.upfall.hastening_gear.fuel", "Kairite: %s%%");
    }

    static void blockStates(ModBlockStateProvider states) {
        states.simpleBlockWithItem(MachinesBlocks.VOID_TETHER.get(), states.models().cubeBottomTop("void_tether",
                states.blockTexture("void_tether_side"), states.blockTexture("void_tether_bottom"), states.blockTexture("void_tether_top")));
        rewinder(states);
        hasteningGear(states);
    }

    private static void rewinder(ModBlockStateProvider states) {
        BlockModelBuilder model = states.models().getBuilder("rewinder")
                .parent(new ModelFile.UncheckedModelFile(states.mcLoc("block/block")))
                .texture("side", states.blockTexture("rewinder_side")).texture("top", states.blockTexture("rewinder_top"))
                .texture("particle", states.blockTexture("rewinder_side"));
        model.element().from(2, 0, 2).to(14, 3, 14).allFaces((dir, face) -> face.texture("#side")).end();
        model.element().from(5, 3, 5).to(11, 11, 11).allFaces((dir, face) -> face.texture("#side")).end();
        model.element().from(3, 11, 3).to(13, 14, 13).allFaces((dir, face) -> face.texture(dir == Direction.UP ? "#top" : "#side")).end();
        states.simpleBlockWithItem(MachinesBlocks.REWINDER.get(), model);
    }

    private static void hasteningGear(ModBlockStateProvider states) {
        BlockModelBuilder body = gearBody(states, "hastening_gear");
        BlockModelBuilder cog = states.models().getBuilder(COG_MODEL).parent(new ModelFile.UncheckedModelFile(states.mcLoc("block/block")))
                .texture("cog", states.blockTexture("hastening_gear_cog")).texture("particle", states.blockTexture("hastening_gear_cog"))
                .renderType("cutout");
        cogElements(cog);
        BlockModelBuilder item = gearBody(states, "hastening_gear_item");
        cogElements(item);
        states.getVariantBuilder(MachinesBlocks.HASTENING_GEAR.get()).forAllStates(state -> {
            Direction facing = state.getValue(HasteningGearBlock.FACING);
            int x = facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90;
            int y = facing.getAxis().isHorizontal() ? ((int) facing.toYRot() + 180) % 360 : 0;
            return ConfiguredModel.builder().modelFile(body).rotationX(x).rotationY(y).build();
        });
        states.itemModels().withExistingParent("hastening_gear", states.modLoc("block/hastening_gear_item"));
    }

    private static BlockModelBuilder gearBody(ModBlockStateProvider states, String name) {
        BlockModelBuilder model = states.models().getBuilder(name).parent(new ModelFile.UncheckedModelFile(states.mcLoc("block/block")))
                .texture("plate", states.blockTexture("hastening_gear")).texture("cog", states.blockTexture("hastening_gear_cog"))
                .texture("particle", states.blockTexture("hastening_gear")).renderType("cutout");
        model.element().from(0, 0, 0).to(16, 2, 16).allFaces((dir, face) -> face.texture("#plate")).end();
        for (int[] post : new int[][] {{1, 1}, {13, 1}, {1, 13}, {13, 13}}) {
            model.element().from(post[0], 2, post[1]).to(post[0] + 2, 6, post[1] + 2).allFaces((dir, face) -> face.texture("#plate").uvs(0, 0, 2, 4)).end();
        }
        model.element().from(7, 2, 7).to(9, 6, 9).allFaces((dir, face) -> face.texture("#plate").uvs(6, 6, 10, 10)).end();
        return model;
    }

    private static void cogElements(BlockModelBuilder model) {
        int[][] boxes = {{1, 5, 15, 11}, {5, 1, 11, 15}, {3, 3, 13, 13}};
        for (int[] b : boxes) {
            model.element().from(b[0], 3, b[1]).to(b[2], 5, b[3]).allFaces((dir, face) -> {
                face.texture("#cog");
                if (dir.getAxis() != Direction.Axis.Y) {
                    face.uvs(6, 10, 10, 12);
                }
            }).end();
        }
    }
}
