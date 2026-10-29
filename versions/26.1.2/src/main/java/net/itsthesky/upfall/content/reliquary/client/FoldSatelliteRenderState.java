package net.itsthesky.upfall.content.reliquary.client;

import net.itsthesky.upfall.content.reliquary.boss.FoldSatellite;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class FoldSatelliteRenderState extends LivingEntityRenderState {
    public int entityId;
    public FoldSatellite.Mode mode = FoldSatellite.Mode.ORBIT;
}
