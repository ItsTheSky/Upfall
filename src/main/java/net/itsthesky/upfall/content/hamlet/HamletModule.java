package net.itsthesky.upfall.content.hamlet;

import net.itsthesky.upfall.content.UpfallModule;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.registry.ModCreativeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public class HamletModule implements UpfallModule {
    @Override
    public void register(IEventBus modEventBus) {
        HamletSounds.SOUND_EVENTS.register(modEventBus);
        HamletBlocks.BLOCKS.register(modEventBus);
        HamletBlocks.BLOCK_ITEMS.register(modEventBus);
        HamletBlocks.BLOCK_ENTITIES.register(modEventBus);
        HamletItems.ITEMS.register(modEventBus);
        HamletItems.DATA_COMPONENTS.register(modEventBus);
        HamletEntities.ENTITY_TYPES.register(modEventBus);
        HamletStructures.STRUCTURE_TYPES.register(modEventBus);
        HamletStructures.PIECE_TYPES.register(modEventBus);
        modEventBus.addListener(HamletEntities::registerAttributes);
        modEventBus.addListener(HamletModule::commonSetup);

        ModCreativeTabs.EXTRA.addAll(HamletBlocks.CREATIVE_ORDER);
        ModCreativeTabs.EXTRA.add(HamletItems.ZIPLINE_HANDLE);
        ModCreativeTabs.EXTRA.add(HamletItems.LOFTKIN_SPAWN_EGG);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            FireBlock fire = (FireBlock) Blocks.FIRE;
            for (Block block : new Block[]{HamletBlocks.WOVEN_PLANKS.get(), HamletBlocks.WOVEN_STAIRS.get(), HamletBlocks.WOVEN_SLAB.get(),
                    HamletBlocks.ROPE_BRIDGE.get(), HamletBlocks.ROPE_SPOOL.get()}) {
                fire.setFlammable(block, 5, 20);
            }
            for (HamletBlocks.CanopySet canopy : HamletBlocks.CANOPIES) {
                for (Block block : new Block[]{canopy.block().get(), canopy.stairs().get(), canopy.slab().get()}) {
                    fire.setFlammable(block, 30, 60);
                }
            }
            fire.setFlammable(HamletBlocks.ROPE.get(), 15, 100);
        });
    }

    @Override
    public DataContribution data() {
        return new HamletData();
    }
}
