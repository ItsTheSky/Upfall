package net.itsthesky.upfall.compat.mc;

import net.itsthesky.upfall.Upfall;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = Upfall.MODID)
public final class DaylightCycleSync {
    private static final int PERIOD_TICKS = 20;

    private static volatile boolean clientValue = true;
    private static boolean lastBroadcast = true;
    private static int ticks;

    private DaylightCycleSync() {}

    public static boolean clientValue() {
        return clientValue;
    }

    public record Payload(boolean enabled) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<Payload> TYPE = new CustomPacketPayload.Type<>(Upfall.id("daylight_cycle"));
        public static final StreamCodec<io.netty.buffer.ByteBuf, Payload> STREAM_CODEC = ByteBufCodecs.BOOL.map(Payload::new, Payload::enabled);

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional().playToClient(Payload.TYPE, Payload.STREAM_CODEC, (payload, context) -> clientValue = payload.enabled());
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            send(player, new Payload(player.level().getGameRules().get(GameRules.ADVANCE_TIME)));
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (++ticks < PERIOD_TICKS) {
            return;
        }
        ticks = 0;
        boolean value = event.getServer().getGameRules().get(GameRules.ADVANCE_TIME);
        if (value != lastBroadcast) {
            lastBroadcast = value;
            Payload payload = new Payload(value);
            for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                send(player, payload);
            }
        }
    }

    private static void send(ServerPlayer player, Payload payload) {
        if (player.connection != null && player.connection.hasChannel(payload)) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }
}
