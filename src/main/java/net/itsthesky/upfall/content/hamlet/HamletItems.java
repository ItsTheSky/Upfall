package net.itsthesky.upfall.content.hamlet;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.hamlet.item.ZiplineHandleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class HamletItems {
    private HamletItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Upfall.MODID);
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Upfall.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> ROPE_LINK_START = DATA_COMPONENTS.registerComponentType(
            "rope_link_start", builder -> builder.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC));

    public static final DeferredItem<ZiplineHandleItem> ZIPLINE_HANDLE = ITEMS.registerItem("zipline_handle", ZiplineHandleItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<DeferredSpawnEggItem> LOFTKIN_SPAWN_EGG = ITEMS.registerItem("loftkin_spawn_egg",
            p -> new DeferredSpawnEggItem(HamletEntities.LOFTKIN, 0xF2EEF5, 0xE8B4C0, p), new Item.Properties());
}
