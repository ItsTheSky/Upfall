package net.itsthesky.upfall.content.guide;

import net.itsthesky.upfall.Upfall;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GuideItems {
    private GuideItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Upfall.MODID);

    public static final DeferredItem<SkyAlmanacItem> SKY_ALMANAC = ITEMS.registerItem("sky_almanac", SkyAlmanacItem::new,
            new Item.Properties().stacksTo(1));
}
