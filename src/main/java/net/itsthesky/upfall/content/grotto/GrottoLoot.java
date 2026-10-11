package net.itsthesky.upfall.content.grotto;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class GrottoLoot implements LootTableSubProvider {
    public static class Provider implements DataProvider {
        private final PackOutput.PathProvider paths;
        private final CompletableFuture<HolderLookup.Provider> registries;

        public Provider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            this.paths = output.createRegistryElementsPathProvider(Registries.LOOT_TABLE);
            this.registries = registries;
        }

        @Override
        public CompletableFuture<?> run(CachedOutput cache) {
            return registries.thenCompose(lookup -> {
                java.util.List<CompletableFuture<?>> saves = new java.util.ArrayList<>();
                new GrottoLoot(lookup).generate((key, builder) -> {
                    LootTable table = builder.setParamSet(LootContextParamSets.CHEST).build();
                    saves.add(DataProvider.saveStable(cache, lookup, LootTable.DIRECT_CODEC, table, paths.json(key.location())));
                });
                return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
            });
        }

        @Override
        public String getName() {
            return "Grotto Loot Tables";
        }
    }

    public static final ResourceKey<LootTable> CRYSTAL_GROTTO = ResourceKey.create(Registries.LOOT_TABLE, Upfall.id("chests/crystal_grotto"));

    public GrottoLoot(HolderLookup.Provider registries) {
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(CRYSTAL_GROTTO, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(item(GrottoBlocks.MISTGLOW_DUST, 1, 6, 12)))
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(1, 2))
                        .add(item(GrottoBlocks.LUMEN_SHARD, 1, 3, 8)))
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(3, 5))
                        .add(item(ModBlocks.MISTGLOW_CRYSTAL.get(), 8, 1, 3))
                        .add(item(GrottoBlocks.MISTGLOW_LANTERN.get(), 5, 1, 2))
                        .add(item(ModItems.NIMBUSITE_INGOT.get(), 9, 1, 3))
                        .add(item(ModItems.LEVITITE_SHARD.get(), 6, 1, 3))
                        .add(item(ModItems.CLOUDBERRY_TART.get(), 6, 1, 3))
                        .add(item(ModItems.GILDFRUIT.get(), 6, 1, 3))
                        .add(item(Items.GOLD_INGOT, 6, 2, 5))
                        .add(item(Items.DIAMOND, 5, 1, 3))
                        .add(item(Items.AMETHYST_SHARD, 5, 3, 8))
                        .add(item(GrottoBlocks.AZURE.cluster().get(), 3, 1, 1))
                        .add(item(GrottoBlocks.ROSE.cluster().get(), 3, 1, 1))
                        .add(item(GrottoBlocks.VERDANT.cluster().get(), 3, 1, 1)))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(item(Items.ECHO_SHARD, 10, 1, 2))
                        .add(item(ModItems.LEVITITE_INGOT.get(), 4, 1, 1))
                        .add(EmptyLootItem.emptyItem().setWeight(86))));
    }

    private static LootPoolEntryContainer.Builder<?> item(ItemLike item, int weight, int min, int max) {
        return LootItem.lootTableItem(item).setWeight(weight)
                .apply(SetItemCountFunction.setCount(min == max ? ConstantValue.exactly(min) : UniformGenerator.between(min, max)));
    }
}
