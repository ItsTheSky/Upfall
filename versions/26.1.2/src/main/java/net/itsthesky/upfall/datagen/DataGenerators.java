package net.itsthesky.upfall.datagen;

import net.itsthesky.upfall.Upfall;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.EquipmentAssetProvider;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@EventBusSubscriber(modid = Upfall.MODID, value = Dist.CLIENT)
public final class DataGenerators {
    private DataGenerators() {}

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        PackOutput output = event.getGenerator().getPackOutput();
        ModWorldgenProvider worldgen = event.addProvider(new ModWorldgenProvider(output, event.getLookupProvider()));
        CompletableFuture<HolderLookup.Provider> lookup = worldgen.getRegistryProvider();
        DataGenContext ctx = new DataGenContext(event, lookup);

        ctx.addClient(new Models(output));
        ctx.addClient(new Equipment(output));
        ctx.addClient(new ModSoundProvider(output));
        ctx.addClient(new ModParticleProvider(output));

        ctx.add(new ModRecipeProvider.Runner(output, lookup));
        ModTagProviders.Blocks blockTags = ctx.add(new ModTagProviders.Blocks(output, lookup));
        ctx.add(new ModTagProviders.Items(output, lookup, blockTags.contentsGetter()));

        UpfallDataProviders.register(ctx);
    }

    private static final class Models extends ModelProvider {
        Models(PackOutput output) {
            super(output, Upfall.MODID);
        }

        @Override
        protected void registerModels(BlockModelGenerators blocks, ItemModelGenerators items) {
            new ModBlockStateProvider(blocks, items).register();
            new ModItemModelProvider(blocks, items).register();
        }
    }

    private static final class Equipment extends EquipmentAssetProvider {
        Equipment(PackOutput output) {
            super(output);
        }

        @Override
        protected void registerModels(BiConsumer<ResourceKey<EquipmentAsset>, EquipmentClientInfo> output) {
            for (String name : List.of("nimbusite", "levitite")) {
                output.accept(ResourceKey.create(EquipmentAssets.ROOT_ID, Upfall.id(name)),
                        EquipmentClientInfo.builder().addHumanoidLayers(Upfall.id(name)).build());
            }
            output.accept(ResourceKey.create(EquipmentAssets.ROOT_ID, Upfall.id("veil_mask")),
                    EquipmentClientInfo.builder().addMainHumanoidLayer(Upfall.id("veil_mask"), false).build());
        }

        @Override
        public String getName() {
            return "Upfall Equipment Assets";
        }
    }
}
