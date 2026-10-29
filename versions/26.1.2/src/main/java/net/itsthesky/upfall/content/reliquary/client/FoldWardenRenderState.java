package net.itsthesky.upfall.content.reliquary.client;

import net.itsthesky.upfall.content.reliquary.boss.FoldWarden;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

import java.util.List;

public class FoldWardenRenderState extends LivingEntityRenderState {
    public boolean preview;
    public int entityId;
    public FoldWarden.Phase phase = FoldWarden.Phase.AWAKEN;
    public int phaseTicks;
    public FoldWarden.Attack attack = FoldWarden.Attack.NONE;
    public int attackTicks;
    public float shutterO;
    public float shutter;
    List<FoldWyrmPass.Piece> pieces = List.of();
}
