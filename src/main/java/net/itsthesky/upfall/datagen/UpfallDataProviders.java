package net.itsthesky.upfall.datagen;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.UpfallModules;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;

final class UpfallDataProviders {
    private UpfallDataProviders() {}

    static void register(DataGenContext ctx) {
        ctx.addClient(new ModLanguageProvider(ctx.output()));
        ctx.addClient(new ModFrenchLanguageProvider(ctx.output()));

        ctx.add(ctx.lootTables("Loot Tables", List.of(
                new LootTableProvider.SubProviderEntry(ModBlockLootProvider::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(ModChestLootProvider::new, LootContextParamSets.CHEST))));
        ctx.add(ctx.advancements("Advancements", List.of(new ModAdvancementGenerator())));
        ctx.add(new ModTestStructureProvider(ctx.output()));
        ctx.add(new ModDataMapProvider(ctx.output(), ctx.registries()));
        UpfallModules.forEachData(data -> data.addProviders(ctx));
        ctx.tags(Registries.STRUCTURE, "Tags for minecraft:worldgen/structure mod id " + Upfall.MODID, ModTagProviders::structures);
    }
}
