package net.itsthesky.upfall.content.rustworks;

import net.itsthesky.upfall.Upfall;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RustworksItems {
    private RustworksItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Upfall.MODID);

    public static final DeferredItem<Item> KAIRITE_DUST = ITEMS.registerSimpleItem("kairite_dust");
    public static final DeferredItem<Item> KAIRITE_GEAR = ITEMS.registerSimpleItem("kairite_gear");
    public static final DeferredItem<Item> RUSTY_PLATE = ITEMS.registerSimpleItem("rusty_plate");
}
