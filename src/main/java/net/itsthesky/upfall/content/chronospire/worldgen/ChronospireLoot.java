package net.itsthesky.upfall.content.chronospire.worldgen;

import net.itsthesky.upfall.Upfall;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public final class ChronospireLoot {
    private ChronospireLoot() {}

    public static final ResourceKey<LootTable> VAULT = ResourceKey.create(Registries.LOOT_TABLE, Upfall.id("chests/chronospire_vault"));
}
