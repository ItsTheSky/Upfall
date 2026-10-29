package net.itsthesky.upfall.content.reliquary.boss;

import net.itsthesky.upfall.compat.mc.DataIn;
import net.itsthesky.upfall.compat.mc.DataOut;
import net.itsthesky.upfall.compat.mc.McEntities;
import net.itsthesky.upfall.compat.mc.UpfallFlyingMob;
import net.itsthesky.upfall.content.mobs.entity.AirSteering;
import net.itsthesky.upfall.content.quantum.QuantumParticles;
import net.itsthesky.upfall.content.reliquary.ReliquaryBlocks;
import net.itsthesky.upfall.content.reliquary.ReliquaryEntities;
import net.itsthesky.upfall.content.reliquary.ReliquarySounds;
import net.itsthesky.upfall.content.reliquary.block.FoldSocketBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FoldWarden extends UpfallFlyingMob implements Enemy {
    public enum Phase { AWAKEN, CUBE, UNFOLD, WYRM }

    public enum Attack { NONE, TILT, WEIGHT, COIL, SHED, DIVE, BREATH }

    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(FoldWarden.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PHASE_TICKS = SynchedEntityData.defineId(FoldWarden.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK = SynchedEntityData.defineId(FoldWarden.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_TICKS = SynchedEntityData.defineId(FoldWarden.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SHIELDED = SynchedEntityData.defineId(FoldWarden.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> SHED = SynchedEntityData.defineId(FoldWarden.class, EntityDataSerializers.INT);
    public static final int AWAKEN_TICKS = 100;
    public static final int UNFOLD_TICKS = 80;
    public static final int DEATH_TICKS = 60;
    public static final int TILT_WINDUP = 30;
    public static final int TILT_PUSH = 80;
    public static final int WEIGHT_WINDUP = 30;
    public static final int SEGMENTS = 12;
    public static final float PHASE_TWO_HEALTH = 200.0F;
    public static final float ENRAGE_HEALTH = 100.0F;
    public static final double ARENA_RADIUS = 24.0;
    private static final int WEIGHT_LIFT = 40;
    private static final int SATELLITES = 4;
    private static final int SATELLITE_RESPAWN = 600;
    private static final double BAR_RADIUS = 40.0;
    private static final double HOVER_HEIGHT = 6.5;
    private static final double TILT_FORCE = 0.075;
    private static final float CONTACT_DAMAGE = 6.6F;
    private static final float WEIGHT_DAMAGE = 4.4F;
    private static final EntityDimensions WYRM_DIMENSIONS = EntityDimensions.scalable(2.0F, 1.7F);

    //? if >=26.1 {
    /*private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(Mth.createInsecureUUID(random), getDisplayName(),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10).setDarkenScreen(true);
    *///?} else {
    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(getDisplayName(),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10).setDarkenScreen(true);
    //?}
    private final FoldWardenSegment[] segments = new FoldWardenSegment[SEGMENTS];
    private final FoldWyrmBrain wyrm = new FoldWyrmBrain(this);
    private final List<UUID> lifted = new ArrayList<>();
    private BlockPos home;
    private int attackCooldown = 60;
    private int satelliteRespawn;
    private boolean killing;
    private Vec3 tiltDirection = Vec3.ZERO;
    public float shutterO;
    public float shutter;

    public FoldWarden(EntityType<? extends FoldWarden> type, Level level) {
        super(type, level);
        for (int i = 0; i < SEGMENTS; i++) {
            segments[i] = new FoldWardenSegment(this, i, segmentSize(i));
        }
        setId(ENTITY_COUNTER.getAndAdd(SEGMENTS + 1) + 1);
        this.moveControl = new AirSteering.SteerMoveControl(this, 0.08, 0.5);
        this.xpReward = 250;
        setPersistenceRequired();
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return UpfallFlyingMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 400.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.ATTACK_DAMAGE, 11.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.FLYING_SPEED, 0.6);
    }

    public static float segmentSize(int index) {
        return 1.4F - 0.75F * index / (SEGMENTS - 1);
    }

    public static double segmentSpacing(int index) {
        return index == 0 ? 1.4 : (segmentSize(index - 1) + segmentSize(index)) * 0.6;
    }

    public static void summon(ServerLevel level, List<BlockPos> sockets) {
        if (sockets.isEmpty()) return;
        double x = 0, y = 0, z = 0;
        for (BlockPos socket : sockets) {
            x += socket.getX();
            y += socket.getY();
            z += socket.getZ();
        }
        BlockPos center = BlockPos.containing(x / sockets.size(), y / sockets.size(), z / sockets.size());
        AABB around = new AABB(center).inflate(ARENA_RADIUS * 2);
        if (!level.getEntitiesOfClass(FoldWarden.class, around, w -> w.home != null && w.home.closerThan(center, 4.0)).isEmpty()) return;
        FoldWarden warden = ReliquaryEntities.create(ReliquaryEntities.FOLD_WARDEN.get(), level);
        if (warden == null) return;
        McEntities.snapTo(warden, center.getX() + 0.5, center.getY() + 1, center.getZ() + 0.5, 0.0F, 0.0F);
        warden.home = center;
        level.addFreshEntity(warden);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PHASE, Phase.AWAKEN.ordinal());
        builder.define(PHASE_TICKS, 0);
        builder.define(ATTACK, Attack.NONE.ordinal());
        builder.define(ATTACK_TICKS, 0);
        builder.define(SHIELDED, true);
        builder.define(SHED, 0);
    }

    public Phase phase() {
        return Phase.values()[Mth.clamp(entityData.get(PHASE), 0, Phase.values().length - 1)];
    }

    public int phaseTicks() {
        return entityData.get(PHASE_TICKS);
    }

    public Attack attack() {
        return Attack.values()[Mth.clamp(entityData.get(ATTACK), 0, Attack.values().length - 1)];
    }

    public int attackTicks() {
        return entityData.get(ATTACK_TICKS);
    }

    public boolean isShielded() {
        return entityData.get(SHIELDED);
    }

    public int shed() {
        return entityData.get(SHED);
    }

    void setShed(int count) {
        entityData.set(SHED, Mth.clamp(count, 0, SEGMENTS));
    }

    public boolean isEnraged() {
        return phase() == Phase.WYRM && getHealth() <= ENRAGE_HEALTH;
    }

    public FoldWardenSegment[] segments() {
        return segments;
    }

    public boolean segmentVisible(int index) {
        Phase phase = phase();
        return (phase == Phase.UNFOLD || phase == Phase.WYRM) && index < SEGMENTS - shed();
    }

    boolean segmentActive(int index) {
        return phase() == Phase.WYRM && index < SEGMENTS - shed() && isAlive();
    }

    BlockPos home() {
        return home;
    }

    private void enter(Phase next) {
        entityData.set(PHASE, next.ordinal());
        entityData.set(PHASE_TICKS, 0);
    }

    void begin(Attack next) {
        entityData.set(ATTACK, next.ordinal());
        entityData.set(ATTACK_TICKS, 0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (PHASE.equals(key)) refreshDimensions();
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        return phase() == Phase.WYRM ? WYRM_DIMENSIONS : super.getDefaultDimensions(pose);
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        for (int i = 0; i < SEGMENTS; i++) {
            segments[i].setId(id + i + 1);
        }
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public PartEntity<?>[] getParts() {
        return segments;
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public void tick() {
        this.noPhysics = true;
        super.tick();
        this.noPhysics = false;
        updateSegments();
        if (level().isClientSide()) {
            shutterO = shutter;
            float open = phase() == Phase.UNFOLD || phase() == Phase.CUBE && !isShielded() ? 1.0F : 0.0F;
            shutter = Mth.approach(shutter, open, 0.06F);
            if (phase() == Phase.AWAKEN) awakenParticles();
        }
    }

    private void updateSegments() {
        Phase phase = phase();
        boolean folded = phase == Phase.AWAKEN || phase == Phase.CUBE;
        Vec3 lead = position().add(0.0, getBbHeight() / 2.0, 0.0);
        for (int i = 0; i < SEGMENTS; i++) {
            FoldWardenSegment segment = segments[i];
            segment.setOldPosAndRot();
            Vec3 current = segment.position().add(0.0, segment.size / 2.0, 0.0);
            if (folded) {
                current = lead;
            } else {
                Vec3 diff = current.subtract(lead);
                double gap = segmentSpacing(i);
                if (diff.lengthSqr() > gap * gap) current = lead.add(diff.normalize().scale(gap));
                lead = current;
            }
            segment.setPos(current.x, current.y - segment.size / 2.0, current.z);
        }
    }

    private void awakenParticles() {
        float progress = phaseTicks() / (float) AWAKEN_TICKS;
        for (int i = 0; i < 3; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = 4.0 * (1.0 - progress) + 1.0;
            level().addParticle(QuantumParticles.QUANTUM_SPARK.get(), getX() + Math.cos(angle) * radius, getY(0.5) + random.nextGaussian(),
                    getZ() + Math.sin(angle) * radius, -Math.cos(angle) * 0.1, 0.02, -Math.sin(angle) * 0.1);
        }
    }

    //? if >=26.1 {
    /*@Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
    *///?} else {
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
    //?}
        if (home == null) home = blockPosition();
        entityData.set(PHASE_TICKS, phaseTicks() + 1);
        switch (phase()) {
            case AWAKEN -> tickAwaken();
            case CUBE -> tickCube();
            case UNFOLD -> tickUnfold();
            case WYRM -> wyrm.tick();
        }
        bossEvent.setProgress(getHealth() / getMaxHealth());
        if (tickCount % 10 == 0) refreshBossViewers();
        if (tickCount % 20 == 0 && shed() > 0) {
            setShed(Math.min(shed(), fragments().size()));
        }
    }

    private void tickAwaken() {
        int ticks = phaseTicks();
        if (ticks == 1) {
            playSound(ReliquarySounds.WARDEN_AWAKEN.get(), 3.0F, 1.0F);
        }
        if (ticks > 30) {
            double targetY = home.getY() + HOVER_HEIGHT;
            setDeltaMovement(0.0, getY() < targetY ? 0.09 : 0.0, 0.0);
        }
        if (ticks >= AWAKEN_TICKS) {
            enter(Phase.CUBE);
            spawnSatellites();
        }
    }

    private void tickCube() {
        if (getHealth() <= PHASE_TWO_HEALTH) {
            startUnfold();
            return;
        }
        hover();
        tickShield();
        tickContact();
        if (attack() == Attack.NONE) {
            if (--attackCooldown <= 0) chooseAttack();
        } else {
            tickAttackTimer();
            switch (attack()) {
                case TILT -> tickTilt();
                case WEIGHT -> tickWeight();
                default -> endAttack();
            }
        }
    }

    void tickAttackTimer() {
        entityData.set(ATTACK_TICKS, attackTicks() + 1);
    }

    private void startUnfold() {
        enter(Phase.UNFOLD);
        begin(Attack.NONE);
        getMoveControl().setWantedPosition(getX(), getY(), getZ(), 0.0);
        lifted.clear();
        for (FoldSatellite satellite : satellites()) {
            satellite.discard();
        }
        entityData.set(SHIELDED, false);
        bossEvent.setColor(BossEvent.BossBarColor.BLUE);
        playSound(ReliquarySounds.WARDEN_UNFOLD.get(), 4.0F, 1.0F);
    }

    private void tickUnfold() {
        int ticks = phaseTicks();
        Vec3 center = Vec3.atBottomCenterOf(home);
        double angle = ticks * 0.12;
        double radius = Math.min(6.0, ticks * 0.1);
        Vec3 point = center.add(Math.cos(angle) * radius, HOVER_HEIGHT + ticks * 0.06, Math.sin(angle) * radius);
        steerTo(point, 0.35);
        AirSteering.faceMotion(this, 20.0F);
        if (level() instanceof ServerLevel server && ticks % 3 == 0) {
            server.sendParticles(QuantumParticles.QUANTUM_SPARK.get(), getX(), getY(0.5), getZ(), 12, 0.8, 0.8, 0.8, 0.15);
        }
        if (ticks >= UNFOLD_TICKS) {
            enter(Phase.WYRM);
            wyrm.reset();
        }
    }

    private void hover() {
        LivingEntity target = getTarget();
        Vec3 center = Vec3.atBottomCenterOf(home);
        Vec3 wanted;
        if (target != null && target.isAlive() && target.distanceToSqr(center) < (ARENA_RADIUS + 8) * (ARENA_RADIUS + 8)) {
            Vec3 flat = new Vec3(target.getX() - center.x, 0.0, target.getZ() - center.z);
            if (flat.length() > ARENA_RADIUS * 0.6) flat = flat.normalize().scale(ARENA_RADIUS * 0.6);
            wanted = center.add(flat.x, Math.max(HOVER_HEIGHT - 2.0, target.getY() - center.y + 4.5), flat.z);
            facePoint(target.getX(), target.getZ(), 6.0F);
        } else {
            setTarget(null);
            double angle = tickCount * 0.01;
            wanted = center.add(Math.cos(angle) * 4.0, HOVER_HEIGHT, Math.sin(angle) * 4.0);
        }
        wanted = wanted.add(0.0, Math.sin(tickCount * 0.05) * 0.8, 0.0);
        getMoveControl().setWantedPosition(wanted.x, wanted.y, wanted.z, attack() == Attack.TILT ? 0.3 : 1.0);
    }

    private void tickShield() {
        if (tickCount % 10 != 0) return;
        boolean shielded = !satellites().isEmpty();
        if (shielded != isShielded()) {
            entityData.set(SHIELDED, shielded);
            if (!shielded) {
                satelliteRespawn = SATELLITE_RESPAWN;
                playSound(ReliquarySounds.WARDEN_SHIELD_BREAK.get(), 3.0F, 1.0F);
                if (level() instanceof ServerLevel server) {
                    server.sendParticles(QuantumParticles.QUANTUM_SPARK.get(), getX(), getY(0.55), getZ(), 80, 1.5, 1.5, 1.5, 0.2);
                }
            }
        }
        if (!shielded && (satelliteRespawn -= 10) <= 0) {
            spawnSatellites();
        }
    }

    private void spawnSatellites() {
        if (!(level() instanceof ServerLevel server)) return;
        for (int i = 0; i < SATELLITES; i++) {
            FoldSatellite satellite = ReliquaryEntities.create(ReliquaryEntities.FOLD_SATELLITE.get(), server);
            if (satellite == null) continue;
            double angle = i * Math.PI / 2.0;
            McEntities.snapTo(satellite, getX() + Math.cos(angle) * 1.5, getY(0.5), getZ() + Math.sin(angle) * 1.5, 0.0F, 0.0F);
            satellite.bind(this, i);
            server.addFreshEntity(satellite);
        }
        entityData.set(SHIELDED, true);
        playSound(ReliquarySounds.WARDEN_SHIELD_RESTORE.get(), 3.0F, 1.0F);
    }

    private List<FoldSatellite> satellites() {
        return level().getEntitiesOfClass(FoldSatellite.class, getBoundingBox().inflate(48.0), s -> s.isAlive() && s.isOwnedBy(this));
    }

    List<FoldFragment> fragments() {
        return level().getEntitiesOfClass(FoldFragment.class, getBoundingBox().inflate(64.0), f -> f.isAlive() && f.getOwner() == this);
    }

    private void tickContact() {
        if (tickCount % 10 != 0) return;
        DamageSource source = damageSources().mobAttack(this);
        for (Player player : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(0.3), this::fightable)) {
            if (McEntities.hurt(player, source, CONTACT_DAMAGE)) {
                player.knockback(1.2, getX() - player.getX(), getZ() - player.getZ());
            }
        }
    }

    private void chooseAttack() {
        List<ServerPlayer> players = arenaPlayers();
        if (players.isEmpty()) {
            attackCooldown = 20;
            return;
        }
        List<FoldSatellite> idle = satellites().stream().filter(FoldSatellite::isIdle).toList();
        int roll = random.nextInt(100);
        if (!idle.isEmpty() && roll < 50) {
            ServerPlayer target = players.get(random.nextInt(players.size()));
            idle.get(random.nextInt(idle.size())).slam(target.position());
            attackCooldown = 45 + random.nextInt(25);
        } else if (roll < (idle.isEmpty() ? 50 : 75)) {
            double angle = random.nextDouble() * Math.PI * 2;
            tiltDirection = new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
            begin(Attack.TILT);
            playSound(ReliquarySounds.WARDEN_TILT.get(), 3.0F, 1.0F);
        } else {
            begin(Attack.WEIGHT);
            playSound(ReliquarySounds.WARDEN_WEIGHT.get(), 3.0F, 1.0F);
        }
    }

    private void endAttack() {
        begin(Attack.NONE);
        attackCooldown = isShielded() ? 60 + random.nextInt(30) : 35 + random.nextInt(20);
    }

    private void tickTilt() {
        int ticks = attackTicks();
        if (!(level() instanceof ServerLevel server)) return;
        List<ServerPlayer> players = arenaPlayers();
        if (ticks < TILT_WINDUP) {
            if (ticks % 3 == 0) {
                for (ServerPlayer player : players) {
                    for (int i = 1; i <= 4; i++) {
                        Vec3 at = player.position().add(tiltDirection.scale(i * 0.7)).add(0.0, 0.2, 0.0);
                        server.sendParticles(QuantumParticles.QUANTUM_SPARK.get(), at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
                    }
                }
            }
            return;
        }
        for (ServerPlayer player : players) {
            double force = player.isShiftKeyDown() ? TILT_FORCE * 0.5 : TILT_FORCE;
            player.setDeltaMovement(player.getDeltaMovement().add(tiltDirection.scale(force)));
            player.hurtMarked = true;
            if (ticks % 4 == 0) {
                server.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.3, player.getZ(), 1, 0.2, 0.1, 0.2, 0.0);
            }
        }
        if (ticks >= TILT_WINDUP + TILT_PUSH) endAttack();
    }

    private void tickWeight() {
        int ticks = attackTicks();
        if (!(level() instanceof ServerLevel server)) return;
        if (ticks < WEIGHT_WINDUP) {
            if (ticks % 2 == 0) {
                for (ServerPlayer player : arenaPlayers()) {
                    server.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 0.1, player.getZ(), 4, 0.4, 0.05, 0.4, 0.05);
                }
            }
            return;
        }
        if (ticks == WEIGHT_WINDUP) {
            lifted.clear();
            for (ServerPlayer player : arenaPlayers()) {
                if (onFilledSocket(player)) {
                    server.sendParticles(QuantumParticles.QUANTUM_SPARK.get(), player.getX(), player.getY() + 0.5, player.getZ(), 20, 0.3, 0.5, 0.3, 0.05);
                    continue;
                }
                player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, WEIGHT_LIFT, 2), this);
                lifted.add(player.getUUID());
            }
        }
        if (ticks == WEIGHT_WINDUP + WEIGHT_LIFT) {
            DamageSource source = damageSources().indirectMagic(this, this);
            for (UUID id : lifted) {
                if (!(server.getEntity(id) instanceof ServerPlayer player) || !player.isAlive()) continue;
                player.removeEffect(MobEffects.LEVITATION);
                Vec3 motion = player.getDeltaMovement();
                player.setDeltaMovement(motion.x * 0.2, -2.5, motion.z * 0.2);
                player.hurtMarked = true;
                player.hurt(source, WEIGHT_DAMAGE);
            }
            lifted.clear();
            playSound(ReliquarySounds.WARDEN_DROP.get(), 3.0F, 1.0F);
            endAttack();
        }
    }

    private boolean onFilledSocket(Player player) {
        BlockState below = level().getBlockState(BlockPos.containing(player.getX(), player.getY() - 0.2, player.getZ()));
        return below.is(ReliquaryBlocks.FOLD_SOCKET.get()) && below.getValue(FoldSocketBlock.FILLED);
    }

    void steerTo(Vec3 point, double speed) {
        Vec3 delta = point.subtract(position());
        double distance = delta.length();
        Vec3 motion = getDeltaMovement();
        Vec3 desired = distance < 0.3 ? Vec3.ZERO : delta.scale(Math.min(speed, distance * 0.4) / distance);
        setDeltaMovement(motion.add(desired.subtract(motion).scale(0.15)));
    }

    Vec3 clampToArena(Vec3 point) {
        Vec3 center = Vec3.atBottomCenterOf(home);
        Vec3 flat = new Vec3(point.x - center.x, 0.0, point.z - center.z);
        double limit = ARENA_RADIUS - 2.0;
        if (flat.length() > limit) flat = flat.normalize().scale(limit);
        double y = Mth.clamp(point.y, center.y + 2.0, center.y + 22.0);
        return new Vec3(center.x + flat.x, y, center.z + flat.z);
    }

    List<ServerPlayer> arenaPlayers() {
        if (!(level() instanceof ServerLevel server) || home == null) return List.of();
        Vec3 center = Vec3.atBottomCenterOf(home);
        double range = ARENA_RADIUS + 8.0;
        return server.getPlayers(p -> fightable(p) && p.distanceToSqr(center) < range * range);
    }

    boolean fightable(Player player) {
        return player.isAlive() && !player.isSpectator() && !player.isCreative();
    }

    private void facePoint(double x, double z, float maxTurn) {
        float yaw = (float) (Mth.atan2(z - getZ(), x - getX()) * Mth.RAD_TO_DEG) - 90.0F;
        setYRot(Mth.approachDegrees(getYRot(), yaw, maxTurn));
        yBodyRot = getYRot();
        yHeadRot = getYRot();
    }

    private void refreshBossViewers() {
        if (!(level() instanceof ServerLevel server)) return;
        Vec3 anchor = home != null ? Vec3.atCenterOf(home) : position();
        for (ServerPlayer player : server.players()) {
            boolean near = player.distanceToSqr(anchor) < BAR_RADIUS * BAR_RADIUS && isAlive();
            if (near) {
                bossEvent.addPlayer(player);
            } else {
                bossEvent.removePlayer(player);
            }
        }
    }

    boolean hurtFromSegment(DamageSource source, float amount) {
        //? if >=26.1 {
        /*return level() instanceof ServerLevel server && hurtServer(server, source, amount * 0.5F);
        *///?} else {
        return hurt(source, amount * 0.5F);
        //?}
    }

    //? if >=26.1 {
    /*@Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        if (super.isInvulnerableTo(level, source)) return true;
    *///?} else {
    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (super.isInvulnerableTo(source)) return true;
    //?}
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        Phase phase = phase();
        return phase == Phase.AWAKEN || phase == Phase.UNFOLD || phase == Phase.CUBE && isShielded();
    }

    //? if >=26.1 {
    /*@Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
    *///?} else {
    @Override
    public boolean hurt(DamageSource source, float amount) {
    //?}
        if (!level().isClientSide() && phase() == Phase.CUBE && isShielded() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                && source.getEntity() instanceof Player) {
            playSound(ReliquarySounds.WARDEN_SHIELD_HIT.get(), 2.0F, 1.0F);
            if (level() instanceof ServerLevel server) {
                Vec3 at = source.getSourcePosition() != null ? source.getSourcePosition() : position();
                server.sendParticles(QuantumParticles.QUANTUM_SPARK.get(), Mth.lerp(0.5, at.x, getX()), getY(0.55),
                        Mth.lerp(0.5, at.z, getZ()), 8, 0.3, 0.3, 0.3, 0.05);
            }
        }
        //? if >=26.1 {
        /*return super.hurtServer(level, source, amount);
        *///?} else {
        return super.hurt(source, amount);
        //?}
    }

    @Override
    public void setHealth(float health) {
        if (!killing && phase() == Phase.CUBE && health < PHASE_TWO_HEALTH) {
            health = PHASE_TWO_HEALTH;
        }
        super.setHealth(health);
    }

    //? if >=26.1 {
    /*@Override
    public void kill(ServerLevel level) {
        killing = true;
        super.kill(level);
    }
    *///?} else {
    @Override
    public void kill() {
        killing = true;
        super.kill();
    }
    //?}

    @Override
    public void die(DamageSource source) {
        super.die(source);
        bossEvent.setProgress(0.0F);
        for (FoldSatellite satellite : satellites()) {
            satellite.discard();
        }
        for (FoldFragment fragment : fragments()) {
            fragment.discard();
        }
    }

    @Override
    protected void tickDeath() {
        deathTime++;
        if (level() instanceof ServerLevel server && deathTime % 4 == 0) {
            server.sendParticles(ParticleTypes.EXPLOSION, getX() + random.nextGaussian(), getY(0.5) + random.nextGaussian(),
                    getZ() + random.nextGaussian(), 1, 0.0, 0.0, 0.0, 0.0);
            for (FoldWardenSegment segment : segments) {
                if (segmentVisible(segment.index) && random.nextInt(3) == 0) {
                    server.sendParticles(QuantumParticles.QUANTUM_SPARK.get(), segment.getX(), segment.getY(0.5), segment.getZ(), 6,
                            0.3, 0.3, 0.3, 0.1);
                }
            }
        }
        setDeltaMovement(getDeltaMovement().scale(0.8).add(0.0, 0.01, 0.0));
        if (deathTime >= DEATH_TICKS && !level().isClientSide() && !isRemoved()) {
            level().broadcastEntityEvent(this, (byte) 60);
            remove(RemovalReason.KILLED);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return phase() == Phase.WYRM ? ReliquarySounds.WYRM_AMBIENT.get() : ReliquarySounds.WARDEN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ReliquarySounds.WARDEN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ReliquarySounds.WARDEN_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 3.0F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        bossEvent.removeAllPlayers();
    }

    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        bossEvent.setName(getDisplayName());
    }

    private void save(DataOut out) {
        if (home != null) out.putBlockPos("Home", home);
        out.putInt("Phase", entityData.get(PHASE));
        out.putInt("PhaseTicks", phaseTicks());
        out.putInt("SatelliteRespawn", satelliteRespawn);
    }

    private void load(DataIn in) {
        in.getBlockPos("Home").ifPresent(pos -> home = pos);
        entityData.set(PHASE, in.getIntOr("Phase", 0));
        entityData.set(PHASE_TICKS, in.getIntOr("PhaseTicks", 0));
        satelliteRespawn = in.getIntOr("SatelliteRespawn", 0);
        if (phase() == Phase.UNFOLD || phase() == Phase.WYRM) {
            entityData.set(SHIELDED, false);
            bossEvent.setColor(BossEvent.BossBarColor.BLUE);
        }
        if (hasCustomName()) bossEvent.setName(getDisplayName());
    }

    //? if >=26.1 {
    /*@Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
        super.addAdditionalSaveData(output);
        save(DataOut.of(output));
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
        super.readAdditionalSaveData(input);
        load(DataIn.of(input));
    }
    *///?} else {
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        save(DataOut.of(tag, level().registryAccess()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        load(DataIn.of(tag, level().registryAccess()));
    }
    //?}

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    //? if >=26.1 {
    /*public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
    *///?} else {
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
    //?}
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    //? if <26.1 {
    @Override
    public AABB getBoundingBoxForCulling() {
        return cullingBox();
    }
    //?}

    public AABB cullingBox() {
        AABB box = getBoundingBox().inflate(3.0);
        if (phase() == Phase.UNFOLD || phase() == Phase.WYRM) {
            for (FoldWardenSegment segment : segments) {
                box = box.minmax(segment.getBoundingBox());
            }
        }
        return box;
    }
}
