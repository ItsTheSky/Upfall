package net.itsthesky.upfall.content.mobs;

import net.itsthesky.upfall.content.UpfallModule;
import net.itsthesky.upfall.datagen.DataContribution;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

public class MobsModule implements UpfallModule {
    private static final DataContribution DATA = new MobsData();

    @Override
    public void register(IEventBus modEventBus) {
        MobEntities.ENTITY_TYPES.register(modEventBus);
        MobItems.ITEMS.register(modEventBus);
        MobSounds.SOUND_EVENTS.register(modEventBus);
        modEventBus.addListener(MobEntities::registerAttributes);
        modEventBus.addListener(MobEntities::registerSpawnPlacements);
        NeoForge.EVENT_BUS.addListener(MobsModule::registerBrewing);
    }

    private static void registerBrewing(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addMix(Potions.AWKWARD, MobItems.DUSKWING_PLUME.get(), Potions.SLOW_FALLING);
        event.getBuilder().addMix(Potions.AWKWARD, MobItems.CRAGMAW_FANG.get(), Potions.STRENGTH);
        event.getBuilder().addMix(Potions.AWKWARD, MobItems.MIST_GEL.get(), Potions.INVISIBILITY);
    }

    @Override
    public DataContribution data() {
        return DATA;
    }
}
