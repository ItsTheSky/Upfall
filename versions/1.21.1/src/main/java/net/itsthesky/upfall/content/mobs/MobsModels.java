package net.itsthesky.upfall.content.mobs;

import net.itsthesky.upfall.datagen.ModItemModelProvider;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;

final class MobsModels {
    private MobsModels() {}

    static void spawnEggs(ModItemModelProvider models) {
        for (DeferredHolder<Item, ? extends Item> egg : MobsData.SPAWN_EGGS) {
            models.withExistingParent(egg.getId().getPath(), models.mcLoc("item/template_spawn_egg"));
        }
    }
}
