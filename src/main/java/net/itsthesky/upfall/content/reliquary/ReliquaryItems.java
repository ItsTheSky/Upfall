package net.itsthesky.upfall.content.reliquary;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.compat.mc.McRegistration;
import net.itsthesky.upfall.content.reliquary.item.FoldAnchorItem;
import net.itsthesky.upfall.content.reliquary.item.FoldHeartItem;
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

    public static final DeferredItem<Item> FOLD_SHARD = McRegistration.item(ITEMS, "fold_shard", FoldShardItem::new, new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<FoldAnchorItem> FOLD_ANCHOR = McRegistration.item(ITEMS, "fold_anchor", FoldAnchorItem::new, ReliquaryItems::anchorProperties);
    public static final DeferredItem<FoldHeartItem> FOLD_HEART = McRegistration.item(ITEMS, "fold_heart", FoldHeartItem::new,
            new Item.Properties().stacksTo(16).rarity(Rarity.EPIC).fireResistant());
    public static final DeferredItem<Item> FOLD_WARDEN_SPAWN_EGG = McRegistration.spawnEgg(ITEMS, "fold_warden_spawn_egg", ReliquaryEntities.FOLD_WARDEN,
            0xE9E4DA, 0x5FF2FF);

    private static Item.Properties anchorProperties() {
        Item.Properties properties = new Item.Properties().durability(64).rarity(Rarity.EPIC);
        //? if >=26.1 {
        /*properties = properties.repairable(net.itsthesky.upfall.registry.ModTags.Items.REPAIRS_FOLD_ANCHOR);
        *///?}
        return properties;
    }

    static {
        CREATIVE_ORDER.add(FOLD_SHARD);
        CREATIVE_ORDER.add(FOLD_ANCHOR);
        CREATIVE_ORDER.add(FOLD_HEART);
        CREATIVE_ORDER.add(FOLD_WARDEN_SPAWN_EGG);
    }
}
