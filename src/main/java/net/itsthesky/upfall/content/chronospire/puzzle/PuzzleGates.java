package net.itsthesky.upfall.content.chronospire.puzzle;

import com.mojang.serialization.Codec;
import net.itsthesky.upfall.compat.mc.DataIn;
import net.itsthesky.upfall.compat.mc.DataOut;
import net.itsthesky.upfall.content.chronospire.block.ChronoSealBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public final class PuzzleGates {
    private static final BoundingBox DEFAULT = new BoundingBox(-6, -2, -6, 6, 8, 6);
    private static final Codec<List<int[]>> GATES = Codec.INT_STREAM.xmap(IntStream::toArray, Arrays::stream).listOf();

    private final List<BoundingBox> boxes = new ArrayList<>();

    public void add(BoundingBox relative) {
        boxes.add(relative);
    }

    public List<BoundingBox> boxes() {
        return boxes.isEmpty() ? List.of(DEFAULT) : boxes;
    }

    public int open(ServerLevel level, BlockPos origin) {
        int opened = 0;
        for (BoundingBox box : boxes()) {
            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(box.minX(), box.minY(), box.minZ()),
                    origin.offset(box.maxX(), box.maxY(), box.maxZ()))) {
                if (level.isLoaded(pos) && ChronoSealBlock.open(level, pos)) {
                    opened++;
                }
            }
        }
        return opened;
    }

    public void save(DataOut out) {
        List<int[]> list = new ArrayList<>();
        for (BoundingBox box : boxes) {
            list.add(new int[]{box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ()});
        }
        out.store("Gates", GATES, list);
    }

    public void load(DataIn in) {
        boxes.clear();
        for (int[] v : in.read("Gates", GATES).orElse(List.of())) {
            if (v.length == 6) {
                boxes.add(new BoundingBox(v[0], v[1], v[2], v[3], v[4], v[5]));
            }
        }
    }
}
