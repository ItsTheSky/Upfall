package net.itsthesky.upfall.gametest;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.portal.HaloPortalShape;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Upfall.MODID)
@PrefixGameTestTemplate(false)
public final class HaloPortalGameTests {
    private HaloPortalGameTests() {}

    private static void buildFrame(GameTestHelper helper, BlockPos origin, boolean skipOneBlock) {
        for (int w = -1; w <= 2; w++) {
            for (int h = -1; h <= 3; h++) {
                boolean edge = w == -1 || w == 2 || h == -1 || h == 3;
                if (!edge) continue;
                if (skipOneBlock && w == 2 && h == 1) continue;
                helper.setBlock(origin.offset(-w, h, 0), ModBlocks.HALO_FRAME.get());
            }
        }
    }

    @GameTest(template = "empty")
    public static void frameLightsAndCollapses(GameTestHelper helper) {
        BlockPos origin = new BlockPos(4, 2, 4);
        buildFrame(helper, origin, false);
        var shape = HaloPortalShape.findEmptyPortalShape(helper.getLevel(), helper.absolutePos(origin), Direction.Axis.X);
        helper.assertTrue(shape.isPresent(), "A complete 4x5 Halo Frame was not detected");
        helper.assertTrue(shape.get().width() == 2 && shape.get().height() == 3, "Unexpected portal size");
        shape.get().createPortalBlocks();
        helper.assertBlockPresent(ModBlocks.HALO_PORTAL.get(), origin);
        helper.assertBlockPresent(ModBlocks.HALO_PORTAL.get(), origin.offset(-1, 2, 0));
        helper.setBlock(origin.offset(1, 1, 0), Blocks.AIR);
        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(ModBlocks.HALO_PORTAL.get(), origin);
            helper.assertBlockNotPresent(ModBlocks.HALO_PORTAL.get(), origin.offset(-1, 2, 0));
        });
    }

    @GameTest(template = "empty")
    public static void incompleteFrameIsNotDetected(GameTestHelper helper) {
        BlockPos origin = new BlockPos(4, 2, 4);
        buildFrame(helper, origin, true);
        helper.assertTrue(HaloPortalShape.findEmptyPortalShape(helper.getLevel(), helper.absolutePos(origin), Direction.Axis.X).isEmpty(),
                "An incomplete frame must not be detected");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void keyRefusesLowAltitude(GameTestHelper helper) {
        BlockPos origin = new BlockPos(4, 2, 4);
        buildFrame(helper, origin, false);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.SKYWARD_KEY.get()));
        BlockPos clicked = helper.absolutePos(origin.below());
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(clicked), Direction.UP, clicked, false);
        ModItems.SKYWARD_KEY.get().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
        helper.assertBlockNotPresent(ModBlocks.HALO_PORTAL.get(), origin);
        helper.assertTrue(player.getMainHandItem().getCount() == 1, "The key must not be consumed below Y 200");
        helper.succeed();
    }
}
