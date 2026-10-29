package net.itsthesky.upfall.gametest;

import net.itsthesky.upfall.UpfallConfig;
import net.itsthesky.upfall.compat.mc.McEntities;
import net.itsthesky.upfall.compat.mc.McLevel;
import net.itsthesky.upfall.content.chronospire.ChronoBlocks;
import net.itsthesky.upfall.content.chronospire.ChronoRegistries;
import net.itsthesky.upfall.content.mobs.MobItems;
import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.itsthesky.upfall.content.quantum.QuantumItems;
import net.itsthesky.upfall.content.quantum.QuantumRecipeTypes;
import net.itsthesky.upfall.content.quantum.pedestal.IncantationInput;
import net.itsthesky.upfall.content.quantum.pedestal.IncantationLogic;
import net.itsthesky.upfall.content.quantum.pedestal.PedestalBlockEntity;
import net.itsthesky.upfall.content.quantum.pedestal.QuantumIncantationRecipe;
import net.itsthesky.upfall.content.quantum.pedestal.QuantumPedestalBlockEntity;
import net.itsthesky.upfall.content.quantum.upgrade.QuantumUpgrade;
import net.itsthesky.upfall.content.quantum.upgrade.QuantumUpgrades;
import net.itsthesky.upfall.gametest.framework.UpfallGameTest;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class QuantumUpgradeGameTests {
    private QuantumUpgradeGameTests() {}

    private static final String PLAYER_BATCH = "quantum_pedestal";
    private static final BlockPos CENTER = new BlockPos(4, 1, 4);

    private static ItemStack stack(net.minecraft.world.level.ItemLike item) {
        return new ItemStack(item);
    }

    private static List<RecipeHolder<QuantumIncantationRecipe>> incantations(ServerLevel level) {
        //? if >=26.1 {
        /*return List.copyOf(McLevel.serverRecipes(level).recipeMap().byType(QuantumRecipeTypes.INCANTATION.get()));
        *///?} else {
        return McLevel.serverRecipes(level).getAllRecipesFor(QuantumRecipeTypes.INCANTATION.get());
        //?}
    }

    private static List<ItemStack> stones() {
        return List.of(stack(QuantumItems.QUANTUM_STONE.get()), stack(QuantumItems.QUANTUM_STONE.get()),
                stack(QuantumItems.QUANTUM_STONE.get()), stack(QuantumItems.QUANTUM_STONE.get()));
    }

    private static List<ItemStack> uncertaintySides() {
        return List.of(stack(MobItems.MIST_GEL.get()), stack(QuantumItems.BOTTLED_VEIL_GAS.get()),
                stack(MobItems.MIST_GEL.get()), stack(QuantumItems.BOTTLED_VEIL_GAS.get()));
    }

    @UpfallGameTest(template = "empty")
    public static void incantationRecipesMatch(GameTestHelper helper) {
        var level = helper.getLevel();
        var recipes = incantations(level);
        Set<QuantumUpgrade> upgrades = EnumSet.noneOf(QuantumUpgrade.class);
        for (RecipeHolder<QuantumIncantationRecipe> holder : recipes) {
            QuantumIncantationRecipe recipe = holder.value();
            upgrades.add(recipe.upgrade());
            List<ItemStack> sides = new ArrayList<>(recipe.ingredients().stream().map(i -> IncantationLogic.stacks(i).get(0).copy()).toList());
            Collections.reverse(sides);
            ItemStack base = IncantationLogic.stacks(recipe.base()).get(0).copy();
            IncantationInput input = new IncantationInput(base, sides, stones());
            var found = McLevel.serverRecipes(level).getRecipeFor(QuantumRecipeTypes.INCANTATION.get(), input, level);
            helper.assertTrue(found.isPresent() && found.get().value().upgrade() == recipe.upgrade(), "Recipe does not match itself: " + holder.id());
            helper.assertTrue(recipe.upgrade().canApplyTo(base), "Recipe base cannot take its upgrade: " + holder.id());
        }
        helper.assertTrue(upgrades.size() == QuantumUpgrade.values().length, "Every upgrade needs an incantation, got " + upgrades);

        ItemStack chestplate = stack(ModItems.LEVITITE_CHESTPLATE.get());
        var shuffled = McLevel.serverRecipes(level).getRecipeFor(QuantumRecipeTypes.INCANTATION.get(),
                new IncantationInput(chestplate, uncertaintySides(), stones()), level);
        helper.assertTrue(shuffled.isPresent() && shuffled.get().value().upgrade() == QuantumUpgrade.UNCERTAINTY, "Side ingredients are shapeless");
        List<ItemStack> wrongCorners = new ArrayList<>(stones());
        wrongCorners.set(2, stack(Items.DIAMOND));
        helper.assertTrue(McLevel.serverRecipes(level).getRecipeFor(QuantumRecipeTypes.INCANTATION.get(),
                new IncantationInput(chestplate, uncertaintySides(), wrongCorners), level).isEmpty(), "Corners must all be Quantum Stones");
        List<ItemStack> missing = new ArrayList<>(uncertaintySides());
        missing.set(0, ItemStack.EMPTY);
        helper.assertTrue(McLevel.serverRecipes(level).getRecipeFor(QuantumRecipeTypes.INCANTATION.get(),
                new IncantationInput(chestplate, missing, stones()), level).isEmpty(), "Every side ingredient is required");
        helper.succeed();
    }

    @UpfallGameTest(template = "empty")
    public static void rewindIsCraftableInSurvival(GameTestHelper helper) {
        var recipe = incantations(helper.getLevel()).stream()
                .map(RecipeHolder::value).filter(r -> r.upgrade() == QuantumUpgrade.REWIND).findFirst().orElseThrow();
        helper.assertTrue(recipe.ingredients().get(0).test(stack(ChronoRegistries.REWINDING_HOURGLASS.get())), "Rewind starts with the hourglass");
        helper.assertTrue(recipe.ingredients().stream().skip(1).allMatch(i -> i.test(stack(ChronoBlocks.HOUR_DIAL.get()))),
                "Rewind takes three Hour Dials: " + recipe.ingredients());
        helper.succeed();
    }

    @UpfallGameTest(template = "empty")
    public static void upgradeLimits(GameTestHelper helper) {
        ItemStack chestplate = stack(ModItems.LEVITITE_CHESTPLATE.get());
        helper.assertTrue(QuantumUpgrades.canApply(chestplate, QuantumUpgrade.UNCERTAINTY) == QuantumUpgrades.Refusal.NONE, "Fresh piece accepts");
        QuantumUpgrades.apply(chestplate, QuantumUpgrade.UNCERTAINTY);
        helper.assertTrue(QuantumUpgrades.canApply(chestplate, QuantumUpgrade.UNCERTAINTY) == QuantumUpgrades.Refusal.DUPLICATE, "No duplicates");
        QuantumUpgrades.apply(chestplate, QuantumUpgrade.ZIPLINE);
        helper.assertTrue(QuantumUpgrades.canApply(chestplate, QuantumUpgrade.REWIND) == QuantumUpgrades.Refusal.FULL, "Max two upgrades");
        helper.assertTrue(QuantumUpgrades.canApply(stack(ModItems.LEVITITE_HELMET.get()), QuantumUpgrade.UNCERTAINTY)
                == QuantumUpgrades.Refusal.NOT_APPLICABLE, "Uncertainty is chestplate only");
        helper.assertTrue(QuantumUpgrades.canApply(stack(Items.DIAMOND_CHESTPLATE), QuantumUpgrade.REWIND)
                == QuantumUpgrades.Refusal.NOT_APPLICABLE, "Only Levitite equipment");
        helper.succeed();
    }

    private static Pig dummy(GameTestHelper helper, BlockPos pos) {
        Pig pig = helper.spawn(EntityType.PIG, pos);
        pig.setNoAi(true);
        pig.setDeltaMovement(Vec3.ZERO);
        return pig;
    }

    @UpfallGameTest(template = "empty")
    public static void anchoredCancelsKnockback(GameTestHelper helper) {
        Pig anchored = dummy(helper, new BlockPos(2, 1, 2));
        ItemStack leggings = stack(ModItems.LEVITITE_LEGGINGS.get());
        QuantumUpgrades.apply(leggings, QuantumUpgrade.ANCHORED);
        anchored.setItemSlot(EquipmentSlot.LEGS, leggings);
        Pig plain = dummy(helper, new BlockPos(5, 1, 5));
        anchored.knockback(1.0, 1.0, 0.0);
        plain.knockback(1.0, 1.0, 0.0);
        helper.assertTrue(anchored.getDeltaMovement().horizontalDistanceSqr() < 1.0E-6, "Anchored should cancel knockback");
        helper.assertTrue(plain.getDeltaMovement().horizontalDistanceSqr() > 0.1, "Baseline knockback missing");
        anchored.discard();
        plain.discard();
        helper.succeed();
    }

    @UpfallGameTest(template = "empty")
    public static void uncertaintyDodges(GameTestHelper helper) {
        if (!UpfallConfig.SPEC.isLoaded()) {
            helper.succeed();
            return;
        }
        Pig target = dummy(helper, new BlockPos(2, 1, 2));
        ItemStack chestplate = stack(ModItems.LEVITITE_CHESTPLATE.get());
        QuantumUpgrades.apply(chestplate, QuantumUpgrade.UNCERTAINTY);
        target.setItemSlot(EquipmentSlot.CHEST, chestplate);
        Pig attacker = dummy(helper, new BlockPos(5, 1, 5));
        double chance = UpfallConfig.UNCERTAINTY_CHANCE.get();
        try {
            UpfallConfig.UNCERTAINTY_CHANCE.set(1.0);
            helper.assertFalse(McEntities.hurt(target, target.damageSources().mobAttack(attacker), 4.0F), "A certain dodge should cancel the hit");
            helper.assertTrue(target.getHealth() == target.getMaxHealth(), "Dodged hit must not hurt");
            UpfallConfig.UNCERTAINTY_CHANCE.set(0.0);
            helper.assertTrue(McEntities.hurt(target, target.damageSources().mobAttack(attacker), 4.0F), "Without the dodge the hit lands");
        } finally {
            UpfallConfig.UNCERTAINTY_CHANCE.set(chance);
        }
        target.discard();
        attacker.discard();
        helper.succeed();
    }

    @SuppressWarnings("removal")
    private static ServerPlayer player(GameTestHelper helper, Vec3 relativeFeet, BlockPos lookAt) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Vec3 feet = helper.absoluteVec(relativeFeet);
        McEntities.snapTo(player, feet.x, feet.y, feet.z);
        player.setItemInHand(InteractionHand.OFF_HAND, stack(Items.TORCH));
        QuantumGameTests.look(helper, player, lookAt);
        return player;
    }

    private static QuantumPedestalBlockEntity ritualSetup(GameTestHelper helper) {
        helper.setBlock(CENTER, QuantumBlocks.QUANTUM_PEDESTAL.get());
        BlockPos center = helper.absolutePos(CENTER);
        List<BlockPos> sides = QuantumPedestalBlockEntity.sides(center);
        List<BlockPos> corners = QuantumPedestalBlockEntity.corners(center);
        List<ItemStack> sideItems = uncertaintySides();
        for (int i = 0; i < 4; i++) {
            helper.getLevel().setBlockAndUpdate(sides.get(i), QuantumBlocks.PEDESTAL.get().defaultBlockState());
            helper.getLevel().setBlockAndUpdate(corners.get(i), QuantumBlocks.PEDESTAL.get().defaultBlockState());
            ((PedestalBlockEntity) helper.getLevel().getBlockEntity(sides.get(i))).setItem(sideItems.get(i).copy());
            ((PedestalBlockEntity) helper.getLevel().getBlockEntity(corners.get(i))).setItem(stack(QuantumItems.QUANTUM_STONE.get()));
        }
        QuantumPedestalBlockEntity pedestal = (QuantumPedestalBlockEntity) helper.getLevel().getBlockEntity(center);
        pedestal.setItem(stack(ModItems.LEVITITE_CHESTPLATE.get()));
        return pedestal;
    }

    private static boolean ringEmpty(GameTestHelper helper) {
        BlockPos center = helper.absolutePos(CENTER);
        List<BlockPos> ring = new ArrayList<>(QuantumPedestalBlockEntity.sides(center));
        ring.addAll(QuantumPedestalBlockEntity.corners(center));
        return ring.stream().allMatch(pos -> ((PedestalBlockEntity) helper.getLevel().getBlockEntity(pos)).item().isEmpty());
    }

    @UpfallGameTest(template = "empty", timeoutTicks = 300, batch = PLAYER_BATCH)
    public static void incantationSucceeds(GameTestHelper helper) {
        QuantumPedestalBlockEntity pedestal = ritualSetup(helper);
        ServerPlayer player = player(helper, new Vec3(4.5, 1.0, 0.3), CENTER);
        helper.assertTrue(pedestal.tryStart(player) == null, "Incantation should start");
        helper.assertTrue(pedestal.active(), "Ritual should be active");
        helper.assertTrue(pedestal.tryStart(player) != null, "A second start must be refused");
        int duration = UpfallConfig.getInt(UpfallConfig.RITUAL_TICKS);
        helper.runAfterDelay(duration + 5, () -> {
            helper.assertFalse(pedestal.active(), "Ritual should be over");
            helper.assertTrue(QuantumUpgrades.has(pedestal.item(), QuantumUpgrade.UNCERTAINTY), "Upgrade should be applied");
            helper.assertTrue(ringEmpty(helper), "Ingredients and stones should be consumed");
            BlockPos center = helper.absolutePos(CENTER);
            List<BlockPos> sides = QuantumPedestalBlockEntity.sides(center);
            List<BlockPos> corners = QuantumPedestalBlockEntity.corners(center);
            List<ItemStack> sideItems = uncertaintySides();
            for (int i = 0; i < 4; i++) {
                ((PedestalBlockEntity) helper.getLevel().getBlockEntity(sides.get(i))).setItem(sideItems.get(i).copy());
                ((PedestalBlockEntity) helper.getLevel().getBlockEntity(corners.get(i))).setItem(stack(QuantumItems.QUANTUM_STONE.get()));
            }
            helper.assertTrue(pedestal.tryStart(player) != null && !pedestal.active(), "A duplicate upgrade must be refused");
            QuantumGameTests.leave(helper, player);
            helper.succeed();
        });
    }

    @UpfallGameTest(template = "empty", timeoutTicks = 200, batch = PLAYER_BATCH)
    public static void incantationCollapsesWhenLookingAway(GameTestHelper helper) {
        QuantumPedestalBlockEntity pedestal = ritualSetup(helper);
        ServerPlayer player = player(helper, new Vec3(4.5, 1.0, 0.3), CENTER);
        helper.assertTrue(pedestal.tryStart(player) == null, "Incantation should start");
        helper.runAfterDelay(20, () -> {
            helper.assertTrue(pedestal.active(), "Ritual should still run while observed");
            QuantumGameTests.look(helper, player, new BlockPos(4, 1, -20));
        });
        helper.runAfterDelay(30, () -> {
            helper.assertFalse(pedestal.active(), "Looking away should cancel the ritual");
            helper.assertTrue(pedestal.item().is(ModItems.LEVITITE_CHESTPLATE.get()), "The piece is kept");
            helper.assertTrue(QuantumUpgrades.get(pedestal.item()).upgrades().isEmpty(), "No upgrade on a failed ritual");
            helper.assertTrue(ringEmpty(helper), "Ingredients and stones are lost");
            QuantumGameTests.leave(helper, player);
            helper.succeed();
        });
    }

    @UpfallGameTest(template = "empty", batch = PLAYER_BATCH)
    public static void superpositionBreaksAdjacentOre(GameTestHelper helper) {
        BlockPos a = new BlockPos(2, 2, 2);
        BlockPos b = a.east();
        BlockPos c = b.east();
        for (BlockPos pos : List.of(a, b, c)) helper.setBlock(pos, ModBlocks.NIMBUSITE_ORE.get());
        helper.setBlock(a.north(), Blocks.STONE);
        ServerPlayer player = player(helper, new Vec3(2.5, 1.0, 5.5), a);
        ItemStack pickaxe = stack(ModItems.LEVITITE_PICKAXE.get());
        QuantumUpgrades.apply(pickaxe, QuantumUpgrade.SUPERPOSITION);
        player.setItemInHand(InteractionHand.MAIN_HAND, pickaxe);
        player.gameMode.destroyBlock(helper.absolutePos(a));
        QuantumGameTests.leave(helper, player);
        helper.assertBlockNotPresent(ModBlocks.NIMBUSITE_ORE.get(), a);
        helper.assertBlockNotPresent(ModBlocks.NIMBUSITE_ORE.get(), b);
        helper.assertBlockPresent(ModBlocks.NIMBUSITE_ORE.get(), c);
        helper.assertBlockPresent(Blocks.STONE, a.north());
        helper.succeed();
    }
}
