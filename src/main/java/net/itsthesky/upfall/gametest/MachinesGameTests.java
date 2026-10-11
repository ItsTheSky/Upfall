package net.itsthesky.upfall.gametest;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.machines.MachinesBlocks;
import net.itsthesky.upfall.content.machines.MachinesItems;
import net.itsthesky.upfall.content.machines.block.GearliftBlock;
import net.itsthesky.upfall.content.rustworks.RustworksItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Upfall.MODID)
@PrefixGameTestTemplate(false)
public final class MachinesGameTests {
    private MachinesGameTests() {}

    private static ItemStack smith(GameTestHelper helper, Item template, Item base) {
        var level = helper.getLevel();
        var input = new SmithingRecipeInput(new ItemStack(template), new ItemStack(base), new ItemStack(RustworksItems.KAIRITE_GEAR.get()));
        return level.getRecipeManager().getRecipeFor(RecipeType.SMITHING, input, level)
                .map(holder -> holder.value().assemble(input, level.registryAccess()))
                .orElse(ItemStack.EMPTY);
    }

    private static void expect(GameTestHelper helper, Item template, Item base, Item result, int count) {
        ItemStack stack = smith(helper, template, base);
        helper.assertTrue(stack.is(result) && stack.getCount() == count, "Smithing " + template + " + " + base + " gave " + stack);
    }

    @GameTest(template = "empty")
    public static void machineSmithingRecipes(GameTestHelper helper) {
        expect(helper, MachinesItems.PNEUMATIC_PIPE_BLUEPRINT.get(), Items.COPPER_BLOCK, MachinesBlocks.PNEUMATIC_PIPE.get().asItem(), 8);
        expect(helper, MachinesItems.VOID_TETHER_BLUEPRINT.get(), Items.ENDER_PEARL, MachinesBlocks.VOID_TETHER.get().asItem(), 1);
        expect(helper, MachinesItems.REWINDER_BLUEPRINT.get(), Items.ANVIL, MachinesBlocks.REWINDER.get().asItem(), 1);
        expect(helper, MachinesItems.HASTENING_GEAR_BLUEPRINT.get(), Items.REDSTONE_BLOCK, MachinesBlocks.HASTENING_GEAR.get().asItem(), 1);
        expect(helper, MachinesItems.PHASE_WATCH_BLUEPRINT.get(), Items.CLOCK, MachinesItems.PHASE_WATCH.get(), 1);
        expect(helper, MachinesItems.GEARLIFT_BLUEPRINT.get(), Items.PISTON, MachinesBlocks.GEARLIFT.get().asItem(), 2);
        helper.assertTrue(smith(helper, MachinesItems.GEARLIFT_BLUEPRINT.get(), Items.ANVIL).isEmpty(), "Wrong base must not smith");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void machineCraftingRecipes(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager();
        for (String id : new String[]{"pneumatic_pipe_entrance", "pneumatic_pipe_exit"}) {
            helper.assertTrue(recipes.byKey(Upfall.id(id)).isPresent(), "Missing recipe " + id);
        }
        for (var blueprint : MachinesItems.blueprints()) {
            helper.assertTrue(recipes.byKey(blueprint.getId()).isPresent(), "Missing duplication recipe " + blueprint.getId());
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void gearliftTargets(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos base = helper.absolutePos(new BlockPos(1, 1, 1));
        var lift = MachinesBlocks.GEARLIFT.get().defaultBlockState();
        level.setBlockAndUpdate(base, lift);
        level.setBlockAndUpdate(base.above(10), lift);
        level.setBlockAndUpdate(base.above(30), lift);
        helper.assertTrue(base.above(10).equals(GearliftBlock.findTarget(level, base, true)), "Up should find the next lift");
        helper.assertTrue(base.equals(GearliftBlock.findTarget(level, base.above(10), false)), "Down should find the lift below");
        level.setBlockAndUpdate(base.above(11), Blocks.STONE.defaultBlockState());
        helper.assertTrue(base.above(30).equals(GearliftBlock.findTarget(level, base, true)), "Blocked lift must be skipped");
        level.setBlockAndUpdate(base.above(30).above(2), Blocks.STONE.defaultBlockState());
        helper.assertTrue(GearliftBlock.findTarget(level, base, true) == null, "No headroom means no target");
        helper.assertTrue(GearliftBlock.findTarget(level, base.above(30), true) == null, "Nothing above the last lift");
        helper.succeed();
    }
}
