package net.itsthesky.upfall.gametest;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.enchanting.EnchantingEffects;
import net.itsthesky.upfall.content.enchanting.EnchantingModule;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Upfall.MODID)
@PrefixGameTestTemplate(false)
public final class EnchantingGameTests {
    private EnchantingGameTests() {}

    private static Holder<Enchantment> holder(GameTestHelper helper, ResourceKey<Enchantment> key) {
        return helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }

    private static ItemStack enchanted(GameTestHelper helper, Item item, ResourceKey<Enchantment> key, int level) {
        ItemStack stack = new ItemStack(item);
        stack.enchant(holder(helper, key), level);
        return stack;
    }

    @GameTest(template = "empty")
    public static void enchantmentsSupportTheRightItems(GameTestHelper helper) {
        Enchantment skywalker = holder(helper, EnchantingModule.SKYWALKER).value();
        Enchantment uplifted = holder(helper, EnchantingModule.UPLIFTED).value();
        Enchantment cloudsight = holder(helper, EnchantingModule.CLOUDSIGHT).value();
        Enchantment jumpstart = holder(helper, EnchantingModule.JUMPSTART).value();
        helper.assertTrue(skywalker.isSupportedItem(new ItemStack(Items.DIAMOND_BOOTS)), "Skywalker on boots");
        helper.assertTrue(skywalker.isSupportedItem(new ItemStack(ModItems.NIMBUSITE_BOOTS.get())), "Skywalker on nimbusite boots");
        helper.assertTrue(!skywalker.isSupportedItem(new ItemStack(Items.DIAMOND_HELMET)), "Skywalker not on helmets");
        helper.assertTrue(uplifted.isSupportedItem(new ItemStack(Items.NETHERITE_CHESTPLATE)), "Uplifted on chestplates");
        helper.assertTrue(!uplifted.isSupportedItem(new ItemStack(Items.NETHERITE_BOOTS)), "Uplifted not on boots");
        helper.assertTrue(cloudsight.isSupportedItem(new ItemStack(ModItems.LEVITITE_HELMET.get())), "Cloudsight on helmets");
        helper.assertTrue(cloudsight.getMaxLevel() == 1 && skywalker.getMaxLevel() == 3, "Max levels");
        helper.assertTrue(jumpstart.isSupportedItem(new ItemStack(ModItems.LEVITITE_BOOTS.get())), "Jumpstart on levitite boots");
        helper.assertTrue(!jumpstart.isSupportedItem(new ItemStack(ModItems.NIMBUSITE_BOOTS.get())), "Jumpstart only on levitite boots");
        helper.assertTrue(!jumpstart.isSupportedItem(new ItemStack(Items.DIAMOND_BOOTS)), "Jumpstart not on diamond boots");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void mistSlowsAndCloudsightCancels(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, ModBlocks.MIST.get());
        var state = ModBlocks.MIST.get().defaultBlockState();
        BlockPos abs = helper.absolutePos(pos);

        Player bare = helper.makeMockPlayer(GameType.SURVIVAL);
        bare.setDeltaMovement(new Vec3(1.0, 0.0, 0.0));
        state.entityInside(helper.getLevel(), abs, bare);
        helper.assertTrue(bare.getDeltaMovement().x < 0.9, "Mist must slow a bare player: " + bare.getDeltaMovement().x);

        Player seeing = helper.makeMockPlayer(GameType.SURVIVAL);
        seeing.setItemSlot(EquipmentSlot.HEAD, enchanted(helper, ModItems.NIMBUSITE_HELMET.get(), EnchantingModule.CLOUDSIGHT, 1));
        seeing.setDeltaMovement(new Vec3(1.0, 0.0, 0.0));
        state.entityInside(helper.getLevel(), abs, seeing);
        helper.assertTrue(seeing.getDeltaMovement().x == 1.0, "Cloudsight must cancel the slowdown: " + seeing.getDeltaMovement().x);
        helper.assertTrue(EnchantingEffects.hasCloudsight(seeing) && !EnchantingEffects.hasCloudsight(bare), "Cloudsight detection");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void upliftedIncreasesUpdraftLift(GameTestHelper helper) {
        BlockPos plain = new BlockPos(2, 1, 2);
        BlockPos boosted = new BlockPos(5, 1, 5);
        helper.setBlock(plain, ModBlocks.UPDRAFT_VENT.get());
        helper.setBlock(boosted, ModBlocks.UPDRAFT_VENT.get());
        helper.runAfterDelay(5, () -> {
            ArmorStand a = helper.spawn(EntityType.ARMOR_STAND, plain.above());
            ArmorStand b = helper.spawn(EntityType.ARMOR_STAND, boosted.above());
            b.setItemSlot(EquipmentSlot.CHEST, enchanted(helper, Items.NETHERITE_CHESTPLATE, EnchantingModule.UPLIFTED, 3));
            double startA = a.getY();
            double startB = b.getY();
            helper.runAfterDelay(4, () -> {
                double liftA = a.getY() - startA;
                double liftB = b.getY() - startB;
                helper.assertTrue(liftB > liftA + 0.5, "Uplifted must lift faster: plain " + liftA + ", uplifted " + liftB);
                helper.succeed();
            });
        });
    }

    @GameTest(template = "empty")
    public static void skywalkerSpeedsWalkingInMist(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(helper.absoluteVec(new Vec3(3.5, 1.0, 3.5)));
        player.setItemSlot(EquipmentSlot.FEET, enchanted(helper, Items.NETHERITE_BOOTS, EnchantingModule.SKYWALKER, 3));
        double base = player.getAttributeValue(Attributes.MOVEMENT_SPEED);

        EnchantingEffects.updateSkywalker(player);
        helper.assertTrue(player.getAttributeValue(Attributes.MOVEMENT_SPEED) == base, "No speed bonus outside mist");

        helper.setBlock(pos, ModBlocks.MIST.get());
        EnchantingEffects.updateSkywalker(player);
        double inMist = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
        helper.assertTrue(inMist > base * 2.0, "Skywalker III must drastically speed walking in mist: " + base + " -> " + inMist);

        var state = ModBlocks.MIST.get().defaultBlockState();
        player.setDeltaMovement(new Vec3(1.0, 0.0, 0.0));
        state.entityInside(helper.getLevel(), helper.absolutePos(pos), player);
        helper.assertTrue(player.getDeltaMovement().x == 1.0, "Skywalker III must cancel the mist slowdown");

        helper.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR);
        EnchantingEffects.updateSkywalker(player);
        helper.assertTrue(player.getAttributeValue(Attributes.MOVEMENT_SPEED) == base, "Bonus must end outside mist");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void jumpstartRaisesJumpStrength(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        double base = player.getAttributeValue(Attributes.JUMP_STRENGTH);
        ItemStack boots = enchanted(helper, ModItems.LEVITITE_BOOTS.get(), EnchantingModule.JUMPSTART, 3);
        player.setItemSlot(EquipmentSlot.FEET, boots);
        double[] bonus = {0.0};
        boots.forEachModifier(EquipmentSlot.FEET, (attribute, modifier) -> {
            if (attribute.is(Attributes.JUMP_STRENGTH.unwrapKey().orElseThrow())) bonus[0] += modifier.amount();
        });
        helper.assertTrue(bonus[0] > 0.15, "Jumpstart III must raise jump strength: " + base + " + " + bonus[0]);
        helper.succeed();
    }
}
