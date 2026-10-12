package net.itsthesky.upfall.content.guide;

import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.datagen.ModFrenchLanguageProvider;
import net.itsthesky.upfall.datagen.ModItemModelProvider;
import net.itsthesky.upfall.datagen.ModLanguageProvider;
import net.itsthesky.upfall.datagen.ModRecipeProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;

final class GuideData implements DataContribution {
    @Override
    public void lang(ModLanguageProvider lang) {
        lang.add(GuideItems.SKY_ALMANAC.get(), "Sky Almanac");
        lang.add("item.upfall.sky_almanac.desc", "A field guide to The Upfall. Use it to read.");
    }

    @Override
    public void langFr(ModFrenchLanguageProvider lang) {
        lang.add("item.upfall.sky_almanac", "Almanach céleste");
        lang.add("item.upfall.sky_almanac.desc", "Un guide de terrain de l'Upfall. Utilisez-le pour le lire.");
    }

    @Override
    public void itemModels(ModItemModelProvider models) {
        models.basicItem(GuideItems.SKY_ALMANAC.get());
    }

    @Override
    public void recipes(RecipeOutput output) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, GuideItems.SKY_ALMANAC.get())
                .requires(Items.BOOK).requires(Items.FEATHER)
                .unlockedBy(ModRecipeProvider.hasName(Items.BOOK), ModRecipeProvider.hasItem(Items.BOOK))
                .save(output);
    }
}
