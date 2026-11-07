package net.itsthesky.upfall.content.farming;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;

public class CloudberryCropBlock extends CropBlock {
    public static final MapCodec<CloudberryCropBlock> CODEC = simpleCodec(CloudberryCropBlock::new);

    public CloudberryCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends CropBlock> codec() {
        return CODEC;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return FarmingBlocks.CLOUDBERRY_SEEDS.get();
    }
}
