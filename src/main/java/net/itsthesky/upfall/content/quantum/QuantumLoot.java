package net.itsthesky.upfall.content.quantum;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.rustworks.RustworksItems;
import net.itsthesky.upfall.worldgen.ExplorerMaps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
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

import java.util.function.BiConsumer;

public final class QuantumLoot implements LootTableSubProvider {
    public static final ResourceKey<LootTable> GAS_CAVE_SUPPLY = ResourceKey.create(Registries.LOOT_TABLE, Upfall.id("chests/gas_cave_supply"));
    public static final ResourceKey<LootTable> SHIFTING_VAULT = ResourceKey.create(Registries.LOOT_TABLE, Upfall.id("chests/shifting_vault"));

    public QuantumLoot(HolderLookup.Provider registries) {
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(GAS_CAVE_SUPPLY, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(1, 3))
                        .add(item(QuantumItems.FLICKER_SHARD.get(), 10, 1, 3))
                        .add(item(RustworksItems.KAIRITE_DUST.get(), 10, 2, 5))
                        .add(item(QuantumItems.BOTTLED_VEIL_GAS.get(), 6, 1, 2))
                        .add(EmptyLootItem.emptyItem().setWeight(4)))
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(1, 4))
                        .add(item(Items.TORCH, 10, 2, 6))
                        .add(item(Items.IRON_NUGGET, 8, 2, 9))
                        .add(item(Items.RAIL, 5, 2, 6))
                        .add(item(Items.BREAD, 5, 1, 2))
                        .add(item(Items.STICK, 6, 2, 5))
                        .add(item(Items.GLASS_BOTTLE, 4, 1, 3)))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(ExplorerMaps.loot(ExplorerMaps.STALLED_ENGINE, 10))
                        .add(EmptyLootItem.emptyItem().setWeight(90))));

        output.accept(SHIFTING_VAULT, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(item(QuantumItems.QUANTUM_PEDESTAL_BLUEPRINT.get(), 1, 1, 1)))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(item(QuantumItems.QUANTUM_STONE.get(), 1, 2, 4)))
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(2, 4))
                        .add(item(QuantumItems.FLICKER_SHARD.get(), 10, 3, 8))
                        .add(item(QuantumItems.QUANTUM_STONE.get(), 4, 1, 2))
                        .add(item(QuantumItems.BOTTLED_VEIL_GAS.get(), 5, 1, 3))
                        .add(item(Items.DIAMOND, 3, 1, 3))
                        .add(item(Items.ENDER_PEARL, 4, 1, 3))
                        .add(item(Items.AMETHYST_SHARD, 5, 3, 8))));
    }

    static LootPoolEntryContainer.Builder<?> item(ItemLike item, int weight, int min, int max) {
        return LootItem.lootTableItem(item).setWeight(weight)
                .apply(SetItemCountFunction.setCount(min == max ? ConstantValue.exactly(min) : UniformGenerator.between(min, max)));
    }
}
