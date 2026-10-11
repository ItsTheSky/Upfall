package net.itsthesky.upfall.client;

import net.itsthesky.upfall.content.gravity.InvertedGravity;
import net.itsthesky.upfall.gameplay.ArmorSetBonuses;
import net.itsthesky.upfall.network.DoubleJumpPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class LevititeMovementHandler {
    private LevititeMovementHandler() {}

    private static boolean jumpWasDown;
    private static boolean doubleJumpAvailable;
    private static int airTicks;

    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        boolean jumpDown = Minecraft.getInstance().options.keyJump.isDown();
        boolean grounded = player.onGround() || player.isInWater() || player.isInLava() || player.onClimbable()
                || player.isPassenger() || player.getAbilities().flying || player.isFallFlying();
        if (grounded) {
            doubleJumpAvailable = true;
            airTicks = 0;
            jumpWasDown = jumpDown;
            return;
        }
        airTicks++;
        if (ArmorSetBonuses.hasLevititeSet(player)) {
            Vec3 motion = player.getDeltaMovement();
            boolean inverted = InvertedGravity.isInverted(player);
            double upSpeed = inverted ? -motion.y : motion.y;
            double feetY = inverted ? player.getBoundingBox().maxY : player.getY();
            if (jumpDown && !jumpWasDown && doubleJumpAvailable && airTicks > 2) {
                doubleJumpAvailable = false;
                player.setDeltaMovement(ArmorSetBonuses.doubleJumpMotion(motion, player.getYRot(), player.input.forwardImpulse, inverted));
                player.resetFallDistance();
                for (int i = 0; i < 6; i++) {
                    player.level().addParticle(ParticleTypes.CLOUD, player.getX(), feetY, player.getZ(),
                            (player.getRandom().nextDouble() - 0.5) * 0.1, inverted ? 0.05 : -0.05, (player.getRandom().nextDouble() - 0.5) * 0.1);
                }
                PacketDistributor.sendToServer(DoubleJumpPayload.INSTANCE);
            } else if (jumpDown && upSpeed < ArmorSetBonuses.GLIDE_FALL_SPEED) {
                player.setDeltaMovement(motion.x, inverted ? -ArmorSetBonuses.GLIDE_FALL_SPEED : ArmorSetBonuses.GLIDE_FALL_SPEED, motion.z);
                player.resetFallDistance();
            }
        }
        jumpWasDown = jumpDown;
    }
}
