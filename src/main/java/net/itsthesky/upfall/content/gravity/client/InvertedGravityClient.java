package net.itsthesky.upfall.content.gravity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.itsthesky.upfall.content.gravity.InvertedGravity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.CalculatePlayerTurnEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Quaternionf;

import java.util.ArrayDeque;
import java.util.Deque;

public final class InvertedGravityClient {
    private InvertedGravityClient() {}

    private record SavedRotation(Player player, boolean mirrored, float xRot, float xRotO, float yHeadRot, float yHeadRotO) {}

    private static final Deque<SavedRotation> SAVED = new ArrayDeque<>();

    public static void register() {
        NeoForge.EVENT_BUS.addListener(InvertedGravityClient::onCameraAngles);
        NeoForge.EVENT_BUS.addListener(InvertedGravityClient::onPlayerTurn);
        NeoForge.EVENT_BUS.addListener(InvertedGravityClient::onMovementInput);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, InvertedGravityClient::onRenderPlayerPre);
        NeoForge.EVENT_BUS.addListener(InvertedGravityClient::onRenderPlayerPost);
    }

    private static float eased(float progress) {
        return progress * progress * (3.0F - 2.0F * progress);
    }

    private static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        float flip = InvertedGravity.flipProgress(event.getCamera().getEntity(), (float) event.getPartialTick());
        if (flip > 0.0F) {
            event.setRoll(event.getRoll() + 180.0F * eased(flip));
        }
    }

    private static boolean viewUpsideDown() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        return player != null && mc.getCameraEntity() == player && InvertedGravity.flipProgress(player, 1.0F) > 0.5F;
    }

    private static void onPlayerTurn(CalculatePlayerTurnEvent event) {
        if (!viewUpsideDown()) return;
        event.setMouseSensitivity(-(event.getMouseSensitivity() * 0.6 + 0.4) / 0.6);
        event.setCinematicCameraEnabled(false);
    }

    private static void onMovementInput(MovementInputUpdateEvent event) {
        if (event.getEntity() != Minecraft.getInstance().player || !viewUpsideDown()) return;
        Input input = event.getInput();
        input.leftImpulse = -input.leftImpulse;
        boolean left = input.left;
        input.left = input.right;
        input.right = left;
    }

    private static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        float partialTick = event.getPartialTick();
        float flip = InvertedGravity.flipProgress(player, partialTick);
        if (flip <= 0.0F) return;
        float angle = 180.0F * eased(flip);
        float bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot) * Mth.DEG_TO_RAD;
        float half = player.getBbHeight() / 2.0F;
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(0.0F, half, 0.0F);
        poseStack.mulPose(new Quaternionf().rotationAxis(angle * Mth.DEG_TO_RAD, -Mth.sin(bodyYaw), 0.0F, Mth.cos(bodyYaw)));
        poseStack.translate(0.0F, -half, 0.0F);

        boolean mirrored = angle > 90.0F;
        SAVED.push(new SavedRotation(player, mirrored, player.getXRot(), player.xRotO, player.yHeadRot, player.yHeadRotO));
        if (mirrored) {
            player.setXRot(-player.getXRot());
            player.xRotO = -player.xRotO;
            player.yHeadRot = 2.0F * player.yBodyRot - player.yHeadRot;
            player.yHeadRotO = 2.0F * player.yBodyRotO - player.yHeadRotO;
        }
    }

    private static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        SavedRotation saved = SAVED.peek();
        if (saved == null || saved.player() != event.getEntity()) return;
        SAVED.pop();
        Player player = saved.player();
        if (saved.mirrored()) {
            player.setXRot(saved.xRot());
            player.xRotO = saved.xRotO();
            player.yHeadRot = saved.yHeadRot();
            player.yHeadRotO = saved.yHeadRotO();
        }
        event.getPoseStack().popPose();
    }
}
