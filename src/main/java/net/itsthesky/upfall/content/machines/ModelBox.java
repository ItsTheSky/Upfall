package net.itsthesky.upfall.content.machines;

import net.minecraft.core.Direction;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public record ModelBox(float x1, float y1, float z1, float x2, float y2, float z2, Map<Direction, Face> faces) {
    public record Face(String texture, float[] uvs, Direction cullface) {
        public static Face of(String texture) {
            return new Face(texture, null, null);
        }

        public Face uvs(float u1, float v1, float u2, float v2) {
            return new Face(texture, new float[]{u1, v1, u2, v2}, cullface);
        }

        public Face cull(Direction direction) {
            return new Face(texture, uvs, direction);
        }
    }

    public static ModelBox cube(float x1, float y1, float z1, float x2, float y2, float z2, Function<Direction, Face> face) {
        Map<Direction, Face> faces = new LinkedHashMap<>();
        for (Direction direction : Direction.values()) {
            faces.put(direction, face.apply(direction));
        }
        return new ModelBox(x1, y1, z1, x2, y2, z2, faces);
    }

    public static ModelBox cube(float x1, float y1, float z1, float x2, float y2, float z2, String texture) {
        return cube(x1, y1, z1, x2, y2, z2, direction -> Face.of(texture));
    }
}
