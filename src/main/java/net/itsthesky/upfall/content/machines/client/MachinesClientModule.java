package net.itsthesky.upfall.content.machines.client;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.UpfallClientModule;
import net.itsthesky.upfall.content.chronospire.ChronoClock;
import net.itsthesky.upfall.content.machines.GearliftPayload;
import net.itsthesky.upfall.content.machines.MachinesItems;
import net.itsthesky.upfall.content.machines.block.GearliftBlock;
import net.itsthesky.upfall.content.machines.item.PhaseWatchItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

public class MachinesClientModule implements UpfallClientModule {
    private static boolean jumpWasDown;
    private static boolean sneakWasDown;

    @Override
    public void register(IEventBus modEventBus) {
        modEventBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() ->
                ItemProperties.register(MachinesItems.PHASE_WATCH.get(), Upfall.id("phase"), (ClampedItemPropertyFunction) (stack, level, entity, seed) -> {
                    var world = level != null ? level : Minecraft.getInstance().level;
                    if (world == null || !PhaseWatchItem.hasSignal(world)) return 0.0F;
                    return 0.2F * (ChronoClock.phase(world).ordinal() + 1);
                })));
        NeoForge.EVENT_BUS.addListener(MachinesClientModule::onClientTick);
    }

    private static void onClientTick(ClientTickEvent.Pre event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        var options = Minecraft.getInstance().options;
        boolean jump = options.keyJump.isDown();
        boolean sneak = options.keyShift.isDown();
        if (player.onGround() && !player.isSpectator()
                && GearliftBlock.below(player.level(), player.getX(), player.getY(), player.getZ()) != null) {
            if (jump && !jumpWasDown) {
                PacketDistributor.sendToServer(new GearliftPayload(true));
            } else if (sneak && !sneakWasDown) {
                PacketDistributor.sendToServer(new GearliftPayload(false));
            }
        }
        jumpWasDown = jump;
        sneakWasDown = sneak;
    }
}
