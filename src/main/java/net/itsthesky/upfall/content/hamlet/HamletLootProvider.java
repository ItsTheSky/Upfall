package net.itsthesky.upfall.content.hamlet;

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
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.BiConsumer;

final class HamletLootProvider implements LootTableSubProvider {
    HamletLootProvider(HolderLookup.Provider registries) {
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(HamletLoot.HOME, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(3, 6))
                        .add(item(ModItems.CLOUDBERRY.get(), 12, 2, 6))
                        .add(item(ModItems.CLOUDBERRY_TART.get(), 6, 1, 2))
                        .add(item(ModItems.GILDFRUIT.get(), 8, 1, 3))
                        .add(item(HamletBlocks.ROPE_ITEM.get(), 12, 4, 12))
                        .add(item(HamletBlocks.CLOUD_LANTERN.get(), 6, 1, 2))
                        .add(item(HamletBlocks.WOVEN_PLANKS.get(), 6, 4, 10))
                        .add(item(HamletBlocks.DUSK_CANOPY.block().get(), 4, 2, 6))
                        .add(item(HamletBlocks.SKY_CANOPY.block().get(), 4, 2, 6))
                        .add(item(ModItems.RAW_NIMBUSITE.get(), 6, 1, 3))
                        .add(item(ModBlocks.MISTGLOW_CRYSTAL.get(), 4, 1, 2))
                        .add(item(Items.STRING, 6, 2, 6))
                        .add(item(Items.FEATHER, 4, 2, 5)))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(item(HamletItems.ZIPLINE_HANDLE.get(), 15, 1, 1))
                        .add(EmptyLootItem.emptyItem().setWeight(85)))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(ExplorerMaps.loot(ExplorerMaps.PEARL_AERIE, 8))
                        .add(EmptyLootItem.emptyItem().setWeight(92))));

        output.accept(HamletLoot.WORKSHOP, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(item(HamletItems.ZIPLINE_HANDLE.get(), 1, 1, 1)))
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(3, 5))
                        .add(item(HamletBlocks.ROPE_ITEM.get(), 12, 6, 16))
                        .add(item(HamletBlocks.ROPE_ANCHOR.get(), 8, 2, 4))
                        .add(item(HamletBlocks.ZIPLINE_ANCHOR.get(), 4, 1, 2))
                        .add(item(HamletBlocks.ROPE_BRIDGE.get(), 8, 3, 8))
                        .add(item(HamletBlocks.WOVEN_PLANKS.get(), 6, 4, 12))
                        .add(item(ModItems.NIMBUSITE_INGOT.get(), 4, 1, 2))
                        .add(item(ModItems.RAW_NIMBUSITE.get(), 6, 1, 4))
                        .add(item(Items.STRING, 8, 3, 9))
                        .add(item(HamletBlocks.ROPE_SPOOL.get(), 4, 1, 1))));
    }

    private static LootPoolEntryContainer.Builder<?> item(ItemLike item, int weight, int min, int max) {
        return LootItem.lootTableItem(item).setWeight(weight)
                .apply(SetItemCountFunction.setCount(min == max ? ConstantValue.exactly(min) : UniformGenerator.between(min, max)));
    }
}
