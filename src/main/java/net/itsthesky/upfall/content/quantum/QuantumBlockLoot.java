package net.itsthesky.upfall.content.quantum;

import net.itsthesky.upfall.datagen.ModBlockLootProvider;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

final class QuantumBlockLoot {
    private QuantumBlockLoot() {}

    static void register(ModBlockLootProvider loot) {
        loot.add(QuantumBlocks.FLICKER_SHARD_CLUSTER.get(), loot.createSilkTouchDispatchTable(QuantumBlocks.FLICKER_SHARD_CLUSTER.get(),
                LootItem.lootTableItem(QuantumItems.FLICKER_SHARD.get())
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F)))
                        .apply(ApplyBonusCount.addOreBonusCount(loot.fortuneEnchantment()))));
        QuantumBlockLootExtra.register(loot);
    }
}
