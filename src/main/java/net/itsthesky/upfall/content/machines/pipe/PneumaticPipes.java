package net.itsthesky.upfall.content.machines.pipe;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.machines.MachinesBlocks;
import net.itsthesky.upfall.content.machines.pipe.client.PipeTravelClient;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PneumaticPipes {
    private PneumaticPipes() {}

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Upfall.MODID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Upfall.MODID);
    public static final Map<String, String> SUBTITLES = new LinkedHashMap<>();

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PneumaticPipeEntranceBlockEntity>> ENTRANCE = BLOCK_ENTITIES.register(
            "pneumatic_pipe_entrance", () -> BlockEntityType.Builder.of(PneumaticPipeEntranceBlockEntity::new,
                    MachinesBlocks.PNEUMATIC_PIPE_ENTRANCE.get()).build(null));

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTER = sound("block.pneumatic_pipe.enter", "Pneumatic pipe inhales");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRAVEL = sound("block.pneumatic_pipe.travel", "Air rushes through a pipe");
    public static final DeferredHolder<SoundEvent, SoundEvent> BEND = sound("block.pneumatic_pipe.bend", "Pipe rattles");
    public static final DeferredHolder<SoundEvent, SoundEvent> EXIT = sound("block.pneumatic_pipe.exit", "Pneumatic pipe blows");
    public static final DeferredHolder<SoundEvent, SoundEvent> POP = sound("block.pneumatic_pipe.pop", "Pipe pops");
    public static final DeferredHolder<SoundEvent, SoundEvent> ITEM = sound("block.pneumatic_pipe.item", "Pipe swallows an item");
    public static final DeferredHolder<SoundEvent, SoundEvent> JAMMED = sound("block.pneumatic_pipe.jammed", "Pipe clunks");

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String path, String subtitle) {
        SUBTITLES.put(path, subtitle);
        return SOUND_EVENTS.register(path, () -> SoundEvent.createVariableRangeEvent(Upfall.id(path)));
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);
        modEventBus.addListener(PneumaticPipes::registerPayloads);
        modEventBus.addListener(PneumaticPipes::registerCapabilities);
        PipeTravel.register();
        if (FMLEnvironment.dist.isClient()) {
            PipeTravelClient.register();
        }
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToClient(PipeTravelStartPayload.TYPE, PipeTravelStartPayload.STREAM_CODEC, PipeTravelStartPayload::handle)
                .playToClient(PipeTravelEndPayload.TYPE, PipeTravelEndPayload.STREAM_CODEC, PipeTravelEndPayload::handle);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ENTRANCE.get(), (entrance, side) -> entrance.inlet());
    }
}
