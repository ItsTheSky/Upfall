package net.itsthesky.upfall.registry;

import net.itsthesky.upfall.Upfall;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class ModCreativeTabs {
    private ModCreativeTabs() {}

    public static final List<Supplier<? extends ItemLike>> EXTRA = new ArrayList<>();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Upfall.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> UPFALL = CREATIVE_TABS.register("upfall", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.upfall"))
            .icon(() -> ModBlocks.SKYGRASS_BLOCK.asItem().getDefaultInstance())
            .displayItems((params, output) -> {
                ModBlocks.CREATIVE_ORDER.forEach(item -> output.accept(item.get()));
                ModItems.CREATIVE_ORDER.forEach(item -> output.accept(item.get()));
                EXTRA.forEach(item -> output.accept(item.get()));
            })
            .build());
}
