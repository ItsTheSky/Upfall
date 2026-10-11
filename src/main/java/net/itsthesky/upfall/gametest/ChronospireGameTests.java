package net.itsthesky.upfall.gametest;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.chronospire.ChronoBlocks;
import net.itsthesky.upfall.content.chronospire.ChronoClock;
import net.itsthesky.upfall.content.chronospire.ChronoPhase;
import net.itsthesky.upfall.content.chronospire.ChronoRegistries;
import net.itsthesky.upfall.content.chronospire.block.ChronoSealBlock;
import net.itsthesky.upfall.content.chronospire.block.ChronoglyphBlock;
import net.itsthesky.upfall.content.chronospire.block.GlyphPedestalBlock;
import net.itsthesky.upfall.content.chronospire.block.PhaseStoneBlock;
import net.itsthesky.upfall.content.chronospire.block.PhaseTrackingBlock;
import net.itsthesky.upfall.content.chronospire.block.entity.ChronoGnomonBlockEntity;
import net.itsthesky.upfall.content.chronospire.block.entity.GlyphPedestalBlockEntity;
import net.itsthesky.upfall.content.chronospire.entity.DriftPlatform;
import net.itsthesky.upfall.content.chronospire.entity.PlatformPath;
import net.itsthesky.upfall.content.chronospire.item.HourglassTracker;
import net.itsthesky.upfall.content.chronospire.puzzle.WheelRing;
import net.itsthesky.upfall.content.chronospire.worldgen.ChronospireBlueprint;
import net.itsthesky.upfall.content.chronospire.worldgen.ChronospireLoot;
import net.itsthesky.upfall.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@GameTestHolder(Upfall.MODID)
@PrefixGameTestTemplate(false)
public final class ChronospireGameTests {
    private ChronospireGameTests() {}

