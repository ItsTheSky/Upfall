package net.itsthesky.upfall.content.chronospire.data;

import net.itsthesky.upfall.content.chronospire.ChronoBlocks;
import net.itsthesky.upfall.content.chronospire.ChronoRegistries;
import net.itsthesky.upfall.content.chronospire.worldgen.ChronospireLoot;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModItems;
import net.itsthesky.upfall.worldgen.ExplorerMaps;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.BiConsumer;

public class ChronospireLootProvider implements LootTableSubProvider {
    private final HolderLookup.Provider registries;

    public ChronospireLootProvider(HolderLookup.Provider registries) {
        this.registries = registries;
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(ChronospireLoot.VAULT, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(item(ChronoRegistries.REWINDING_HOURGLASS.get(), 1, 1, 1)))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(item(ModItems.LEVITITE_UPGRADE_SMITHING_TEMPLATE.get(), 3, 1, 1))
                        .add(EmptyLootItem.emptyItem().setWeight(2)))
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(1, 2))
                        .add(LootItem.lootTableItem(Items.BOOK)
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(registries, UniformGenerator.between(30, 39)))))
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(4, 6))
                        .add(item(ModItems.LEVITITE_SHARD.get(), 10, 2, 4))
                        .add(item(ModItems.LEVITITE_INGOT.get(), 3, 1, 1))
                        .add(item(ModItems.NIMBUSITE_INGOT.get(), 10, 3, 6))
                        .add(item(Items.DIAMOND, 6, 2, 5))
                        .add(item(ModItems.GILDFRUIT.get(), 5, 2, 4))
                        .add(item(ModItems.CLOUDBERRY_TART.get(), 5, 1, 3))
                        .add(item(ModBlocks.MISTGLOW_CRYSTAL.get(), 4, 2, 4))
                        .add(item(Items.ECHO_SHARD, 3, 1, 2))
                        .add(item(ChronoBlocks.PHASE_LANTERN.get(), 4, 2, 4))
                        .add(item(ChronoBlocks.HOUR_DIAL.get(), 2, 1, 1)))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(ExplorerMaps.loot(ExplorerMaps.TILTED_RELIQUARY, 25))
                        .add(EmptyLootItem.emptyItem().setWeight(75))));
    }

    private static LootPoolEntryContainer.Builder<?> item(ItemLike item, int weight, int min, int max) {
        return LootItem.lootTableItem(item).setWeight(weight)
                .apply(SetItemCountFunction.setCount(min == max ? ConstantValue.exactly(min) : UniformGenerator.between(min, max)));
    }
}
