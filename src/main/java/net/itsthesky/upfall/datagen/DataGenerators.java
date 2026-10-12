package net.itsthesky.upfall.datagen;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.UpfallModules;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = Upfall.MODID)
public final class DataGenerators {
    private DataGenerators() {}

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper helper = event.getExistingFileHelper();
        ModWorldgenProvider worldgen = generator.addProvider(event.includeServer(), new ModWorldgenProvider(output, event.getLookupProvider()));
        CompletableFuture<HolderLookup.Provider> lookup = worldgen.getRegistryProvider();

        generator.addProvider(event.includeClient(), new ModBlockStateProvider(output, helper));
        generator.addProvider(event.includeClient(), new ModItemModelProvider(output, helper));
        generator.addProvider(event.includeClient(), new ModLanguageProvider(output));
        generator.addProvider(event.includeClient(), new ModFrenchLanguageProvider(output));
        generator.addProvider(event.includeClient(), new ModSoundProvider(output, helper));
        generator.addProvider(event.includeClient(), new ModParticleProvider(output, helper));

        generator.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(ModBlockLootProvider::new, LootContextParamSets.BLOCK),
                        new LootTableProvider.SubProviderEntry(ModChestLootProvider::new, LootContextParamSets.CHEST)), lookup));
        generator.addProvider(event.includeServer(), new AdvancementProvider(output, lookup, helper, List.of(new ModAdvancementGenerator())));
        generator.addProvider(event.includeServer(), new ModRecipeProvider(output, lookup));
        generator.addProvider(event.includeServer(), new ModTestStructureProvider(output));
        generator.addProvider(event.includeServer(), new ModDataMapProvider(output, lookup));
        UpfallModules.forEachData(data -> data.addProviders(event, generator, output, lookup, helper));
        ModTagProviders.Blocks blockTags = generator.addProvider(event.includeServer(), new ModTagProviders.Blocks(output, lookup, helper));
        generator.addProvider(event.includeServer(), new ModTagProviders.Items(output, lookup, blockTags.contentsGetter(), helper));
    }
}
