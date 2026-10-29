package net.itsthesky.upfall.content.quantum;

import net.itsthesky.upfall.content.UpfallModule;
import net.itsthesky.upfall.content.quantum.cluster.FlickerShardClusterBlockEntity;
import net.itsthesky.upfall.content.quantum.gas.BottledVeilGasItem;
import net.itsthesky.upfall.content.quantum.pedestal.QuantumPedestalBlock;
import net.itsthesky.upfall.content.quantum.upgrade.QuantumUpgradeEffects;
import net.itsthesky.upfall.content.quantum.upgrade.ZiplineLatchPayload;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.registry.ModCreativeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

public class QuantumModule implements UpfallModule {
    @Override
    public void register(IEventBus modEventBus) {
        QuantumSounds.SOUND_EVENTS.register(modEventBus);
        QuantumComponents.COMPONENTS.register(modEventBus);
        QuantumTriggers.TRIGGERS.register(modEventBus);
        QuantumParticles.PARTICLE_TYPES.register(modEventBus);
        //? if <26.1 {
        QuantumItems.ARMOR_MATERIALS.register(modEventBus);
        //?}
        QuantumBlocks.BLOCKS.register(modEventBus);
        QuantumBlocks.BLOCK_ITEMS.register(modEventBus);
        QuantumBlocks.BLOCK_ENTITIES.register(modEventBus);
        QuantumItems.ITEMS.register(modEventBus);
        QuantumRecipeTypes.TYPES.register(modEventBus);
        QuantumRecipeTypes.SERIALIZERS.register(modEventBus);
        QuantumWorldgen.CARVERS.register(modEventBus);
        QuantumWorldgen.FEATURES.register(modEventBus);
        QuantumStructures.TYPES.register(modEventBus);
        QuantumStructures.PIECES.register(modEventBus);
        QuantumStructures.PLACEMENTS.register(modEventBus);
        ModCreativeTabs.EXTRA.addAll(QuantumItems.CREATIVE_ORDER);
        ModCreativeTabs.EXTRA.addAll(QuantumBlocks.CREATIVE_ORDER);

        NeoForge.EVENT_BUS.addListener(BottledVeilGasItem::onUseItem);
        NeoForge.EVENT_BUS.addListener(FlickerShardClusterBlockEntity::onLeftClick);
        NeoForge.EVENT_BUS.addListener(QuantumPedestalBlock::onRightClickBlock);
        QuantumUpgradeEffects.register();
        modEventBus.addListener(ZiplineLatchPayload::register);
    }

    @Override
    public DataContribution data() {
        return new QuantumData();
    }
}
