package net.itsthesky.upfall.content.machines.pipe;

import net.itsthesky.upfall.content.machines.MachinesBlocks;
import net.itsthesky.upfall.content.machines.ModelBoxes;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.Map;

public final class PipeModels {
    private PipeModels() {}

    public static void blockStates(ModBlockStateProvider states) {
        Material band = states.blockMaterial("pneumatic_pipe_band").withForceTranslucent(true);
        Map<String, Material> textures = Map.of("glass", states.blockMaterial("pneumatic_pipe_glass").withForceTranslucent(true),
                "band", band, "particle", band);
        Block pipe = MachinesBlocks.PNEUMATIC_PIPE.get();
        MultiPartGenerator multipart = MultiPartGenerator.multiPart(pipe);
        for (Direction dir : Direction.values()) {
            Identifier arm = ModelBoxes.create(states.modelOutput(), states.modLoc("block/pneumatic_pipe_arm_" + dir.getName()), textures,
                    PipeData.armBoxes(dir));
            Identifier cap = ModelBoxes.create(states.modelOutput(), states.modLoc("block/pneumatic_pipe_cap_" + dir.getName()), textures,
                    PipeData.capBoxes(dir));
            BooleanProperty side = PipeBlock.PROPERTY_BY_DIRECTION.get(dir);
            multipart.with(BlockModelGenerators.condition(side, true), BlockModelGenerators.plainVariant(arm));
            multipart.with(BlockModelGenerators.condition(side, false), BlockModelGenerators.plainVariant(cap));
        }
        states.blockStateOutput().accept(multipart);
        Identifier item = ModelBoxes.create(states.modelOutput(), states.modLoc("item/" + ModBlockStateProvider.name(pipe)), textures,
                PipeData.itemBoxes());
        states.blockModels().registerSimpleItemModel(pipe, item);

        Material casing = states.blockMaterial("pneumatic_pipe_casing");
        terminal(states, MachinesBlocks.PNEUMATIC_PIPE_ENTRANCE.get(), casing, states.blockMaterial("pneumatic_pipe_entrance_front"));
        terminal(states, MachinesBlocks.PNEUMATIC_PIPE_EXIT.get(), casing, states.blockMaterial("pneumatic_pipe_exit_front"));
    }

    private static void terminal(ModBlockStateProvider states, Block block, Material casing, Material front) {
        Identifier model = ModelTemplates.CUBE.create(states.modLoc("block/" + ModBlockStateProvider.name(block)), new TextureMapping()
                .put(TextureSlot.DOWN, casing).put(TextureSlot.UP, front).put(TextureSlot.NORTH, casing).put(TextureSlot.SOUTH, casing)
                .put(TextureSlot.EAST, casing).put(TextureSlot.WEST, casing).put(TextureSlot.PARTICLE, casing), states.modelOutput());
        states.blockStateOutput().accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model))
                .with(BlockModelGenerators.ROTATIONS_COLUMN_WITH_FACING));
        states.blockItem(block);
    }
}
