package net.itsthesky.upfall.datagen;

import net.itsthesky.upfall.content.UpfallModules;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

import java.util.concurrent.CompletableFuture;

public class ModDataMapProvider extends DataMapProvider {
    public ModDataMapProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        var compostables = builder(NeoForgeDataMaps.COMPOSTABLES);
        for (ModBlocks.WoodSet set : new ModBlocks.WoodSet[]{ModBlocks.SKYREACH, ModBlocks.GILDROOT}) {
            add(compostables, set.leaves().get(), 0.3F);
            add(compostables, set.sapling().get(), 0.3F);
        }
        add(compostables, ModBlocks.DUSKBLOOM.get(), 0.65F);
        add(compostables, ModBlocks.CLOUD_FERN.get(), 0.65F);
        add(compostables, ModBlocks.GILDED_TUFT.get(), 0.65F);
        add(compostables, ModBlocks.GLOWVINE.get(), 0.5F);
        add(compostables, ModItems.CLOUDBERRY.get(), 0.3F);
        add(compostables, ModItems.GILDFRUIT.get(), 0.65F);
        add(compostables, ModItems.CLOUDBERRY_TART.get(), 1.0F);
        UpfallModules.forEachData(data -> data.dataMaps(this));
    }

    private static void add(Builder<Compostable, net.minecraft.world.item.Item> builder, ItemLike item, float chance) {
        builder.add(item.asItem().builtInRegistryHolder(), new Compostable(chance), false);
    }
}
