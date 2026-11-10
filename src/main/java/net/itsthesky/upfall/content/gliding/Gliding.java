package net.itsthesky.upfall.content.gliding;

import net.itsthesky.upfall.compat.mc.McLevel;
import net.itsthesky.upfall.compat.mc.McPlayers;
import net.itsthesky.upfall.content.gravity.InvertedGravity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Gliding {
    private Gliding() {}

    public static final double MAX_DESCENT = 0.1;
    public static final int TICKS_PER_WEAR = 20;
    public static final int MAX_WEAR_PER_FLIGHT = 10;
    private static final double STEERING = 0.08;

    private static final Map<UUID, Flight> FLIGHTS = new HashMap<>();

    private static final class Flight {
        ItemStack glider = ItemStack.EMPTY;
        int ticks;
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(Gliding::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(Gliding::onFall);
        NeoForge.EVENT_BUS.addListener(Gliding::onLogout);
        NeoForge.EVENT_BUS.addListener(Gliding::onServerStopped);
    }

    public static ItemStack heldGliderStack(LivingEntity entity) {
        if (entity.getMainHandItem().getItem() instanceof GliderItem) return entity.getMainHandItem();
        if (entity.getOffhandItem().getItem() instanceof GliderItem) return entity.getOffhandItem();
        return ItemStack.EMPTY;
    }

    public static GliderItem heldGlider(LivingEntity entity) {
        return heldGliderStack(entity).getItem() instanceof GliderItem glider ? glider : null;
    }

    public static boolean canGlide(Player player) {
        return !player.onGround() && !player.isInLiquid() && !player.onClimbable() && !player.isFallFlying()
                && !player.isPassenger() && !player.isSpectator() && !player.getAbilities().flying
                && !InvertedGravity.isInverted(player);
    }

    private static boolean isGrounded(Player player) {
        return player.onGround() || player.isInLiquid() || player.onClimbable() || player.isPassenger();
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer) {
            trackFlight(serverPlayer);
        } else if (player.level().isClientSide() && player.isLocalPlayer()) {
            // Player movement is client-authoritative, so only the controlling client steers the glide.
            steer(player);
        }
    }

    private static void steer(Player player) {
        GliderItem glider = heldGlider(player);
        if (glider == null || !canGlide(player)) return;
        Vec3 motion = player.getDeltaMovement();
        if (motion.y >= 0) return;

        double x = motion.x;
        double z = motion.z;
        Vec3 wish = new Vec3(player.xxa, 0, player.zza);
        if (wish.lengthSqr() > 1.0E-4) {
            wish = wish.normalize();
            double sin = Mth.sin(player.getYRot() * Mth.DEG_TO_RAD);
            double cos = Mth.cos(player.getYRot() * Mth.DEG_TO_RAD);
            double targetX = (wish.x * cos - wish.z * sin) * glider.glideSpeed();
            double targetZ = (wish.z * cos + wish.x * sin) * glider.glideSpeed();
            x += (targetX - x) * STEERING;
            z += (targetZ - z) * STEERING;
        }
        player.setDeltaMovement(x, Math.max(motion.y, -MAX_DESCENT), z);
    }

    private static void trackFlight(ServerPlayer player) {
        ItemStack held = heldGliderStack(player);
        // Without this, the distance fallen while gliding is applied in full once the glider leaves the hand.
        if (!held.isEmpty()) {
            player.resetFallDistance();
        }
        if (isGrounded(player)) {
            land(player);
            return;
        }
        if (!held.isEmpty() && canGlide(player)) {
            Flight flight = FLIGHTS.computeIfAbsent(player.getUUID(), id -> new Flight());
            flight.glider = held;
            flight.ticks++;
        }
    }

    private static void land(ServerPlayer player) {
        Flight flight = FLIGHTS.remove(player.getUUID());
        if (flight == null) return;
        int wear = Math.min(MAX_WEAR_PER_FLIGHT, flight.ticks / TICKS_PER_WEAR);
        if (wear <= 0) return;
        ItemStack glider = flight.glider;
        if (player.getMainHandItem() == glider) {
            glider.hurtAndBreak(wear, player, EquipmentSlot.MAINHAND);
        } else if (player.getOffhandItem() == glider) {
            glider.hurtAndBreak(wear, player, EquipmentSlot.OFFHAND);
        } else if (McPlayers.mainItems(player.getInventory()).stream().anyMatch(stack -> stack == glider)) {
            glider.hurtAndBreak(wear, McLevel.serverLevel(player), player, item -> {});
        }
    }

    private static void onFall(LivingFallEvent event) {
        if (heldGlider(event.getEntity()) != null) {
            event.setDistance(0);
        }
    }

    private static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        FLIGHTS.remove(event.getEntity().getUUID());
    }

    private static void onServerStopped(ServerStoppedEvent event) {
        FLIGHTS.clear();
    }
}
