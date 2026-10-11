package net.itsthesky.upfall.datagen;

import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModItems;
import net.itsthesky.upfall.worldgen.ModLootTables;
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
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.BiConsumer;

public class ModChestLootProvider implements LootTableSubProvider {
    public ModChestLootProvider(HolderLookup.Provider registries) {
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(ModLootTables.DRIFTING_RUINS, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(3, 6))
                        .add(item(ModItems.GILDFRUIT.get(), 12, 1, 3))
                        .add(item(ModItems.CLOUDBERRY.get(), 12, 2, 6))
                        .add(item(ModItems.CLOUDBERRY_TART.get(), 4, 1, 1))
                        .add(item(ModItems.RAW_NIMBUSITE.get(), 10, 1, 4))
                        .add(item(ModItems.NIMBUSITE_INGOT.get(), 4, 1, 2))
                        .add(item(ModBlocks.SKYREACH.sapling().get(), 6, 1, 2))
                        .add(item(ModBlocks.MISTGLOW_CRYSTAL.get(), 6, 1, 3))
                        .add(item(ModBlocks.DENSE_CLOUD.get(), 6, 2, 6))
                        .add(item(Items.GOLD_NUGGET, 8, 3, 9))
                        .add(item(Items.FEATHER, 6, 2, 5)))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(item(ModItems.NIMBUSITE_UPGRADE_SMITHING_TEMPLATE.get(), 15, 1, 1))
                        .add(EmptyLootItem.emptyItem().setWeight(85))));

        output.accept(ModLootTables.INVERTED_OBSERVATORY, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(item(ModItems.LEVITITE_UPGRADE_SMITHING_TEMPLATE.get(), 1, 1, 1)))
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(4, 7))
                        .add(item(ModItems.NIMBUSITE_INGOT.get(), 10, 2, 5))
                        .add(item(ModItems.LEVITITE_SHARD.get(), 5, 1, 3))
                        .add(item(ModItems.GILDFRUIT.get(), 10, 2, 4))
                        .add(item(ModItems.CLOUDBERRY_TART.get(), 8, 1, 2))
                        .add(item(ModBlocks.MISTGLOW_CRYSTAL.get(), 8, 2, 5))
                        .add(item(ModItems.UPDRAFT_COMPASS.get(), 3, 1, 1))
                        .add(item(ModItems.NIMBUSITE_UPGRADE_SMITHING_TEMPLATE.get(), 4, 1, 1))
                        .add(item(Items.GOLD_INGOT, 8, 2, 6))
                        .add(item(Items.DIAMOND, 4, 1, 3))
                        .add(item(Items.ECHO_SHARD, 2, 1, 2))));
    }

    private static LootPoolEntryContainer.Builder<?> item(ItemLike item, int weight, int min, int max) {
        return LootItem.lootTableItem(item).setWeight(weight)
                .apply(SetItemCountFunction.setCount(min == max ? ConstantValue.exactly(min) : UniformGenerator.between(min, max)));
    }
}
