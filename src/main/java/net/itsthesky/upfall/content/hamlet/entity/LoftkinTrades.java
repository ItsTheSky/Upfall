package net.itsthesky.upfall.content.hamlet.entity;

import net.itsthesky.upfall.content.hamlet.HamletBlocks;
import net.itsthesky.upfall.content.hamlet.HamletItems;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModItems;
import net.itsthesky.upfall.worldgen.ExplorerMaps;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public final class LoftkinTrades {
    private LoftkinTrades() {}

    private static final int POOL_PICKS = 5;
    private static final int MAX_MAP_PICKS = 2;

    @FunctionalInterface
    private interface Listing {
        @Nullable
        MerchantOffer getOffer(Entity trader, RandomSource random);
    }

    private static List<Listing> staples() {
        return List.of(
                offer(ModItems.RAW_NIMBUSITE.get(), 2, null, 0, HamletItems.ZIPLINE_HANDLE.get(), 1, 4, 10),
                offer(ModItems.CLOUDBERRY.get(), 4, null, 0, HamletBlocks.ROPE.get(), 6, 16, 2),
                offer(ModItems.NIMBUSITE_INGOT.get(), 10, ModItems.GILDFRUIT.get(), 4, ModItems.NIMBUSITE_UPGRADE_SMITHING_TEMPLATE.get(), 1, 1, 30));
    }

    private static List<Listing> pool() {
        List<Listing> pool = new ArrayList<>(List.of(
                offer(ModItems.NIMBUSITE_INGOT.get(), 1, null, 0, HamletBlocks.ZIPLINE_ANCHOR.get(), 2, 8, 5),
                offer(ModItems.CLOUDBERRY.get(), 6, null, 0, HamletBlocks.ROPE_ANCHOR.get(), 3, 12, 2),
                offer(ModItems.GILDFRUIT.get(), 1, null, 0, HamletBlocks.CLOUD_LANTERN.get(), 4, 12, 3),
                offer(ModItems.CLOUDBERRY.get(), 3, null, 0, HamletBlocks.WOVEN_PLANKS.get(), 8, 16, 2),
                offer(ModItems.GILDFRUIT.get(), 2, null, 0, HamletBlocks.ROPE_BRIDGE.get(), 8, 12, 3),
                offer(ModItems.RAW_NIMBUSITE.get(), 3, null, 0, ModItems.CLOUDBERRY_TART.get(), 2, 12, 3),
                offer(ModBlocks.DENSE_CLOUD.get(), 8, null, 0, ModBlocks.MISTGLOW_CRYSTAL.get(), 2, 12, 3),
                offer(ModItems.LEVITITE_SHARD.get(), 1, null, 0, ModItems.NIMBUSITE_INGOT.get(), 3, 6, 15),
                offer(ModItems.CLOUDBERRY.get(), 8, null, 0, ModItems.GILDFRUIT.get(), 1, 12, 2),
                offer(ModItems.NIMBUSITE_INGOT.get(), 2, ModBlocks.MISTGLOW_CRYSTAL.get(), 2, ModItems.UPDRAFT_COMPASS.get(), 1, 3, 15),
                offer(ModItems.CLOUDBERRY.get(), 5, null, 0, HamletBlocks.ROPE_SPOOL.get(), 2, 12, 2)));
        for (HamletBlocks.CanopySet canopy : HamletBlocks.CANOPIES) {
            pool.add(offer(ModItems.CLOUDBERRY.get(), 4, null, 0, canopy.block().get(), 6, 12, 2));
        }
        return pool;
    }

    private static List<Listing> maps() {
        return List.of(
                map((level, pos) -> ExplorerMaps.toStructure(level, pos, ExplorerMaps.STALLED_ENGINE), ModItems.GILDFRUIT.get(), 3, 2, 5),
                map((level, pos) -> ExplorerMaps.toStructure(level, pos, ExplorerMaps.PEARL_AERIE), ModItems.NIMBUSITE_INGOT.get(), 3, 1, 10),
                map((level, pos) -> ExplorerMaps.toStructure(level, pos, ExplorerMaps.CHRONOSPIRE), ModItems.NIMBUSITE_INGOT.get(), 3, 1, 10),
                map((level, pos) -> ExplorerMaps.toStructure(level, pos, ExplorerMaps.SHIFTING_VAULT), ModItems.RAW_NIMBUSITE.get(), 6, 1, 10),
                map(ExplorerMaps::toGildedRidge, ModItems.GILDFRUIT.get(), 6, 1, 10),
                map((level, pos) -> ExplorerMaps.toStructure(level, pos, ExplorerMaps.TILTED_RELIQUARY), ModItems.LEVITITE_SHARD.get(), 3, 1, 20));
    }

    static void fill(Loftkin loftkin, MerchantOffers offers, RandomSource random) {
        for (Listing listing : staples()) {
            MerchantOffer offer = listing.getOffer(loftkin, random);
            if (offer != null) {
                offers.add(offer);
            }
        }
        List<Listing> pool = new ArrayList<>(pool());
        for (int i = 0; i < POOL_PICKS && !pool.isEmpty(); i++) {
            MerchantOffer offer = pool.remove(random.nextInt(pool.size())).getOffer(loftkin, random);
            if (offer != null) {
                offers.add(offer);
            }
        }
        List<Listing> maps = new ArrayList<>(maps());
        int mapPicks = 1 + random.nextInt(MAX_MAP_PICKS);
        for (int i = 0; i < mapPicks && !maps.isEmpty(); i++) {
            MerchantOffer offer = maps.remove(random.nextInt(maps.size())).getOffer(loftkin, random);
            if (offer != null) {
                offers.add(offer);
            }
        }
    }

    private static Listing offer(ItemLike cost, int count, ItemLike extraCost, int extraCount,
                                 ItemLike result, int resultCount, int maxUses, int xp) {
        return (trader, random) -> new MerchantOffer(new ItemCost(cost, count),
                extraCost == null ? Optional.empty() : Optional.of(new ItemCost(extraCost, extraCount)),
                new ItemStack(result, resultCount), maxUses, xp, 0.05F);
    }

    private static Listing map(BiFunction<ServerLevel, BlockPos, ItemStack> map, ItemLike cost, int count, int maxUses, int xp) {
        return (trader, random) -> {
            if (!(trader.level() instanceof ServerLevel level)) {
                return null;
            }
            ItemStack stack = map.apply(level, trader.blockPosition());
            return stack == null ? null : new MerchantOffer(new ItemCost(cost, count), Optional.of(new ItemCost(Items.COMPASS)), stack, maxUses, xp, 0.05F);
        };
    }
}
