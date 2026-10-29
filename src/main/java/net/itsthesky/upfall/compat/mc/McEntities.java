package net.itsthesky.upfall.compat.mc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class McEntities {
    private McEntities() {}

    public static void snapTo(Entity entity, double x, double y, double z, float yRot, float xRot) {
        //? if >=26.1 {
        /*entity.snapTo(x, y, z, yRot, xRot);
        *///?} else {
        entity.moveTo(x, y, z, yRot, xRot);
        //?}
    }

    public static void snapTo(Entity entity, double x, double y, double z) {
        //? if >=26.1 {
        /*entity.snapTo(x, y, z);
        *///?} else {
        entity.moveTo(x, y, z);
        //?}
    }

    public static void snapTo(Entity entity, Vec3 pos) {
        //? if >=26.1 {
        /*entity.snapTo(pos);
        *///?} else {
        entity.moveTo(pos);
        //?}
    }

    public static void snapTo(Entity entity, Vec3 pos, float yRot, float xRot) {
        //? if >=26.1 {
        /*entity.snapTo(pos, yRot, xRot);
        *///?} else {
        entity.moveTo(pos, yRot, xRot);
        //?}
    }

    public static void snapTo(Entity entity, BlockPos pos, float yRot, float xRot) {
        //? if >=26.1 {
        /*entity.snapTo(pos, yRot, xRot);
        *///?} else {
        entity.moveTo(pos, yRot, xRot);
        //?}
    }

    public static boolean hurt(Entity entity, DamageSource source, float amount) {
        //? if >=26.1 {
        /*return entity.hurtOrSimulate(source, amount);
        *///?} else {
        return entity.hurt(source, amount);
        //?}
    }
}
