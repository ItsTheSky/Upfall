package net.itsthesky.upfall.worldgen;

import com.mojang.datafixers.util.Pair;
import net.itsthesky.upfall.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ExplorationMapFunction;
import net.minecraft.world.level.storage.loot.functions.SetNameFunction;
import org.jetbrains.annotations.Nullable;

public final class ExplorerMaps {
    private ExplorerMaps() {}

    public static final byte ZOOM = 2;

    public record Destination(String name, TagKey<Structure> structures, Holder<MapDecorationType> decoration, int searchRadius) {
        public String translationKey() {
            return "filled_map.upfall." + name;
        }
    }

    public static final Destination PEARL_AERIE = new Destination("pearl_aerie", ModTags.Structures.ON_PEARL_AERIE_MAPS,
            MapDecorationTypes.WOODLAND_MANSION, 16);
    public static final Destination CHRONOSPIRE = new Destination("chronospire", ModTags.Structures.ON_CHRONOSPIRE_MAPS,
            MapDecorationTypes.TRIAL_CHAMBERS, 16);
    public static final Destination SHIFTING_VAULT = new Destination("shifting_vault", ModTags.Structures.ON_SHIFTING_VAULT_MAPS,
            MapDecorationTypes.OCEAN_MONUMENT, 24);
    public static final Destination STALLED_ENGINE = new Destination("stalled_engine", ModTags.Structures.ON_STALLED_ENGINE_MAPS,
            MapDecorationTypes.RED_X, 64);
    public static final Destination TILTED_RELIQUARY = new Destination("tilted_reliquary", ModTags.Structures.ON_TILTED_RELIQUARY_MAPS,
            MapDecorationTypes.TARGET_X, 8);

    public static final String GILDED_RIDGE = "filled_map.upfall.gilded_ridge";
    public static final Holder<MapDecorationType> GILDED_RIDGE_DECORATION = MapDecorationTypes.TARGET_POINT;
    public static final int BIOME_SEARCH_RADIUS = 3200;
    public static final int BIOME_SEARCH_STEP = 32;

    public static ItemStack create(ServerLevel level, BlockPos target, Holder<MapDecorationType> decoration, String translationKey) {
        ItemStack map = MapItem.create(level, target.getX(), target.getZ(), ZOOM, true, true);
        MapItem.renderBiomePreviewMap(level, map);
        MapItemSavedData.addTargetDecoration(map, target, "+", decoration);
        map.set(DataComponents.ITEM_NAME, Component.translatable(translationKey));
        return map;
    }

    public static @Nullable ItemStack toStructure(ServerLevel level, BlockPos origin, Destination destination) {
        BlockPos target = level.findNearestMapStructure(destination.structures(), origin, destination.searchRadius(), true);
        return target == null ? null : create(level, target, destination.decoration(), destination.translationKey());
    }

    public static @Nullable ItemStack toGildedRidge(ServerLevel level, BlockPos origin) {
        Pair<BlockPos, Holder<Biome>> found = level.findClosestBiome3d(biome -> biome.is(ModBiomes.GILDED_RIDGE), origin,
                BIOME_SEARCH_RADIUS, BIOME_SEARCH_STEP, UpfallTerrain.HEIGHT);
        return found == null ? null : create(level, found.getFirst(), GILDED_RIDGE_DECORATION, GILDED_RIDGE);
    }

    public static LootPoolEntryContainer.Builder<?> loot(Destination destination, int weight) {
        return LootItem.lootTableItem(Items.MAP).setWeight(weight)
                .apply(ExplorationMapFunction.makeExplorationMap().setDestination(destination.structures())
                        .setMapDecoration(destination.decoration()).setZoom(ZOOM).setSearchRadius(destination.searchRadius()))
                .apply(SetNameFunction.setName(Component.translatable(destination.translationKey()), SetNameFunction.Target.ITEM_NAME));
    }
}
