package net.itsthesky.upfall.datagen;

import net.itsthesky.upfall.Upfall;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

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
        DataGenContext ctx = new DataGenContext(event, lookup);

        generator.addProvider(event.includeClient(), new ModBlockStateProvider(output, helper));
        generator.addProvider(event.includeClient(), new ModItemModelProvider(output, helper));
        generator.addProvider(event.includeClient(), new ModSoundProvider(output, helper));
        generator.addProvider(event.includeClient(), new ModParticleProvider(output, helper));

        generator.addProvider(event.includeServer(), new ModRecipeProvider(output, lookup));
        ModTagProviders.Blocks blockTags = generator.addProvider(event.includeServer(), new ModTagProviders.Blocks(output, lookup, helper));
        generator.addProvider(event.includeServer(), new ModTagProviders.Items(output, lookup, blockTags.contentsGetter(), helper));

        UpfallDataProviders.register(ctx);
    }
}