    @GameTest(template = "empty")
    public static void chronoClockPhases(GameTestHelper helper) {
        helper.assertTrue(ChronoClock.phaseAt(0) == ChronoPhase.DAWN, "0 is dawn");
        helper.assertTrue(ChronoClock.phaseAt(5999) == ChronoPhase.DAWN, "5999 is dawn");
        helper.assertTrue(ChronoClock.phaseAt(6000) == ChronoPhase.NOON, "6000 is noon");
        helper.assertTrue(ChronoClock.phaseAt(12000) == ChronoPhase.DUSK, "12000 is dusk");
        helper.assertTrue(ChronoClock.phaseAt(18500) == ChronoPhase.MIDNIGHT, "18500 is midnight");
        helper.assertTrue(ChronoClock.phaseAt(24000 * 7 + 6001) == ChronoPhase.NOON, "days wrap");
        helper.assertTrue(ChronoClock.ticksUntilNextPhase(5600) == 400, "400 ticks left at 5600");
        helper.assertTrue(ChronoClock.formatTicks(6000).equals("5:00"), "a phase lasts five minutes");
        helper.assertTrue(PhaseStoneBlock.modeAt(ChronoPhase.DAWN, 100) == PhaseStoneBlock.Mode.SOLID, "solid in its phase");
        helper.assertTrue(PhaseStoneBlock.modeAt(ChronoPhase.DAWN, 5700) == PhaseStoneBlock.Mode.FADING, "fading at the end of it");
        helper.assertTrue(PhaseStoneBlock.modeAt(ChronoPhase.NOON, 100) == PhaseStoneBlock.Mode.GHOST, "ghost outside of it");
        helper.assertTrue(ChronoPhase.MIDNIGHT.next() == ChronoPhase.DAWN, "midnight is followed by dawn");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "chronospire_clock", timeoutTicks = 300)
    public static void phaseStonesFollowTheOverworldTime(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(true, level.getServer());
        long day = level.getDayTime() - Math.floorMod(level.getDayTime(), ChronoClock.DAY) + ChronoClock.DAY;
        level.setDayTime(day + 1000);
        ChronoPhase[] phases = ChronoPhase.values();
        for (int i = 0; i < phases.length; i++) {
            helper.setBlock(new BlockPos(1 + i, 1, 2), ChronoBlocks.PHASE_STONE.get().defaultBlockState()
                    .setValue(PhaseTrackingBlock.PHASE, phases[i]).setValue(PhaseStoneBlock.MODE, PhaseStoneBlock.Mode.GHOST));
        }
        helper.setBlock(new BlockPos(1, 1, 4), ChronoBlocks.PHASE_LANTERN.get());
        helper.runAfterDelay(5, () -> {
            assertMode(helper, 0, PhaseStoneBlock.Mode.SOLID);
            assertMode(helper, 1, PhaseStoneBlock.Mode.GHOST);
            BlockPos dawn = helper.absolutePos(new BlockPos(1, 1, 2));
            BlockPos noon = helper.absolutePos(new BlockPos(2, 1, 2));
            helper.assertTrue(!level.getBlockState(dawn).getCollisionShape(level, dawn).isEmpty(), "a solid phase stone must have collision");
            helper.assertTrue(level.getBlockState(noon).getCollisionShape(level, noon).isEmpty(), "a ghost phase stone must not have collision");
            helper.assertTrue(helper.getBlockState(new BlockPos(1, 1, 4)).getValue(PhaseTrackingBlock.PHASE) == ChronoPhase.DAWN, "lantern shows dawn");
            level.setDayTime(day + ChronoClock.PHASE_LENGTH - 300);
            helper.runAfterDelay(PhaseTrackingBlock.MAX_CHECK_INTERVAL + 5, () -> {
                assertMode(helper, 0, PhaseStoneBlock.Mode.FADING);
                level.setDayTime(day + 3 * ChronoClock.PHASE_LENGTH + 100);
                helper.runAfterDelay(PhaseTrackingBlock.MAX_CHECK_INTERVAL + 5, () -> {
                    assertMode(helper, 0, PhaseStoneBlock.Mode.GHOST);
                    assertMode(helper, 3, PhaseStoneBlock.Mode.SOLID);
                    helper.assertTrue(helper.getBlockState(new BlockPos(1, 1, 4)).getValue(PhaseTrackingBlock.PHASE) == ChronoPhase.MIDNIGHT,
                            "lantern shows midnight");
                    helper.succeed();
                });
            });
        });
    }

