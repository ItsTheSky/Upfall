package net.itsthesky.upfall.datagen;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.UpfallModules;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModItems;
import net.itsthesky.upfall.registry.ModTags;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.List;

public final class UpfallRecipes {
    private UpfallRecipes() {}

    public static void build(RecipeContext r) {
        stoneRecipes(r);
        woodRecipes(r, ModBlocks.SKYREACH, ModTags.Items.SKYREACH_LOGS);
        woodRecipes(r, ModBlocks.GILDROOT, ModTags.Items.GILDROOT_LOGS);
        woodRecipes(r, ModBlocks.IRONBARK, ModTags.Items.IRONBARK_LOGS);
        materialRecipes(r);
        gearRecipes(r);
        miscRecipes(r);
        UpfallModules.forEachData(data -> data.recipes(r));
    }

    private static void stoneRecipes(RecipeContext r) {
        smelting(r, ModBlocks.COBBLED_PEARLSTONE.get(), ModBlocks.PEARLSTONE.get(), 0.1F, "pearlstone");
        smelting(r, ModBlocks.COBBLED_DUSKSLATE.get(), ModBlocks.DUSKSLATE.get(), 0.1F, "duskslate");

        square(r, ModBlocks.POLISHED_PEARLSTONE.get(), ModBlocks.PEARLSTONE.get(), 4);
        square(r, ModBlocks.PEARLSTONE_BRICKS.get(), ModBlocks.POLISHED_PEARLSTONE.get(), 4);
        square(r, ModBlocks.POLISHED_DUSKSLATE.get(), ModBlocks.DUSKSLATE.get(), 4);
        square(r, ModBlocks.DUSKSLATE_BRICKS.get(), ModBlocks.POLISHED_DUSKSLATE.get(), 4);

        stonecutterChain(r, ModBlocks.PEARLSTONE.get(), ModBlocks.PEARLSTONE_VARIANTS, List.of(
                ModBlocks.POLISHED_PEARLSTONE.get(), ModBlocks.PEARLSTONE_BRICKS.get()),
                List.of(ModBlocks.POLISHED_PEARLSTONE_VARIANTS, ModBlocks.PEARLSTONE_BRICK_VARIANTS));
        stonecutterChain(r, ModBlocks.POLISHED_PEARLSTONE.get(), ModBlocks.POLISHED_PEARLSTONE_VARIANTS,
                List.of(ModBlocks.PEARLSTONE_BRICKS.get()), List.of(ModBlocks.PEARLSTONE_BRICK_VARIANTS));
        stonecutterChain(r, ModBlocks.PEARLSTONE_BRICKS.get(), ModBlocks.PEARLSTONE_BRICK_VARIANTS,
                List.of(ModBlocks.CHISELED_PEARLSTONE_BRICKS.get()), List.of());
        stonecutterChain(r, ModBlocks.MOSSY_PEARLSTONE_BRICKS.get(), ModBlocks.MOSSY_PEARLSTONE_BRICK_VARIANTS, List.of(), List.of());
        stonecutterChain(r, ModBlocks.COBBLED_PEARLSTONE.get(), ModBlocks.COBBLED_PEARLSTONE_VARIANTS, List.of(), List.of());
        for (ItemLike moss : List.of(Items.VINE, Items.MOSS_BLOCK)) {
            r.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.MOSSY_PEARLSTONE_BRICKS.get())
                    .requires(ModBlocks.PEARLSTONE_BRICKS.get()).requires(moss)
                    .unlockedBy(RecipeContext.hasName(moss), r.has(moss))
                    .save(r.output(), Upfall.id("mossy_pearlstone_bricks_from_" + RecipeContext.itemName(moss)).toString());
        }

        stonecutterChain(r, ModBlocks.DUSKSLATE.get(), ModBlocks.DUSKSLATE_VARIANTS, List.of(
                ModBlocks.POLISHED_DUSKSLATE.get(), ModBlocks.DUSKSLATE_BRICKS.get()),
                List.of(ModBlocks.POLISHED_DUSKSLATE_VARIANTS, ModBlocks.DUSKSLATE_BRICK_VARIANTS));
        stonecutterChain(r, ModBlocks.POLISHED_DUSKSLATE.get(), ModBlocks.POLISHED_DUSKSLATE_VARIANTS,
                List.of(ModBlocks.DUSKSLATE_BRICKS.get()), List.of(ModBlocks.DUSKSLATE_BRICK_VARIANTS));
        stonecutterChain(r, ModBlocks.DUSKSLATE_BRICKS.get(), ModBlocks.DUSKSLATE_BRICK_VARIANTS, List.of(), List.of());
        stonecutterChain(r, ModBlocks.COBBLED_DUSKSLATE.get(), ModBlocks.COBBLED_DUSKSLATE_VARIANTS, List.of(), List.of());

