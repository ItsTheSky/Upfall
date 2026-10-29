package net.itsthesky.upfall.content.machines;

import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.Map;

final class ClockworkModels {
    private ClockworkModels() {}

    static void blockStates(ModBlockStateProvider states) {
        states.simpleBlockWithItem(MachinesBlocks.VOID_TETHER.get(), states.cubeBottomTop("void_tether",
                "void_tether_side", "void_tether_bottom", "void_tether_top"));
        rewinder(states);
        hasteningGear(states);
    }

    private static void rewinder(ModBlockStateProvider states) {
        Identifier model = ModelBoxes.create(states.modelOutput(), states.modLoc("block/rewinder"), Map.of(
                "side", states.blockMaterial("rewinder_side"), "top", states.blockMaterial("rewinder_top"),
                "particle", states.blockMaterial("rewinder_side")), ClockworkData.rewinderBoxes());
        states.simpleBlockWithItem(MachinesBlocks.REWINDER.get(), model);
    }

    private static void hasteningGear(ModBlockStateProvider states) {
        Block gear = MachinesBlocks.HASTENING_GEAR.get();
        Map<String, Material> plate = Map.of("plate", states.blockMaterial("hastening_gear"), "cog", states.blockMaterial("hastening_gear_cog"),
                "particle", states.blockMaterial("hastening_gear"));
        Identifier body = ModelBoxes.create(states.modelOutput(), states.modLoc("block/hastening_gear"), plate, ClockworkData.gearBodyBoxes());
        ModelBoxes.create(states.modelOutput(), states.modLoc("block/" + ClockworkData.COG_MODEL), Map.of(
                "cog", states.blockMaterial("hastening_gear_cog"), "particle", states.blockMaterial("hastening_gear_cog")), ClockworkData.cogBoxes());
        Identifier item = ModelBoxes.create(states.modelOutput(), states.modLoc("block/hastening_gear_item"), plate, ClockworkData.gearItemBoxes());
        states.blockStateOutput().accept(MultiVariantGenerator.dispatch(gear, BlockModelGenerators.plainVariant(body))
                .with(BlockModelGenerators.ROTATIONS_COLUMN_WITH_FACING));
        states.blockModels().registerSimpleItemModel(gear, item);
    }
}
