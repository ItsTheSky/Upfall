package net.itsthesky.upfall.content.machines;

import net.itsthesky.upfall.datagen.ModLanguageProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class ClockworkData {
    private ClockworkData() {}

    static final String COG_MODEL = "hastening_gear_cog";

    static boolean owns(Block block) {
        return Set.of(MachinesBlocks.VOID_TETHER.get(), MachinesBlocks.REWINDER.get(), MachinesBlocks.HASTENING_GEAR.get()).contains(block);
    }

    static void lang(ModLanguageProvider lang) {
        lang.add("message.upfall.rewinder.fuel", "Kairite: %s%%");
        lang.add("message.upfall.hastening_gear.fuel", "Kairite: %s%%");
    }

    static List<ModelBox> rewinderBoxes() {
        return List.of(
                ModelBox.cube(2, 0, 2, 14, 3, 14, "side"),
                ModelBox.cube(5, 3, 5, 11, 11, 11, "side"),
                ModelBox.cube(3, 11, 3, 13, 14, 13, dir -> ModelBox.Face.of(dir == Direction.UP ? "top" : "side")));
    }

    static List<ModelBox> gearBodyBoxes() {
        List<ModelBox> boxes = new ArrayList<>();
        boxes.add(ModelBox.cube(0, 0, 0, 16, 2, 16, "plate"));
        for (int[] post : new int[][] {{1, 1}, {13, 1}, {1, 13}, {13, 13}}) {
            boxes.add(ModelBox.cube(post[0], 2, post[1], post[0] + 2, 6, post[1] + 2, dir -> ModelBox.Face.of("plate").uvs(0, 0, 2, 4)));
        }
        boxes.add(ModelBox.cube(7, 2, 7, 9, 6, 9, dir -> ModelBox.Face.of("plate").uvs(6, 6, 10, 10)));
        return boxes;
    }

    static List<ModelBox> gearItemBoxes() {
        List<ModelBox> boxes = new ArrayList<>(gearBodyBoxes());
        boxes.addAll(cogBoxes());
        return boxes;
    }

    static List<ModelBox> cogBoxes() {
        List<ModelBox> boxes = new ArrayList<>();
        for (int[] b : new int[][] {{1, 5, 15, 11}, {5, 1, 11, 15}, {3, 3, 13, 13}}) {
            boxes.add(ModelBox.cube(b[0], 3, b[1], b[2], 5, b[3], dir -> {
                ModelBox.Face face = ModelBox.Face.of("cog");
                return dir.getAxis() != Direction.Axis.Y ? face.uvs(6, 10, 10, 12) : face;
            }));
        }
        return boxes;
    }
}
