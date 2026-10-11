package net.itsthesky.upfall.gameplay;

import net.itsthesky.upfall.registry.ModEffects;
import net.itsthesky.upfall.worldgen.ModBiomes;
import net.itsthesky.upfall.worldgen.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class WeightlessZones {
    private WeightlessZones() {}

    public static final int CHECK_INTERVAL = 10;
    public static final int CEILING_SEARCH = 28;
    private static final int EFFECT_TICKS = 50;

    public static void register() {
        NeoForge.EVENT_BUS.addListener(WeightlessZones::onPlayerTick);
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % CHECK_INTERVAL != 0 || player.isSpectator()) return;
        ServerLevel level = player.serverLevel();
        if (level.dimension() != ModDimensions.UPFALL) return;
        if (isInZone(level, player.blockPosition())) {
            player.addEffect(new MobEffectInstance(ModEffects.WEIGHTLESS, EFFECT_TICKS, 0, true, false, true));
        }
    }

    public static boolean isInZone(ServerLevel level, BlockPos pos) {
        if (!level.getBiome(pos).is(ModBiomes.HANGING_ROOTS)) return false;
        BlockPos.MutableBlockPos cursor = pos.mutable();
        for (int i = 2; i <= CEILING_SEARCH; i++) {
            cursor.setY(pos.getY() + i);
            if (level.getBlockState(cursor).isSolid()) return true;
        }
        return false;
    }
}
