package net.itsthesky.upfall.content.music;

import net.itsthesky.upfall.compat.mc.McLevel;
import net.itsthesky.upfall.content.aerie.AerieStructures;
import net.itsthesky.upfall.content.chronospire.data.ChronospireData;
import net.itsthesky.upfall.content.engine.EngineStructures;
import net.itsthesky.upfall.content.grotto.GrottoStructures;
import net.itsthesky.upfall.content.hamlet.HamletStructures;
import net.itsthesky.upfall.content.quantum.QuantumStructures;
import net.itsthesky.upfall.content.reliquary.ReliquaryStructures;
import net.itsthesky.upfall.worldgen.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class StructureMusic {
    private StructureMusic() {}

    public static final int NONE = -1;
    private static final int CHECK_INTERVAL = 20;
    private static final int MIN_DELAY = 100;
    private static final int MAX_DELAY = 400;

    private record Entry(ResourceKey<Structure> structure, Music music) {
        Entry(ResourceKey<Structure> structure, Holder<SoundEvent> sound) {
            this(structure, new Music(sound, MIN_DELAY, MAX_DELAY, true));
        }
    }

    private static final List<Entry> ENTRIES = List.of(
            new Entry(ReliquaryStructures.TILTED_RELIQUARY, MusicSounds.TILTED_RELIQUARY),
            new Entry(ChronospireData.STRUCTURE, MusicSounds.CHRONOSPIRE),
            new Entry(GrottoStructures.CRYSTAL_GROTTO, MusicSounds.CRYSTAL_GROTTO),
            new Entry(AerieStructures.PEARL_AERIE, MusicSounds.PEARL_AERIE),
            new Entry(HamletStructures.CLOUD_HAMLET, MusicSounds.CLOUD_HAMLET),
            new Entry(QuantumStructures.SHIFTING_VAULT, MusicSounds.SHIFTING_VAULT),
            new Entry(EngineStructures.STALLED_ENGINE, MusicSounds.STALLED_ENGINE));

    private static final Map<UUID, Integer> SENT = new HashMap<>();

    static void register() {
        NeoForge.EVENT_BUS.addListener(StructureMusic::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(StructureMusic::onLogout);
    }

    public static @Nullable Music music(int index) {
        return index >= 0 && index < ENTRIES.size() ? ENTRIES.get(index).music() : null;
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % CHECK_INTERVAL != 0) return;
        int index = structureAt(player);
        Integer previous = SENT.put(player.getUUID(), index);
        if ((previous == null || previous != index) && player.connection != null) {
            StructureMusicPayload payload = new StructureMusicPayload(index);
            if (player.connection.hasChannel(payload)) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        }
    }

    private static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        SENT.remove(event.getEntity().getUUID());
    }

    private static int structureAt(ServerPlayer player) {
        if (!ModDimensions.isUpfall(player.level())) return NONE;
        StructureManager structures = McLevel.serverLevel(player).structureManager();
        BlockPos pos = player.blockPosition();
        for (int i = 0; i < ENTRIES.size(); i++) {
            ResourceKey<Structure> key = ENTRIES.get(i).structure();
            if (structures.getStructureWithPieceAt(pos, holder -> holder.is(key)).isValid()) return i;
        }
        return NONE;
    }
}
