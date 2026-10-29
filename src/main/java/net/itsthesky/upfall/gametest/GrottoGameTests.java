package net.itsthesky.upfall.gametest;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.compat.mc.McLevel;
import net.itsthesky.upfall.content.grotto.GrottoBlocks;
import net.itsthesky.upfall.content.grotto.GrottoLayout;
import net.itsthesky.upfall.content.grotto.GrottoLoot;
import net.itsthesky.upfall.content.grotto.GrottoPiece;
import net.itsthesky.upfall.gametest.framework.UpfallGameTest;
import net.itsthesky.upfall.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class GrottoGameTests {
    private GrottoGameTests() {}

    private static List<ItemStack> drops(GameTestHelper helper, Block block, ItemStack tool) {
        return block.defaultBlockState().getDrops(new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.ORIGIN, Vec3.ZERO).withParameter(LootContextParams.TOOL, tool));
    }

    private static ItemStack pickaxe(GameTestHelper helper, ResourceKey<Enchantment> enchant, int level) {
        ItemStack stack = new ItemStack(Items.NETHERITE_PICKAXE);
        if (enchant != null) {
            stack.enchant(helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchant), level);
        }
        return stack;
    }

    private static int count(List<ItemStack> stacks, Item item) {
        return stacks.stream().filter(s -> s.is(item)).mapToInt(ItemStack::getCount).sum();
    }

    @UpfallGameTest(template = "empty")
    public static void mistglowCrystalDropsDust(GameTestHelper helper) {
        Block crystal = ModBlocks.MISTGLOW_CRYSTAL.get();
        Item dust = GrottoBlocks.MISTGLOW_DUST.get();
        int fortuneMax = 0;
        for (int i = 0; i < 100; i++) {
            int plain = count(drops(helper, crystal, pickaxe(helper, null, 0)), dust);
            helper.assertTrue(plain >= 3 && plain <= 4, "Plain pickaxe should drop 3 to 4 dust, got " + plain);
            int fortune = count(drops(helper, crystal, pickaxe(helper, Enchantments.FORTUNE, 3)), dust);
            helper.assertTrue(fortune >= 3 && fortune <= 4, "Fortune is capped at 4 dust, got " + fortune);
            fortuneMax = Math.max(fortuneMax, fortune);
            List<ItemStack> silk = drops(helper, crystal, pickaxe(helper, Enchantments.SILK_TOUCH, 1));
            helper.assertTrue(silk.size() == 1 && silk.get(0).is(crystal.asItem()), "Silk Touch should keep the block: " + silk);
        }
        helper.assertTrue(fortuneMax == 4, "Fortune should reach 4 dust");
        helper.succeed();
    }

    @UpfallGameTest(template = "empty")
    public static void gildedDuskslateDropsNuggets(GameTestHelper helper) {
        Block ore = ModBlocks.GILDED_DUSKSLATE.get();
        int plainMax = 0;
        int fortuneMax = 0;
        for (int i = 0; i < 200; i++) {
            List<ItemStack> plain = drops(helper, ore, pickaxe(helper, null, 0));
            int nuggets = count(plain, Items.GOLD_NUGGET);
            helper.assertTrue(nuggets >= 2 && nuggets <= 6 && plain.size() == 1, "Expected 2 to 6 nuggets, got " + plain);
            plainMax = Math.max(plainMax, nuggets);
            fortuneMax = Math.max(fortuneMax, count(drops(helper, ore, pickaxe(helper, Enchantments.FORTUNE, 3)), Items.GOLD_NUGGET));
            List<ItemStack> silk = drops(helper, ore, pickaxe(helper, Enchantments.SILK_TOUCH, 1));
            helper.assertTrue(silk.size() == 1 && silk.get(0).is(ore.asItem()), "Silk Touch should keep the block: " + silk);
        }
        helper.assertTrue(plainMax == 6 && fortuneMax > 6, "Fortune should exceed the plain maximum: " + plainMax + " / " + fortuneMax);
        helper.succeed();
    }

    @UpfallGameTest(template = "empty")
    public static void lanternStandsAndHangs(GameTestHelper helper) {
        BlockPos floor = new BlockPos(2, 1, 2);
        helper.setBlock(floor, Blocks.STONE);
        helper.setBlock(floor.above(3), Blocks.STONE);
        BlockState standing = GrottoBlocks.MISTGLOW_LANTERN.get().defaultBlockState();
        BlockState hanging = standing.setValue(LanternBlock.HANGING, true);
        helper.assertTrue(standing.canSurvive(helper.getLevel(), helper.absolutePos(floor.above())), "Lantern should stand on a block");
        helper.assertTrue(hanging.canSurvive(helper.getLevel(), helper.absolutePos(floor.above(2))), "Lantern should hang from a block");
        helper.assertFalse(hanging.canSurvive(helper.getLevel(), helper.absolutePos(floor.above())), "Lantern must not hang in the air");
        helper.setBlock(floor.above(), standing);
        helper.assertTrue(helper.getBlockState(floor.above()).getLightEmission(helper.getLevel(), helper.absolutePos(floor.above())) == 15, "Lantern light level");
        helper.succeed();
    }

    @UpfallGameTest(template = "empty")
    public static void layoutHasAllFeatures(GameTestHelper helper) {
        GrottoLayout layout = new GrottoLayout(1234L, 0, 0, 56);
        BoundingBox box = layout.bounds();
        int air = 0, clusters = 0, pools = 0, sprouts = 0, steps = 0, mist = 0;
        for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int z = box.minZ(); z <= box.maxZ(); z++) {
                for (int y = box.minY(); y <= box.maxY(); y++) {
                    BlockState s = layout.stateAt(x, y, z);
                    if (s == null) continue;
                    if (s.isAir()) air++;
                    else if (s.is(GrottoBlocks.PRISM_POOL.get())) pools++;
                    else if (s.is(GrottoBlocks.LUMEN_SPROUTS.get())) sprouts++;
                    else if (s.is(GrottoBlocks.MOONSHELL.get()) && Math.hypot(x, z) < 4) steps++;
                    else if (s.is(ModBlocks.UPDRAFT_MIST.get())) mist++;
                    else if (GrottoBlocks.CRYSTALS.stream().anyMatch(c -> c.clusters().stream().anyMatch(b -> s.is(b.get())))) clusters++;
                }
            }
        }
        Upfall.LOGGER.info("[test] grotto layout: air {} clusters {} pools {} sprouts {} steps {} mist {}", air, clusters, pools, sprouts, steps, mist);
        helper.assertTrue(air > 8000 && clusters > 100 && pools > 5 && sprouts > 5 && steps > 20 && mist > 30, "Layout is missing features");
        helper.assertTrue(layout.stateAt(0, layout.vent().getY(), 0).is(ModBlocks.UPDRAFT_VENT.get()), "Vent expected at the centre");
        for (BlockPos cache : layout.caches()) {
            helper.assertTrue(layout.stateAt(cache.getX(), cache.getY(), cache.getZ()).is(ModBlocks.SKY_CACHE.get()), "Cache expected at " + cache);
            helper.assertTrue(layout.stateAt(cache.getX(), cache.getY() + 1, cache.getZ()).isAir(), "Cache must have headroom");
        }
        helper.assertTrue(layout.stateAt(10, 20, 10) == new GrottoLayout(1234L, 0, 0, 56).stateAt(10, 20, 10), "Layout must be deterministic");
        helper.succeed();
    }

    @UpfallGameTest(template = "empty", timeoutTicks = 1200)
    public static void pieceGeneratesChunkByChunk(GameTestHelper helper) {
        var level = helper.getLevel();
        GrottoPiece piece = GrottoPiece.create(99L, 8000, 8000, 56);
        BoundingBox bounds = piece.getBoundingBox();
        for (int cx = bounds.minX() >> 4; cx <= bounds.maxX() >> 4; cx++) {
            for (int cz = bounds.minZ() >> 4; cz <= bounds.maxZ() >> 4; cz++) {
                ChunkPos chunk = new ChunkPos(cx, cz);
                BoundingBox chunkBox = new BoundingBox(chunk.getMinBlockX(), McLevel.minY(level), chunk.getMinBlockZ(),
                        chunk.getMaxBlockX(), McLevel.maxYExclusive(level), chunk.getMaxBlockZ());
                piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), level.getRandom(), chunkBox, chunk, BlockPos.ZERO);
            }
        }
        for (BlockPos cache : piece.layout().caches()) {
            helper.assertTrue(level.getBlockState(cache).is(ModBlocks.SKY_CACHE.get()), "Cache not placed at " + cache);
            helper.assertTrue(level.getBlockEntity(cache) instanceof RandomizableContainer container
                    && GrottoLoot.CRYSTAL_GROTTO.equals(container.getLootTable()), "Cache without the grotto loot table");
        }
        helper.assertTrue(level.getBlockState(piece.layout().vent()).is(ModBlocks.UPDRAFT_VENT.get()), "Vent not placed");
        helper.succeed();
    }

    @UpfallGameTest(template = "empty")
    public static void grottoLootIsRich(GameTestHelper helper) {
        var table = helper.getLevel().getServer().reloadableRegistries().getLootTable(GrottoLoot.CRYSTAL_GROTTO);
        LootParams params = new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.ORIGIN, Vec3.ZERO)
                .create(LootContextParamSets.CHEST);
        for (int i = 0; i < 30; i++) {
            List<ItemStack> loot = table.getRandomItems(params);
            helper.assertTrue(count(loot, GrottoBlocks.MISTGLOW_DUST.get()) >= 6 && count(loot, GrottoBlocks.LUMEN_SHARD.get()) >= 3,
                    "Grotto cache should always hold dust and shards: " + loot);
        }
        helper.succeed();
    }
}
