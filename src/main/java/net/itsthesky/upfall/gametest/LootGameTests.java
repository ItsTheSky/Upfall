package net.itsthesky.upfall.gametest;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.registry.ModItems;
import net.itsthesky.upfall.worldgen.ModLootTables;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(Upfall.MODID)
@PrefixGameTestTemplate(false)
public final class LootGameTests {
    private LootGameTests() {}

    private static List<ItemStack> roll(GameTestHelper helper, net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> key) {
        var table = helper.getLevel().getServer().reloadableRegistries().getLootTable(key);
        LootParams params = new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(helper.absolutePos(net.minecraft.core.BlockPos.ZERO)))
                .create(LootContextParamSets.CHEST);
        return table.getRandomItems(params);
    }

    @GameTest(template = "empty")
    public static void observatoryAlwaysHoldsLevititeTemplate(GameTestHelper helper) {
        for (int i = 0; i < 20; i++) {
            List<ItemStack> loot = roll(helper, ModLootTables.INVERTED_OBSERVATORY);
            helper.assertTrue(loot.stream().anyMatch(s -> s.is(ModItems.LEVITITE_UPGRADE_SMITHING_TEMPLATE.get())),
                    "Observatory loot without Levitite template: " + loot);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void ruinsSometimesHoldNimbusiteTemplate(GameTestHelper helper) {
        int found = 0;
        for (int i = 0; i < 200; i++) {
            List<ItemStack> loot = roll(helper, ModLootTables.DRIFTING_RUINS);
            helper.assertFalse(loot.isEmpty(), "Empty ruins loot");
            if (loot.stream().anyMatch(s -> s.is(ModItems.NIMBUSITE_UPGRADE_SMITHING_TEMPLATE.get()))) found++;
        }
        helper.assertTrue(found > 10 && found < 60, "Nimbusite template should appear in roughly 15% of ruins: " + found + "/200");
        helper.succeed();
    }
}
