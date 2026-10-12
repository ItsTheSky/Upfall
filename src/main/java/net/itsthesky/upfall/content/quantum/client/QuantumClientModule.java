package net.itsthesky.upfall.content.quantum.client;

import net.itsthesky.upfall.client.UpfallParticle;
import net.itsthesky.upfall.content.UpfallClientModule;
import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.itsthesky.upfall.content.quantum.QuantumParticles;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.common.NeoForge;

public class QuantumClientModule implements UpfallClientModule {
    @Override
    public void register(IEventBus modEventBus) {
        modEventBus.addListener(QuantumClientModule::registerParticles);
        modEventBus.addListener(QuantumClientModule::modifyModels);
        NeoForge.EVENT_BUS.addListener(VeilVision::onClientTick);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, true, VeilVision::onRenderFog);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, VeilVision::onComputeFogColor);
        QuantumClientExtras.register(modEventBus);
    }

    private static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(QuantumParticles.VEIL_MOTE.get(), sprites -> new UpfallParticle.Provider(sprites, UpfallParticle.Kind.VEIL_MOTE));
        event.registerSpriteSet(QuantumParticles.QUANTUM_SPARK.get(), sprites -> new UpfallParticle.Provider(sprites, UpfallParticle.Kind.QUANTUM_SPARK));
    }

    private static void modifyModels(ModelEvent.ModifyBakingResult event) {
        for (var state : QuantumBlocks.VEIL_GAS.get().getStateDefinition().getPossibleStates()) {
            ModelResourceLocation location = BlockModelShaper.stateToModelLocation(state);
            BakedModel model = event.getModels().get(location);
            if (model != null) {
                event.getModels().put(location, new VeilGasModel(model));
            }
        }
    }
}
