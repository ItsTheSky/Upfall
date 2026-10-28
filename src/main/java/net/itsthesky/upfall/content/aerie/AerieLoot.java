package net.itsthesky.upfall.content.aerie;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.grotto.GrottoBlocks;
import net.itsthesky.upfall.content.world.WorldItems;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModItems;
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
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.BiConsumer;

public final class AerieLoot implements LootTableSubProvider {
    public static final ResourceKey<LootTable> TOWER = ResourceKey.create(Registries.LOOT_TABLE, Upfall.id("chests/pearl_aerie_tower"));
    public static final ResourceKey<LootTable> ARMORY = ResourceKey.create(Registries.LOOT_TABLE, Upfall.id("chests/pearl_aerie_armory"));
    public static final ResourceKey<LootTable> KEEP = ResourceKey.create(Registries.LOOT_TABLE, Upfall.id("chests/pearl_aerie_keep"));

    private final HolderLookup.Provider registries;

    public AerieLoot(HolderLookup.Provider registries) {
        this.registries = registries;
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(TOWER, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(3, 6))
                        .add(item(ModItems.RAW_NIMBUSITE.get(), 10, 1, 4))
                        .add(item(ModItems.NIMBUSITE_INGOT.get(), 5, 1, 2))
                        .add(item(ModItems.LEVITITE_SHARD.get(), 4, 1, 2))
                        .add(item(WorldItems.CLOUD_PEARL.get(), 8, 1, 3))
                        .add(item(GrottoBlocks.LUMEN_SHARD.get(), 6, 1, 3))
                        .add(item(ModBlocks.MISTGLOW_CRYSTAL.get(), 6, 1, 3))
                        .add(item(ModItems.GILDFRUIT.get(), 10, 1, 3))
                        .add(item(ModItems.CLOUDBERRY_TART.get(), 6, 1, 2))
                        .add(item(Items.GOLD_NUGGET, 8, 3, 9))
                        .add(item(Items.ARROW, 6, 4, 12))
                        .add(item(Items.FEATHER, 5, 2, 5))));

        output.accept(ARMORY, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(item(ModItems.NIMBUSITE_UPGRADE_SMITHING_TEMPLATE.get(), 10, 1, 1))
                        .add(item(ModItems.LEVITITE_UPGRADE_SMITHING_TEMPLATE.get(), 2, 1, 1))
                        .add(item(ModItems.UPDRAFT_COMPASS.get(), 4, 1, 1))
                        .add(EmptyLootItem.emptyItem().setWeight(12)))
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(1, 2))
                        .add(LootItem.lootTableItem(Items.BOOK).setWeight(6)
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(registries, UniformGenerator.between(20, 30))))
                        .add(LootItem.lootTableItem(Items.BOW).setWeight(3)
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(registries, UniformGenerator.between(15, 25))))
                        .add(item(ModItems.NIMBUSITE_INGOT.get(), 5, 1, 3))
                        .add(EmptyLootItem.emptyItem().setWeight(4)))
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(3, 5))
                        .add(item(Items.ARROW, 10, 6, 16))
                        .add(item(Items.SPECTRAL_ARROW, 4, 2, 6))
                        .add(item(Items.IRON_INGOT, 8, 2, 5))
                        .add(item(Items.GOLD_INGOT, 6, 1, 4))
                        .add(item(ModItems.RAW_NIMBUSITE.get(), 8, 2, 5))
                        .add(item(Items.DIAMOND, 2, 1, 2))));

        output.accept(KEEP, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(item(AerieItems.GRAVITY_SCEPTRE.get(), 1, 1, 1)))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(item(ModItems.NIMBUSITE_UPGRADE_SMITHING_TEMPLATE.get(), 6, 1, 1))
                        .add(item(ModItems.LEVITITE_UPGRADE_SMITHING_TEMPLATE.get(), 3, 1, 1))
                        .add(item(ModItems.UPDRAFT_COMPASS.get(), 3, 1, 1)))
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(4, 7))
                        .add(item(ModItems.NIMBUSITE_INGOT.get(), 10, 2, 5))
                        .add(item(ModItems.LEVITITE_SHARD.get(), 6, 1, 3))
                        .add(item(ModItems.LEVITITE_INGOT.get(), 2, 1, 1))
                        .add(item(WorldItems.CLOUD_PEARL.get(), 8, 2, 4))
                        .add(item(Items.DIAMOND, 5, 1, 3))
                        .add(item(Items.GOLD_INGOT, 8, 2, 6))
                        .add(item(Items.ENDER_PEARL, 4, 1, 3))
                        .add(LootItem.lootTableItem(Items.BOOK).setWeight(5)
                                .apply(EnchantWithLevelsFunction.enchantWithLevels(registries, UniformGenerator.between(25, 35)))))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(ExplorerMaps.loot(ExplorerMaps.TILTED_RELIQUARY, 25))
                        .add(EmptyLootItem.emptyItem().setWeight(75))));
    }

    private static LootPoolEntryContainer.Builder<?> item(ItemLike item, int weight, int min, int max) {
        return LootItem.lootTableItem(item).setWeight(weight)
                .apply(SetItemCountFunction.setCount(min == max ? ConstantValue.exactly(min) : UniformGenerator.between(min, max)));
    }
}
