package net.itsthesky.upfall.content.aerie;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.aerie.item.GravitySceptreItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public final class AerieItems {
    private AerieItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Upfall.MODID);
    static final List<DeferredItem<? extends Item>> CREATIVE_ORDER = new ArrayList<>();

    public static final DeferredItem<GravitySceptreItem> GRAVITY_SCEPTRE = ITEMS.registerItem("gravity_sceptre", GravitySceptreItem::new,
            new Item.Properties().durability(250).rarity(Rarity.EPIC));

    static {
        CREATIVE_ORDER.add(GRAVITY_SCEPTRE);
    }
}
