package net.itsthesky.upfall.content.hamlet;

import net.itsthesky.upfall.Upfall;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.advancements.critereon.StartRidingTrigger;
import net.minecraft.advancements.critereon.TradeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

final class HamletAdvancements implements AdvancementSubProvider {
    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver) {
        AdvancementHolder enter = AdvancementSubProvider.createPlaceholder(Upfall.id("upfall/enter").toString());
        AdvancementHolder hamlet = advancement(saver, enter, "cloud_hamlet", HamletBlocks.DUSK_CANOPY.block().get(), AdvancementType.TASK, "visited",
                PlayerTrigger.TriggerInstance.located(LocationPredicate.Builder.inStructure(
                        registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(HamletStructures.CLOUD_HAMLET))));
        advancement(saver, hamlet, "zipline", HamletItems.ZIPLINE_HANDLE.get(), AdvancementType.TASK, "rode_zipline",
                StartRidingTrigger.TriggerInstance.playerStartsRiding(EntityPredicate.Builder.entity()
                        .vehicle(entityOf(registries, HamletEntities.ZIPLINE_PULLEY.get()))));
        advancement(saver, hamlet, "loftkin_trade", HamletBlocks.ROPE_ITEM.get(), AdvancementType.TASK, "traded",
                TradeTrigger.TriggerInstance.tradedWithVillager(entityOf(registries, HamletEntities.LOFTKIN.get())));
    }

    private static EntityPredicate.Builder entityOf(HolderLookup.Provider registries, EntityType<?> type) {
        //? if >=26.1 {
        /*return EntityPredicate.Builder.entity().of(registries.lookupOrThrow(Registries.ENTITY_TYPE), type);
        *///?} else {
        return EntityPredicate.Builder.entity().of(type);
        //?}
    }

    private static AdvancementHolder advancement(Consumer<AdvancementHolder> saver, AdvancementHolder parent, String name, ItemLike icon,
                                                 AdvancementType type, String criterion, Criterion<?> trigger) {
        return Advancement.Builder.advancement().parent(parent)
                .display(icon, Component.translatable("advancements.upfall." + name + ".title"),
                        Component.translatable("advancements.upfall." + name + ".description"), null, type, true, true, false)
                .addCriterion(criterion, trigger)
                .save(saver, Upfall.id("upfall/" + name).toString());
    }
}
