package net.itsthesky.upfall.content.engine;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.machines.MachinesItems;
import net.itsthesky.upfall.content.quantum.QuantumItems;
import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.itsthesky.upfall.content.rustworks.RustworksItems;
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
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.BiConsumer;

public final class EngineLoot implements LootTableSubProvider {
    public static final ResourceKey<LootTable> VAULT = ResourceKey.create(Registries.LOOT_TABLE, Upfall.id("chests/stalled_engine_vault"));
    public static final ResourceKey<LootTable> CACHE = ResourceKey.create(Registries.LOOT_TABLE, Upfall.id("chests/stalled_engine"));

    public EngineLoot(HolderLookup.Provider registries) {
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(VAULT, LootTable.lootTable()
                .withPool(blueprints(LootPool.lootPool().setRolls(UniformGenerator.between(1, 2))))
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(2, 3))
                        .add(item(RustworksItems.KAIRITE_GEAR.get(), 8, 1, 3))
                        .add(item(RustworksItems.KAIRITE_DUST.get(), 10, 3, 8)))
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(3, 5))
                        .add(item(Items.IRON_INGOT, 10, 2, 5))
                        .add(item(Items.COPPER_INGOT, 8, 3, 8))
                        .add(item(Items.REDSTONE, 8, 3, 9))
                        .add(item(Items.CHAIN, 6, 2, 6))
                        .add(item(Items.GOLD_INGOT, 4, 1, 3))
                        .add(item(ModItems.NIMBUSITE_INGOT.get(), 4, 1, 2))
                        .add(item(Items.CLOCK, 3, 1, 1))
                        .add(item(Items.COMPASS, 3, 1, 1))
                        .add(item(Items.DIAMOND, 2, 1, 2))));

        output.accept(CACHE, LootTable.lootTable()
                .withPool(blueprints(LootPool.lootPool().setRolls(ConstantValue.exactly(1)))
                        .add(item(QuantumItems.VEIL_MASK_BLUEPRINT.get(), 12, 1, 1)).add(EmptyLootItem.emptyItem().setWeight(60)))
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(1, 2))
                        .add(item(RustworksItems.KAIRITE_DUST.get(), 10, 2, 6))
                        .add(item(RustworksItems.KAIRITE_GEAR.get(), 3, 1, 1))
                        .add(EmptyLootItem.emptyItem().setWeight(4)))
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(2, 5))
                        .add(item(Items.IRON_NUGGET, 12, 4, 12))
                        .add(item(Items.IRON_INGOT, 8, 1, 3))
                        .add(item(Items.COPPER_INGOT, 8, 2, 6))
                        .add(item(Items.CHAIN, 6, 1, 4))
                        .add(item(Items.REDSTONE, 6, 2, 6))
                        .add(item(RustworksBlocks.COBBLED_RUSTSLATE.get(), 6, 4, 12))
                        .add(item(RustworksBlocks.RUSTY_COG.get(), 4, 1, 2))
                        .add(item(RustworksBlocks.RUSTY_PIPE.get(), 4, 2, 6))
                        .add(item(Items.LANTERN, 3, 1, 2)))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(ExplorerMaps.loot(ExplorerMaps.SHIFTING_VAULT, 12))
                        .add(EmptyLootItem.emptyItem().setWeight(88))));
    }

    private static LootPool.Builder blueprints(LootPool.Builder pool) {
        return pool.add(item(MachinesItems.PHASE_WATCH_BLUEPRINT.get(), 10, 1, 1))
                .add(item(MachinesItems.GEARLIFT_BLUEPRINT.get(), 8, 1, 1))
                .add(item(MachinesItems.PNEUMATIC_PIPE_BLUEPRINT.get(), 8, 1, 1))
                .add(item(MachinesItems.HASTENING_GEAR_BLUEPRINT.get(), 6, 1, 1))
                .add(item(MachinesItems.VOID_TETHER_BLUEPRINT.get(), 5, 1, 1))
                .add(item(MachinesItems.REWINDER_BLUEPRINT.get(), 3, 1, 1));
    }

    private static LootPoolEntryContainer.Builder<?> item(ItemLike item, int weight, int min, int max) {
        return LootItem.lootTableItem(item).setWeight(weight)
                .apply(SetItemCountFunction.setCount(min == max ? ConstantValue.exactly(min) : UniformGenerator.between(min, max)));
    }
}