        r.save(r.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.GILDED_DUSKSLATE.get(), 2)
                .requires(ModBlocks.DUSKSLATE.get(), 2).requires(Items.GOLD_NUGGET)
                .unlockedBy(RecipeContext.hasName(ModBlocks.DUSKSLATE.get()), r.has(ModBlocks.DUSKSLATE.get())),
                Upfall.id("gilded_duskslate"));
    }

    private static void stonecutterChain(RecipeContext r, Block base, ModBlocks.StoneVariants own,
                                         List<Block> others, List<ModBlocks.StoneVariants> othersVariants) {
        cut(r, base, own.stairs().get(), 1);
        cut(r, base, own.slab().get(), 2);
        cut(r, base, own.wall().get(), 1);
        for (Block other : others) {
            cut(r, base, other, 1);
        }
        for (var variants : othersVariants) {
            cut(r, base, variants.stairs().get(), 1);
            cut(r, base, variants.slab().get(), 2);
            cut(r, base, variants.wall().get(), 1);
        }
    }

    private static void woodRecipes(RecipeContext r, ModBlocks.WoodSet set, TagKey<Item> logs) {
        r.shapeless(RecipeCategory.BUILDING_BLOCKS, set.planks().get(), 4)
                .requires(logs).group("planks")
                .unlockedBy("has_logs", r.has(logs))
                .save(r.output());
        r.shaped(RecipeCategory.BUILDING_BLOCKS, set.wood().get(), 3)
                .define('#', set.log().get()).pattern("##").pattern("##").group("bark")
                .unlockedBy(RecipeContext.hasName(set.log().get()), r.has(set.log().get()))
                .save(r.output());
        r.shaped(RecipeCategory.BUILDING_BLOCKS, set.strippedWood().get(), 3)
                .define('#', set.strippedLog().get()).pattern("##").pattern("##").group("bark")
                .unlockedBy(RecipeContext.hasName(set.strippedLog().get()), r.has(set.strippedLog().get()))
                .save(r.output());
    }

    private static void materialRecipes(RecipeContext r) {
        smelting(r, ModItems.RAW_NIMBUSITE.get(), ModItems.NIMBUSITE_INGOT.get(), 1.0F, "nimbusite_ingot");
        smelting(r, ModBlocks.NIMBUSITE_ORE.get(), ModItems.NIMBUSITE_INGOT.get(), 1.0F, "nimbusite_ingot");
        smelting(r, ModBlocks.DUSKSLATE_NIMBUSITE_ORE.get(), ModItems.NIMBUSITE_INGOT.get(), 1.0F, "nimbusite_ingot");
        smelting(r, ModBlocks.LEVITITE_ORE.get(), ModItems.LEVITITE_SHARD.get(), 2.0F, "levitite_shard");

        storage(r, ModItems.RAW_NIMBUSITE.get(), ModBlocks.RAW_NIMBUSITE_BLOCK.get());
        storage(r, ModItems.NIMBUSITE_INGOT.get(), ModBlocks.NIMBUSITE_BLOCK.get());
        storage(r, ModItems.LEVITITE_INGOT.get(), ModBlocks.LEVITITE_BLOCK.get());

        r.shapeless(RecipeCategory.MISC, ModItems.LEVITITE_INGOT.get())
                .requires(ModItems.LEVITITE_SHARD.get(), 4).requires(ModItems.NIMBUSITE_INGOT.get(), 4)
                .group("levitite_ingot")
                .unlockedBy(RecipeContext.hasName(ModItems.LEVITITE_SHARD.get()), r.has(ModItems.LEVITITE_SHARD.get()))
                .save(r.output(), Upfall.id("levitite_ingot_from_shards").toString());

        templateCopy(r, ModItems.NIMBUSITE_UPGRADE_SMITHING_TEMPLATE.get(), Items.DIAMOND, ModBlocks.PEARLSTONE.get());
        templateCopy(r, ModItems.LEVITITE_UPGRADE_SMITHING_TEMPLATE.get(), ModItems.NIMBUSITE_INGOT.get(), ModBlocks.DUSKSLATE.get());
    }

    private static void gearRecipes(RecipeContext r) {
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
            upgrade(r, ModItems.NIMBUSITE_UPGRADE_SMITHING_TEMPLATE.get(), row[0], ModItems.NIMBUSITE_INGOT.get(), row[1]);
            upgrade(r, ModItems.LEVITITE_UPGRADE_SMITHING_TEMPLATE.get(), row[1], ModItems.LEVITITE_INGOT.get(), row[2]);
        }
    }

    private static void miscRecipes(RecipeContext r) {
        r.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.HALO_FRAME.get(), 8)
                .define('G', Items.LIGHT_BLUE_STAINED_GLASS).define('P', Items.PURPUR_BLOCK).define('N', Items.NETHERITE_INGOT)
                .pattern("GPG").pattern("PNP").pattern("GPG")
                .unlockedBy(RecipeContext.hasName(Items.NETHERITE_INGOT), r.has(Items.NETHERITE_INGOT))
                .save(r.output());
        r.shaped(RecipeCategory.TOOLS, ModItems.SKYWARD_KEY.get())
                .define('F', Items.FEATHER).define('S', Items.NETHER_STAR).define('E', Items.ELYTRA)
                .pattern("FSF").pattern("FEF").pattern(" F ")
                .unlockedBy(RecipeContext.hasName(Items.NETHER_STAR), r.has(Items.NETHER_STAR))
                .save(r.output());
        r.shaped(RecipeCategory.TOOLS, ModItems.UPDRAFT_COMPASS.get())
                .define('C', ModBlocks.DENSE_CLOUD.get()).define('X', Items.COMPASS).define('N', ModItems.NIMBUSITE_INGOT.get())
                .pattern(" C ").pattern("CXC").pattern(" N ")
                .unlockedBy(RecipeContext.hasName(ModItems.NIMBUSITE_INGOT.get()), r.has(ModItems.NIMBUSITE_INGOT.get()))
                .save(r.output());
        r.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.UPDRAFT_VENT.get())
                .define('P', ModBlocks.POLISHED_PEARLSTONE.get()).define('C', ModBlocks.DENSE_CLOUD.get()).define('N', ModItems.NIMBUSITE_INGOT.get())
                .pattern("P P").pattern("PCP").pattern("PNP")
                .unlockedBy(RecipeContext.hasName(ModItems.NIMBUSITE_INGOT.get()), r.has(ModItems.NIMBUSITE_INGOT.get()))
                .save(r.output());
        r.shaped(RecipeCategory.DECORATIONS, ModBlocks.SKY_CACHE.get())
                .define('P', ModBlocks.SKYREACH.planks().get()).define('G', Items.GOLD_INGOT)
                .pattern("PGP").pattern("P P").pattern("PPP")
                .unlockedBy(RecipeContext.hasName(ModBlocks.SKYREACH.planks().get()), r.has(ModBlocks.SKYREACH.planks().get()))
                .save(r.output());
        r.shapeless(RecipeCategory.FOOD, ModItems.CLOUDBERRY_TART.get())
                .requires(ModItems.CLOUDBERRY.get(), 3).requires(Items.SUGAR).requires(Items.EGG).requires(Items.WHEAT)
                .unlockedBy(RecipeContext.hasName(ModItems.CLOUDBERRY.get()), r.has(ModItems.CLOUDBERRY.get()))
                .save(r.output());
    }

    private static void smelting(RecipeContext r, ItemLike input, ItemLike result, float xp, String group) {
        smelting(r, input, result, xp, group, !(result instanceof Block));
    }

    private static void smelting(RecipeContext r, ItemLike input, ItemLike result, float xp, String group, boolean blasting) {
        String base = RecipeContext.itemName(result) + "_from_" + RecipeContext.itemName(input);
        r.smelting(Ingredient.of(input), RecipeCategory.MISC, result, xp, 200).group(group)
                .unlockedBy(RecipeContext.hasName(input), r.has(input)).save(r.output(), Upfall.id(base + "_smelting").toString());
        if (blasting) {
            r.blasting(Ingredient.of(input), RecipeCategory.MISC, result, xp, 100).group(group)
                    .unlockedBy(RecipeContext.hasName(input), r.has(input)).save(r.output(), Upfall.id(base + "_blasting").toString());
        }
    }

    private static void square(RecipeContext r, Block result, Block input, int count) {
        r.shaped(RecipeCategory.BUILDING_BLOCKS, result, count)
                .define('#', input).pattern("##").pattern("##")
                .unlockedBy(RecipeContext.hasName(input), r.has(input))
                .save(r.output());
    }

    private static void cut(RecipeContext r, ItemLike input, ItemLike result, int count) {
        r.stonecutting(Ingredient.of(input), RecipeCategory.BUILDING_BLOCKS, result, count)
                .unlockedBy(RecipeContext.hasName(input), r.has(input))
                .save(r.output(), Upfall.id(RecipeContext.itemName(result) + "_from_" + RecipeContext.itemName(input) + "_stonecutting").toString());
    }

    private static void storage(RecipeContext r, Item item, Block block) {
        r.shaped(RecipeCategory.BUILDING_BLOCKS, block)
                .define('#', item).pattern("###").pattern("###").pattern("###")
                .unlockedBy(RecipeContext.hasName(item), r.has(item))
                .save(r.output());
        r.shapeless(RecipeCategory.MISC, item, 9)
                .requires(block)
                .unlockedBy(RecipeContext.hasName(block), r.has(block))
                .save(r.output(), Upfall.id(RecipeContext.itemName(item) + "_from_" + RecipeContext.itemName(block)).toString());
    }

    private static void templateCopy(RecipeContext r, Item template, ItemLike material, ItemLike base) {
        r.shaped(RecipeCategory.MISC, template, 2)
                .define('#', material).define('C', base).define('S', template)
                .pattern("#S#").pattern("#C#").pattern("###")
                .unlockedBy(RecipeContext.hasName(template), r.has(template))
                .save(r.output());
    }

    private static void upgrade(RecipeContext r, Item template, Item base, Item addition, Item result) {
        r.smithing(Ingredient.of(template), Ingredient.of(base), Ingredient.of(addition), RecipeCategory.COMBAT, result)
                .unlocks(RecipeContext.hasName(addition), r.has(addition))
                .save(r.output(), Upfall.id(RecipeContext.itemName(result) + "_smithing").toString());
    }
}
