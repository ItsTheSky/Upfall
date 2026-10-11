package net.itsthesky.upfall.content.rustworks;

import net.itsthesky.upfall.content.rustworks.block.ExhaustVentBlock;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.itsthesky.upfall.datagen.ModRecipeProvider;
import net.itsthesky.upfall.datagen.ModSoundProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;

final class RustworksVentData {
    private RustworksVentData() {}

    static void blockStates(ModBlockStateProvider states) {
        ModelFile idle = states.models().cubeBottomTop("exhaust_vent", states.blockTexture("exhaust_vent_side"),
                states.blockTexture("rusty_plating"), states.blockTexture("exhaust_vent_top"));
        ModelFile active = states.models().cubeBottomTop("exhaust_vent_active", states.blockTexture("exhaust_vent_side_active"),
                states.blockTexture("rusty_plating"), states.blockTexture("exhaust_vent_top_active"));
        states.getVariantBuilder(RustworksBlocks.EXHAUST_VENT.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(ExhaustVentBlock.ACTIVE) ? active : idle).build());
        states.blockItem(RustworksBlocks.EXHAUST_VENT.get());
    }

    static void recipes(RecipeOutput output) {
        Item plate = RustworksItems.RUSTY_PLATE.get();
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, RustworksBlocks.EXHAUST_VENT.get())
                .define('#', plate).define('B', Items.IRON_BARS).define('P', RustworksBlocks.RUSTY_PIPE.get()).define('F', Items.FURNACE)
                .pattern("#B#").pattern("#P#").pattern("#F#")
                .unlockedBy(ModRecipeProvider.hasName(plate), ModRecipeProvider.hasItem(plate))
                .save(output);
    }

    static void sounds(ModSoundProvider sounds) {
        sounds.addSound(RustworksSounds.EXHAUST_VENT_HISS, ModSoundProvider.def(RustworksSounds.EXHAUST_VENT_HISS)
                .with(ModSoundProvider.event("block.fire.extinguish").pitch(0.6).volume(0.9).weight(2),
                        ModSoundProvider.event("entity.breeze.wind_burst").pitch(0.7).volume(0.6),
                        ModSoundProvider.event("block.lava.extinguish").pitch(0.5).volume(0.7)));
    }
}
