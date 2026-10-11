package net.itsthesky.upfall.datagen;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.UpfallModules;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModItems;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Set;

public class ModBlockLootProvider extends BlockLootSubProvider {
    private static final float[] SAPLING_CHANCES = {0.05F, 0.0625F, 0.083333336F, 0.1F};

    public ModBlockLootProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        for (Block block : getKnownBlocks()) {
            if (block.getLootTable() != BuiltInLootTables.EMPTY && block.asItem() != Items.AIR) {
                dropSelf(block);
            }
        }

        add(ModBlocks.PEARLSTONE.get(), b -> createSingleItemTableWithSilkTouch(b, ModBlocks.COBBLED_PEARLSTONE.get()));
        add(ModBlocks.DUSKSLATE.get(), b -> createSingleItemTableWithSilkTouch(b, ModBlocks.COBBLED_DUSKSLATE.get()));
        add(ModBlocks.SKYGRASS_BLOCK.get(), b -> createSingleItemTableWithSilkTouch(b, ModBlocks.SKYSOIL.get()));

        for (ModBlocks.StoneVariants variants : new ModBlocks.StoneVariants[]{ModBlocks.PEARLSTONE_VARIANTS, ModBlocks.COBBLED_PEARLSTONE_VARIANTS,
                ModBlocks.POLISHED_PEARLSTONE_VARIANTS, ModBlocks.PEARLSTONE_BRICK_VARIANTS, ModBlocks.DUSKSLATE_VARIANTS,
                ModBlocks.COBBLED_DUSKSLATE_VARIANTS, ModBlocks.POLISHED_DUSKSLATE_VARIANTS, ModBlocks.DUSKSLATE_BRICK_VARIANTS}) {
            add(variants.slab().get(), this::createSlabItemTable);
        }

        for (ModBlocks.WoodSet set : new ModBlocks.WoodSet[]{ModBlocks.SKYREACH, ModBlocks.GILDROOT}) {
            add(set.slab().get(), this::createSlabItemTable);
            add(set.door().get(), this::createDoorTable);
        }
        add(ModBlocks.SKYREACH.leaves().get(), b -> createLeavesDrops(b, ModBlocks.SKYREACH.sapling().get(), SAPLING_CHANCES));
        add(ModBlocks.GILDROOT.leaves().get(), b -> createLeavesDrops(b, ModBlocks.GILDROOT.sapling().get(), SAPLING_CHANCES)
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .when(shearsOrSilkTouch().invert())
                        .add(applyExplosionCondition(b, LootItem.lootTableItem(ModItems.GILDFRUIT.get()))
                                .when(BonusLevelTableCondition.bonusLevelFlatChance(fortune(), 0.03F, 0.035F, 0.04F, 0.05F, 0.1F)))));

        add(ModBlocks.CLOUD_FERN.get(), b -> createShearsDispatchTable(b, applyExplosionDecay(b,
                LootItem.lootTableItem(ModItems.CLOUDBERRY.get())
                        .when(LootItemRandomChanceCondition.randomChance(0.3F))
                        .apply(ApplyBonusCount.addUniformBonusCount(fortune(), 1)))));
        add(ModBlocks.GILDED_TUFT.get(), b -> createShearsOnlyDrop(b));
        addNetherVinesDropTable(ModBlocks.GLOWVINE.get(), ModBlocks.GLOWVINE_PLANT.get());

        add(ModBlocks.NIMBUSITE_ORE.get(), b -> createOreDrop(b, ModItems.RAW_NIMBUSITE.get()));
        add(ModBlocks.DUSKSLATE_NIMBUSITE_ORE.get(), b -> createOreDrop(b, ModItems.RAW_NIMBUSITE.get()));
        add(ModBlocks.LEVITITE_ORE.get(), b -> createSingleItemTableWithSilkTouch(b, ModItems.LEVITITE_SHARD.get()));

        dropPottedContents(ModBlocks.POTTED_DUSKBLOOM.get());
        dropPottedContents(ModBlocks.POTTED_CLOUD_FERN.get());
        dropPottedContents(ModBlocks.POTTED_SKYREACH_SAPLING.get());
        UpfallModules.forEachData(data -> data.blockLoot(this));
    }

    @Override
    public void add(Block block, LootTable.Builder builder) {
        super.add(block, builder);
    }

    @Override
    public void dropSelf(Block block) {
        super.dropSelf(block);
    }

    @Override
    public void dropOther(Block block, net.minecraft.world.level.ItemLike item) {
        super.dropOther(block, item);
    }

    @Override
    public LootTable.Builder createSingleItemTableWithSilkTouch(Block block, net.minecraft.world.level.ItemLike item) {
        return super.createSingleItemTableWithSilkTouch(block, item);
    }

    @Override
    public LootTable.Builder createSingleItemTableWithSilkTouch(Block block, net.minecraft.world.level.ItemLike item,
                                                                net.minecraft.world.level.storage.loot.providers.number.NumberProvider count) {
        return super.createSingleItemTableWithSilkTouch(block, item, count);
    }

    @Override
    public LootTable.Builder createSilkTouchDispatchTable(Block block,
                                                          net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Builder<?> builder) {
        return super.createSilkTouchDispatchTable(block, builder);
    }

    @Override
    public LootTable.Builder createOreDrop(Block block, net.minecraft.world.item.Item item) {
        return super.createOreDrop(block, item);
    }

    @Override
    public LootTable.Builder createSlabItemTable(Block block) {
        return super.createSlabItemTable(block);
    }

    @Override
    public LootTable.Builder createDoorTable(Block block) {
        return super.createDoorTable(block);
    }

    @Override
    public LootItemCondition.Builder hasSilkTouch() {
        return super.hasSilkTouch();
    }

    public <T extends net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder<T>> T explosionDecay(
            net.minecraft.world.level.ItemLike item, net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder<T> builder) {
        return applyExplosionDecay(item, builder);
    }

    public HolderLookup.Provider registries() {
        return registries;
    }

    public Holder<Enchantment> fortuneEnchantment() {
        return fortune();
    }

    private Holder<Enchantment> fortune() {
        return registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
    }

    private LootItemCondition.Builder shearsOrSilkTouch() {
        return MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.SHEARS)).or(hasSilkTouch());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return BuiltInRegistries.BLOCK.entrySet().stream()
                .filter(entry -> entry.getKey().location().getNamespace().equals(Upfall.MODID))
                .map(java.util.Map.Entry::getValue)
                .toList();
    }
}
