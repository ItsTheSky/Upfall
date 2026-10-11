package net.itsthesky.upfall.content.chronospire.item;

import net.itsthesky.upfall.content.chronospire.ChronoRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class HourglassTracker {
    private HourglassTracker() {}

    public record Footing(ResourceKey<Level> dimension, Vec3 pos, float yRot, float xRot, long time) {}

    public static final int SAMPLE_INTERVAL = 5;
    public static final int HISTORY_TICKS = 200;

    private static final Map<UUID, ArrayDeque<Footing>> HISTORY = new HashMap<>();

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % SAMPLE_INTERVAL != 0) return;
        if (!player.getInventory().contains(stack -> stack.is(ChronoRegistries.REWINDING_HOURGLASS.get()))) {
            HISTORY.remove(player.getUUID());
            return;
        }
        if (player.onGround() && !player.isPassenger()) {
            record(player.getUUID(), new Footing(player.level().dimension(), player.position(), player.getYRot(), player.getXRot(),
                    player.level().getGameTime()));
        }
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        HISTORY.remove(event.getEntity().getUUID());
    }

    public static void record(UUID player, Footing footing) {
        ArrayDeque<Footing> history = HISTORY.computeIfAbsent(player, k -> new ArrayDeque<>());
        history.addLast(footing);
        while (!history.isEmpty() && footing.time() - history.peekFirst().time() > HISTORY_TICKS) {
            history.removeFirst();
        }
    }

    public static @Nullable Footing rewind(UUID player, ResourceKey<Level> dimension, long now, int minAge) {
        ArrayDeque<Footing> history = HISTORY.get(player);
        if (history == null) return null;
        Footing best = null;
        for (Iterator<Footing> it = history.descendingIterator(); it.hasNext(); ) {
            Footing footing = it.next();
            if (!footing.dimension().equals(dimension) || now - footing.time() > HISTORY_TICKS) continue;
            best = footing;
            if (now - footing.time() >= minAge) break;
        }
        return best;
    }

    public static void clear(UUID player) {
        HISTORY.remove(player);
    }
}
