package net.itsthesky.upfall.content.reliquary.boss;

import net.itsthesky.upfall.content.mobs.entity.AirSteering;
import net.itsthesky.upfall.content.quantum.QuantumParticles;
import net.itsthesky.upfall.content.reliquary.ReliquaryBlocks;
import net.itsthesky.upfall.content.reliquary.ReliquaryEntities;
import net.itsthesky.upfall.content.reliquary.ReliquarySounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

final class FoldWyrmBrain {
    private static final int COIL_TICKS = 100;
    private static final int SHED_RELEASE = 15;
    private static final int SHED_TICKS = 30;
    private static final int DIVE_RISE = 30;
    private static final int DIVE_MAX = 60;
    private static final int BREATH_WINDUP = 30;
    private static final int BREATH_BEAM = 12;
    private static final int BREATH_TICKS = 45;
    private static final int UPDRAFT_TICKS = 80;
    private static final int MIN_BODY = 4;
    private static final double BREATH_RANGE = 24.0;
    private static final float CONTACT_DAMAGE = 5.5F;
    private static final float DIVE_DAMAGE = 11.0F;
    private static final float BREATH_DAMAGE = 6.6F;
    private static final BlockPos[] CAGE = {
            new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(0, 0, -1),
            new BlockPos(1, 1, 0), new BlockPos(-1, 1, 0), new BlockPos(0, 1, 1), new BlockPos(0, 1, -1),
            new BlockPos(0, 2, 0)};

    private record Updraft(Vec3 pos, int expires) {}

    private final FoldWarden warden;
    private final List<Updraft> updrafts = new ArrayList<>();
    private final Set<UUID> breathHit = new HashSet<>();
    private ServerPlayer target;
    private int cooldown;
    private double roamAngle;
    private double coilAngle;
    private Vec3 diveTarget = Vec3.ZERO;
    private Vec3 breathAim = Vec3.ZERO;

    FoldWyrmBrain(FoldWarden warden) {
        this.warden = warden;
    }

    void reset() {
        cooldown = 40;
        warden.begin(FoldWarden.Attack.NONE);
        roamAngle = warden.getRandom().nextDouble() * Math.PI * 2;
    }

    void tick() {
        if (!(warden.level() instanceof ServerLevel server)) return;
        double pace = warden.isEnraged() ? 1.3 : 1.0;
        tickUpdrafts(server);
        tickContact(server);
        if (target == null || !target.isAlive() || !warden.arenaPlayers().contains(target)) {
            target = nearest();
        }
        boolean aiming = false;
        if (warden.attack() == FoldWarden.Attack.NONE || target == null) {
            if (warden.attack() != FoldWarden.Attack.NONE) warden.begin(FoldWarden.Attack.NONE);
            roam(pace);
            if (target != null && --cooldown <= 0) choose();
        } else {
            warden.tickAttackTimer();
            int ticks = warden.attackTicks();
            switch (warden.attack()) {
                case COIL -> coil(ticks, pace);
                case SHED -> shed(server, ticks);
                case DIVE -> dive(server, ticks, pace);
                case BREATH -> aiming = breath(server, ticks);
                default -> end();
            }
        }
        if (!aiming) {
            AirSteering.faceMotion(warden, 20.0F);
            Vec3 motion = warden.getDeltaMovement();
            float pitch = (float) (-Mth.atan2(motion.y, motion.horizontalDistance()) * Mth.RAD_TO_DEG);
            warden.setXRot(Mth.approachDegrees(warden.getXRot(), Mth.clamp(pitch, -60.0F, 60.0F), 8.0F));
        }
    }

    private ServerPlayer nearest() {
        ServerPlayer best = null;
        double bestDistance = Double.MAX_VALUE;
        for (ServerPlayer player : warden.arenaPlayers()) {
            double distance = player.distanceToSqr(warden);
            if (distance < bestDistance) {
                best = player;
                bestDistance = distance;
            }
        }
        return best;
    }

    private void choose() {
        int roll = warden.getRandom().nextInt(100);
        boolean canShed = warden.shed() == 0;
        if (roll < 30) {
            warden.begin(FoldWarden.Attack.COIL);
            coilAngle = Mth.atan2(warden.getZ() - target.getZ(), warden.getX() - target.getX());
        } else if (roll < 55 && canShed) {
            warden.begin(FoldWarden.Attack.SHED);
        } else if (roll < 80) {
            warden.begin(FoldWarden.Attack.DIVE);
        } else {
            warden.begin(FoldWarden.Attack.BREATH);
            warden.playSound(ReliquarySounds.WYRM_BREATH_CHARGE.get(), 3.0F, 1.0F);
        }
    }

    private void end() {
        warden.begin(FoldWarden.Attack.NONE);
        int base = warden.isEnraged() ? 25 : 45;
        cooldown = base + warden.getRandom().nextInt(base / 2 + 1);
    }

