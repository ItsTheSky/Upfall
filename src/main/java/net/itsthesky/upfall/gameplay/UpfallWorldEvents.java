package net.itsthesky.upfall.gameplay;

import net.itsthesky.upfall.compat.mc.McLevel;
import net.itsthesky.upfall.compat.mc.McPlayers;
import net.itsthesky.upfall.registry.ModSounds;
import net.itsthesky.upfall.worldgen.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class UpfallWorldEvents {
    private UpfallWorldEvents() {}

    public static final int RESCUE_SLOW_FALLING_TICKS = 30 * 20;
    private static final int RESCUE_HEIGHT = 72;
    private static final int RESCUE_SPREAD = 256;

    public static void register() {
        NeoForge.EVENT_BUS.addListener(UpfallWorldEvents::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(UpfallWorldEvents::onCanSleep);
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator()) return;
        ServerLevel level = McLevel.serverLevel(player);
        if (level.dimension() == ModDimensions.UPFALL && player.getY() < McLevel.minY(level)) {
            rescue(player);
        }
    }

    public static void rescue(ServerPlayer player) {
        ServerLevel overworld = McLevel.server(player).overworld();
        RandomSource random = player.getRandom();
        int x = Mth.floor(player.getX()) + random.nextInt(RESCUE_SPREAD * 2 + 1) - RESCUE_SPREAD;
        int z = Mth.floor(player.getZ()) + random.nextInt(RESCUE_SPREAD * 2 + 1) - RESCUE_SPREAD;
        int ground = overworld.getChunk(x >> 4, z >> 4).getHeight(Heightmap.Types.MOTION_BLOCKING, x & 15, z & 15);
        int y = Math.min(McLevel.maxYExclusive(overworld) - 2, Math.max(ground, overworld.getSeaLevel()) + RESCUE_HEIGHT);

        player.resetFallDistance();
        DimensionTransition transition = new DimensionTransition(overworld, new Vec3(x + 0.5, y, z + 0.5), Vec3.ZERO,
                player.getYRot(), 30.0F, DimensionTransition.DO_NOTHING);
        //? if >=26.1 {
        /*player.teleport(transition);
        *///?} else {
        player.changeDimension(transition);
        //?}
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, RESCUE_SLOW_FALLING_TICKS, 0, false, true, true));
        overworld.playSound(null, BlockPos.containing(x, y, z), ModSounds.VOID_RESCUE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        McPlayers.overlay(player, Component.translatable("message.upfall.void_rescue"));
    }

    private static void onCanSleep(CanPlayerSleepEvent event) {
        //? if >=26.1 {
        /*Player.BedSleepingProblem problem = event.getProblem();
        if (event.getLevel().dimension() == ModDimensions.UPFALL && problem != null) {
            net.minecraft.world.attribute.BedRule rule = event.getLevel().environmentAttributes()
                    .getValue(net.minecraft.world.attribute.EnvironmentAttributes.BED_RULE, event.getPos());
            if (rule.canSetSpawn(event.getLevel()) && !rule.canSleep(event.getLevel()) && problem.equals(rule.asProblem())) {
                event.setProblem(null);
            }
        }
        *///?} else {
        if (event.getLevel().dimension() == ModDimensions.UPFALL && event.getProblem() == Player.BedSleepingProblem.NOT_POSSIBLE_NOW) {
            event.setProblem(null);
        }
        //?}
    }
}
