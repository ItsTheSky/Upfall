package net.itsthesky.upfall.content.reliquary;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.reliquary.item.FoldAnchorItem;
import net.itsthesky.upfall.content.reliquary.item.FoldShardItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public final class ReliquaryItems {
    private ReliquaryItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Upfall.MODID);
    static final List<DeferredItem<? extends Item>> CREATIVE_ORDER = new ArrayList<>();

    public static final DeferredItem<Item> FOLD_SHARD = ITEMS.registerItem("fold_shard", FoldShardItem::new, new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<FoldAnchorItem> FOLD_ANCHOR = ITEMS.registerItem("fold_anchor", FoldAnchorItem::new,
            new Item.Properties().durability(64).rarity(Rarity.EPIC));

    static {
        CREATIVE_ORDER.add(FOLD_SHARD);
        CREATIVE_ORDER.add(FOLD_ANCHOR);
    }
}
