package net.itsthesky.upfall.content.quantum;

import net.itsthesky.upfall.content.machines.ModelBoxes;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.itsthesky.upfall.datagen.ModItemModelProvider;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;

import java.util.Map;

final class QuantumModels {
    private QuantumModels() {}

    static void blockStates(ModBlockStateProvider states) {
        states.translucentCube(QuantumBlocks.VEIL_GAS, false);
        floorDeco(states, QuantumBlocks.ABANDONED_PICKAXE.get());
        floorDeco(states, QuantumBlocks.CRACKED_VEIL_MASK.get());
        floorDeco(states, QuantumBlocks.SCATTERED_BONES.get());
        TextureMapping lanternTexture = new TextureMapping().put(TextureSlot.LANTERN, states.blockMaterial("spent_lantern"));
        Identifier lantern = ModelTemplates.LANTERN.create(states.modLoc("block/spent_lantern"), lanternTexture, states.modelOutput());
        Identifier hanging = ModelTemplates.HANGING_LANTERN.create(states.modLoc("block/spent_lantern_hanging"), lanternTexture, states.modelOutput());
        states.blockStateOutput().accept(MultiVariantGenerator.dispatch(QuantumBlocks.SPENT_LANTERN.get()).with(PropertyDispatch.initial(LanternBlock.HANGING)
                .select(true, BlockModelGenerators.plainVariant(hanging))
                .select(false, BlockModelGenerators.plainVariant(lantern))));
        Material clusterTexture = states.blockMaterial("flicker_shard_cluster");
        Identifier cluster = ModelTemplates.CROSS.create(states.modLoc("block/flicker_shard_cluster"), TextureMapping.cross(clusterTexture), states.modelOutput());
        Block clusterBlock = QuantumBlocks.FLICKER_SHARD_CLUSTER.get();
        states.blockStateOutput().accept(MultiVariantGenerator.dispatch(clusterBlock, BlockModelGenerators.plainVariant(cluster))
                .with(BlockModelGenerators.ROTATIONS_COLUMN_WITH_FACING));
        flatBlockItem(states, clusterBlock, "flicker_shard_cluster");
        QuantumBlockModels.register(states);
    }

    private static void floorDeco(ModBlockStateProvider states, Block block) {
        String name = ModBlockStateProvider.name(block);
        Identifier model = ModelBoxes.create(states.modelOutput(), states.modLoc("block/" + name), Map.of(
                "deco", states.blockMaterial(name), "particle", states.blockMaterial(name)), QuantumData.floorDecoBoxes());
        states.horizontalBlock(block, model);
        flatBlockItem(states, block, name);
    }

    private static void flatBlockItem(ModBlockStateProvider states, Block block, String name) {
        states.blockModels().registerSimpleItemModel(block, ModelTemplates.FLAT_ITEM.create(states.modLoc("item/" + name),
                TextureMapping.layer0(states.blockMaterial(name)), states.modelOutput()));
    }

    static void spentLanternItem(ModItemModelProvider models) {
        models.blockModels().registerSimpleItemModel(QuantumBlocks.SPENT_LANTERN.get(), models.modLoc("block/spent_lantern"));
    }
}
