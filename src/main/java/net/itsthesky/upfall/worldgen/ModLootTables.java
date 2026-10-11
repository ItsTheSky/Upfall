package net.itsthesky.upfall.worldgen;

import net.itsthesky.upfall.Upfall;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public final class ModLootTables {
    private ModLootTables() {}

    public static final ResourceKey<LootTable> DRIFTING_RUINS = key("chests/drifting_ruins");
    public static final ResourceKey<LootTable> INVERTED_OBSERVATORY = key("chests/inverted_observatory");

    private static ResourceKey<LootTable> key(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, Upfall.id(path));
    }
}
