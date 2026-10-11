package net.itsthesky.upfall.gametest;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.mobs.MobEntities;
import net.itsthesky.upfall.content.mobs.MobItems;
import net.itsthesky.upfall.content.mobs.entity.Skygrazer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(Upfall.MODID)
@PrefixGameTestTemplate(false)
public final class MobsGameTests {
    private MobsGameTests() {}

    private static List<ItemStack> roll(GameTestHelper helper, Mob mob) {
        var table = helper.getLevel().getServer().reloadableRegistries().getLootTable(mob.getType().getDefaultLootTable());
        LootParams params = new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.THIS_ENTITY, mob)
                .withParameter(LootContextParams.ORIGIN, mob.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, helper.getLevel().damageSources().generic())
                .create(LootContextParamSets.ENTITY);
        return table.getRandomItems(params);
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
    }

    private static void assertDrops(GameTestHelper helper, EntityType<? extends Mob> type, net.minecraft.world.item.Item drop) {
        Mob mob = helper.spawnWithNoFreeWill(type, 2, 2, 2);
        boolean found = false;
        for (int i = 0; i < 40 && !found; i++) {
            found = roll(helper, mob).stream().anyMatch(stack -> stack.is(drop));
        }
        helper.assertTrue(found, type.getDescriptionId() + " never dropped " + drop);
        mob.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void allMobsSpawn(GameTestHelper helper) {
        floor(helper);
        for (EntityType<? extends Mob> type : List.of(MobEntities.DUSKWING.get(), MobEntities.CRAGMAW.get(),
                MobEntities.MISTJELLY.get(), MobEntities.SKYGRAZER.get())) {
            Mob mob = helper.spawn(type, 2, 2, 2);
            helper.assertTrue(mob.isAlive(), type.getDescriptionId() + " is not alive");
            helper.assertTrue(mob.getMaxHealth() > 10, type.getDescriptionId() + " has no health attribute");
        }
        helper.runAfterDelay(20, helper::succeed);
    }

    @GameTest(template = "empty")
    public static void duskwingDrops(GameTestHelper helper) {
        assertDrops(helper, MobEntities.DUSKWING.get(), MobItems.DUSKWING_PLUME.get());
    }

    @GameTest(template = "empty")
    public static void cragmawDrops(GameTestHelper helper) {
        assertDrops(helper, MobEntities.CRAGMAW.get(), MobItems.CRAGMAW_FANG.get());
    }

    @GameTest(template = "empty")
    public static void mistjellyDrops(GameTestHelper helper) {
        assertDrops(helper, MobEntities.MISTJELLY.get(), MobItems.MIST_GEL.get());
    }

    @GameTest(template = "empty")
    public static void skygrazerDrops(GameTestHelper helper) {
        Mob mob = helper.spawnWithNoFreeWill(MobEntities.SKYGRAZER.get(), 2, 2, 2);
        List<ItemStack> loot = roll(helper, mob);
        helper.assertTrue(loot.stream().anyMatch(s -> s.is(MobItems.RAW_SKYGRAZER_MEAT.get())), "No meat: " + loot);
        helper.assertTrue(loot.stream().anyMatch(s -> s.is(MobItems.DOWN_TUFT.get())), "No down tuft: " + loot);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dropRecipesExist(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager();
        for (String id : List.of("arrow_from_duskwing_plume", "white_wool_from_down_tuft", "cloudberry_jelly",
                "cooked_skygrazer_meat_smelting", "cooked_skygrazer_meat_smoking", "cooked_skygrazer_meat_campfire_cooking")) {
            helper.assertTrue(recipes.byKey(Upfall.id(id)).isPresent(), "Missing recipe " + id);
        }
        var brewing = helper.getLevel().potionBrewing();
        ItemStack awkward = PotionContents.createItemStack(Items.POTION, Potions.AWKWARD);
        for (var drop : List.of(MobItems.DUSKWING_PLUME, MobItems.CRAGMAW_FANG, MobItems.MIST_GEL)) {
            helper.assertTrue(brewing.hasMix(awkward, new ItemStack(drop.get())), "No brewing recipe for " + drop.getId());
        }
        helper.succeed();
    }

    @GameTest(template = "empty_large", timeoutTicks = 400)
    public static void duskwingDivesAndHurts(GameTestHelper helper) {
        floor(helper);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(12.5, 1.0, 12.5)));
        Mob mob = helper.spawn(MobEntities.DUSKWING.get(), 8, 8, 8);
        mob.setTarget(player);
        float start = player.getHealth();
        helper.succeedWhen(() -> helper.assertTrue(player.getHealth() < start, "Duskwing did not hurt the player"));
    }

    @GameTest(template = "empty_large", timeoutTicks = 300)
    public static void cragmawBitesPlayer(GameTestHelper helper) {
        for (int x = 0; x < 24; x++) {
            for (int z = 0; z < 24; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(12.5, 1.0, 12.5)));
        Mob mob = helper.spawn(MobEntities.CRAGMAW.get(), 6, 1, 6);
        mob.setTarget(player);
        float start = player.getHealth();
        helper.succeedWhen(() -> helper.assertTrue(player.getHealth() < start, "Cragmaw did not hurt the player"));
    }

    @GameTest(template = "empty_large", timeoutTicks = 300)
    public static void mistjellyRetaliatesWhenHurt(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(12.5, 3.0, 12.5)));
        Mob mob = helper.spawn(MobEntities.MISTJELLY.get(), 11, 3, 12);
        helper.runAfterDelay(20, () -> helper.assertTrue(mob.getTarget() == null, "Mistjelly must be peaceful by default"));
        boolean[] hurt = new boolean[1];
        helper.runAfterDelay(25, () -> hurt[0] = mob.hurt(helper.getLevel().damageSources().playerAttack(player), 1.0F));
        float start = player.getHealth();
        helper.succeedWhen(() -> {
            helper.assertTrue(mob.getTarget() == player, "Mistjelly did not retaliate: hurt=" + hurt[0] + " target=" + mob.getTarget());
            helper.assertTrue(player.getHealth() < start, "Mistjelly did not sting");
        });
    }

    @GameTest(template = "empty", batch = "mobs_peaceful")
    public static void hostileMobsVanishInPeaceful(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        Difficulty previous = helper.getLevel().getDifficulty();
        server.setDifficulty(Difficulty.PEACEFUL, true);
        Mob duskwing = helper.spawn(MobEntities.DUSKWING.get(), 2, 3, 2);
        Mob cragmaw = helper.spawn(MobEntities.CRAGMAW.get(), 4, 1, 4);
        helper.runAfterDelay(5, () -> {
            server.setDifficulty(previous, true);
            helper.assertFalse(duskwing.isAlive(), "Duskwing survived peaceful");
            helper.assertFalse(cragmaw.isAlive(), "Cragmaw survived peaceful");
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void skygrazerBreedsAndIsNeutral(GameTestHelper helper) {
        floor(helper);
        Skygrazer grazer = helper.spawn(MobEntities.SKYGRAZER.get(), 2, 1, 2);
        helper.assertTrue(grazer.isFood(new ItemStack(net.itsthesky.upfall.registry.ModItems.CLOUDBERRY.get())), "Cloudberry must be breeding food");
        helper.assertFalse(grazer.isFood(new ItemStack(Items.WHEAT)), "Wheat must not be food");
        helper.assertTrue(grazer.getBreedOffspring(helper.getLevel(), grazer) != null, "No offspring");
        LivingEntity target = grazer.getTarget();
        helper.assertTrue(target == null, "Skygrazer must not start with a target");
        helper.succeed();
    }
}
