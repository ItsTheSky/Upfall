package net.itsthesky.upfall.gametest;

import net.itsthesky.upfall.compat.mc.McLevel;
import net.itsthesky.upfall.content.gravity.SetBonusTooltips;
import net.itsthesky.upfall.gameplay.ArmorSetBonuses;
import net.itsthesky.upfall.gametest.framework.GameTestCompat;
import net.itsthesky.upfall.gametest.framework.UpfallGameTest;
import net.itsthesky.upfall.registry.ModItems;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.GameType;

import java.util.Arrays;
import java.util.List;

public final class GearGameTests {
    private GearGameTests() {}

    private static ItemStack smith(GameTestHelper helper, Item template, Item base, Item addition) {
        var level = helper.getLevel();
        var input = new SmithingRecipeInput(new ItemStack(template), new ItemStack(base), new ItemStack(addition));
        return McLevel.serverRecipes(level).getRecipeFor(RecipeType.SMITHING, input, level)
                //? if >=26.1 {
                /*.map(holder -> holder.value().assemble(input))
                *///?} else {
                .map(holder -> holder.value().assemble(input, level.registryAccess()))
                //?}
                .orElse(ItemStack.EMPTY);
    }

    @UpfallGameTest(template = "empty")
    public static void smithingUpgradeChain(GameTestHelper helper) {
        ItemStack nimbusite = smith(helper, ModItems.NIMBUSITE_UPGRADE_SMITHING_TEMPLATE.get(), Items.NETHERITE_SWORD, ModItems.NIMBUSITE_INGOT.get());
        helper.assertTrue(nimbusite.is(ModItems.NIMBUSITE_SWORD.get()), "Netherite sword did not upgrade to Nimbusite: " + nimbusite);
        ItemStack levitite = smith(helper, ModItems.LEVITITE_UPGRADE_SMITHING_TEMPLATE.get(), ModItems.NIMBUSITE_CHESTPLATE.get(), ModItems.LEVITITE_INGOT.get());
        helper.assertTrue(levitite.is(ModItems.LEVITITE_CHESTPLATE.get()), "Nimbusite chestplate did not upgrade to Levitite: " + levitite);
        ItemStack wrong = smith(helper, ModItems.LEVITITE_UPGRADE_SMITHING_TEMPLATE.get(), Items.NETHERITE_SWORD, ModItems.LEVITITE_INGOT.get());
        helper.assertTrue(wrong.isEmpty(), "Levitite must not upgrade netherite directly");
        helper.succeed();
    }

    private static double armorValue(Item helmet) {
        double[] total = {0.0};
        helmet.getDefaultInstance().forEachModifier(EquipmentSlot.HEAD, (attribute, modifier) -> {
            if (attribute.is(Attributes.ARMOR.unwrapKey().orElseThrow())) total[0] += modifier.amount();
        });
        return total[0];
    }

    @UpfallGameTest(template = "empty")
    public static void tierStatsBeatNetherite(GameTestHelper helper) {
        helper.assertTrue(ModItems.NIMBUSITE_SWORD.get().getDefaultInstance().getMaxDamage() > Items.NETHERITE_SWORD.getDefaultInstance().getMaxDamage(),
                "Nimbusite durability must exceed netherite");
        helper.assertTrue(ModItems.LEVITITE_SWORD.get().getDefaultInstance().getMaxDamage() > ModItems.NIMBUSITE_SWORD.get().getDefaultInstance().getMaxDamage(),
                "Levitite durability must exceed nimbusite");
        helper.assertTrue(armorValue(ModItems.LEVITITE_HELMET.get()) > armorValue(Items.NETHERITE_HELMET), "Levitite helmet must beat netherite");
        helper.succeed();
    }

    @UpfallGameTest(template = "empty")
    public static void miningTiers(GameTestHelper helper) {
        var nimbusiteOre = net.itsthesky.upfall.registry.ModBlocks.NIMBUSITE_ORE.get().defaultBlockState();
        var levititeOre = net.itsthesky.upfall.registry.ModBlocks.LEVITITE_ORE.get().defaultBlockState();
        helper.assertFalse(new ItemStack(Items.DIAMOND_PICKAXE).isCorrectToolForDrops(nimbusiteOre), "Diamond must not mine Nimbusite ore");
        helper.assertTrue(new ItemStack(Items.NETHERITE_PICKAXE).isCorrectToolForDrops(nimbusiteOre), "Netherite must mine Nimbusite ore");
        helper.assertFalse(new ItemStack(Items.NETHERITE_PICKAXE).isCorrectToolForDrops(levititeOre), "Netherite must not mine Levitite ore");
        helper.assertTrue(new ItemStack(ModItems.NIMBUSITE_PICKAXE.get()).isCorrectToolForDrops(levititeOre), "Nimbusite must mine Levitite ore");
        helper.assertTrue(new ItemStack(ModItems.LEVITITE_PICKAXE.get()).isCorrectToolForDrops(levititeOre), "Levitite must mine Levitite ore");
        helper.succeed();
    }

