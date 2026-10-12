package net.itsthesky.upfall.datagen;

import net.itsthesky.upfall.registry.ModBlocks;
import net.minecraft.data.BlockFamily;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public final class ModBlockFamilies {
    private ModBlockFamilies() {}

    public static List<BlockFamily> all() {
        List<BlockFamily> families = new ArrayList<>();
        families.add(stone(ModBlocks.PEARLSTONE.get(), ModBlocks.PEARLSTONE_VARIANTS));
        families.add(stone(ModBlocks.COBBLED_PEARLSTONE.get(), ModBlocks.COBBLED_PEARLSTONE_VARIANTS));
        families.add(stone(ModBlocks.POLISHED_PEARLSTONE.get(), ModBlocks.POLISHED_PEARLSTONE_VARIANTS));
        families.add(new BlockFamily.Builder(ModBlocks.PEARLSTONE_BRICKS.get())
                .stairs(ModBlocks.PEARLSTONE_BRICK_VARIANTS.stairs().get()).slab(ModBlocks.PEARLSTONE_BRICK_VARIANTS.slab().get())
                .wall(ModBlocks.PEARLSTONE_BRICK_VARIANTS.wall().get())
                .cracked(ModBlocks.CRACKED_PEARLSTONE_BRICKS.get()).chiseled(ModBlocks.CHISELED_PEARLSTONE_BRICKS.get())
                .getFamily());
        families.add(stone(ModBlocks.MOSSY_PEARLSTONE_BRICKS.get(), ModBlocks.MOSSY_PEARLSTONE_BRICK_VARIANTS));
        families.add(stone(ModBlocks.DUSKSLATE.get(), ModBlocks.DUSKSLATE_VARIANTS));
        families.add(stone(ModBlocks.COBBLED_DUSKSLATE.get(), ModBlocks.COBBLED_DUSKSLATE_VARIANTS));
        families.add(stone(ModBlocks.POLISHED_DUSKSLATE.get(), ModBlocks.POLISHED_DUSKSLATE_VARIANTS));
        families.add(stone(ModBlocks.DUSKSLATE_BRICKS.get(), ModBlocks.DUSKSLATE_BRICK_VARIANTS));
        families.add(wood(ModBlocks.SKYREACH));
        families.add(wood(ModBlocks.GILDROOT));
        families.add(wood(ModBlocks.IRONBARK));
        return families;
    }

    private static BlockFamily stone(Block base, ModBlocks.StoneVariants variants) {
        return new BlockFamily.Builder(base)
                .stairs(variants.stairs().get()).slab(variants.slab().get()).wall(variants.wall().get())
                .getFamily();
    }

    private static BlockFamily wood(ModBlocks.WoodSet set) {
        return new BlockFamily.Builder(set.planks().get())
                .stairs(set.stairs().get()).slab(set.slab().get()).fence(set.fence().get()).fenceGate(set.fenceGate().get())
                .door(set.door().get()).trapdoor(set.trapdoor().get()).button(set.button().get())
                .pressurePlate(set.pressurePlate().get())
                .recipeGroupPrefix("wooden").recipeUnlockedBy("has_planks")
                .getFamily();
    }
}
