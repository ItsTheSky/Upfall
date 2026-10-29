package net.itsthesky.upfall.content.machines;

import net.itsthesky.upfall.content.machines.block.HasteningGearBlock;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;

final class ClockworkModels {
    private ClockworkModels() {}

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
        ModelBoxes.apply(model, ClockworkData.rewinderBoxes());
        states.simpleBlockWithItem(MachinesBlocks.REWINDER.get(), model);
    }

    private static void hasteningGear(ModBlockStateProvider states) {
        BlockModelBuilder body = gearBody(states, "hastening_gear");
        BlockModelBuilder cog = states.models().getBuilder(ClockworkData.COG_MODEL).parent(new ModelFile.UncheckedModelFile(states.mcLoc("block/block")))
                .texture("cog", states.blockTexture("hastening_gear_cog")).texture("particle", states.blockTexture("hastening_gear_cog"))
                .renderType("cutout");
        ModelBoxes.apply(cog, ClockworkData.cogBoxes());
        BlockModelBuilder item = gearBody(states, "hastening_gear_item");
        ModelBoxes.apply(item, ClockworkData.cogBoxes());
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
        ModelBoxes.apply(model, ClockworkData.gearBodyBoxes());
        return model;
    }
}