    private void roam(double pace) {
        Vec3 center = Vec3.atBottomCenterOf(warden.home());
        Vec3 point;
        if (target == null) {
            roamAngle += 0.02;
            point = center.add(Math.cos(roamAngle) * 10.0, 10.0 + Math.sin(warden.tickCount * 0.04) * 2.0, Math.sin(roamAngle) * 10.0);
        } else {
            roamAngle += 0.035 * pace;
            point = target.position().add(Math.cos(roamAngle) * 9.0, 4.0 + Math.sin(warden.tickCount * 0.05) * 2.0, Math.sin(roamAngle) * 9.0);
        }
        warden.steerTo(warden.clampToArena(point), 0.45 * pace);
    }

    private void coil(int ticks, double pace) {
        coilAngle += 0.12 * pace;
        double radius = Mth.lerp(ticks / (double) COIL_TICKS, 8.0, 2.5);
        Vec3 point = target.position().add(Math.cos(coilAngle) * radius, 1.0, Math.sin(coilAngle) * radius);
        warden.steerTo(warden.clampToArena(point), 0.8 * pace);
        if (ticks >= COIL_TICKS) end();
    }

    private void shed(ServerLevel server, int ticks) {
        if (ticks < SHED_RELEASE) {
            warden.steerTo(warden.clampToArena(target.position().add(0.0, 6.0, 0.0)), 0.2);
            return;
        }
        if (ticks == SHED_RELEASE) {
            int active = FoldWarden.SEGMENTS - warden.shed();
            int count = Math.min(warden.isEnraged() ? 5 : 3, active - MIN_BODY);
            List<ServerPlayer> players = warden.arenaPlayers();
            for (int i = 0; i < count && !players.isEmpty(); i++) {
                FoldWardenSegment segment = warden.segments()[active - 1 - i];
                FoldFragment fragment = ReliquaryEntities.FOLD_FRAGMENT.get().create(server);
                if (fragment == null) continue;
                ServerPlayer aim = players.get(warden.getRandom().nextInt(players.size()));
                Vec3 spread = new Vec3(warden.getRandom().nextGaussian(), 0.0, warden.getRandom().nextGaussian()).scale(0.8);
                fragment.launch(warden, segment.position().add(0.0, segment.size / 2.0, 0.0), aim.getEyePosition().add(spread));
                server.addFreshEntity(fragment);
            }
            warden.setShed(warden.shed() + Math.max(0, count));
            warden.playSound(ReliquarySounds.WYRM_SHED.get(), 3.0F, 1.0F);
        }
        if (ticks >= SHED_TICKS) end();
    }

    private void dive(ServerLevel server, int ticks, double pace) {
        if (ticks < DIVE_RISE) {
            warden.steerTo(warden.clampToArena(target.position().add(0.0, 14.0, 0.0)), 0.7 * pace);
            return;
        }
        if (ticks == DIVE_RISE) {
            diveTarget = target.position();
            warden.playSound(ReliquarySounds.WYRM_DIVE.get(), 3.0F, 1.0F);
        }
        Vec3 toTarget = diveTarget.add(0.0, 1.0, 0.0).subtract(warden.position());
        warden.setDeltaMovement(toTarget.normalize().scale(1.4 * pace));
        if (toTarget.length() < 1.8 || warden.getY() <= diveTarget.y + 0.5 || ticks >= DIVE_MAX) {
            server.sendParticles(ParticleTypes.EXPLOSION, diveTarget.x, diveTarget.y + 0.5, diveTarget.z, 3, 1.0, 0.2, 1.0, 0.0);
            server.sendParticles(QuantumParticles.QUANTUM_SPARK.get(), diveTarget.x, diveTarget.y + 0.5, diveTarget.z, 60, 2.0, 0.3, 2.0, 0.2);
            warden.playSound(ReliquarySounds.WARDEN_SLAM.get(), 3.0F, 0.8F);
            DamageSource source = warden.damageSources().mobAttack(warden);
            for (Player player : server.getEntitiesOfClass(Player.class, new AABB(diveTarget, diveTarget).inflate(4.0, 2.5, 4.0), warden::fightable)) {
                if (player.hurt(source, DIVE_DAMAGE)) {
                    player.setDeltaMovement(player.getDeltaMovement().add(0.0, 0.8, 0.0));
                    player.hurtMarked = true;
                }
            }
            updrafts.add(new Updraft(diveTarget, warden.tickCount + UPDRAFT_TICKS));
            warden.setDeltaMovement(0.0, 0.6, 0.0);
            end();
        }
    }

