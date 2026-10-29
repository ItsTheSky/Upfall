package net.itsthesky.upfall.content.aerie;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.compat.mc.McRegistration;
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

    // Lazy: 26.1.2 repairable(TagKey) writes to the item registry, which is frozen until RegisterEvent.
    public static final DeferredItem<GravitySceptreItem> GRAVITY_SCEPTRE = McRegistration.item(ITEMS, "gravity_sceptre", GravitySceptreItem::new,
            AerieItems::sceptreProperties);

    private static Item.Properties sceptreProperties() {
        Item.Properties properties = new Item.Properties().durability(250).rarity(Rarity.EPIC);
        //? if >=26.1 {
        /*properties = properties.repairable(net.itsthesky.upfall.registry.ModTags.Items.REPAIRS_LEVITITE);
        *///?}
        return properties;
    }

    static {
        CREATIVE_ORDER.add(GRAVITY_SCEPTRE);
    }
}
