package net.itsthesky.upfall.content.machines.pipe;

import net.itsthesky.upfall.content.machines.MachinesBlocks;
import net.itsthesky.upfall.content.machines.ModelBoxes;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;

public final class PipeModels {
    private PipeModels() {}

    public static void blockStates(ModBlockStateProvider states) {
        ResourceLocation glass = states.blockTexture("pneumatic_pipe_glass");
        ResourceLocation band = states.blockTexture("pneumatic_pipe_band");
        Block pipe = MachinesBlocks.PNEUMATIC_PIPE.get();
        MultiPartBlockStateBuilder multipart = states.getMultipartBuilder(pipe);
        for (Direction dir : Direction.values()) {
            ModelBuilder<?> arm = pipeModel(states, "pneumatic_pipe_arm_" + dir.getName(), glass, band);
            ModelBoxes.apply(arm, PipeData.armBoxes(dir));
            ModelBuilder<?> cap = pipeModel(states, "pneumatic_pipe_cap_" + dir.getName(), glass, band);
            ModelBoxes.apply(cap, PipeData.capBoxes(dir));
            multipart.part().modelFile(arm).addModel().condition(PipeBlock.PROPERTY_BY_DIRECTION.get(dir), true).end();
            multipart.part().modelFile(cap).addModel().condition(PipeBlock.PROPERTY_BY_DIRECTION.get(dir), false).end();
        }
        ModelBuilder<?> item = states.itemModels().withExistingParent(ModBlockStateProvider.name(pipe), states.mcLoc("block/block"))
                .texture("glass", glass).texture("band", band).texture("particle", band).renderType("translucent");
        ModelBoxes.apply(item, PipeData.itemBoxes());

        ResourceLocation casing = states.blockTexture("pneumatic_pipe_casing");
        terminal(states, MachinesBlocks.PNEUMATIC_PIPE_ENTRANCE.get(), casing, states.blockTexture("pneumatic_pipe_entrance_front"));
        terminal(states, MachinesBlocks.PNEUMATIC_PIPE_EXIT.get(), casing, states.blockTexture("pneumatic_pipe_exit_front"));
    }

    private static void terminal(ModBlockStateProvider states, Block block, ResourceLocation casing, ResourceLocation front) {
        ModelFile model = states.models().cube(ModBlockStateProvider.name(block), casing, front, casing, casing, casing, casing)
                .texture("particle", casing);
        states.directionalBlock(block, model);
        states.blockItem(block);
    }

    private static ModelBuilder<?> pipeModel(ModBlockStateProvider states, String name, ResourceLocation glass, ResourceLocation band) {
        return states.models().withExistingParent(name, states.mcLoc("block/block"))
                .texture("glass", glass).texture("band", band).texture("particle", band).renderType("translucent");
    }
}
