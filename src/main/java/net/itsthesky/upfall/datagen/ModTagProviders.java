package net.itsthesky.upfall.datagen;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.UpfallModules;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModItems;
import net.itsthesky.upfall.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider.IntrinsicTagAppender;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class ModTagProviders {
    private ModTagProviders() {}

    private static final List<ModBlocks.StoneVariants> ALL_VARIANTS = List.of(
            ModBlocks.PEARLSTONE_VARIANTS, ModBlocks.COBBLED_PEARLSTONE_VARIANTS, ModBlocks.POLISHED_PEARLSTONE_VARIANTS,
            ModBlocks.PEARLSTONE_BRICK_VARIANTS, ModBlocks.MOSSY_PEARLSTONE_BRICK_VARIANTS, ModBlocks.DUSKSLATE_VARIANTS, ModBlocks.COBBLED_DUSKSLATE_VARIANTS,
            ModBlocks.POLISHED_DUSKSLATE_VARIANTS, ModBlocks.DUSKSLATE_BRICK_VARIANTS);
    private static final List<ModBlocks.WoodSet> WOODS = List.of(ModBlocks.SKYREACH, ModBlocks.GILDROOT, ModBlocks.IRONBARK);

    public static final TagKey<Block> C_ORES_NIMBUSITE = TagKey.create(Registries.BLOCK, Tags.Blocks.ORES.location().withSuffix("/nimbusite"));
    public static final TagKey<Block> C_ORES_LEVITITE = TagKey.create(Registries.BLOCK, Tags.Blocks.ORES.location().withSuffix("/levitite"));
    public static final TagKey<Block> C_STORAGE_NIMBUSITE = TagKey.create(Registries.BLOCK, Tags.Blocks.STORAGE_BLOCKS.location().withSuffix("/nimbusite"));
    public static final TagKey<Block> C_STORAGE_RAW_NIMBUSITE = TagKey.create(Registries.BLOCK, Tags.Blocks.STORAGE_BLOCKS.location().withSuffix("/raw_nimbusite"));
    public static final TagKey<Block> C_STORAGE_LEVITITE = TagKey.create(Registries.BLOCK, Tags.Blocks.STORAGE_BLOCKS.location().withSuffix("/levitite"));

    public static class Blocks extends BlockTagsProvider {
        public Blocks(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper helper) {
            super(output, lookup, Upfall.MODID, helper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            var pickaxe = tag(BlockTags.MINEABLE_WITH_PICKAXE);
            for (Block block : new Block[]{ModBlocks.PEARLSTONE.get(), ModBlocks.COBBLED_PEARLSTONE.get(), ModBlocks.POLISHED_PEARLSTONE.get(),
                    ModBlocks.PEARLSTONE_BRICKS.get(), ModBlocks.CRACKED_PEARLSTONE_BRICKS.get(), ModBlocks.CHISELED_PEARLSTONE_BRICKS.get(),
                    ModBlocks.MOSSY_PEARLSTONE_BRICKS.get(), ModBlocks.DUSKSLATE.get(), ModBlocks.COBBLED_DUSKSLATE.get(),
                    ModBlocks.POLISHED_DUSKSLATE.get(), ModBlocks.DUSKSLATE_BRICKS.get(), ModBlocks.GILDED_DUSKSLATE.get(),
                    ModBlocks.UPDRAFT_VENT.get(), ModBlocks.MISTGLOW_CRYSTAL.get(), ModBlocks.NIMBUSITE_ORE.get(),
                    ModBlocks.DUSKSLATE_NIMBUSITE_ORE.get(), ModBlocks.LEVITITE_ORE.get(), ModBlocks.RAW_NIMBUSITE_BLOCK.get(),
                    ModBlocks.NIMBUSITE_BLOCK.get(), ModBlocks.LEVITITE_BLOCK.get(), ModBlocks.HALO_FRAME.get()}) {
                pickaxe.add(block);
            }
            for (ModBlocks.StoneVariants v : ALL_VARIANTS) {
                pickaxe.add(v.stairs().get(), v.slab().get(), v.wall().get());
                tag(BlockTags.STAIRS).add(v.stairs().get());
                tag(BlockTags.SLABS).add(v.slab().get());
                tag(BlockTags.WALLS).add(v.wall().get());
            }
            tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.SKY_CACHE.get());
            tag(BlockTags.MINEABLE_WITH_SHOVEL).add(ModBlocks.SKYSOIL.get(), ModBlocks.SKYGRASS_BLOCK.get(), ModBlocks.DENSE_CLOUD.get(), ModBlocks.MIST.get());

            tag(BlockTags.NEEDS_DIAMOND_TOOL).add(ModBlocks.HALO_FRAME.get());
            tag(Tags.Blocks.NEEDS_NETHERITE_TOOL).add(ModBlocks.NIMBUSITE_ORE.get(), ModBlocks.DUSKSLATE_NIMBUSITE_ORE.get(),
                    ModBlocks.RAW_NIMBUSITE_BLOCK.get(), ModBlocks.NIMBUSITE_BLOCK.get());
            tag(ModTags.Blocks.NEEDS_NIMBUSITE_TOOL).add(ModBlocks.LEVITITE_ORE.get(), ModBlocks.LEVITITE_BLOCK.get());
            for (TagKey<Block> incorrect : List.of(BlockTags.INCORRECT_FOR_WOODEN_TOOL, BlockTags.INCORRECT_FOR_STONE_TOOL,
                    BlockTags.INCORRECT_FOR_IRON_TOOL, BlockTags.INCORRECT_FOR_GOLD_TOOL, BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
                    BlockTags.INCORRECT_FOR_NETHERITE_TOOL)) {
                tag(incorrect).addTag(ModTags.Blocks.NEEDS_NIMBUSITE_TOOL);
            }
            tag(ModTags.Blocks.INCORRECT_FOR_NIMBUSITE_TOOL);
            tag(ModTags.Blocks.INCORRECT_FOR_LEVITITE_TOOL);

            for (ModBlocks.WoodSet set : WOODS) {
                TagKey<Block> logs = set == ModBlocks.SKYREACH ? ModTags.Blocks.SKYREACH_LOGS : set == ModBlocks.IRONBARK ? ModTags.Blocks.IRONBARK_LOGS : ModTags.Blocks.GILDROOT_LOGS;
                tag(logs).add(set.log().get(), set.wood().get(), set.strippedLog().get(), set.strippedWood().get());
                tag(BlockTags.LOGS_THAT_BURN).addTag(logs);
                tag(BlockTags.PLANKS).add(set.planks().get());
                tag(BlockTags.WOODEN_STAIRS).add(set.stairs().get());
                tag(BlockTags.WOODEN_SLABS).add(set.slab().get());
                tag(BlockTags.WOODEN_FENCES).add(set.fence().get());
                tag(BlockTags.FENCE_GATES).add(set.fenceGate().get());
                tag(Tags.Blocks.FENCE_GATES_WOODEN).add(set.fenceGate().get());
                tag(BlockTags.WOODEN_DOORS).add(set.door().get());
                tag(BlockTags.WOODEN_TRAPDOORS).add(set.trapdoor().get());
                tag(BlockTags.WOODEN_BUTTONS).add(set.button().get());
                tag(BlockTags.WOODEN_PRESSURE_PLATES).add(set.pressurePlate().get());
                tag(BlockTags.LEAVES).add(set.leaves().get());
                tag(BlockTags.MINEABLE_WITH_HOE).add(set.leaves().get());
                tag(BlockTags.SAPLINGS).add(set.sapling().get());
            }

            tag(BlockTags.SMALL_FLOWERS).add(ModBlocks.DUSKBLOOM.get());
            tag(BlockTags.FLOWER_POTS).add(ModBlocks.POTTED_DUSKBLOOM.get(), ModBlocks.POTTED_CLOUD_FERN.get(), ModBlocks.POTTED_SKYREACH_SAPLING.get(),
                    ModBlocks.POTTED_IRONBARK_SAPLING.get());
            tag(BlockTags.REPLACEABLE_BY_TREES).add(ModBlocks.CLOUD_FERN.get(), ModBlocks.GILDED_TUFT.get(), ModBlocks.MIST.get(),
                    ModBlocks.GLOWVINE.get(), ModBlocks.GLOWVINE_PLANT.get());
            tag(BlockTags.SWORD_EFFICIENT).add(ModBlocks.CLOUD_FERN.get(), ModBlocks.GILDED_TUFT.get(), ModBlocks.DUSKBLOOM.get(),
                    ModBlocks.GLOWVINE.get(), ModBlocks.GLOWVINE_PLANT.get());
            tag(BlockTags.CLIMBABLE).add(ModBlocks.GLOWVINE.get(), ModBlocks.GLOWVINE_PLANT.get());
            tag(BlockTags.DIRT).add(ModBlocks.SKYSOIL.get(), ModBlocks.SKYGRASS_BLOCK.get());
            tag(BlockTags.BEACON_BASE_BLOCKS).add(ModBlocks.NIMBUSITE_BLOCK.get(), ModBlocks.LEVITITE_BLOCK.get());

            tag(ModTags.Blocks.SKY_PLANTABLE).addTag(BlockTags.DIRT).add(ModBlocks.DENSE_CLOUD.get(), ModBlocks.DUSKSLATE.get());
            tag(ModTags.Blocks.UPFALL_STONE).add(ModBlocks.PEARLSTONE.get(), ModBlocks.DUSKSLATE.get());
            tag(ModTags.Blocks.FEATURE_REPLACEABLE).add(net.minecraft.world.level.block.Blocks.AIR, ModBlocks.MIST.get(),
                    ModBlocks.CLOUD_FERN.get(), ModBlocks.GILDED_TUFT.get(), ModBlocks.DUSKBLOOM.get(), ModBlocks.GLOWVINE.get(),
                    ModBlocks.GLOWVINE_PLANT.get(), ModBlocks.SKYREACH.leaves().get(), ModBlocks.GILDROOT.leaves().get(), ModBlocks.IRONBARK.leaves().get());
            tag(ModTags.Blocks.HALO_PORTAL_FRAMES).add(ModBlocks.HALO_FRAME.get());

            tag(C_ORES_NIMBUSITE).add(ModBlocks.NIMBUSITE_ORE.get(), ModBlocks.DUSKSLATE_NIMBUSITE_ORE.get());
            tag(C_ORES_LEVITITE).add(ModBlocks.LEVITITE_ORE.get());
            tag(Tags.Blocks.ORES).addTag(C_ORES_NIMBUSITE).addTag(C_ORES_LEVITITE);
            tag(C_STORAGE_NIMBUSITE).add(ModBlocks.NIMBUSITE_BLOCK.get());
            tag(C_STORAGE_RAW_NIMBUSITE).add(ModBlocks.RAW_NIMBUSITE_BLOCK.get());
            tag(C_STORAGE_LEVITITE).add(ModBlocks.LEVITITE_BLOCK.get());
            tag(Tags.Blocks.STORAGE_BLOCKS).addTag(C_STORAGE_NIMBUSITE).addTag(C_STORAGE_RAW_NIMBUSITE).addTag(C_STORAGE_LEVITITE);
            UpfallModules.forEachData(data -> data.blockTags(this));
        }

        @Override
        public IntrinsicTagAppender<Block> tag(TagKey<Block> tag) {
            return super.tag(tag);
        }
    }

    public static class Items extends ItemTagsProvider {
        public Items(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                     CompletableFuture<TagsProvider.TagLookup<Block>> blockTags, ExistingFileHelper helper) {
            super(output, lookup, blockTags, Upfall.MODID, helper);
        }

        private static TagKey<Item> c(TagKey<Item> parent, String suffix) {
            return TagKey.create(Registries.ITEM, parent.location().withSuffix("/" + suffix));
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            copy(ModTags.Blocks.SKYREACH_LOGS, ModTags.Items.SKYREACH_LOGS);
            copy(ModTags.Blocks.GILDROOT_LOGS, ModTags.Items.GILDROOT_LOGS);
            copy(ModTags.Blocks.IRONBARK_LOGS, ModTags.Items.IRONBARK_LOGS);
            copy(BlockTags.LOGS_THAT_BURN, ItemTags.LOGS_THAT_BURN);
            copy(BlockTags.PLANKS, ItemTags.PLANKS);
            copy(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
            copy(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
            copy(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES);
            copy(BlockTags.FENCE_GATES, ItemTags.FENCE_GATES);
            copy(Tags.Blocks.FENCE_GATES_WOODEN, Tags.Items.FENCE_GATES_WOODEN);
            copy(BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS);
            copy(BlockTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_TRAPDOORS);
            copy(BlockTags.WOODEN_BUTTONS, ItemTags.WOODEN_BUTTONS);
            copy(BlockTags.WOODEN_PRESSURE_PLATES, ItemTags.WOODEN_PRESSURE_PLATES);
            copy(BlockTags.LEAVES, ItemTags.LEAVES);
            copy(BlockTags.SAPLINGS, ItemTags.SAPLINGS);
            copy(BlockTags.SMALL_FLOWERS, ItemTags.SMALL_FLOWERS);
            copy(BlockTags.STAIRS, ItemTags.STAIRS);
            copy(BlockTags.SLABS, ItemTags.SLABS);
            copy(BlockTags.WALLS, ItemTags.WALLS);
            copy(BlockTags.DIRT, ItemTags.DIRT);
            copy(C_ORES_NIMBUSITE, c(Tags.Items.ORES, "nimbusite"));
            copy(C_ORES_LEVITITE, c(Tags.Items.ORES, "levitite"));
            copy(Tags.Blocks.ORES, Tags.Items.ORES);
            copy(C_STORAGE_NIMBUSITE, c(Tags.Items.STORAGE_BLOCKS, "nimbusite"));
            copy(C_STORAGE_RAW_NIMBUSITE, c(Tags.Items.STORAGE_BLOCKS, "raw_nimbusite"));
            copy(C_STORAGE_LEVITITE, c(Tags.Items.STORAGE_BLOCKS, "levitite"));
            copy(Tags.Blocks.STORAGE_BLOCKS, Tags.Items.STORAGE_BLOCKS);

            tag(ItemTags.SWORDS).add(ModItems.NIMBUSITE_SWORD.get(), ModItems.LEVITITE_SWORD.get());
            tag(ItemTags.PICKAXES).add(ModItems.NIMBUSITE_PICKAXE.get(), ModItems.LEVITITE_PICKAXE.get());
            tag(ItemTags.AXES).add(ModItems.NIMBUSITE_AXE.get(), ModItems.LEVITITE_AXE.get());
            tag(ItemTags.SHOVELS).add(ModItems.NIMBUSITE_SHOVEL.get(), ModItems.LEVITITE_SHOVEL.get());
            tag(ItemTags.HOES).add(ModItems.NIMBUSITE_HOE.get(), ModItems.LEVITITE_HOE.get());
            tag(ItemTags.HEAD_ARMOR).add(ModItems.NIMBUSITE_HELMET.get(), ModItems.LEVITITE_HELMET.get());
            tag(ItemTags.CHEST_ARMOR).add(ModItems.NIMBUSITE_CHESTPLATE.get(), ModItems.LEVITITE_CHESTPLATE.get());
            tag(ItemTags.LEG_ARMOR).add(ModItems.NIMBUSITE_LEGGINGS.get(), ModItems.LEVITITE_LEGGINGS.get());
            tag(ItemTags.FOOT_ARMOR).add(ModItems.NIMBUSITE_BOOTS.get(), ModItems.LEVITITE_BOOTS.get());
            tag(ModTags.Items.NIMBUSITE_ARMOR).add(ModItems.NIMBUSITE_HELMET.get(), ModItems.NIMBUSITE_CHESTPLATE.get(),
                    ModItems.NIMBUSITE_LEGGINGS.get(), ModItems.NIMBUSITE_BOOTS.get());
            tag(ModTags.Items.LEVITITE_ARMOR).add(ModItems.LEVITITE_HELMET.get(), ModItems.LEVITITE_CHESTPLATE.get(),
                    ModItems.LEVITITE_LEGGINGS.get(), ModItems.LEVITITE_BOOTS.get());
            tag(ItemTags.TRIMMABLE_ARMOR).addTag(ModTags.Items.NIMBUSITE_ARMOR).addTag(ModTags.Items.LEVITITE_ARMOR);
            tag(ItemTags.BEACON_PAYMENT_ITEMS).add(ModItems.NIMBUSITE_INGOT.get(), ModItems.LEVITITE_INGOT.get());

            tag(c(Tags.Items.INGOTS, "nimbusite")).add(ModItems.NIMBUSITE_INGOT.get());
            tag(c(Tags.Items.INGOTS, "levitite")).add(ModItems.LEVITITE_INGOT.get());
            tag(Tags.Items.INGOTS).addTag(c(Tags.Items.INGOTS, "nimbusite")).addTag(c(Tags.Items.INGOTS, "levitite"));
            tag(c(Tags.Items.RAW_MATERIALS, "nimbusite")).add(ModItems.RAW_NIMBUSITE.get());
            tag(Tags.Items.RAW_MATERIALS).addTag(c(Tags.Items.RAW_MATERIALS, "nimbusite"));
            tag(ItemTags.FOX_FOOD).add(ModItems.CLOUDBERRY.get());
            UpfallModules.forEachData(data -> data.itemTags(this));
        }

        @Override
        public IntrinsicTagAppender<Item> tag(TagKey<Item> tag) {
            return super.tag(tag);
        }

        @Override
        public void copy(TagKey<Block> blockTag, TagKey<Item> itemTag) {
            super.copy(blockTag, itemTag);
        }
    }
}
