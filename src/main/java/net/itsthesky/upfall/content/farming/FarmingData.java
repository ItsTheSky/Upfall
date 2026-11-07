package net.itsthesky.upfall.content.farming;

import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.datagen.ModBlockLootProvider;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.itsthesky.upfall.datagen.ModDataMapProvider;
import net.itsthesky.upfall.datagen.ModFrenchLanguageProvider;
//? if <26.1 {
import net.itsthesky.upfall.datagen.ModItemModelProvider;
//?}
import net.itsthesky.upfall.datagen.ModLanguageProvider;
import net.itsthesky.upfall.datagen.ModTagProviders;
import net.itsthesky.upfall.registry.ModItems;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

final class FarmingData implements DataContribution {
    @Override
    public void lang(ModLanguageProvider lang) {
        lang.add(FarmingBlocks.CLOUDBERRY_CROP.get(), "Cloudberry Bush");
    }

    @Override
    public void langFr(ModFrenchLanguageProvider lang) {
        lang.add("block.upfall.cloudberry_crop", "Plant de baies des nuages");
        lang.add("item.upfall.cloudberry_seeds", "Graines de baies des nuages");
    }

    @Override
    public void blockStates(ModBlockStateProvider states) {
        states.crop(FarmingBlocks.CLOUDBERRY_CROP.get(), CropBlock.AGE, 0, 0, 1, 1, 2, 2, 2, 3);
    }

    //? if <26.1 {
    @Override
    public void itemModels(ModItemModelProvider models) {
        models.basicItem(FarmingBlocks.CLOUDBERRY_SEEDS.get());
    }
    //?}

    @Override
    public void blockLoot(ModBlockLootProvider loot) {
        CropBlock crop = FarmingBlocks.CLOUDBERRY_CROP.get();
        LootItemCondition.Builder mature = LootItemBlockStatePropertyCondition.hasBlockStateProperties(crop)
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CropBlock.AGE, CropBlock.MAX_AGE));
        loot.add(crop, loot.explosionDecay(crop, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ModItems.CLOUDBERRY.get()).when(mature)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F)))
                                .apply(ApplyBonusCount.addUniformBonusCount(loot.fortuneEnchantment(), 1))
                                .otherwise(LootItem.lootTableItem(FarmingBlocks.CLOUDBERRY_SEEDS.get()))))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(mature)
                        .add(LootItem.lootTableItem(FarmingBlocks.CLOUDBERRY_SEEDS.get())
                                .apply(ApplyBonusCount.addBonusBinomialDistributionCount(loot.fortuneEnchantment(), 0.5714286F, 2))))));
    }

    @Override
    public void blockTags(ModTagProviders.Blocks tags) {
        tags.tag(BlockTags.CROPS).add(FarmingBlocks.CLOUDBERRY_CROP.get());
        tags.tag(BlockTags.BEE_GROWABLES).add(FarmingBlocks.CLOUDBERRY_CROP.get());
        tags.tag(BlockTags.MAINTAINS_FARMLAND).add(FarmingBlocks.CLOUDBERRY_CROP.get());
    }

    @Override
    public void dataMaps(ModDataMapProvider dataMaps) {
        dataMaps.builder(NeoForgeDataMaps.COMPOSTABLES)
                .add(FarmingBlocks.CLOUDBERRY_SEEDS.get().builtInRegistryHolder(), new Compostable(0.3F), false);
    }
}
