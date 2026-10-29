package net.itsthesky.upfall.content.world;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.compat.mc.McRegistration;
import net.itsthesky.upfall.content.world.item.NimbusFlaskItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WorldItems {
    private WorldItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Upfall.MODID);

    public static final DeferredItem<Item> CLOUD_PEARL = ITEMS.registerSimpleItem("cloud_pearl");
    public static final DeferredItem<NimbusFlaskItem> NIMBUS_FLASK = McRegistration.item(ITEMS, "nimbus_flask", NimbusFlaskItem::new,
            new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON));
}
