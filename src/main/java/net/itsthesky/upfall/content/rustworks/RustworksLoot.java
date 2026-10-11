package net.itsthesky.upfall.content.rustworks;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.machines.MachinesItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
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
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public final class RustworksLoot implements LootTableSubProvider {
    public static final ResourceKey<LootTable> AUTOMATON = ResourceKey.create(Registries.LOOT_TABLE, Upfall.id("chests/stalled_automaton"));

    public RustworksLoot(HolderLookup.Provider registries) {
    }

    static DataProvider provider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        LootTableProvider tables = new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(RustworksLoot::new, LootContextParamSets.CHEST)), registries);
        return new DataProvider() {
            @Override
            public CompletableFuture<?> run(CachedOutput cache) {
                return tables.run(cache);
            }

            @Override
            public String getName() {
                return "Stalled Automaton loot tables";
            }
        };
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(AUTOMATON, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(2, 3))
                        .add(item(RustworksItems.KAIRITE_DUST.get(), 10, 2, 5))
                        .add(item(RustworksItems.RUSTY_PLATE.get(), 8, 2, 6))
                        .add(item(RustworksItems.KAIRITE_GEAR.get(), 3, 1, 1))
                        .add(item(Items.IRON_NUGGET, 6, 3, 9))
                        .add(item(Items.CHAIN, 3, 1, 3))
                        .add(item(Items.REDSTONE, 3, 1, 4)))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(item(MachinesItems.PHASE_WATCH_BLUEPRINT.get(), 2, 1, 1))
                        .add(item(MachinesItems.GEARLIFT_BLUEPRINT.get(), 2, 1, 1))
                        .add(item(MachinesItems.PNEUMATIC_PIPE_BLUEPRINT.get(), 2, 1, 1))
                        .add(item(MachinesItems.HASTENING_GEAR_BLUEPRINT.get(), 1, 1, 1))
                        .add(item(MachinesItems.VOID_TETHER_BLUEPRINT.get(), 1, 1, 1))
                        .add(item(MachinesItems.REWINDER_BLUEPRINT.get(), 1, 1, 1))
                        .add(EmptyLootItem.emptyItem().setWeight(100))));
    }

    private static LootPoolEntryContainer.Builder<?> item(ItemLike item, int weight, int min, int max) {
        return LootItem.lootTableItem(item).setWeight(weight)
                .apply(SetItemCountFunction.setCount(min == max ? ConstantValue.exactly(min) : UniformGenerator.between(min, max)));
    }
}
