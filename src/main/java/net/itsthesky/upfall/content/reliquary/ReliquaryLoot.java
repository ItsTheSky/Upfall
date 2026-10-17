package net.itsthesky.upfall.content.reliquary;

import net.itsthesky.upfall.content.enchanting.EnchantingModule;
import net.itsthesky.upfall.content.world.WorldItems;
import net.itsthesky.upfall.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.BiConsumer;

public final class ReliquaryLoot implements LootTableSubProvider {
    private final HolderLookup.Provider registries;

    public ReliquaryLoot(HolderLookup.Provider registries) {
        this.registries = registries;
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(ReliquaryStructures.SATELLITE_LOOT, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(item(ReliquaryItems.FOLD_SHARD.get(), 1, 1, 1)))
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(2, 4))
                        .add(item(ModItems.RAW_NIMBUSITE.get(), 10, 1, 3))
                        .add(item(ModItems.NIMBUSITE_INGOT.get(), 6, 1, 2))
                        .add(item(ModItems.LEVITITE_SHARD.get(), 5, 1, 2))
                        .add(item(WorldItems.CLOUD_PEARL.get(), 8, 1, 3))
                        .add(item(ModItems.GILDFRUIT.get(), 8, 1, 3))
                        .add(item(ModItems.CLOUDBERRY_TART.get(), 5, 1, 2))
                        .add(item(Items.GOLD_NUGGET, 8, 3, 9))
                        .add(item(Items.AMETHYST_SHARD, 6, 2, 6))));

        output.accept(ReliquaryStructures.ARCHIVE_LOOT, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(book(ReliquaryBooks.SURVEYOR_JOURNAL, 5))
                        .add(book(ReliquaryBooks.HOROLOGIST_LETTER, 5))
                        .add(book(ReliquaryBooks.WARDEN_NOTE, 2))
                        .add(EmptyLootItem.emptyItem().setWeight(10)))
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(1, 3))
                        .add(LootItem.lootTableItem(Items.BOOK).setWeight(6)
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(registries, UniformGenerator.between(20, 30))))
                        .add(modEnchantedBook(4))
                        .add(item(Items.PAPER, 10, 4, 10))
                        .add(item(Items.EXPERIENCE_BOTTLE, 8, 2, 5))
                        .add(item(Items.LAPIS_LAZULI, 6, 3, 8))
                        .add(item(Items.COMPASS, 3, 1, 1))
                        .add(item(Items.CLOCK, 3, 1, 1)))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(item(ModItems.NIMBUSITE_UPGRADE_SMITHING_TEMPLATE.get(), 3, 1, 1))
                        .add(item(ModItems.LEVITITE_UPGRADE_SMITHING_TEMPLATE.get(), 1, 1, 1))
                        .add(EmptyLootItem.emptyItem().setWeight(16))));

        output.accept(ReliquaryStructures.CORE_LOOT, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(book(ReliquaryBooks.WARDEN_NOTE, 1)))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(item(ModItems.LEVITITE_UPGRADE_SMITHING_TEMPLATE.get(), 6, 1, 1))
                        .add(item(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 2, 1, 1))
                        .add(item(ModItems.UPDRAFT_COMPASS.get(), 4, 1, 1)))
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(5, 8))
                        .add(item(Items.DIAMOND, 8, 2, 5))
                        .add(item(ModItems.LEVITITE_INGOT.get(), 8, 1, 3))
                        .add(item(ModItems.NIMBUSITE_INGOT.get(), 8, 2, 5))
                        .add(item(Items.ECHO_SHARD, 5, 1, 2))
                        .add(item(WorldItems.CLOUD_PEARL.get(), 8, 2, 5))
                        .add(item(Items.ENDER_PEARL, 4, 1, 3))
                        .add(item(Items.EXPERIENCE_BOTTLE, 6, 4, 10))
                        .add(item(Items.GOLD_INGOT, 6, 3, 8))
                        .add(LootItem.lootTableItem(Items.BOOK).setWeight(6)
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(registries, UniformGenerator.between(28, 38))))
                        .add(modEnchantedBook(6))));
    }

    private LootPoolEntryContainer.Builder<?> modEnchantedBook(int weight) {
        HolderLookup.RegistryLookup<Enchantment> lookup = registries.lookupOrThrow(Registries.ENCHANTMENT);
        return LootItem.lootTableItem(Items.ENCHANTED_BOOK).setWeight(weight)
                .apply(new EnchantRandomlyFunction.Builder()
                        .withEnchantment(lookup.getOrThrow(EnchantingModule.SKYWALKER))
                        .withEnchantment(lookup.getOrThrow(EnchantingModule.UPLIFTED))
                        .withEnchantment(lookup.getOrThrow(EnchantingModule.CLOUDSIGHT))
                        .withEnchantment(lookup.getOrThrow(EnchantingModule.JUMPSTART)));
    }

    private static LootPoolEntryContainer.Builder<?> book(ReliquaryBooks.Book book, int weight) {
        return LootItem.lootTableItem(Items.WRITTEN_BOOK).setWeight(weight)
                .apply(SetComponentsFunction.setComponent(DataComponents.WRITTEN_BOOK_CONTENT, book.content()));
    }

    private static LootPoolEntryContainer.Builder<?> item(ItemLike item, int weight, int min, int max) {
        return LootItem.lootTableItem(item).setWeight(weight)
                .apply(SetItemCountFunction.setCount(min == max ? ConstantValue.exactly(min) : UniformGenerator.between(min, max)));
    }
}
