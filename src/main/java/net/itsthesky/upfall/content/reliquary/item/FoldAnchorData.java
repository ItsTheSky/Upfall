package net.itsthesky.upfall.content.reliquary.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FoldAnchorData(GlobalPos pos, long time) {
    public static final Codec<FoldAnchorData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GlobalPos.CODEC.fieldOf("pos").forGetter(FoldAnchorData::pos),
            Codec.LONG.fieldOf("time").forGetter(FoldAnchorData::time)).apply(instance, FoldAnchorData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, FoldAnchorData> STREAM_CODEC = StreamCodec.composite(
            GlobalPos.STREAM_CODEC, FoldAnchorData::pos,
            ByteBufCodecs.VAR_LONG, FoldAnchorData::time,
            FoldAnchorData::new);
}
