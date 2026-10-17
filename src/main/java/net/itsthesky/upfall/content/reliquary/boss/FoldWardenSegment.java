package net.itsthesky.upfall.content.reliquary.boss;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.entity.PartEntity;

import javax.annotation.Nullable;

public class FoldWardenSegment extends PartEntity<FoldWarden> {
    public final int index;
    public final float size;
    private final EntityDimensions dimensions;

    FoldWardenSegment(FoldWarden parent, int index, float size) {
        super(parent);
        this.index = index;
        this.size = size;
        this.dimensions = EntityDimensions.scalable(size, size);
        refreshDimensions();
    }

    public boolean isActive() {
        return getParent().segmentActive(index);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public boolean isPickable() {
        return isActive();
    }

    @Nullable
    @Override
    public ItemStack getPickResult() {
        return getParent().getPickResult();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!isActive() || isInvulnerableTo(source)) return false;
        return getParent().hurtFromSegment(source, amount);
    }

    @Override
    public boolean is(Entity entity) {
        return this == entity || getParent() == entity;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return dimensions;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
