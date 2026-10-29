package net.itsthesky.upfall.content.reliquary;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.reliquary.worldgen.ReliquaryPlacement;
import net.itsthesky.upfall.content.reliquary.worldgen.TiltedReliquaryStructure;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.datagen.DataGenContext;
import net.itsthesky.upfall.worldgen.ModBiomes;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

final class ReliquaryWorldgenData implements DataContribution {
    @Override
    public void structures(BootstrapContext<Structure> context) {
        context.register(ReliquaryStructures.TILTED_RELIQUARY, new TiltedReliquaryStructure(
                new Structure.StructureSettings.Builder(context.lookup(Registries.BIOME).getOrThrow(ReliquaryStructures.HAS_TILTED_RELIQUARY))
                        .generationStep(GenerationStep.Decoration.SURFACE_STRUCTURES)
                        .terrainAdapation(TerrainAdjustment.NONE)
                        .spawnOverrides(noSpawns())
                        .build()));
    }

    private static Map<MobCategory, StructureSpawnOverride> noSpawns() {
        Map<MobCategory, StructureSpawnOverride> overrides = new EnumMap<>(MobCategory.class);
        for (MobCategory category : MobCategory.values()) {
            if (category != MobCategory.MISC) {
                //? if >=26.1 {
                /*overrides.put(category, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, net.minecraft.util.random.WeightedList.of()));
                *///?} else {
                overrides.put(category, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, net.minecraft.util.random.WeightedRandomList.create()));
                //?}
            }
        }
        return overrides;
    }

    @Override
    public void structureSets(BootstrapContext<StructureSet> context) {
        context.register(ReliquaryStructures.TILTED_RELIQUARY_SET, new StructureSet(
                context.lookup(Registries.STRUCTURE).getOrThrow(ReliquaryStructures.TILTED_RELIQUARY), new ReliquaryPlacement(1510202618)));
    }

    @Override
    public void addProviders(DataGenContext ctx) {
        ctx.add(ctx.advancements("Tilted Reliquary visit advancement", List.of(ReliquaryWorldgenData::advancements)));
        ctx.<Biome>tags(Registries.BIOME, "Tilted Reliquary Biome Tags", tags -> tags.tag(ReliquaryStructures.HAS_TILTED_RELIQUARY)
                .add(ModBiomes.DRIFTING_ARCHIPELAGO, ModBiomes.MISTSEA, ModBiomes.GILDED_RIDGE));
    }

    private static void advancements(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver) {
        AdvancementHolder enter = AdvancementSubProvider.createPlaceholder("upfall:upfall/enter");
        Advancement.Builder.advancement().parent(enter)
                .display(ReliquaryBlocks.CARVED_LATTICE_STONE.get(), Component.translatable("advancements.upfall.tilted_reliquary.title"),
                        Component.translatable("advancements.upfall.tilted_reliquary.description"), null, AdvancementType.TASK, true, true, false)
                .addCriterion("visited", PlayerTrigger.TriggerInstance.located(LocationPredicate.Builder.inStructure(
                        registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(ReliquaryStructures.TILTED_RELIQUARY))))
                .save(saver, Upfall.id("upfall/tilted_reliquary").toString());
    }
}
