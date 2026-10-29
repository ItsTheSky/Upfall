package net.itsthesky.upfall.content.machines.client;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.UpfallClientModule;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import net.neoforged.neoforge.common.NeoForge;

public class MachinesClientModule implements UpfallClientModule {
    @Override
    public void register(IEventBus modEventBus) {
        modEventBus.addListener((RegisterRangeSelectItemModelPropertyEvent event) -> event.register(Upfall.id("phase"), PhaseProperty.MAP_CODEC));
        NeoForge.EVENT_BUS.addListener(GearliftClientInput::onClientTick);
    }

    private record PhaseProperty() implements RangeSelectItemModelProperty {
        static final MapCodec<PhaseProperty> MAP_CODEC = MapCodec.unit(new PhaseProperty());

        @Override
        public float get(ItemStack stack, ClientLevel level, ItemOwner owner, int seed) {
            return PhaseWatchProperty.value(level);
        }

        @Override
        public MapCodec<PhaseProperty> type() {
            return MAP_CODEC;
        }
    }
}
