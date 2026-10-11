package net.itsthesky.upfall.content.machines;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.machines.pipe.PipeData;
import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.itsthesky.upfall.content.rustworks.RustworksItems;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.itsthesky.upfall.datagen.ModItemModelProvider;
import net.itsthesky.upfall.datagen.ModLanguageProvider;
import net.itsthesky.upfall.datagen.ModRecipeProvider;
import net.itsthesky.upfall.datagen.ModSoundProvider;
import net.itsthesky.upfall.datagen.ModTagProviders;
import net.itsthesky.upfall.item.UpfallSmithingTemplates;
import net.minecraft.Util;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.SmithingTemplateItem;
import net.neoforged.neoforge.registries.DeferredItem;

final class MachinesData implements DataContribution {
    @Override
    public void lang(ModLanguageProvider lang) {
        WatchLiftData.lang(lang);
        ClockworkData.lang(lang);
        PipeData.lang(lang);
        for (DeferredItem<SmithingTemplateItem> blueprint : MachinesItems.blueprints()) {
            String upgrade = blueprint.getId().getPath();
            String machine = ModLanguageProvider.name(upgrade.substring(0, upgrade.length() - "_blueprint".length()));
            lang.add(Util.makeDescriptionId("upgrade", Upfall.id(upgrade)), machine);
            lang.add(UpfallSmithingTemplates.key("applies_to", upgrade), MachinesRecipes.APPLIES_TO.get(upgrade));
            lang.add(UpfallSmithingTemplates.key("ingredients", upgrade), "Kairite Gear");
            lang.add(UpfallSmithingTemplates.key("base_slot_description", upgrade), "Add the machine base");
            lang.add(UpfallSmithingTemplates.key("additions_slot_description", upgrade), "Add a Kairite Gear");
        }
    }

    @Override
    public void blockStates(ModBlockStateProvider states) {
        MachinesBlocks.all().stream().filter(block -> block != MachinesBlocks.GEARLIFT && !ClockworkData.owns(block.get()) && !PipeData.owns(block.get()))
                .forEach(block -> states.simpleBlockWithItem(block.get(), states.cubeAll(block.get())));
        WatchLiftData.blockStates(states);
        ClockworkData.blockStates(states);
        PipeData.blockStates(states);
    }

    @Override
    public void itemModels(ModItemModelProvider models) {
        WatchLiftData.itemModels(models);
        MachinesItems.blueprints().forEach(blueprint -> models.basicItem(blueprint.get()));
    }

    @Override
    public void recipes(RecipeOutput output) {
        MachinesRecipes.register(output);
        for (DeferredItem<SmithingTemplateItem> blueprint : MachinesItems.blueprints()) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, blueprint.get(), 2)
                    .define('#', RustworksItems.KAIRITE_DUST.get()).define('C', RustworksBlocks.RUSTSLATE.get()).define('S', blueprint.get())
                    .pattern("#S#").pattern("#C#").pattern("###")
                    .unlockedBy(ModRecipeProvider.hasName(blueprint.get()), ModRecipeProvider.hasItem(blueprint.get()))
                    .save(output);
        }
    }

    @Override
    public void sounds(ModSoundProvider sounds) {
        PipeData.sounds(sounds);
    }

    @Override
    public void blockTags(ModTagProviders.Blocks tags) {
        var pickaxe = tags.tag(BlockTags.MINEABLE_WITH_PICKAXE);
        MachinesBlocks.all().forEach(block -> pickaxe.add(block.get()));
    }
}
