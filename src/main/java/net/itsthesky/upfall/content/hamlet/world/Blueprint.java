package net.itsthesky.upfall.content.hamlet.world;

import net.itsthesky.upfall.compat.mc.McEntities;
import net.itsthesky.upfall.compat.mc.McLevel;
import net.itsthesky.upfall.content.hamlet.HamletBlocks;
import net.itsthesky.upfall.content.hamlet.HamletEntities;
import net.itsthesky.upfall.content.hamlet.block.RopeAnchorBlockEntity;
import net.itsthesky.upfall.content.hamlet.block.RopeBlock;
import net.itsthesky.upfall.content.hamlet.entity.Loftkin;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class Blueprint {
    private record Link(BlockPos anchor, BlockPos other) {}

    private record Cache(BlockPos pos, ResourceKey<LootTable> lootTable) {}

    private record Drop(BlockPos top, int maxLength) {}

    private final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
    private final List<Link> links = new ArrayList<>();
    private final List<Cache> caches = new ArrayList<>();
    private final List<Drop> drops = new ArrayList<>();
    private final List<BlockPos> inhabitants = new ArrayList<>();
    private final Map<BlockPos, Integer> variants = new HashMap<>();

    void set(BlockPos pos, BlockState state) {
        blocks.put(pos.immutable(), state);
    }

    void remove(BlockPos pos) {
        blocks.remove(pos);
    }

    BlockState get(BlockPos pos) {
        return blocks.get(pos);
    }

    boolean isFree(BlockPos pos) {
        return !blocks.containsKey(pos) || blocks.get(pos).isAir();
    }

    void link(BlockPos anchor, BlockPos other) {
        links.add(new Link(anchor.immutable(), other.immutable()));
    }

    void cache(BlockPos pos, ResourceKey<LootTable> lootTable) {
        caches.add(new Cache(pos.immutable(), lootTable));
    }

    void ropeDrop(BlockPos top, int maxLength) {
        drops.add(new Drop(top.immutable(), maxLength));
    }

    void inhabitant(BlockPos feet, int variant) {
        inhabitants.add(feet.immutable());
        variants.put(feet.immutable(), variant);
    }

    boolean isNearInhabitant(BlockPos pos) {
        for (BlockPos feet : inhabitants) {
            if (Math.abs(feet.getX() - pos.getX()) <= 1 && Math.abs(feet.getZ() - pos.getZ()) <= 1 && Math.abs(feet.getY() - pos.getY()) <= 1) {
                return true;
            }
        }
        return false;
    }

    Map<BlockPos, BlockState> blocks() {
        return blocks;
    }

    int cacheCount() {
        return caches.size();
    }

    int linkCount() {
        return links.size();
    }

    int inhabitantCount() {
        return inhabitants.size();
    }

    boolean isLineClear(Vec3 from, Vec3 to, BlockPos... ignored) {
        List<BlockPos> skip = List.of(ignored);
        int steps = (int) Math.ceil(from.distanceTo(to) * 3.0);
        for (int i = 1; i < steps; i++) {
            BlockPos pos = BlockPos.containing(from.lerp(to, (double) i / steps));
            if (!skip.contains(pos) && !isFree(pos)) {
                return false;
            }
        }
        return true;
    }

    BoundingBox bounds(int extraDown) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        List<BlockPos> corners = new ArrayList<>(blocks.keySet());
        for (Drop drop : drops) {
            corners.add(drop.top);
            corners.add(drop.top.below(drop.maxLength));
        }
        if (corners.isEmpty()) {
            return new BoundingBox(BlockPos.ZERO);
        }
        for (BlockPos pos : corners) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        return new BoundingBox(minX, minY - extraDown, minZ, maxX, maxY, maxZ);
    }

    void place(WorldGenLevel level, BoundingBox box, RandomSource random) {
        blocks.forEach((pos, state) -> {
            if (!box.isInside(pos)) return;
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
            if (state.getBlock() instanceof FenceBlock || state.getBlock() instanceof StairBlock) {
                level.getChunk(pos).markPosForPostprocessing(pos);
            }
        });
        for (Drop drop : drops) {
            if (box.isInside(drop.top)) {
                hangRope(level, drop);
            }
        }
        for (Link link : links) {
            if (box.isInside(link.anchor) && level.getBlockEntity(link.anchor) instanceof RopeAnchorBlockEntity anchor) {
                anchor.addLinkSilently(link.other);
            }
        }
        for (Cache cache : caches) {
            if (box.isInside(cache.pos)) {
                RandomizableContainer.setBlockEntityLootTable(level, random, cache.pos, cache.lootTable);
            }
        }
        for (BlockPos feet : inhabitants) {
            if (box.isInside(feet)) {
                spawnLoftkin(level, feet, variants.getOrDefault(feet, 0));
            }
        }
    }

    private static final int VOID_ROPE_LENGTH = 14;

    private static void hangRope(WorldGenLevel level, Drop drop) {
        int length = VOID_ROPE_LENGTH;
        BlockPos.MutableBlockPos pos = drop.top.mutable();
        for (int i = 0; i < drop.maxLength && pos.getY() > McLevel.minY(level); i++, pos.move(0, -1, 0)) {
            if (!level.getBlockState(pos).canBeReplaced()) {
                length = i;
                break;
            }
        }
        BlockState rope = HamletBlocks.ROPE.get().defaultBlockState();
        pos.set(drop.top);
        for (int i = 0; i < length; i++, pos.move(0, -1, 0)) {
            level.setBlock(pos, rope.setValue(RopeBlock.END, i == length - 1), Block.UPDATE_CLIENTS);
        }
    }

    private static void spawnLoftkin(WorldGenLevel level, BlockPos feet, int variant) {
        //? if >=26.1 {
        /*Loftkin loftkin = HamletEntities.LOFTKIN.get().create(level.getLevel(), MobSpawnType.STRUCTURE);
        *///?} else {
        Loftkin loftkin = HamletEntities.LOFTKIN.get().create(level.getLevel());
        //?}
        if (loftkin == null) return;
        McEntities.snapTo(loftkin, feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, level.getRandom().nextFloat() * 360.0F, 0.0F);
        loftkin.setHome(feet);
        EventHooks.finalizeMobSpawn(loftkin, level, level.getCurrentDifficultyAt(feet), MobSpawnType.STRUCTURE, null);
        loftkin.setVariant(variant);
        loftkin.setPersistenceRequired();
        if (!(level instanceof net.minecraft.server.level.ServerLevel)) loftkin.deferTradesUntilLoaded();
        level.addFreshEntityWithPassengers(loftkin);
    }
}