    private boolean breath(ServerLevel server, int ticks) {
        Vec3 mouth = mouth();
        if (ticks < BREATH_WINDUP) {
            Vec3 away = new Vec3(warden.getX() - target.getX(), 0.0, warden.getZ() - target.getZ());
            away = away.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : away.normalize();
            warden.steerTo(warden.clampToArena(target.position().add(away.scale(8.0)).add(0.0, 4.0, 0.0)), 0.4);
            face(target.getEyePosition());
            server.sendParticles(QuantumParticles.QUANTUM_SPARK.get(), mouth.x, mouth.y, mouth.z, 3, 0.2, 0.2, 0.2, 0.02);
            return true;
        }
        if (ticks == BREATH_WINDUP) {
            breathAim = target.getEyePosition();
            breathHit.clear();
            cage(server, target.blockPosition());
            warden.playSound(ReliquarySounds.WYRM_BREATH.get(), 3.0F, 1.0F);
        }
        face(breathAim);
        warden.setDeltaMovement(warden.getDeltaMovement().scale(0.6));
        if (ticks < BREATH_WINDUP + BREATH_BEAM) {
            Vec3 direction = breathAim.subtract(mouth).normalize();
            Vec3 end = mouth.add(direction.scale(BREATH_RANGE));
            for (double d = 0.0; d < BREATH_RANGE; d += 0.6) {
                Vec3 at = mouth.add(direction.scale(d));
                server.sendParticles(QuantumParticles.QUANTUM_SPARK.get(), at.x, at.y, at.z, 1, 0.05, 0.05, 0.05, 0.0);
            }
            DamageSource source = warden.damageSources().indirectMagic(warden, warden);
            for (Player player : server.getEntitiesOfClass(Player.class, new AABB(mouth, end).inflate(1.5), warden::fightable)) {
                if (breathHit.contains(player.getUUID())) continue;
                if (distanceToSegment(player.position().add(0.0, player.getBbHeight() / 2.0, 0.0), mouth, end) > 1.2) continue;
                breathHit.add(player.getUUID());
                player.hurt(source, BREATH_DAMAGE);
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1), warden);
            }
        }
        if (ticks >= BREATH_TICKS) end();
        return true;
    }

    private void cage(ServerLevel server, BlockPos center) {
        if (center.distSqr(warden.home()) > (FoldWarden.ARENA_RADIUS + 8) * (FoldWarden.ARENA_RADIUS + 8)) return;
        for (BlockPos offset : CAGE) {
            BlockPos pos = center.offset(offset);
            if (server.getBlockState(pos).isAir()) {
                server.setBlock(pos, ReliquaryBlocks.FOLD_LATTICE.get().defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    private Vec3 mouth() {
        return warden.position().add(0.0, warden.getBbHeight() * 0.45, 0.0).add(warden.getLookAngle().scale(1.6));
    }

    private void face(Vec3 point) {
        Vec3 delta = point.subtract(warden.getEyePosition());
        float yaw = (float) (Mth.atan2(delta.z, delta.x) * Mth.RAD_TO_DEG) - 90.0F;
        float pitch = (float) (-Mth.atan2(delta.y, delta.horizontalDistance()) * Mth.RAD_TO_DEG);
        warden.setYRot(Mth.approachDegrees(warden.getYRot(), yaw, 15.0F));
        warden.yBodyRot = warden.getYRot();
        warden.yHeadRot = warden.getYRot();
        warden.setXRot(Mth.approachDegrees(warden.getXRot(), Mth.clamp(pitch, -60.0F, 60.0F), 10.0F));
    }

    private void tickUpdrafts(ServerLevel server) {
        updrafts.removeIf(updraft -> updraft.expires() <= warden.tickCount);
        for (Updraft updraft : updrafts) {
            Vec3 pos = updraft.pos();
            if (warden.tickCount % 2 == 0) {
                server.sendParticles(ParticleTypes.CLOUD, pos.x, pos.y + 0.2, pos.z, 0, 0.0, 1.0, 0.0, 0.35);
                server.sendParticles(QuantumParticles.QUANTUM_SPARK.get(), pos.x, pos.y + 1.0, pos.z, 2, 0.6, 1.0, 0.6, 0.02);
            }
            for (Player player : server.getEntitiesOfClass(Player.class, new AABB(pos, pos).inflate(1.5, 0.0, 1.5).expandTowards(0.0, 14.0, 0.0)
                    .move(0.0, -1.0, 0.0), p -> p.isAlive() && !p.isSpectator())) {
                Vec3 motion = player.getDeltaMovement();
                player.setDeltaMovement(motion.x, Math.max(motion.y, 0.55), motion.z);
                player.resetFallDistance();
                player.hurtMarked = true;
            }
        }
    }

    private void tickContact(ServerLevel server) {
        if (warden.tickCount % 5 != 0) return;
        DamageSource source = warden.damageSources().mobAttack(warden);
        float damage = warden.isEnraged() ? CONTACT_DAMAGE + 2.2F : CONTACT_DAMAGE;
        List<AABB> boxes = new ArrayList<>();
        boxes.add(warden.getBoundingBox().inflate(0.3));
        for (FoldWardenSegment segment : warden.segments()) {
            if (segment.isActive()) boxes.add(segment.getBoundingBox().inflate(0.2));
        }
        for (ServerPlayer player : warden.arenaPlayers()) {
            for (AABB box : boxes) {
                if (box.intersects(player.getBoundingBox())) {
                    if (player.hurt(source, damage)) {
                        player.knockback(0.8, warden.getX() - player.getX(), warden.getZ() - player.getZ());
                    }
                    break;
                }
            }
        }
    }

    private static double distanceToSegment(Vec3 point, Vec3 a, Vec3 b) {
        Vec3 ab = b.subtract(a);
        double t = Mth.clamp(point.subtract(a).dot(ab) / ab.lengthSqr(), 0.0, 1.0);
        return point.distanceTo(a.add(ab.scale(t)));
    }
}
