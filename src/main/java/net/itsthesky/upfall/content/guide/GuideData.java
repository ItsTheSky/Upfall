package net.itsthesky.upfall.content.guide;

import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.datagen.ModFrenchLanguageProvider;
import net.itsthesky.upfall.datagen.ModItemModelProvider;
import net.itsthesky.upfall.datagen.ModLanguageProvider;
import net.itsthesky.upfall.datagen.RecipeContext;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

final class GuideData implements DataContribution {
    @Override
    public void lang(ModLanguageProvider lang) {
        lang.add(GuideItems.SKY_ALMANAC.get(), "Sky Almanac");
        lang.add("item.upfall.sky_almanac.desc", "A field guide to The Upfall. Use it to read.");
        lang.add("message.upfall.sky_almanac.missing_guideme", "Install GuideME to read the Sky Almanac.");
    }

    @Override
    public void langFr(ModFrenchLanguageProvider lang) {
        lang.add("item.upfall.sky_almanac", "Almanach céleste");
        lang.add("item.upfall.sky_almanac.desc", "Un guide de terrain de l'Upfall. Utilisez-le pour le lire.");
        lang.add("message.upfall.sky_almanac.missing_guideme", "Installez GuideME pour lire l'Almanach céleste.");
    }

    @Override
    public void itemModels(ModItemModelProvider models) {
        models.basicItem(GuideItems.SKY_ALMANAC.get());
    }

    @Override
    public void recipes(RecipeContext recipes) {
        recipes.shapeless(RecipeCategory.MISC, GuideItems.SKY_ALMANAC.get())
                .requires(Items.BOOK).requires(Items.FEATHER)
                .unlockedBy(RecipeContext.hasName(Items.BOOK), recipes.has(Items.BOOK))
                .save(recipes.output().withConditions(new ModLoadedCondition(GuideModule.GUIDEME_ID)));
    }
}
