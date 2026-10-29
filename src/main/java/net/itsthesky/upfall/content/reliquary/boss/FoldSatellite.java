package net.itsthesky.upfall.content.reliquary.boss;

import net.itsthesky.upfall.compat.mc.DataIn;
import net.itsthesky.upfall.compat.mc.DataOut;
import net.itsthesky.upfall.compat.mc.McEntities;
import net.itsthesky.upfall.compat.mc.UpfallFlyingMob;
import net.itsthesky.upfall.content.quantum.QuantumParticles;
import net.itsthesky.upfall.content.reliquary.ReliquarySounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

public class FoldSatellite extends UpfallFlyingMob implements Enemy {
    public enum Mode { ORBIT, MARK, DIVE, RISE }

    private static final EntityDataAccessor<Integer> MODE = SynchedEntityData.defineId(FoldSatellite.class, EntityDataSerializers.INT);
    private static final int MARK_TICKS = 30;
    private static final int DIVE_TICKS = 24;
    private static final int RISE_TICKS = 30;
    private static final double ORBIT_RADIUS = 5.5;
    private static final float SLAM_DAMAGE = 8.8F;
    private static final double SLAM_RADIUS = 2.5;

    @Nullable
    private UUID ownerId;
    private int index;
    private int modeTicks;
    private int orphanTicks;
    private Vec3 slamTarget = Vec3.ZERO;

    public FoldSatellite(EntityType<? extends FoldSatellite> type, Level level) {
        super(type, level);
        this.xpReward = 5;
        setPersistenceRequired();
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return UpfallFlyingMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    void bind(FoldWarden owner, int index) {
        this.ownerId = owner.getUUID();
        this.index = index;
    }

    public boolean isOwnedBy(FoldWarden owner) {
        return owner.getUUID().equals(ownerId);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(MODE, Mode.ORBIT.ordinal());
    }

    public Mode mode() {
        return Mode.values()[Mth.clamp(entityData.get(MODE), 0, Mode.values().length - 1)];
    }

    private void enter(Mode mode) {
        entityData.set(MODE, mode.ordinal());
        modeTicks = 0;
    }

    public boolean isIdle() {
        return mode() == Mode.ORBIT;
    }

    void slam(Vec3 target) {
        slamTarget = target;
        enter(Mode.MARK);
        playSound(ReliquarySounds.WARDEN_SLAM_CHARGE.get(), 1.5F, 1.0F);
    }

    @Override
    public void tick() {
        this.noPhysics = true;
        super.tick();
        this.noPhysics = false;
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
        FoldWarden owner = owner();
        if (owner == null) {
            if (++orphanTicks > 100) discard();
            setDeltaMovement(getDeltaMovement().scale(0.8));
            return;
        }
        orphanTicks = 0;
        modeTicks++;
        switch (mode()) {
            case ORBIT -> steer(orbitPoint(owner), 0.25);
            case MARK -> {
                steer(slamTarget.add(0.0, 7.0, 0.0), 0.3);
                if (modeTicks % 4 == 0) ring(slamTarget, SLAM_RADIUS);
                if (modeTicks >= MARK_TICKS) {
                    enter(Mode.DIVE);
                    playSound(ReliquarySounds.WYRM_DIVE.get(), 1.5F, 1.2F);
                }
            }
            case DIVE -> {
                setDeltaMovement(slamTarget.subtract(position()).normalize().scale(1.1));
                BlockPos below = BlockPos.containing(getX(), getY() - 0.2, getZ());
                boolean grounded = !level().getBlockState(below).getCollisionShape(level(), below).isEmpty();
                if (getY() <= slamTarget.y + 0.2 || grounded || modeTicks >= DIVE_TICKS) {
                    impact(owner);
                    enter(Mode.RISE);
                }
            }
            case RISE -> {
                steer(orbitPoint(owner), 0.12);
                if (modeTicks >= RISE_TICKS) enter(Mode.ORBIT);
            }
        }
    }

    private Vec3 orbitPoint(FoldWarden owner) {
        double angle = owner.tickCount * 0.035 + index * Math.PI / 2.0;
        double bob = Math.sin(owner.tickCount * 0.07 + index * 1.3) * 1.2;
        return new Vec3(owner.getX() + Math.cos(angle) * ORBIT_RADIUS, owner.getY(0.55) + bob + (index % 2 == 0 ? -1.0 : 1.0),
                owner.getZ() + Math.sin(angle) * ORBIT_RADIUS);
    }

    private void steer(Vec3 point, double strength) {
        Vec3 delta = point.subtract(position());
        Vec3 motion = delta.scale(strength);
        if (motion.length() > 1.2) motion = motion.normalize().scale(1.2);
        setDeltaMovement(motion);
    }

    private void ring(Vec3 center, double radius) {
        if (!(level() instanceof ServerLevel server)) return;
        for (int i = 0; i < 16; i++) {
            double angle = i * Math.PI / 8.0;
            server.sendParticles(QuantumParticles.QUANTUM_SPARK.get(), center.x + Math.cos(angle) * radius, center.y + 0.1,
                    center.z + Math.sin(angle) * radius, 1, 0.0, 0.02, 0.0, 0.0);
        }
    }

    private void impact(FoldWarden owner) {
        if (!(level() instanceof ServerLevel server)) return;
        server.playSound(null, getX(), getY(), getZ(), ReliquarySounds.WARDEN_SLAM.get(), SoundSource.HOSTILE, 1.5F, 1.0F);
        server.sendParticles(QuantumParticles.QUANTUM_SPARK.get(), getX(), getY(), getZ(), 40, 1.2, 0.3, 1.2, 0.15);
        DamageSource source = damageSources().mobAttack(owner);
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(SLAM_RADIUS, 1.5, SLAM_RADIUS),
                e -> !(e instanceof FoldSatellite) && !(e instanceof FoldWarden) && e.isAlive())) {
            if (McEntities.hurt(victim, source, SLAM_DAMAGE)) {
                Vec3 push = victim.position().subtract(position()).multiply(1, 0, 1);
                if (push.lengthSqr() > 1.0E-4) victim.knockback(0.8, -push.x, -push.z);
            }
        }
    }

    @Nullable
    private FoldWarden owner() {
        if (ownerId == null || !(level() instanceof ServerLevel server)) return null;
        Entity entity = server.getEntity(ownerId);
        return entity instanceof FoldWarden warden && warden.isAlive() ? warden : null;
    }

    private void save(DataOut out) {
        if (ownerId != null) out.putUUID("Owner", ownerId);
        out.putInt("Index", index);
    }

    private void load(DataIn in) {
        in.getUUID("Owner").ifPresent(id -> ownerId = id);
        index = in.getIntOr("Index", 0);
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
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }
}
