package net.itsthesky.upfall.content.reliquary;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.reliquary.worldgen.ReliquaryPlacement;
import net.itsthesky.upfall.content.reliquary.worldgen.TiltedReliquaryStructure;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.worldgen.ModBiomes;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

final class ReliquaryWorldgenData implements DataContribution {
    @Override
    public void structures(BootstrapContext<Structure> context) {
        context.register(ReliquaryStructures.TILTED_RELIQUARY, new TiltedReliquaryStructure(
                new Structure.StructureSettings.Builder(context.lookup(Registries.BIOME).getOrThrow(ReliquaryStructures.HAS_TILTED_RELIQUARY))
                        .generationStep(GenerationStep.Decoration.SURFACE_STRUCTURES)
                        .terrainAdapation(TerrainAdjustment.NONE)
                        .build()));
    }

    @Override
    public void structureSets(BootstrapContext<StructureSet> context) {
        context.register(ReliquaryStructures.TILTED_RELIQUARY_SET, new StructureSet(
                context.lookup(Registries.STRUCTURE).getOrThrow(ReliquaryStructures.TILTED_RELIQUARY), new ReliquaryPlacement(1510202618)));
    }

    @Override
    public void addProviders(GatherDataEvent event, DataGenerator generator, PackOutput output,
                             CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper helper) {
        generator.addProvider(event.includeServer(), new Named("Tilted Reliquary visit advancement",
                new AdvancementProvider(output, registries, helper, List.of(ReliquaryWorldgenData::advancements))));
        generator.addProvider(event.includeServer(), new BiomeTagsProvider(output, registries, Upfall.MODID, helper) {
            @Override
            protected void addTags(HolderLookup.Provider provider) {
                tag(ReliquaryStructures.HAS_TILTED_RELIQUARY).add(ModBiomes.DRIFTING_ARCHIPELAGO, ModBiomes.MISTSEA, ModBiomes.GILDED_RIDGE,
                        ModBiomes.HANGING_ROOTS);
            }

            @Override
            public String getName() {
                return "Tilted Reliquary Biome Tags";
            }
        });
    }

    private static void advancements(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper helper) {
        AdvancementHolder enter = AdvancementSubProvider.createPlaceholder("upfall:upfall/enter");
        Advancement.Builder.advancement().parent(enter)
                .display(ReliquaryBlocks.CARVED_LATTICE_STONE.get(), Component.translatable("advancements.upfall.tilted_reliquary.title"),
                        Component.translatable("advancements.upfall.tilted_reliquary.description"), null, AdvancementType.TASK, true, true, false)
                .addCriterion("visited", PlayerTrigger.TriggerInstance.located(LocationPredicate.Builder.inStructure(
                        registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(ReliquaryStructures.TILTED_RELIQUARY))))
                .save(saver, Upfall.id("upfall/tilted_reliquary").toString());
    }

    private record Named(String name, DataProvider delegate) implements DataProvider {
        @Override
        public CompletableFuture<?> run(CachedOutput output) {
            return delegate.run(output);
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
