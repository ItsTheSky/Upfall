package net.itsthesky.upfall.content.chronospire.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class DriftPlatform extends Entity {
    private static final EntityDataAccessor<CompoundTag> DATA_PATH = SynchedEntityData.defineId(DriftPlatform.class, EntityDataSerializers.COMPOUND_TAG);
    private static final double MAX_CARRY = 1.0;

    private PlatformPath path;

    public DriftPlatform(EntityType<? extends DriftPlatform> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setPath(PlatformPath path) {
        this.path = path;
        entityData.set(DATA_PATH, path.save());
        setPos(path.positionAt(level().getGameTime()));
    }

    public PlatformPath path() {
        if (path == null) {
            path = PlatformPath.load(entityData.get(DATA_PATH));
        }
        return path;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_PATH, new CompoundTag());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_PATH.equals(key)) {
            path = null;
        }
    }

    @Override
    public void tick() {
        PlatformPath route = path();
        if (route.points().size() < 2) return;
        Vec3 target = route.positionAt(level().getGameTime());
        Vec3 delta = target.subtract(position());
        if (delta.lengthSqr() < 1.0E-10) return;
        AABB before = getBoundingBox();
        setPos(target);
        if (delta.lengthSqr() <= MAX_CARRY * MAX_CARRY) {
            carry(before, delta);
        }
    }

    private void carry(AABB before, Vec3 delta) {
        AABB zone = new AABB(before.minX, before.maxY - 0.05, before.minZ, before.maxX, before.maxY + 0.75, before.maxZ);
        for (Entity entity : level().getEntities(this, zone, EntitySelector.NO_SPECTATORS)) {
            if (entity.noPhysics || entity.isPassenger() || entity instanceof DriftPlatform) continue;
            if (entity.getBoundingBox().minY < before.maxY - 0.05) continue;
            boolean controlledHere = level().isClientSide ? entity.isControlledByLocalInstance() : !(entity instanceof Player);
            if (controlledHere) {
                entity.move(MoverType.SHULKER, delta);
            }
        }
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 256 * 256;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Path")) {
            setPath(PlatformPath.load(tag.getCompound("Path")));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.put("Path", path().save());
    }
}
