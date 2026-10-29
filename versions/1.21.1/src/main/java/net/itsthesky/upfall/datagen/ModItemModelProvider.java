package net.itsthesky.upfall.datagen;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.UpfallModules;
import net.itsthesky.upfall.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;

public class ModItemModelProvider extends ItemModelProvider {
    public static final int COMPASS_FRAMES = 32;

    public ModItemModelProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, Upfall.MODID, helper);
    }

    @Override
    protected void registerModels() {
        List<DeferredItem<? extends Item>> handheld = List.of(
                ModItems.NIMBUSITE_SWORD, ModItems.NIMBUSITE_PICKAXE, ModItems.NIMBUSITE_AXE, ModItems.NIMBUSITE_SHOVEL, ModItems.NIMBUSITE_HOE,
                ModItems.LEVITITE_SWORD, ModItems.LEVITITE_PICKAXE, ModItems.LEVITITE_AXE, ModItems.LEVITITE_SHOVEL, ModItems.LEVITITE_HOE);
        for (DeferredItem<? extends Item> item : ModItems.CREATIVE_ORDER) {
            if (item == ModItems.UPDRAFT_COMPASS) {
                compass();
            } else if (item == ModItems.SKYRAY_SPAWN_EGG) {
                withExistingParent(item.getId().getPath(), mcLoc("item/template_spawn_egg"));
            } else if (handheld.contains(item)) {
                handheldItem(item.get());
            } else {
                basicItem(item.get());
            }
        }
        UpfallModules.forEachData(data -> data.itemModels(this));
    }

    private void compass() {
        String name = BuiltInRegistries.ITEM.getKey(ModItems.UPDRAFT_COMPASS.get()).getPath();
        ItemModelBuilder root = withExistingParent(name, mcLoc("item/generated")).texture("layer0", modLoc("item/" + name + "_00"));
        for (int i = 0; i < COMPASS_FRAMES; i++) {
            String frame = String.format("%s_%02d", name, i);
            withExistingParent(frame, mcLoc("item/generated")).texture("layer0", modLoc("item/" + frame));
        }
        for (int i = 0; i <= COMPASS_FRAMES; i++) {
            float threshold = i == 0 ? 0.0F : (i - 0.5F) / COMPASS_FRAMES;
            String frame = String.format("%s_%02d", name, i % COMPASS_FRAMES);
            root.override().predicate(mcLoc("angle"), threshold).model(new ModelFile.UncheckedModelFile(modLoc("item/" + frame))).end();
        }
    }
}
