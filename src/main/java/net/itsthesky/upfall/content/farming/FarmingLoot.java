package net.itsthesky.upfall.content.farming;

import net.itsthesky.upfall.datagen.ModBlockLootProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public final class FarmingLoot {
    private FarmingLoot() {}

    private static final float CLOUDBERRY_SEEDS_CHANCE = 0.125F;
    private static final float WHEAT_SEEDS_CHANCE = 0.08F;

    public static LootTable.Builder withSeeds(ModBlockLootProvider loot, Block grass, LootTable.Builder table) {
        return table
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .when(loot.shearsOrSilkTouch().invert())
                        .add(loot.explosionDecay(grass, LootItem.lootTableItem(FarmingBlocks.CLOUDBERRY_SEEDS.get())
                                .when(LootItemRandomChanceCondition.randomChance(CLOUDBERRY_SEEDS_CHANCE))
                                .apply(ApplyBonusCount.addUniformBonusCount(loot.fortuneEnchantment(), 2)))))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .when(loot.shearsOrSilkTouch().invert())
                        .add(loot.explosionDecay(grass, LootItem.lootTableItem(Items.WHEAT_SEEDS)
                                .when(LootItemRandomChanceCondition.randomChance(WHEAT_SEEDS_CHANCE)))));
    }
}
