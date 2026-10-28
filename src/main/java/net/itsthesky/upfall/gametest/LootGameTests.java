package net.itsthesky.upfall.gametest;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.reliquary.worldgen.ReliquaryPlacement;
import net.itsthesky.upfall.registry.ModItems;
import net.itsthesky.upfall.worldgen.ExplorerMaps;
import net.itsthesky.upfall.worldgen.ModLootTables;
import net.itsthesky.upfall.worldgen.ReliquaryZones;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.level.ChunkPos;
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
        helper.assertTrue(found > 25 && found < 80, "Nimbusite template should appear in roughly 25% of ruins: " + found + "/200");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void observatorySometimesHoldsReliquaryMap(GameTestHelper helper) {
        int found = 0;
        for (int i = 0; i < 200; i++) {
            List<ItemStack> loot = roll(helper, ModLootTables.INVERTED_OBSERVATORY);
            if (loot.stream().anyMatch(s -> s.get(DataComponents.ITEM_NAME) != null
                    && s.get(DataComponents.ITEM_NAME).getContents() instanceof TranslatableContents name
                    && name.getKey().equals(ExplorerMaps.TILTED_RELIQUARY.translationKey()))) found++;
        }
        helper.assertTrue(found > 10 && found < 55, "Reliquary map should appear in roughly 15% of observatories: " + found + "/200");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void explorerMapMarksItsTarget(GameTestHelper helper) {
        BlockPos target = helper.absolutePos(new BlockPos(900, 80, -1300));
        ItemStack map = ExplorerMaps.create(helper.getLevel(), target, ExplorerMaps.CHRONOSPIRE.decoration(), ExplorerMaps.CHRONOSPIRE.translationKey());
        helper.assertTrue(map.is(Items.FILLED_MAP) && map.get(DataComponents.MAP_ID) != null, "Not a filled map: " + map);
        MapDecorations decorations = map.getOrDefault(DataComponents.MAP_DECORATIONS, MapDecorations.EMPTY);
        helper.assertTrue(decorations.decorations().values().stream().anyMatch(d -> d.type().equals(ExplorerMaps.CHRONOSPIRE.decoration())
                && d.x() == target.getX() && d.z() == target.getZ()), "Map does not mark its target: " + decorations);
        helper.assertTrue(map.get(DataComponents.ITEM_NAME).getContents() instanceof TranslatableContents name
                && name.getKey().equals("filled_map.upfall.chronospire"), "Map is not named: " + map.get(DataComponents.ITEM_NAME));
        helper.assertTrue(ExplorerMaps.toStructure(helper.getLevel(), target, ExplorerMaps.STALLED_ENGINE) == null,
                "A level without the structure must not yield a map");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void reliquaryPlacementStepsByCell(GameTestHelper helper) {
        ReliquaryPlacement placement = new ReliquaryPlacement(1510202618);
        helper.assertTrue(placement.spacing() == ReliquaryZones.CELL_CHUNKS, "Map search rings must step one zone cell at a time");
        long seed = 1234L;
        long key = ReliquaryZones.worldKey(seed);
        int checked = 0;
        for (int cx = -3; cx <= 3; cx++) {
            for (int cz = -3; cz <= 3; cz++) {
                ChunkPos center = ReliquaryZones.nominalCenter(key, cx, cz);
                if (center == null) continue;
                checked++;
                for (int[] probe : new int[][]{{0, 0}, {ReliquaryZones.CELL_CHUNKS - 1, 0}, {0, ReliquaryZones.CELL_CHUNKS - 1}, {57, 113}}) {
                    ChunkPos found = placement.getPotentialStructureChunk(seed, cx * ReliquaryZones.CELL_CHUNKS + probe[0], cz * ReliquaryZones.CELL_CHUNKS + probe[1]);
                    helper.assertTrue(found.equals(center), "Cell " + cx + "," + cz + " probe " + probe[0] + "," + probe[1] + " gave " + found + ", not " + center);
                }
            }
        }
        helper.assertTrue(checked > 30, "Too few zone cells: " + checked);
        helper.succeed();
    }
}
