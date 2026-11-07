package net.itsthesky.upfall.content.farming;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.compat.mc.McRegistration;
import net.itsthesky.upfall.registry.ModCreativeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
//? if >=26.1 {
/*import net.minecraft.world.item.BlockItem;
*///?} else {
import net.minecraft.world.item.ItemNameBlockItem;
//?}

import static net.itsthesky.upfall.registry.UpfallTab.NATURE;

public final class FarmingBlocks {
    private FarmingBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Upfall.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Upfall.MODID);

    public static final DeferredBlock<CloudberryCropBlock> CLOUDBERRY_CROP = McRegistration.block(BLOCKS, "cloudberry_crop",
            CloudberryCropBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks()
                    .instabreak().sound(SoundType.CROP).pushReaction(PushReaction.DESTROY));

    //? if >=26.1 {
    /*public static final DeferredItem<BlockItem> CLOUDBERRY_SEEDS = ModCreativeTabs.add(McRegistration.item(ITEMS, "cloudberry_seeds",
            props -> new BlockItem(CLOUDBERRY_CROP.get(), props), () -> new Item.Properties().useItemDescriptionPrefix()), NATURE);
    *///?} else {
    public static final DeferredItem<ItemNameBlockItem> CLOUDBERRY_SEEDS = ModCreativeTabs.add(McRegistration.item(ITEMS, "cloudberry_seeds",
            props -> new ItemNameBlockItem(CLOUDBERRY_CROP.get(), props), new Item.Properties()), NATURE);
    //?}
}
