package net.itsthesky.upfall.content.rustworks;

import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.itsthesky.upfall.datagen.ModSoundProvider;
import net.itsthesky.upfall.datagen.RecipeContext;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

final class RustworksVentData {
    private RustworksVentData() {}

    static void blockStates(ModBlockStateProvider states) {
        RustworksModels.vent(states);
    }

    static void recipes(RecipeContext recipes) {
        Item plate = RustworksItems.RUSTY_PLATE.get();
        recipes.shaped(RecipeCategory.DECORATIONS, RustworksBlocks.EXHAUST_VENT.get())
                .define('#', plate).define('B', Items.IRON_BARS).define('P', RustworksBlocks.RUSTY_PIPE.get()).define('F', Items.FURNACE)
                .pattern("#B#").pattern("#P#").pattern("#F#")
                .unlockedBy(RecipeContext.hasName(plate), recipes.has(plate))
                .save(recipes.output());
    }

    static void sounds(ModSoundProvider sounds) {
        sounds.addSound(RustworksSounds.EXHAUST_VENT_HISS, ModSoundProvider.def(RustworksSounds.EXHAUST_VENT_HISS)
                .with(ModSoundProvider.event("block.fire.extinguish").pitch(0.6).volume(0.9).weight(2),
                        ModSoundProvider.event("entity.breeze.wind_burst").pitch(0.7).volume(0.6),
                        ModSoundProvider.event("block.lava.extinguish").pitch(0.5).volume(0.7)));
    }
}
