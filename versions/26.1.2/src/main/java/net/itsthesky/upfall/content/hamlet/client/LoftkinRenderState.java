package net.itsthesky.upfall.content.hamlet.client;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public class LoftkinRenderState extends LivingEntityRenderState {
    public int entityId;
    public int unhappyCounter;
    public boolean onZipline;
    public boolean hasParasol;
    public int variant;
    public final AnimationState waveAnimation = new AnimationState();
}
