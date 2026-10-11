package net.itsthesky.upfall.content.chronospire.puzzle;

import net.itsthesky.upfall.content.chronospire.block.ChronoSealBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.List;

public final class PuzzleGates {
    private static final BoundingBox DEFAULT = new BoundingBox(-6, -2, -6, 6, 8, 6);

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

    public void save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (BoundingBox box : boxes) {
            list.add(new IntArrayTag(new int[]{box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ()}));
        }
        tag.put("Gates", list);
    }

    public void load(CompoundTag tag) {
        boxes.clear();
        for (Tag entry : tag.getList("Gates", Tag.TAG_INT_ARRAY)) {
            int[] v = ((IntArrayTag) entry).getAsIntArray();
            if (v.length == 6) {
                boxes.add(new BoundingBox(v[0], v[1], v[2], v[3], v[4], v[5]));
            }
        }
    }
}
