package net.itsthesky.upfall.content.machines.pipe;

import net.itsthesky.upfall.content.machines.MachinesBlocks;
import net.itsthesky.upfall.content.machines.ModelBox;
import net.itsthesky.upfall.datagen.ModLanguageProvider;
import net.itsthesky.upfall.datagen.ModSoundProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PipeData {
    private PipeData() {}

    private static final Set<Direction> ALL_FACES = EnumSet.allOf(Direction.class);

    public static boolean owns(Block block) {
        return block == MachinesBlocks.PNEUMATIC_PIPE.get() || block == MachinesBlocks.PNEUMATIC_PIPE_ENTRANCE.get()
                || block == MachinesBlocks.PNEUMATIC_PIPE_EXIT.get();
    }

    public static void lang(ModLanguageProvider lang) {
        PneumaticPipes.SUBTITLES.forEach((path, text) -> lang.add(ModLanguageProvider.subtitle(path), text));
    }

    public static void sounds(ModSoundProvider sounds) {
        sounds.addSound(PneumaticPipes.ENTER, ModSoundProvider.def(PneumaticPipes.ENTER)
                .with(ModSoundProvider.event("entity.breeze.inhale").pitch(1.4).volume(0.9)));
        sounds.addSound(PneumaticPipes.TRAVEL, ModSoundProvider.def(PneumaticPipes.TRAVEL)
                .with(ModSoundProvider.event("item.elytra.flying").pitch(1.5).volume(0.6)));
        sounds.addSound(PneumaticPipes.BEND, ModSoundProvider.def(PneumaticPipes.BEND)
                .with(ModSoundProvider.event("block.copper_bulb.turn_on").pitch(0.7).volume(0.6),
                        ModSoundProvider.event("block.copper_grate.step").pitch(0.8).volume(0.8)));
        sounds.addSound(PneumaticPipes.EXIT, ModSoundProvider.def(PneumaticPipes.EXIT)
                .with(ModSoundProvider.event("entity.wind_charge.wind_burst").pitch(1.3).volume(0.7)));
        sounds.addSound(PneumaticPipes.POP, ModSoundProvider.def(PneumaticPipes.POP)
                .with(ModSoundProvider.event("block.note_block.bit").pitch(1.6).volume(0.5)));
        sounds.addSound(PneumaticPipes.ITEM, ModSoundProvider.def(PneumaticPipes.ITEM)
                .with(ModSoundProvider.event("entity.breeze.inhale").pitch(2.0).volume(0.4)));
        sounds.addSound(PneumaticPipes.JAMMED, ModSoundProvider.def(PneumaticPipes.JAMMED)
                .with(ModSoundProvider.event("block.iron_trapdoor.close").pitch(0.6).volume(0.8)));
    }

    static List<ModelBox> armBoxes(Direction dir) {
        return List.of(
                box(dir, 2, 2, 1, 2, 14, 2, "glass", EnumSet.of(Direction.WEST, Direction.EAST)),
                box(dir, 14, 2, 1, 14, 14, 2, "glass", EnumSet.of(Direction.EAST, Direction.WEST)),
                box(dir, 2, 2, 1, 14, 2, 2, "glass", EnumSet.of(Direction.DOWN, Direction.UP)),
                box(dir, 2, 14, 1, 14, 14, 2, "glass", EnumSet.of(Direction.UP, Direction.DOWN)),
                box(dir, 1, 14, 0, 15, 15, 1, "band", ALL_FACES),
                box(dir, 1, 1, 0, 15, 2, 1, "band", ALL_FACES),
                box(dir, 1, 2, 0, 2, 14, 1, "band", ALL_FACES),
                box(dir, 14, 2, 0, 15, 14, 1, "band", ALL_FACES));
    }

    static List<ModelBox> capBoxes(Direction dir) {
        return List.of(box(dir, 2, 2, 2, 14, 14, 2, "glass", EnumSet.of(Direction.NORTH, Direction.SOUTH)));
    }

    static List<ModelBox> itemBoxes() {
        List<ModelBox> boxes = new ArrayList<>(armBoxes(Direction.NORTH));
        boxes.addAll(armBoxes(Direction.SOUTH));
        for (Direction dir : new Direction[]{Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN}) {
            boxes.addAll(capBoxes(dir));
        }
        return boxes;
    }

    private static ModelBox box(Direction dir, float x1, float y1, float z1, float x2, float y2, float z2,
                                String texture, Set<Direction> localFaces) {
        Vector3f a = map(dir, x1, y1, z1);
        Vector3f b = map(dir, x2, y2, z2);
        Map<Direction, ModelBox.Face> faces = new LinkedHashMap<>();
        for (Direction local : localFaces) {
            faces.put(mapDirection(dir, local), ModelBox.Face.of(texture));
        }
        return new ModelBox(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.min(a.z, b.z),
                Math.max(a.x, b.x), Math.max(a.y, b.y), Math.max(a.z, b.z), faces);
    }

    private static Vector3f map(Direction dir, float x, float y, float z) {
        return switch (dir) {
            case NORTH -> new Vector3f(x, y, z);
            case SOUTH -> new Vector3f(16 - x, y, 16 - z);
            case EAST -> new Vector3f(16 - z, y, x);
            case WEST -> new Vector3f(z, y, 16 - x);
            case UP -> new Vector3f(x, 16 - z, y);
            case DOWN -> new Vector3f(x, z, 16 - y);
        };
    }

    private static Direction mapDirection(Direction dir, Direction local) {
        Vector3f origin = map(dir, 0, 0, 0);
        Vector3f moved = map(dir, local.getStepX(), local.getStepY(), local.getStepZ());
        int dx = Math.round(moved.x - origin.x);
        int dy = Math.round(moved.y - origin.y);
        int dz = Math.round(moved.z - origin.z);
        for (Direction candidate : Direction.values()) {
            if (candidate.getStepX() == dx && candidate.getStepY() == dy && candidate.getStepZ() == dz) {
                return candidate;
            }
        }
        throw new IllegalStateException("No direction for " + dx + "," + dy + "," + dz);
    }
}
