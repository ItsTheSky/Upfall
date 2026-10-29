package net.itsthesky.upfall.content.machines.pipe;

import net.itsthesky.upfall.compat.mc.McLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class PipeTravel {
    private PipeTravel() {}

    public static final float SPEED = 0.8F;
    private static final int INTEGRITY_INTERVAL = 4;
    private static final int GRACE_TICKS = 40;
    private static final int REENTRY_COOLDOWN = 20;
    private static final double MAX_DRIFT = 3.0;
    private static final double MAX_LEAD = 4.0;
    private static final List<BlockPos> SAFE_OFFSETS = safeOffsets();

    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();

    private static final class Session {
        final ServerLevel level;
        final PipeRoute route;
        final PipePath path;
        final int expectedTicks;
        double progress;
        int ticks;
        int lastCorrection = -100;

        Session(ServerLevel level, PipeRoute route, PipePath path) {
            this.level = level;
            this.route = route;
            this.path = path;
            this.expectedTicks = (int) Math.ceil(path.length() / SPEED);
        }
    }

    static void register() {
        NeoForge.EVENT_BUS.addListener(PipeTravel::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(PipeTravel::onLogout);
        NeoForge.EVENT_BUS.addListener(PipeTravel::onDeath);
        NeoForge.EVENT_BUS.addListener(PipeTravel::onRespawn);
        NeoForge.EVENT_BUS.addListener(PipeTravel::onChangeDimension);
        NeoForge.EVENT_BUS.addListener(PipeTravel::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(PipeTravel::onFall);
        NeoForge.EVENT_BUS.addListener(PipeTravel::onServerStopped);
    }

    public static boolean isTraveling(Player player) {
        return SESSIONS.containsKey(player.getUUID());
    }

    public static boolean wantsToEnter(Player player, BlockPos entrance, Direction facing) {
        if (!player.isShiftKeyDown() || !player.isAlive() || player.isSpectator() || player.isPassenger() || player.isSleeping()) {
            return false;
        }
        AABB mouth = new AABB(entrance.relative(facing));
        AABB box = player.getBoundingBox();
        Vec3 probe = facing == Direction.DOWN
                ? new Vec3(player.getX(), box.maxY - 0.1, player.getZ())
                : new Vec3(player.getX(), player.getY() + 0.1, player.getZ());
        return mouth.contains(probe);
    }

    public static boolean tryStart(ServerPlayer player, BlockPos entrance) {
        if (isTraveling(player)) return false;
        ServerLevel level = McLevel.serverLevel(player);
        Long cooldown = COOLDOWNS.get(player.getUUID());
        if (cooldown != null && level.getGameTime() < cooldown) return false;
        Optional<PipeRoute> route = PipeNetwork.findRoute(level, entrance, exit -> PipeNetwork.exitClear(level, exit));
        if (route.isEmpty()) {
            COOLDOWNS.put(player.getUUID(), level.getGameTime() + REENTRY_COOLDOWN);
            level.playSound(null, entrance, PneumaticPipes.JAMMED.get(), SoundSource.BLOCKS, 0.6F, 0.9F + level.getRandom().nextFloat() * 0.2F);
            return false;
        }
        PipePath path = new PipePath(route.get().playerPoints(player.position()));
        Session session = new Session(level, route.get(), path);
        SESSIONS.put(player.getUUID(), session);
        applyTravelState(player);
        send(player, new PipeTravelStartPayload(path.points(), 0, SPEED, route.get().ejectPush()));
        Vec3 mouth = Vec3.atCenterOf(entrance).add(directionVec(facingOf(level, entrance)).scale(0.6));
        level.playSound(null, entrance, PneumaticPipes.ENTER.get(), SoundSource.BLOCKS, 0.9F, 0.95F + level.getRandom().nextFloat() * 0.1F);
        level.sendParticles(ParticleTypes.POOF, mouth.x, mouth.y, mouth.z, 8, 0.3, 0.3, 0.3, 0.02);
        level.sendParticles(ParticleTypes.SMALL_GUST, mouth.x, mouth.y, mouth.z, 1, 0, 0, 0, 0);
        return true;
    }

    private static void send(ServerPlayer player, CustomPacketPayload payload) {
        if (player.connection != null && player.connection.hasChannel(payload)) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    static Vec3 directionVec(Direction dir) {
        return new Vec3(dir.getStepX(), dir.getStepY(), dir.getStepZ());
    }

    private static Direction facingOf(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.hasProperty(BlockStateProperties.FACING) ? state.getValue(BlockStateProperties.FACING) : Direction.UP;
    }

    private static void applyTravelState(Player player) {
        player.noPhysics = true;
        player.setForcedPose(Pose.SWIMMING);
        player.resetFallDistance();
        player.setDeltaMovement(Vec3.ZERO);
    }

    private static void clearTravelState(ServerPlayer player) {
        SESSIONS.remove(player.getUUID());
        player.noPhysics = player.isSpectator();
        player.setForcedPose(null);
        player.resetFallDistance();
        COOLDOWNS.put(player.getUUID(), McLevel.serverLevel(player).getGameTime() + Math.max(REENTRY_COOLDOWN, GRACE_TICKS));
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Session session = SESSIONS.get(player.getUUID());
        if (session == null) return;
        if (!player.isAlive() || McLevel.serverLevel(player) != session.level) {
            clearTravelState(player);
            return;
        }
        applyTravelState(player);
        session.ticks++;
        session.progress = Math.min(session.path.length(), session.progress + SPEED);
        if (session.ticks % INTEGRITY_INTERVAL == 0 && !PipeNetwork.isIntact(session.level, session.route)) {
            abort(player, session, true);
            return;
        }
        PipePath.Projection client = session.path.project(player.position());
        double length = session.path.length();
        if (client.progress() >= length - 0.3 && session.progress >= length - MAX_LEAD) {
            finish(player, session, false);
            return;
        }
        if (session.ticks > session.expectedTicks + 60) {
            finish(player, session, true);
            return;
        }
        if ((client.distance() > MAX_DRIFT || client.progress() > session.progress + MAX_LEAD) && session.ticks - session.lastCorrection > 10) {
            session.lastCorrection = session.ticks;
            Vec3 expected = session.path.at(session.progress);
            player.teleportTo(expected.x, expected.y, expected.z);
            send(player, new PipeTravelStartPayload(session.path.points(), session.progress, SPEED,
                    session.route.ejectPush()));
        }
        if (session.ticks % 3 == 0) {
            session.level.sendParticles(ParticleTypes.WHITE_SMOKE, player.getX(), player.getY() + 0.3, player.getZ(), 1, 0.1, 0.1, 0.1, 0.0);
        }
    }

    private static void finish(ServerPlayer player, Session session, boolean snap) {
        clearTravelState(player);
        Vec3 push = session.route.ejectPush();
        if (snap) {
            Vec3 end = session.path.end();
            player.teleportTo(end.x, end.y, end.z);
        }
        player.setDeltaMovement(push);
        send(player, new PipeTravelEndPayload(push));
        BlockPos exit = session.route.exit();
        Vec3 mouth = Vec3.atCenterOf(exit).add(directionVec(session.route.exitFacing()).scale(0.7));
        session.level.playSound(null, exit, PneumaticPipes.EXIT.get(), SoundSource.BLOCKS, 0.9F, 0.95F + session.level.getRandom().nextFloat() * 0.1F);
        session.level.playSound(null, exit, PneumaticPipes.POP.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
        session.level.sendParticles(ParticleTypes.POOF, mouth.x, mouth.y, mouth.z, 12, 0.3, 0.3, 0.3, 0.05);
        session.level.sendParticles(ParticleTypes.SMALL_GUST, mouth.x, mouth.y, mouth.z, 2, 0.2, 0.2, 0.2, 0);
    }

    private static void abort(ServerPlayer player, Session session, boolean notify) {
        clearTravelState(player);
        Vec3 spot = safeSpot(player, session);
        if (notify) {
            player.teleportTo(spot.x, spot.y, spot.z);
            player.setDeltaMovement(Vec3.ZERO);
            send(player, new PipeTravelEndPayload(Vec3.ZERO));
            session.level.playSound(null, BlockPos.containing(spot), PneumaticPipes.JAMMED.get(), SoundSource.BLOCKS, 0.8F, 0.8F);
        } else {
            player.setPos(spot);
        }
    }

    private static Vec3 safeSpot(ServerPlayer player, Session session) {
        BlockPos origin = player.blockPosition();
        Vec3 crawl = null;
        for (BlockPos offset : SAFE_OFFSETS) {
            Vec3 candidate = Vec3.atBottomCenterOf(origin.offset(offset));
            if (session.level.noCollision(player, player.getDimensions(Pose.STANDING).makeBoundingBox(candidate))) {
                return candidate;
            }
            if (crawl == null && session.level.noCollision(player, player.getDimensions(Pose.SWIMMING).makeBoundingBox(candidate))) {
                crawl = candidate;
            }
        }
        return crawl != null ? crawl : session.path.points().get(0);
    }

    private static List<BlockPos> safeOffsets() {
        List<BlockPos> offsets = new ArrayList<>();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -2; dy <= 3; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    offsets.add(new BlockPos(dx, dy, dz));
                }
            }
        }
        offsets.sort(Comparator.<BlockPos>comparingInt(p -> p.getX() * p.getX() + p.getY() * p.getY() + p.getZ() * p.getZ())
                .thenComparingInt(p -> -p.getY()));
        return List.copyOf(offsets);
    }

    private static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Session session = SESSIONS.get(player.getUUID());
            if (session != null) abort(player, session, false);
            COOLDOWNS.remove(player.getUUID());
        }
    }

    private static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && SESSIONS.containsKey(player.getUUID())) {
            clearTravelState(player);
            send(player, new PipeTravelEndPayload(Vec3.ZERO));
        }
    }

    private static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && SESSIONS.containsKey(player.getUUID())) {
            clearTravelState(player);
        }
    }

    private static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && SESSIONS.containsKey(player.getUUID())) {
            clearTravelState(player);
            send(player, new PipeTravelEndPayload(Vec3.ZERO));
        }
    }

    private static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var source = event.getSource();
        boolean traveling = isTraveling(player);
        if (traveling && (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CRAMMING) || source.is(DamageTypes.FLY_INTO_WALL)
                || source.is(DamageTypeTags.IS_FALL))) {
            event.setCanceled(true);
        } else if (!traveling && source.is(DamageTypeTags.IS_FALL) && inGrace(player)) {
            event.setCanceled(true);
        }
    }

    private static void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && (isTraveling(player) || inGrace(player))) {
            event.setDistance(0);
        }
    }

    private static boolean inGrace(ServerPlayer player) {
        Long until = COOLDOWNS.get(player.getUUID());
        return until != null && McLevel.serverLevel(player).getGameTime() < until;
    }

    private static void onServerStopped(ServerStoppedEvent event) {
        SESSIONS.clear();
        COOLDOWNS.clear();
    }
}
