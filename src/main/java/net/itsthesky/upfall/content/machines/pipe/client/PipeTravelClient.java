package net.itsthesky.upfall.content.machines.pipe.client;

import net.itsthesky.upfall.content.machines.pipe.PipePath;
import net.itsthesky.upfall.content.machines.pipe.PipeTravelEndPayload;
import net.itsthesky.upfall.content.machines.pipe.PipeTravelStartPayload;
import net.itsthesky.upfall.content.machines.pipe.PneumaticPipes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class PipeTravelClient {
    private PipeTravelClient() {}

    private static final float FOV_BOOST = 1.12F;

    private static PipePath path;
    private static double progress;
    private static float speed;
    private static Vec3 push = Vec3.ZERO;
    private static int segment;
    private static LocalPlayer driven;

    public static void register() {
        NeoForge.EVENT_BUS.addListener(PipeTravelClient::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(PipeTravelClient::onFov);
        NeoForge.EVENT_BUS.addListener(PipeTravelClient::onLogout);
    }

    public static void start(PipeTravelStartPayload payload) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || payload.points().size() < 2) return;
        boolean fresh = path == null;
        path = new PipePath(payload.points());
        progress = payload.progress();
        speed = payload.speed();
        push = payload.push();
        segment = path.segmentAt(progress);
        driven = player;
        player.noPhysics = true;
        player.setForcedPose(Pose.SWIMMING);
        player.setDeltaMovement(Vec3.ZERO);
        if (fresh) {
            Minecraft.getInstance().getSoundManager().play(new TravelSound(player));
        }
    }

    public static void end(PipeTravelEndPayload payload) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (path == null || player == null) return;
        stop(player);
        player.setDeltaMovement(payload.motion());
    }

    private static void stop(LocalPlayer player) {
        path = null;
        driven = null;
        player.noPhysics = player.isSpectator();
        player.setForcedPose(null);
        player.resetFallDistance();
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (path == null || !(event.getEntity() instanceof LocalPlayer player)) return;
        if (player != driven) {
            path = null;
            driven = null;
            return;
        }
        progress = Math.min(path.length(), progress + speed);
        Vec3 pos = path.at(progress);
        player.noPhysics = true;
        player.setForcedPose(Pose.SWIMMING);
        player.resetFallDistance();
        player.setDeltaMovement(Vec3.ZERO);
        player.setPos(pos);
        int current = path.segmentAt(progress);
        if (current != segment) {
            segment = current;
            if (current < path.points().size() - 2) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(PneumaticPipes.BEND.get(),
                        0.9F + player.getRandom().nextFloat() * 0.3F, 0.35F));
            }
        }
        if (progress >= path.length()) {
            Vec3 motion = push;
            stop(player);
            player.setDeltaMovement(motion);
        }
    }

    private static void onFov(ComputeFovModifierEvent event) {
        if (path != null && event.getPlayer() == driven) {
            event.setNewFovModifier(event.getNewFovModifier() * FOV_BOOST);
        }
    }

    private static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        path = null;
        driven = null;
    }

    private static final class TravelSound extends AbstractTickableSoundInstance {
        private final LocalPlayer player;
        private int age;

        TravelSound(LocalPlayer player) {
            super(PneumaticPipes.TRAVEL.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
            this.player = player;
            this.looping = true;
            this.delay = 0;
            this.volume = 0.1F;
            this.x = player.getX();
            this.y = player.getY();
            this.z = player.getZ();
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }

        @Override
        public void tick() {
            if (player.isRemoved() || path == null || driven != player) {
                stop();
                return;
            }
            age++;
            x = player.getX();
            y = player.getY();
            z = player.getZ();
            volume = Mth.clamp(age / 8.0F, 0.1F, 0.8F);
            pitch = 1.0F + 0.15F * Mth.sin(age * 0.35F);
        }
    }
}
