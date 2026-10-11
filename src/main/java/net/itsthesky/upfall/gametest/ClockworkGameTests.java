package net.itsthesky.upfall.gametest;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.machines.MachinesBlocks;
import net.itsthesky.upfall.content.machines.block.HasteningGearBlock;
import net.itsthesky.upfall.content.machines.block.entity.HasteningGearBlockEntity;
import net.itsthesky.upfall.content.machines.block.entity.RewinderBlockEntity;
import net.itsthesky.upfall.content.machines.block.entity.VoidTetherBlockEntity;
import net.itsthesky.upfall.content.rustworks.RustworksItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

@GameTestHolder(Upfall.MODID)
@PrefixGameTestTemplate(false)
public final class ClockworkGameTests {
    private ClockworkGameTests() {}

    private static ItemEntity drop(GameTestHelper helper, Vec3 abs, ItemStack stack) {
        ItemEntity item = new ItemEntity(helper.getLevel(), abs.x, abs.y, abs.z, stack, 0.0, -1.0, 0.0);
        helper.getLevel().addFreshEntity(item);
        return item;
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void voidTetherCatchesLostItems(GameTestHelper helper) {
        helper.setBlock(new BlockPos(2, 2, 2), MachinesBlocks.VOID_TETHER.get().defaultBlockState());
        helper.setBlock(new BlockPos(3, 2, 2), Blocks.CHEST.defaultBlockState());
        BlockPos tether = helper.absolutePos(new BlockPos(2, 2, 2));
        Vec3 spawn = new Vec3(tether.getX() + 4.5, helper.getLevel().getMinBuildHeight() + 2.0, tether.getZ() + 0.5);
        ItemEntity item = drop(helper, spawn, new ItemStack(Items.DIAMOND, 5));
        helper.runAfterDelay(40, () -> {
            helper.assertTrue(item.isRemoved(), "The lost item entity should be gone");
            var chest = (ChestBlockEntity) helper.getBlockEntity(new BlockPos(3, 2, 2));
            int total = 0;
            for (int i = 0; i < chest.getContainerSize(); i++) {
                if (chest.getItem(i).is(Items.DIAMOND)) total += chest.getItem(i).getCount();
            }
            helper.assertTrue(total == 5, "Chest should hold the 5 diamonds, got " + total);
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void voidTetherKeepsRemainderOnTop(GameTestHelper helper) {
        helper.setBlock(new BlockPos(2, 2, 2), MachinesBlocks.VOID_TETHER.get().defaultBlockState());
        BlockPos tether = helper.absolutePos(new BlockPos(2, 2, 2));
        ItemEntity item = drop(helper, new Vec3(tether.getX() + 0.5, tether.getY() - 3.0, tether.getZ() + 0.5), new ItemStack(Items.EMERALD, 3));
        VoidTetherBlockEntity.catchItem(helper.getLevel(), tether, item);
        helper.assertTrue(!item.isRemoved() && item.getItem().getCount() == 3, "Without a container the stack stays");
        helper.assertTrue(item.getY() > tether.getY() + 1.0 && item.getY() < tether.getY() + 1.5, "The stack should sit on top of the tether, y=" + item.getY());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void voidTetherIgnoresSupportedItems(GameTestHelper helper) {
        helper.setBlock(new BlockPos(4, 1, 4), Blocks.STONE.defaultBlockState());
        BlockPos stone = helper.absolutePos(new BlockPos(4, 1, 4));
        ItemEntity above = new ItemEntity(helper.getLevel(), stone.getX() + 0.5, stone.getY() + 3.0, stone.getZ() + 0.5, new ItemStack(Items.DIAMOND), 0.0, -1.0, 0.0);
        helper.assertFalse(VoidTetherBlockEntity.isLost(helper.getLevel(), above), "An item above solid ground is not lost");
        ItemEntity deep = new ItemEntity(helper.getLevel(), stone.getX() + 0.5, helper.getLevel().getMinBuildHeight() + 1.0, stone.getZ() + 0.5, new ItemStack(Items.DIAMOND));
        helper.assertTrue(VoidTetherBlockEntity.isLost(helper.getLevel(), deep), "An item at the bottom of the world is lost");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 300)
    public static void rewinderRepairsSlowlyAndConsumesKairite(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, MachinesBlocks.REWINDER.get().defaultBlockState());
        var rewinder = (RewinderBlockEntity) helper.getBlockEntity(pos);
        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
        pickaxe.setDamageValue(10);
        helper.assertTrue(rewinder.canHold(pickaxe) && !rewinder.canHold(new ItemStack(Items.STONE)), "Only damageable items can be placed");
        rewinder.place(pickaxe);
        rewinder.setFuel(100);
        helper.runAfterDelay(130, () -> {
            int damage = rewinder.stack().getDamageValue();
            helper.assertTrue(damage == 7, "One point per 40 ticks, expected damage 7 got " + damage);
            helper.assertTrue(rewinder.fuel() == 97, "Each point costs one fuel, got " + rewinder.fuel());
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void rewinderStopsWithoutFuel(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, MachinesBlocks.REWINDER.get().defaultBlockState());
        var rewinder = (RewinderBlockEntity) helper.getBlockEntity(pos);
        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
        pickaxe.setDamageValue(10);
        rewinder.place(pickaxe);
        helper.runAfterDelay(90, () -> {
            helper.assertTrue(rewinder.stack().getDamageValue() == 10, "No fuel, no repair");
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void rewinderFuelAndComparator(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, MachinesBlocks.REWINDER.get().defaultBlockState());
        var rewinder = (RewinderBlockEntity) helper.getBlockEntity(pos);
        helper.assertTrue(rewinder.signal() == 0, "Empty rewinder gives no signal");
        helper.assertTrue(rewinder.accept(new ItemStack(RustworksItems.KAIRITE_DUST.get(), 3)) == 3 && rewinder.fuel() == 297, "Three dust give 297 points, got " + rewinder.fuel());
        rewinder.setFuel(0);
        helper.assertTrue(rewinder.accept(new ItemStack(RustworksItems.KAIRITE_GEAR.get(), 2)) == 1, "Only one gear fits in the buffer");
        helper.assertTrue(rewinder.accept(new ItemStack(Items.COAL)) == 0, "Only kairite is accepted");
        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
        pickaxe.setDamageValue(pickaxe.getMaxDamage() / 2);
        rewinder.place(pickaxe);
        helper.assertTrue(rewinder.signal() > 0 && rewinder.signal() < 15, "Half repaired gives a partial signal: " + rewinder.signal());
        rewinder.stack().setDamageValue(0);
        helper.assertTrue(rewinder.signal() == 15, "A repaired item gives 15");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 260)
    public static void hasteningGearSpeedsFurnace(GameTestHelper helper) {
        BlockPos hastened = new BlockPos(1, 2, 1);
        BlockPos control = new BlockPos(1, 2, 5);
        for (BlockPos pos : new BlockPos[] {hastened, control}) {
            helper.setBlock(pos, Blocks.FURNACE.defaultBlockState());
            var furnace = (AbstractFurnaceBlockEntity) helper.getBlockEntity(pos);
            furnace.setItem(0, new ItemStack(Items.RAW_IRON));
            furnace.setItem(1, new ItemStack(Items.COAL));
        }
        helper.setBlock(new BlockPos(2, 2, 1), MachinesBlocks.HASTENING_GEAR.get().defaultBlockState().setValue(HasteningGearBlock.FACING, Direction.WEST));
        var gear = (HasteningGearBlockEntity) helper.getBlockEntity(new BlockPos(2, 2, 1));
        gear.setFuel(5000);
        helper.runAfterDelay(170, () -> {
            var fast = (AbstractFurnaceBlockEntity) helper.getBlockEntity(hastened);
            var slow = (AbstractFurnaceBlockEntity) helper.getBlockEntity(control);
            helper.assertTrue(fast.getItem(2).is(Items.IRON_INGOT), "The hastened furnace should be done after 170 ticks");
            helper.assertTrue(slow.getItem(2).isEmpty(), "The control furnace should not be done after 170 ticks");
            helper.assertTrue(gear.fuel() < 5000 && gear.fuel() > 4500, "The gear should burn fuel only while accelerating, got " + gear.fuel());
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void hasteningGearIdlesWithoutTarget(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, MachinesBlocks.HASTENING_GEAR.get().defaultBlockState());
        var gear = (HasteningGearBlockEntity) helper.getBlockEntity(pos);
        gear.setFuel(1000);
        helper.runAfterDelay(60, () -> {
            helper.assertTrue(gear.fuel() == 1000, "An idle gear must not consume fuel, got " + gear.fuel());
            helper.assertFalse(helper.getBlockState(pos).getValue(HasteningGearBlock.ACTIVE), "An idle gear is not active");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void hasteningGearGrowsCrops(GameTestHelper helper) {
        for (int x = 2; x <= 4; x++) {
            for (int z = 2; z <= 4; z++) {
                helper.setBlock(new BlockPos(x, 2, z), Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7));
                helper.setBlock(new BlockPos(x, 3, z), Blocks.WHEAT.defaultBlockState());
            }
        }
        helper.setBlock(new BlockPos(1, 3, 1), Blocks.LIGHT.defaultBlockState());
        helper.setBlock(new BlockPos(5, 3, 5), Blocks.LIGHT.defaultBlockState());
        BlockPos gearPos = new BlockPos(3, 5, 3);
        helper.setBlock(gearPos, MachinesBlocks.HASTENING_GEAR.get().defaultBlockState().setValue(HasteningGearBlock.FACING, Direction.DOWN));
        var gear = (HasteningGearBlockEntity) helper.getBlockEntity(gearPos);
        gear.setFuel(1000);
        helper.runAfterDelay(25, () -> {
            helper.assertTrue(helper.getBlockState(gearPos).getValue(HasteningGearBlock.ACTIVE), "The gear should be active above growing crops");
            helper.assertTrue(gear.fuel() < 1000, "Active gear consumes fuel");
            BlockPos abs = helper.absolutePos(gearPos);
            for (int i = 0; i < 60; i++) {
                HasteningGearBlockEntity.boostCrops(helper.getLevel(), abs, Direction.DOWN, 1.0F);
            }
            int grown = 0;
            for (int x = 2; x <= 4; x++) {
                for (int z = 2; z <= 4; z++) {
                    if (helper.getBlockState(new BlockPos(x, 3, z)).getValue(CropBlock.AGE) > 0) grown++;
                }
            }
            helper.assertTrue(grown > 0, "Boosted random ticks should have grown some wheat");
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void hasteningGearAcceptsOnlyKairiteFromHoppers(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, MachinesBlocks.HASTENING_GEAR.get().defaultBlockState());
        var gear = (HasteningGearBlockEntity) helper.getBlockEntity(pos);
        IItemHandler handler = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(pos), Direction.UP);
        helper.assertTrue(handler != null, "The gear exposes an item handler");
        helper.assertTrue(handler.insertItem(0, new ItemStack(Items.COAL, 4), false).getCount() == 4, "Coal is refused");
        ItemStack left = handler.insertItem(0, new ItemStack(RustworksItems.KAIRITE_DUST.get(), 3), false);
        helper.assertTrue(left.isEmpty() && gear.fuel() == 3 * 9 * HasteningGearBlockEntity.UNIT_TICKS, "Three dust accepted, fuel " + gear.fuel());
        helper.succeed();
    }
}