    private static void assertMode(GameTestHelper helper, int index, PhaseStoneBlock.Mode mode) {
        BlockState state = helper.getBlockState(new BlockPos(1 + index, 1, 2));
        helper.assertTrue(state.getValue(PhaseStoneBlock.MODE) == mode,
                "phase stone " + state.getValue(PhaseTrackingBlock.PHASE) + " should be " + mode + " but is " + state.getValue(PhaseStoneBlock.MODE)
                        + " at " + Math.floorMod(ChronoClock.time(helper.getLevel()), ChronoClock.DAY));
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void glyphPuzzleOpensSeals(GameTestHelper helper) {
        BlockPos pedestalPos = new BlockPos(4, 1, 4);
        helper.setBlock(pedestalPos, ChronoBlocks.GLYPH_PEDESTAL.get());
        int[][] glyphPos = {{1, 1}, {6, 1}, {1, 6}, {6, 6}};
        for (int i = 0; i < 4; i++) {
            helper.setBlock(new BlockPos(glyphPos[i][0], 2, glyphPos[i][1]), ChronoBlocks.CHRONOGLYPH.get().defaultBlockState()
                    .setValue(ChronoglyphBlock.GLYPH, ChronoPhase.byIndex(i)));
        }
        BlockPos sealA = new BlockPos(2, 1, 4);
        BlockPos sealB = new BlockPos(2, 2, 4);
        helper.setBlock(sealA, ChronoBlocks.CHRONO_SEAL.get());
        helper.setBlock(sealB, ChronoBlocks.CHRONO_SEAL.get());
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        GlyphPedestalBlockEntity pedestal = (GlyphPedestalBlockEntity) helper.getBlockEntity(pedestalPos);
        int show = GlyphPedestalBlockEntity.SHOW_INTERVAL * (GlyphPedestalBlockEntity.LENGTH + 1) + 15;

        pedestal.use(player);
        helper.assertTrue(pedestal.mode() == GlyphPedestalBlock.Mode.SHOWING, "using the pedestal plays the order");
        helper.runAfterDelay(show, () -> {
            helper.assertTrue(pedestal.mode() == GlyphPedestalBlock.Mode.LISTENING, "after the show the pedestal listens");
            int[] sequence = pedestal.sequence();
            pedestal.onGlyph(ChronoPhase.byIndex(sequence[0]), player);
            pedestal.onGlyph(ChronoPhase.byIndex(sequence[1] + 1), player);
            helper.assertTrue(pedestal.mode() == GlyphPedestalBlock.Mode.IDLE, "a wrong glyph silences the pedestal");
            helper.assertTrue(!helper.getBlockState(sealA).getValue(ChronoSealBlock.OPEN), "the seal stays closed after a mistake");
            pedestal.use(player);
            helper.runAfterDelay(show, () -> {
                for (int glyph : sequence) {
                    pedestal.onGlyph(ChronoPhase.byIndex(glyph), player);
                }
                helper.assertTrue(pedestal.mode() == GlyphPedestalBlock.Mode.SOLVED, "the right order solves the puzzle");
                helper.assertTrue(helper.getBlockState(sealA).getValue(ChronoSealBlock.OPEN)
                        && helper.getBlockState(sealB).getValue(ChronoSealBlock.OPEN), "solving opens the seals");
                helper.assertTrue(helper.getBlockState(new BlockPos(1, 2, 1)).getValue(ChronoglyphBlock.LIGHT) == ChronoglyphBlock.Light.SOLVED,
                        "solved glyphs stay lit");
                BlockPos open = helper.absolutePos(sealA);
                helper.assertTrue(helper.getLevel().getBlockState(open).getCollisionShape(helper.getLevel(), open).isEmpty(),
                        "an open seal can be walked through");
                helper.succeed();
            });
        });
    }

    @GameTest(template = "empty")
    public static void wheelRingIsAlwaysSolvable(GameTestHelper helper) {
        Set<String> outcomes = new HashSet<>();
        for (int code = 0; code < 1024; code++) {
            int[] state = new int[5];
            for (int i = 0; i < 5; i++) {
                for (int p = 0; p < ((code >> (2 * i)) & 3); p++) {
                    WheelRing.press(state, i);
                }
            }
            outcomes.add(Arrays.toString(state));
        }
        helper.assertTrue(outcomes.size() == 1024, "press combinations must reach all 1024 arrangements, got " + outcomes.size());
        int[] wheels = {0, 3, 1, 1, 2};
        int[] presses = WheelRing.solution(wheels, 2);
        helper.assertTrue(presses != null, "a solution exists");
        for (int i = 0; i < presses.length; i++) {
            for (int p = 0; p < presses[i]; p++) {
                WheelRing.press(wheels, i);
            }
        }
        helper.assertTrue(WheelRing.solved(wheels, 2), "the solution solves the ring");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "chronospire_wheels", timeoutTicks = 200)
    public static void wheelPuzzleOpensSeals(GameTestHelper helper) {
        BlockPos gnomonPos = new BlockPos(4, 5, 4);
        helper.setBlock(gnomonPos, ChronoBlocks.CHRONO_GNOMON.get());
        for (int k = 0; k < 5; k++) {
            double a = Math.toRadians(72 * k);
            helper.setBlock(new BlockPos(4 + (int) Math.round(3 * Math.cos(a)), 1, 4 + (int) Math.round(3 * Math.sin(a))),
                    ChronoBlocks.PHASE_WHEEL.get());
        }
        BlockPos seal = new BlockPos(1, 4, 7);
        helper.setBlock(seal, ChronoBlocks.CHRONO_SEAL.get());
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ChronoGnomonBlockEntity gnomon = (ChronoGnomonBlockEntity) helper.getBlockEntity(gnomonPos);
        gnomon.scramble();
        List<BlockPos> wheels = gnomon.wheels();
        helper.assertTrue(wheels.size() == 5, "the gnomon finds its five wheels, found " + wheels.size());
        int target = ChronoClock.phase(helper.getLevel()).ordinal();
        helper.assertTrue(!WheelRing.solved(gnomon.readWheels(wheels), target), "a scrambled ring is not solved");
        int[] presses = WheelRing.solution(gnomon.readWheels(wheels), target);
        for (int i = 0; i < presses.length; i++) {
            for (int p = 0; p < presses[i]; p++) {
                gnomon.press(wheels.get(i), player);
            }
        }
        helper.assertTrue(gnomon.solved(), "the ring shows the current hour everywhere");
        helper.assertTrue(helper.getBlockState(seal).getValue(ChronoSealBlock.OPEN), "solving opens the seals");
        int[] before = gnomon.readWheels(wheels);
        gnomon.press(wheels.get(0), player);
        helper.assertTrue(Arrays.equals(before, gnomon.readWheels(wheels)), "a solved ring is locked");
        helper.succeed();
    }

    @GameTest(template = "empty_large", timeoutTicks = 200)
    public static void driftPlatformCarriesEntities(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        int pause = 10;
        Vec3 start = helper.absoluteVec(new Vec3(12.0, 2.0, 12.0));
        Vec3 end = helper.absoluteVec(new Vec3(12.0, 10.0, 12.0));
        int offset = (int) Math.floorMod(pause - level.getGameTime(), 100000L);
        PlatformPath path = new PlatformPath(List.of(start, end), 0.2, pause, offset);
        DriftPlatform platform = ChronoRegistries.DRIFT_PLATFORM.get().create(level);
        platform.setPath(path);
        level.addFreshEntity(platform);
        ArmorStand stand = helper.spawn(EntityType.ARMOR_STAND, new Vec3(12.0, 2.55, 12.0));
        double startY = stand.getY();
        helper.runAfterDelay(30, () -> {
            double top = platform.getBoundingBox().maxY;
            helper.assertTrue(top > start.y + 3.0, "the platform should have risen: " + top);
            helper.assertTrue(stand.getY() > startY + 2.5, "the platform should carry the stand: " + startY + " -> " + stand.getY());
            helper.assertTrue(Math.abs(stand.getY() - top) < 0.35, "the stand should stay on top: " + stand.getY() + " vs " + top);
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void blueprintIsComplete(GameTestHelper helper) {
        for (int rotation = 0; rotation < 4; rotation++) {
            BlockPos origin = new BlockPos(1000, 120, -2000);
            ChronospireBlueprint plan = ChronospireBlueprint.build(origin, rotation, 42L + rotation);
            BoundingBox bounds = ChronospireBlueprint.bounds(origin);
            for (BlockPos pos : plan.find(s -> true)) {
                helper.assertTrue(bounds.isInside(pos), "block outside of the piece bounds: " + pos);
            }
            helper.assertTrue(plan.find(s -> s.is(ChronoBlocks.CHRONOGLYPH)).size() == 4, "four glyphs");
            helper.assertTrue(plan.find(s -> s.is(ChronoBlocks.GLYPH_PEDESTAL)).size() == 1, "one pedestal");
            List<BlockPos> wheels = plan.find(s -> s.is(ChronoBlocks.PHASE_WHEEL));
            helper.assertTrue(wheels.size() == 5, "five wheels");
            helper.assertTrue(plan.find(s -> s.is(ChronoBlocks.CHRONO_GNOMON)).size() == 1, "one gnomon");
            helper.assertTrue(plan.caches().size() == 1, "one vault cache");
            helper.assertTrue(plan.platforms().size() == 2, "two drift platforms");
            Set<ChronoPhase> wheelPhases = new HashSet<>();
            for (BlockPos wheel : wheels) {
                wheelPhases.add(plan.blocks().get(wheel.asLong()).getValue(PhaseTrackingBlock.PHASE));
            }
            helper.assertTrue(wheelPhases.size() > 1, "the wheels start scrambled");
            for (ChronoPhase phase : ChronoPhase.values()) {
                helper.assertTrue(plan.find(s -> s.is(ChronoBlocks.PHASE_STONE) && s.getValue(PhaseTrackingBlock.PHASE) == phase).size() > 150,
                        "a full stair arm for " + phase);
            }
            List<BlockPos> seals = plan.find(s -> s.is(ChronoBlocks.CHRONO_SEAL));
            helper.assertTrue(seals.size() > 60, "seals around the lift, the doors and the vault: " + seals.size());
            for (BlockPos seal : seals) {
                boolean covered = false;
                for (Map.Entry<BlockPos, List<BoundingBox>> entry : plan.gates().entrySet()) {
                    for (BoundingBox box : entry.getValue()) {
                        covered |= box.moved(entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ()).isInside(seal);
                    }
                }
                helper.assertTrue(covered, "seal at " + seal + " is opened by no puzzle (rotation " + rotation + ")");
            }
            BlockPos cache = plan.caches().get(0);
            helper.assertTrue(plan.blocks().get(cache.asLong()).is(ModBlocks.SKY_CACHE), "the vault holds a sky cache");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void platformRoutesAreNeverAirborne(GameTestHelper helper) {
        ChronospireBlueprint plan = ChronospireBlueprint.build(new BlockPos(0, 120, 0), 1, 7L);
        for (PlatformPath path : plan.platforms()) {
            for (int t = 0; t < path.period(); t += 3) {
                Vec3 pos = path.positionAt(t - path.offset());
                AABB rider = new AABB(pos.x - 0.3, pos.y + 0.5, pos.z - 0.3, pos.x + 0.3, pos.y + 2.3, pos.z + 0.3)
                        .inflate(0.0625).expandTowards(0.0, -0.55, 0.0);
                boolean grounded = false;
                for (BlockPos cell : BlockPos.betweenClosed(BlockPos.containing(rider.minX, rider.minY, rider.minZ),
                        BlockPos.containing(rider.maxX, rider.maxY, rider.maxZ))) {
                    BlockState state = plan.blocks().get(cell.asLong());
                    grounded |= state != null && !state.isAir();
                }
                helper.assertTrue(grounded, "a rider at " + pos + " would be floating in the air");
            }
        }
        helper.assertTrue(!ChronoBlocks.CHRONO_CURRENT.get().defaultBlockState().isAir(), "the chrono current must not count as air");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void vaultLootHoldsTheHourglass(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        LootTable table = level.getServer().reloadableRegistries().getLootTable(ChronospireLoot.VAULT);
        List<ItemStack> loot = table.getRandomItems(new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.ZERO).create(LootContextParamSets.CHEST));
        helper.assertTrue(loot.stream().anyMatch(stack -> stack.is(ChronoRegistries.REWINDING_HOURGLASS.get())), "the vault holds the hourglass: " + loot);
        helper.assertTrue(loot.stream().anyMatch(stack -> stack.is(Items.ENCHANTED_BOOK)), "the vault holds an enchanted book: " + loot);
        helper.assertTrue(loot.size() >= 6, "the vault is generous: " + loot);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void hourglassRemembersFooting(GameTestHelper helper) {
        UUID id = UUID.randomUUID();
        for (long t = 0; t <= 100; t += 5) {
            HourglassTracker.record(id, new HourglassTracker.Footing(Level.OVERWORLD, new Vec3(t, 64, 0), 0, 0, 1000 + t));
        }
        HourglassTracker.Footing footing = HourglassTracker.rewind(id, Level.OVERWORLD, 1100, 60);
        helper.assertTrue(footing != null && footing.time() == 1040, "rewinds to the newest footing at least 3 s old: " + footing);
        helper.assertTrue(HourglassTracker.rewind(id, Level.NETHER, 1100, 60) == null, "no footing in another dimension");
        HourglassTracker.clear(id);
        helper.succeed();
    }
}
