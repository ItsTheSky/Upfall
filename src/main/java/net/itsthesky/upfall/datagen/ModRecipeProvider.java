package net.itsthesky.upfall.datagen;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.UpfallModules;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModItems;
import net.itsthesky.upfall.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        for (BlockFamily family : ModBlockFamilies.all()) {
            generateRecipes(output, family, FeatureFlags.DEFAULT_FLAGS);
        }

        stoneRecipes(output);
        woodRecipes(output, ModBlocks.SKYREACH, ModTags.Items.SKYREACH_LOGS);
        woodRecipes(output, ModBlocks.GILDROOT, ModTags.Items.GILDROOT_LOGS);
        materialRecipes(output);
        gearRecipes(output);
        miscRecipes(output);
        UpfallModules.forEachData(data -> data.recipes(output));
    }

    public static net.minecraft.advancements.Criterion<net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance> hasItem(ItemLike item) {
        return has(item);
    }

    public static String hasName(ItemLike item) {
        return getHasName(item);
    }

    public static String itemName(ItemLike item) {
        return getItemName(item);
    }

    private void stoneRecipes(RecipeOutput output) {
        smelting(output, ModBlocks.COBBLED_PEARLSTONE.get(), ModBlocks.PEARLSTONE.get(), 0.1F, "pearlstone");
        smelting(output, ModBlocks.COBBLED_DUSKSLATE.get(), ModBlocks.DUSKSLATE.get(), 0.1F, "duskslate");

        square(output, ModBlocks.POLISHED_PEARLSTONE.get(), ModBlocks.PEARLSTONE.get(), 4);
        square(output, ModBlocks.PEARLSTONE_BRICKS.get(), ModBlocks.POLISHED_PEARLSTONE.get(), 4);
        square(output, ModBlocks.POLISHED_DUSKSLATE.get(), ModBlocks.DUSKSLATE.get(), 4);
        square(output, ModBlocks.DUSKSLATE_BRICKS.get(), ModBlocks.POLISHED_DUSKSLATE.get(), 4);

        stonecutterChain(output, ModBlocks.PEARLSTONE.get(), ModBlocks.PEARLSTONE_VARIANTS, List.of(
                ModBlocks.POLISHED_PEARLSTONE.get(), ModBlocks.PEARLSTONE_BRICKS.get()),
                List.of(ModBlocks.POLISHED_PEARLSTONE_VARIANTS, ModBlocks.PEARLSTONE_BRICK_VARIANTS));
        stonecutterChain(output, ModBlocks.POLISHED_PEARLSTONE.get(), ModBlocks.POLISHED_PEARLSTONE_VARIANTS,
                List.of(ModBlocks.PEARLSTONE_BRICKS.get()), List.of(ModBlocks.PEARLSTONE_BRICK_VARIANTS));
        stonecutterChain(output, ModBlocks.PEARLSTONE_BRICKS.get(), ModBlocks.PEARLSTONE_BRICK_VARIANTS, List.of(), List.of());
        stonecutterChain(output, ModBlocks.COBBLED_PEARLSTONE.get(), ModBlocks.COBBLED_PEARLSTONE_VARIANTS, List.of(), List.of());

        stonecutterChain(output, ModBlocks.DUSKSLATE.get(), ModBlocks.DUSKSLATE_VARIANTS, List.of(
                ModBlocks.POLISHED_DUSKSLATE.get(), ModBlocks.DUSKSLATE_BRICKS.get()),
                List.of(ModBlocks.POLISHED_DUSKSLATE_VARIANTS, ModBlocks.DUSKSLATE_BRICK_VARIANTS));
        stonecutterChain(output, ModBlocks.POLISHED_DUSKSLATE.get(), ModBlocks.POLISHED_DUSKSLATE_VARIANTS,
                List.of(ModBlocks.DUSKSLATE_BRICKS.get()), List.of(ModBlocks.DUSKSLATE_BRICK_VARIANTS));
        stonecutterChain(output, ModBlocks.DUSKSLATE_BRICKS.get(), ModBlocks.DUSKSLATE_BRICK_VARIANTS, List.of(), List.of());
        stonecutterChain(output, ModBlocks.COBBLED_DUSKSLATE.get(), ModBlocks.COBBLED_DUSKSLATE_VARIANTS, List.of(), List.of());

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.GILDED_DUSKSLATE.get(), 2)
                .requires(ModBlocks.DUSKSLATE.get(), 2).requires(Items.GOLD_NUGGET)
                .unlockedBy(getHasName(ModBlocks.DUSKSLATE.get()), has(ModBlocks.DUSKSLATE.get()))
                .save(output, Upfall.id("gilded_duskslate"));
    }

    private void stonecutterChain(RecipeOutput output, Block base, net.itsthesky.upfall.registry.ModBlocks.StoneVariants own,
                                  List<Block> others, List<net.itsthesky.upfall.registry.ModBlocks.StoneVariants> othersVariants) {
        cut(output, base, own.stairs().get(), 1);
        cut(output, base, own.slab().get(), 2);
        cut(output, base, own.wall().get(), 1);
        for (Block other : others) {
            cut(output, base, other, 1);
        }
        for (var variants : othersVariants) {
            cut(output, base, variants.stairs().get(), 1);
            cut(output, base, variants.slab().get(), 2);
            cut(output, base, variants.wall().get(), 1);
        }
    }

    private void woodRecipes(RecipeOutput output, ModBlocks.WoodSet set, TagKey<Item> logs) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, set.planks().get(), 4)
                .requires(logs).group("planks")
                .unlockedBy("has_logs", has(logs))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, set.wood().get(), 3)
                .define('#', set.log().get()).pattern("##").pattern("##").group("bark")
                .unlockedBy(getHasName(set.log().get()), has(set.log().get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, set.strippedWood().get(), 3)
                .define('#', set.strippedLog().get()).pattern("##").pattern("##").group("bark")
                .unlockedBy(getHasName(set.strippedLog().get()), has(set.strippedLog().get()))
                .save(output);
    }

    private void materialRecipes(RecipeOutput output) {
        smelting(output, ModItems.RAW_NIMBUSITE.get(), ModItems.NIMBUSITE_INGOT.get(), 1.0F, "nimbusite_ingot");
        smelting(output, ModBlocks.NIMBUSITE_ORE.get(), ModItems.NIMBUSITE_INGOT.get(), 1.0F, "nimbusite_ingot");
        smelting(output, ModBlocks.DUSKSLATE_NIMBUSITE_ORE.get(), ModItems.NIMBUSITE_INGOT.get(), 1.0F, "nimbusite_ingot");
        smelting(output, ModBlocks.LEVITITE_ORE.get(), ModItems.LEVITITE_SHARD.get(), 2.0F, "levitite_shard");

        storage(output, ModItems.RAW_NIMBUSITE.get(), ModBlocks.RAW_NIMBUSITE_BLOCK.get());
        storage(output, ModItems.NIMBUSITE_INGOT.get(), ModBlocks.NIMBUSITE_BLOCK.get());
        storage(output, ModItems.LEVITITE_INGOT.get(), ModBlocks.LEVITITE_BLOCK.get());

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.LEVITITE_INGOT.get())
                .requires(ModItems.LEVITITE_SHARD.get(), 4).requires(ModItems.NIMBUSITE_INGOT.get(), 4)
                .group("levitite_ingot")
                .unlockedBy(getHasName(ModItems.LEVITITE_SHARD.get()), has(ModItems.LEVITITE_SHARD.get()))
                .save(output, Upfall.id("levitite_ingot_from_shards"));

        templateCopy(output, ModItems.NIMBUSITE_UPGRADE_SMITHING_TEMPLATE.get(), Items.DIAMOND, ModBlocks.PEARLSTONE.get());
        templateCopy(output, ModItems.LEVITITE_UPGRADE_SMITHING_TEMPLATE.get(), ModItems.NIMBUSITE_INGOT.get(), ModBlocks.DUSKSLATE.get());
    }

    private void gearRecipes(RecipeOutput output) {
        Item[][] upgrades = {
                {Items.NETHERITE_SWORD, ModItems.NIMBUSITE_SWORD.get(), ModItems.LEVITITE_SWORD.get()},
                {Items.NETHERITE_PICKAXE, ModItems.NIMBUSITE_PICKAXE.get(), ModItems.LEVITITE_PICKAXE.get()},
                {Items.NETHERITE_AXE, ModItems.NIMBUSITE_AXE.get(), ModItems.LEVITITE_AXE.get()},
                {Items.NETHERITE_SHOVEL, ModItems.NIMBUSITE_SHOVEL.get(), ModItems.LEVITITE_SHOVEL.get()},
                {Items.NETHERITE_HOE, ModItems.NIMBUSITE_HOE.get(), ModItems.LEVITITE_HOE.get()},
                {Items.NETHERITE_HELMET, ModItems.NIMBUSITE_HELMET.get(), ModItems.LEVITITE_HELMET.get()},
                {Items.NETHERITE_CHESTPLATE, ModItems.NIMBUSITE_CHESTPLATE.get(), ModItems.LEVITITE_CHESTPLATE.get()},
                {Items.NETHERITE_LEGGINGS, ModItems.NIMBUSITE_LEGGINGS.get(), ModItems.LEVITITE_LEGGINGS.get()},
                {Items.NETHERITE_BOOTS, ModItems.NIMBUSITE_BOOTS.get(), ModItems.LEVITITE_BOOTS.get()},
        };
        for (Item[] row : upgrades) {
            upgrade(output, ModItems.NIMBUSITE_UPGRADE_SMITHING_TEMPLATE.get(), row[0], ModItems.NIMBUSITE_INGOT.get(), row[1]);
            upgrade(output, ModItems.LEVITITE_UPGRADE_SMITHING_TEMPLATE.get(), row[1], ModItems.LEVITITE_INGOT.get(), row[2]);
        }
    }

    private void miscRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.HALO_FRAME.get(), 8)
                .define('G', Items.LIGHT_BLUE_STAINED_GLASS).define('P', Items.PURPUR_BLOCK).define('N', Items.NETHERITE_INGOT)
                .pattern("GPG").pattern("PNP").pattern("GPG")
                .unlockedBy(getHasName(Items.NETHERITE_INGOT), has(Items.NETHERITE_INGOT))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.SKYWARD_KEY.get())
                .define('F', Items.FEATHER).define('S', Items.NETHER_STAR).define('E', Items.ELYTRA)
                .pattern("FSF").pattern("FEF").pattern(" F ")
                .unlockedBy(getHasName(Items.NETHER_STAR), has(Items.NETHER_STAR))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.UPDRAFT_COMPASS.get())
                .define('C', ModBlocks.DENSE_CLOUD.get()).define('X', Items.COMPASS).define('N', ModItems.NIMBUSITE_INGOT.get())
                .pattern(" C ").pattern("CXC").pattern(" N ")
                .unlockedBy(getHasName(ModItems.NIMBUSITE_INGOT.get()), has(ModItems.NIMBUSITE_INGOT.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.UPDRAFT_VENT.get())
                .define('P', ModBlocks.POLISHED_PEARLSTONE.get()).define('C', ModBlocks.DENSE_CLOUD.get()).define('N', ModItems.NIMBUSITE_INGOT.get())
                .pattern("P P").pattern("PCP").pattern("PNP")
                .unlockedBy(getHasName(ModItems.NIMBUSITE_INGOT.get()), has(ModItems.NIMBUSITE_INGOT.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.SKY_CACHE.get())
                .define('P', ModBlocks.SKYREACH.planks().get()).define('G', Items.GOLD_INGOT)
                .pattern("PGP").pattern("P P").pattern("PPP")
                .unlockedBy(getHasName(ModBlocks.SKYREACH.planks().get()), has(ModBlocks.SKYREACH.planks().get()))
                .save(output);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.CLOUDBERRY_TART.get())
                .requires(ModItems.CLOUDBERRY.get(), 3).requires(Items.SUGAR).requires(Items.EGG).requires(Items.WHEAT)
                .unlockedBy(getHasName(ModItems.CLOUDBERRY.get()), has(ModItems.CLOUDBERRY.get()))
                .save(output);
    }

    private void smelting(RecipeOutput output, ItemLike input, ItemLike result, float xp, String group) {
        smelting(output, input, result, xp, group, !(result instanceof Block));
    }

    private void smelting(RecipeOutput output, ItemLike input, ItemLike result, float xp, String group, boolean blasting) {
        String base = getItemName(result) + "_from_" + getItemName(input);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(input), RecipeCategory.MISC, result, xp, 200).group(group)
                .unlockedBy(getHasName(input), has(input)).save(output, Upfall.id(base + "_smelting"));
        if (blasting) {
            SimpleCookingRecipeBuilder.blasting(Ingredient.of(input), RecipeCategory.MISC, result, xp, 100).group(group)
                    .unlockedBy(getHasName(input), has(input)).save(output, Upfall.id(base + "_blasting"));
        }
    }

    private void square(RecipeOutput output, Block result, Block input, int count) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, result, count)
                .define('#', input).pattern("##").pattern("##")
                .unlockedBy(getHasName(input), has(input))
                .save(output);
    }

    private void cut(RecipeOutput output, ItemLike input, ItemLike result, int count) {
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(input), RecipeCategory.BUILDING_BLOCKS, result, count)
                .unlockedBy(getHasName(input), has(input))
                .save(output, Upfall.id(getItemName(result) + "_from_" + getItemName(input) + "_stonecutting"));
    }

    private void storage(RecipeOutput output, Item item, Block block) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, block)
                .define('#', item).pattern("###").pattern("###").pattern("###")
                .unlockedBy(getHasName(item), has(item))
                .save(output);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item, 9)
                .requires(block)
                .unlockedBy(getHasName(block), has(block))
                .save(output, Upfall.id(getItemName(item) + "_from_" + getItemName(block)));
    }

    private void templateCopy(RecipeOutput output, Item template, ItemLike material, ItemLike base) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, template, 2)
                .define('#', material).define('C', base).define('S', template)
                .pattern("#S#").pattern("#C#").pattern("###")
                .unlockedBy(getHasName(template), has(template))
                .save(output);
    }

    private void upgrade(RecipeOutput output, Item template, Item base, Item addition, Item result) {
        SmithingTransformRecipeBuilder.smithing(Ingredient.of(template), Ingredient.of(base), Ingredient.of(addition), RecipeCategory.COMBAT, result)
                .unlocks(getHasName(addition), has(addition))
                .save(output, Upfall.id(getItemName(result) + "_smithing"));
    }
}
