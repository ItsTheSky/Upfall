package net.itsthesky.upfall.compat.mc.client;

import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class McCamera {
    private McCamera() {}

    public static Vec3 position(Camera camera) {
        //? if >=26.1 {
        /*return camera.position();
        *///?} else {
        return camera.getPosition();
        //?}
    }

    public static BlockPos blockPosition(Camera camera) {
        //? if >=26.1 {
        /*return camera.blockPosition();
        *///?} else {
        return camera.getBlockPosition();
        //?}
    }

    public static float xRot(Camera camera) {
        //? if >=26.1 {
        /*return camera.xRot();
        *///?} else {
        return camera.getXRot();
        //?}
    }

    public static Entity entity(Camera camera) {
        //? if >=26.1 {
        /*return camera.entity();
        *///?} else {
        return camera.getEntity();
        //?}
    }
}