    @SuppressWarnings("removal")
    private static ServerPlayer playerWith(GameTestHelper helper, Item helmet, Item chest, Item legs, Item boots) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(helmet));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(chest));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(legs));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(boots));
        player.setHealth(20.0F);
        return player;
    }

    @UpfallGameTest(template = "empty", timeoutTicks = 200)
    public static void fallDamageSetBonuses(GameTestHelper helper) {
        ServerPlayer plain = playerWith(helper, Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS);
        ServerPlayer nimbusite = playerWith(helper, ModItems.NIMBUSITE_HELMET.get(), ModItems.NIMBUSITE_CHESTPLATE.get(),
                ModItems.NIMBUSITE_LEGGINGS.get(), ModItems.NIMBUSITE_BOOTS.get());
        ServerPlayer levitite = playerWith(helper, ModItems.LEVITITE_HELMET.get(), ModItems.LEVITITE_CHESTPLATE.get(),
                ModItems.LEVITITE_LEGGINGS.get(), ModItems.LEVITITE_BOOTS.get());
        helper.runAfterDelay(80, () -> {
            for (ServerPlayer player : new ServerPlayer[]{plain, nimbusite, levitite}) {
                player.causeFallDamage(15.0F, 1.0F, player.damageSources().fall());
            }
            checkFall(helper, plain, nimbusite, levitite);
        });
    }

    @UpfallGameTest(template = "empty")
    public static void doubleJumpMotion(GameTestHelper helper) {
        Vec3 standing = ArmorSetBonuses.doubleJumpMotion(Vec3.ZERO, 0.0F, 0.0F, false);
        helper.assertTrue(standing.y == ArmorSetBonuses.DOUBLE_JUMP_VELOCITY && standing.horizontalDistance() == 0.0,
                "Without forward input the double jump is purely vertical: " + standing);
        Vec3 sprinting = ArmorSetBonuses.doubleJumpMotion(new Vec3(0.0, -0.3, 0.28), 0.0F, 1.0F, false);
        helper.assertTrue(sprinting.z > 0.85 && sprinting.horizontalDistance() <= ArmorSetBonuses.DOUBLE_JUMP_MAX_HORIZONTAL_SPEED + 1.0E-9,
                "Moving forward adds a capped forward impulse along the look direction (+Z at yaw 0): " + sprinting);
        Vec3 inverted = ArmorSetBonuses.doubleJumpMotion(Vec3.ZERO, 90.0F, 1.0F, true);
        helper.assertTrue(inverted.y < 0.0 && inverted.x < -0.7, "Under inverted gravity the burst goes down, still forward (-X at yaw 90): " + inverted);
        helper.succeed();
    }

    private static boolean mentions(Component component, String key) {
        if (component.getContents() instanceof TranslatableContents contents && contents.getKey().equals(key)) return true;
        return component.getSiblings().stream().anyMatch(sibling -> mentions(sibling, key));
    }

    private static Component line(GameTestHelper helper, List<Component> tooltip, String key) {
        return tooltip.stream().filter(line -> mentions(line, key)).findFirst()
                .orElseThrow(() -> GameTestCompat.failure(helper, "Missing tooltip line " + key + " in " + tooltip));
    }

    @UpfallGameTest(template = "empty")
    public static void setBonusTooltips(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.LEVITITE_HELMET.get()));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.LEVITITE_CHESTPLATE.get()));
        var context = Item.TooltipContext.of(helper.getLevel());
        List<Component> levitite = new ItemStack(ModItems.LEVITITE_BOOTS.get()).getTooltipLines(context, player, TooltipFlag.NORMAL);
        Component header = line(helper, levitite, SetBonusTooltips.HEADER_WORN);
        Object[] args = ((TranslatableContents) header.getContents()).getArgs();
        helper.assertTrue(args.length == 2 && args[0] instanceof Component worn && worn.getString().equals("2") && Integer.valueOf(4).equals(args[1]),
                "The header should count 2 of 4 Levitite pieces: " + Arrays.toString(args));
        line(helper, levitite, SetBonusTooltips.LEVITITE_NO_FALL_DAMAGE);
        line(helper, levitite, SetBonusTooltips.LEVITITE_DOUBLE_JUMP);
        line(helper, levitite, SetBonusTooltips.LEVITITE_GLIDE);
        int headerIndex = levitite.indexOf(header);
        int modifiers = 0;
        for (int i = 0; i < levitite.size(); i++) {
            if (levitite.get(i).getContents() instanceof TranslatableContents contents && contents.getKey().startsWith("item.modifiers.")) modifiers = i;
        }
        helper.assertTrue(headerIndex > 0 && headerIndex < modifiers, "The set bonus sits before the attribute modifiers: " + levitite);

        List<Component> nimbusite = new ItemStack(ModItems.NIMBUSITE_LEGGINGS.get()).getTooltipLines(context, player, TooltipFlag.NORMAL);
        line(helper, nimbusite, SetBonusTooltips.NIMBUSITE_FALL_DAMAGE);
        List<Component> anonymous = new ItemStack(ModItems.NIMBUSITE_LEGGINGS.get()).getTooltipLines(context, null, TooltipFlag.NORMAL);
        line(helper, anonymous, SetBonusTooltips.HEADER);
        List<Component> sword = new ItemStack(ModItems.LEVITITE_SWORD.get()).getTooltipLines(context, player, TooltipFlag.NORMAL);
        helper.assertFalse(sword.stream().anyMatch(l -> mentions(l, SetBonusTooltips.HEADER_WORN)), "Only armor pieces describe the set bonus");
        helper.succeed();
    }

    private static void checkFall(GameTestHelper helper, ServerPlayer plain, ServerPlayer nimbusite, ServerPlayer levitite) {
        float plainLoss = 20.0F - plain.getHealth();
        float nimbusiteLoss = 20.0F - nimbusite.getHealth();
        helper.assertTrue(plainLoss >= 11.0F, "Reference fall damage too low: " + plainLoss);
        helper.assertTrue(nimbusiteLoss <= Math.ceil(plainLoss * 0.25F) && nimbusiteLoss > 0, "Nimbusite should cut fall damage by 75%: " + nimbusiteLoss);
        helper.assertTrue(levitite.getHealth() == 20.0F, "Levitite should negate fall damage: " + levitite.getHealth());
        helper.succeed();
    }
}
