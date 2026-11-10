package net.itsthesky.upfall.content.gliding;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.mobs.MobItems;
import net.itsthesky.upfall.content.world.WorldItems;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.datagen.ModFrenchLanguageProvider;
import net.itsthesky.upfall.datagen.ModItemModelProvider;
import net.itsthesky.upfall.datagen.ModLanguageProvider;
import net.itsthesky.upfall.datagen.ModTagProviders;
import net.itsthesky.upfall.datagen.RecipeContext;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
//? if >=26.1 {
/*import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.renderer.item.properties.select.DisplayContext;
import net.minecraft.world.item.ItemDisplayContext;
*///?}

final class GlidingData implements DataContribution {
    @Override
    public void lang(ModLanguageProvider lang) {
        lang.add("item.upfall.down_glider.desc", "Hold it while falling to glide down slowly, 1.5x faster than sprinting. No fall damage while held.");
        lang.add("item.upfall.dusk_glider.desc", "Hold it while falling to glide down slowly, 2x faster than sprinting. No fall damage while held.");
    }

    @Override
    public void langFr(ModFrenchLanguageProvider lang) {
        lang.add("item.upfall.down_glider", "Planeur de duvet");
        lang.add("item.upfall.down_glider.desc", "Tenez-le en tombant pour planer lentement, 1,5x plus vite qu'en sprintant. Aucun dégât de chute tant qu'il est en main.");
        lang.add("item.upfall.dusk_glider", "Planeur crépusculaire");
        lang.add("item.upfall.dusk_glider.desc", "Tenez-le en tombant pour planer lentement, 2x plus vite qu'en sprintant. Aucun dégât de chute tant qu'il est en main.");
    }

    //? if >=26.1 {
    /*@Override
    public void itemModels(ModItemModelProvider models) {
        for (var glider : java.util.List.of(GliderItems.DOWN_GLIDER, GliderItems.DUSK_GLIDER)) {
            var icon = ItemModelUtils.plainModel(models.itemModels().createFlatItemModel(glider.get(), ModelTemplates.FLAT_ITEM));
            var inHand = ItemModelUtils.plainModel(Upfall.id("item/" + glider.getId().getPath() + "_3d"));
            models.blockModels().itemModelOutput.accept(glider.get(), ItemModelUtils.select(new DisplayContext(), inHand,
                    ItemModelUtils.when(java.util.List.of(ItemDisplayContext.GUI, ItemDisplayContext.FIXED), icon)));
        }
    }
    *///?}

    @Override
    public void itemTags(ModTagProviders.Items tags) {
        tags.tag(GliderItems.REPAIRS_DOWN_GLIDER).add(MobItems.DOWN_TUFT.get());
        tags.tag(GliderItems.REPAIRS_DUSK_GLIDER).add(MobItems.DUSKWING_PLUME.get());
        tags.tag(ItemTags.DURABILITY_ENCHANTABLE).add(GliderItems.DOWN_GLIDER.get(), GliderItems.DUSK_GLIDER.get());
    }

    @Override
    public void recipes(RecipeContext recipes) {
        recipes.save(recipes.shaped(RecipeCategory.TOOLS, GliderItems.DOWN_GLIDER.get())
                .define('S', Items.STICK).define('T', MobItems.DOWN_TUFT.get()).define('F', MobItems.CRAGMAW_FANG.get())
                .pattern("STS").pattern("TFT").pattern("STS")
                .unlockedBy(RecipeContext.hasName(MobItems.DOWN_TUFT.get()), recipes.has(MobItems.DOWN_TUFT.get())), Upfall.id("down_glider"));

        recipes.save(recipes.shaped(RecipeCategory.TOOLS, GliderItems.DUSK_GLIDER.get())
                .define('P', WorldItems.CLOUD_PEARL.get()).define('M', MobItems.MIST_GEL.get())
                .define('D', MobItems.DUSKWING_PLUME.get()).define('G', GliderItems.DOWN_GLIDER.get())
                .pattern("PDM").pattern("DGD").pattern("MDP")
                .unlockedBy(RecipeContext.hasName(GliderItems.DOWN_GLIDER.get()), recipes.has(GliderItems.DOWN_GLIDER.get())), Upfall.id("dusk_glider"));
    }
}
