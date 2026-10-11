package net.itsthesky.upfall.content.hamlet;

import net.itsthesky.upfall.Upfall;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public final class HamletLoot {
    private HamletLoot() {}

    public static final ResourceKey<LootTable> HOME = ResourceKey.create(Registries.LOOT_TABLE, Upfall.id("chests/cloud_hamlet_home"));
    public static final ResourceKey<LootTable> WORKSHOP = ResourceKey.create(Registries.LOOT_TABLE, Upfall.id("chests/cloud_hamlet_workshop"));
}
