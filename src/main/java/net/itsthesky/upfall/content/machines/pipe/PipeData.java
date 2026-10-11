package net.itsthesky.upfall.content.machines.pipe;

import net.itsthesky.upfall.content.machines.MachinesBlocks;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.itsthesky.upfall.datagen.ModLanguageProvider;
import net.itsthesky.upfall.datagen.ModSoundProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import org.joml.Vector3f;

import java.util.EnumSet;
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

    public static void blockStates(ModBlockStateProvider states) {
        ResourceLocation glass = states.blockTexture("pneumatic_pipe_glass");
        ResourceLocation band = states.blockTexture("pneumatic_pipe_band");
        Block pipe = MachinesBlocks.PNEUMATIC_PIPE.get();
        MultiPartBlockStateBuilder multipart = states.getMultipartBuilder(pipe);
        for (Direction dir : Direction.values()) {
            ModelBuilder<?> arm = pipeModel(states, "pneumatic_pipe_arm_" + dir.getName(), glass, band);
            arm(arm, dir);
            ModelBuilder<?> cap = pipeModel(states, "pneumatic_pipe_cap_" + dir.getName(), glass, band);
            cap(cap, dir);
            multipart.part().modelFile(arm).addModel().condition(PipeBlock.PROPERTY_BY_DIRECTION.get(dir), true).end();
            multipart.part().modelFile(cap).addModel().condition(PipeBlock.PROPERTY_BY_DIRECTION.get(dir), false).end();
        }
        ModelBuilder<?> item = states.itemModels().withExistingParent(ModBlockStateProvider.name(pipe), states.mcLoc("block/block"))
                .texture("glass", glass).texture("band", band).texture("particle", band).renderType("translucent");
        arm(item, Direction.NORTH);
        arm(item, Direction.SOUTH);
        for (Direction dir : new Direction[]{Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN}) {
            cap(item, dir);
        }

        ResourceLocation casing = states.blockTexture("pneumatic_pipe_casing");
        terminal(states, MachinesBlocks.PNEUMATIC_PIPE_ENTRANCE.get(), casing, states.blockTexture("pneumatic_pipe_entrance_front"));
        terminal(states, MachinesBlocks.PNEUMATIC_PIPE_EXIT.get(), casing, states.blockTexture("pneumatic_pipe_exit_front"));
    }

    private static void terminal(ModBlockStateProvider states, Block block, ResourceLocation casing, ResourceLocation front) {
        ModelFile model = states.models().cube(ModBlockStateProvider.name(block), casing, front, casing, casing, casing, casing)
                .texture("particle", casing);
        states.directionalBlock(block, model);
        states.blockItem(block);
    }

    private static ModelBuilder<?> pipeModel(ModBlockStateProvider states, String name, ResourceLocation glass, ResourceLocation band) {
        return states.models().withExistingParent(name, states.mcLoc("block/block"))
                .texture("glass", glass).texture("band", band).texture("particle", band).renderType("translucent");
    }

    private static void arm(ModelBuilder<?> model, Direction dir) {
        box(model, dir, 2, 2, 1, 2, 14, 2, "#glass", EnumSet.of(Direction.WEST, Direction.EAST));
        box(model, dir, 14, 2, 1, 14, 14, 2, "#glass", EnumSet.of(Direction.EAST, Direction.WEST));
        box(model, dir, 2, 2, 1, 14, 2, 2, "#glass", EnumSet.of(Direction.DOWN, Direction.UP));
        box(model, dir, 2, 14, 1, 14, 14, 2, "#glass", EnumSet.of(Direction.UP, Direction.DOWN));
        box(model, dir, 1, 14, 0, 15, 15, 1, "#band", ALL_FACES);
        box(model, dir, 1, 1, 0, 15, 2, 1, "#band", ALL_FACES);
        box(model, dir, 1, 2, 0, 2, 14, 1, "#band", ALL_FACES);
        box(model, dir, 14, 2, 0, 15, 14, 1, "#band", ALL_FACES);
    }

    private static void cap(ModelBuilder<?> model, Direction dir) {
        box(model, dir, 2, 2, 2, 14, 14, 2, "#glass", EnumSet.of(Direction.NORTH, Direction.SOUTH));
    }

    private static void box(ModelBuilder<?> model, Direction dir, float x1, float y1, float z1, float x2, float y2, float z2,
                            String texture, Set<Direction> localFaces) {
        Vector3f a = map(dir, x1, y1, z1);
        Vector3f b = map(dir, x2, y2, z2);
        var element = model.element()
                .from(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.min(a.z, b.z))
                .to(Math.max(a.x, b.x), Math.max(a.y, b.y), Math.max(a.z, b.z));
        for (Direction local : localFaces) {
            element.face(mapDirection(dir, local)).texture(texture).end();
        }
        element.end();
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
        Vec3i normal = local.getNormal();
        Vector3f origin = map(dir, 0, 0, 0);
        Vector3f moved = map(dir, normal.getX(), normal.getY(), normal.getZ());
        return Direction.getNearest(moved.x - origin.x, moved.y - origin.y, moved.z - origin.z);
    }
}
