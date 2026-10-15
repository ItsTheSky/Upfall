package net.itsthesky.upfall.registry;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.reliquary.item.FoldAnchorData;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    private ModDataComponents() {}

    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Upfall.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GlobalPos>> UPDRAFT_TARGET = DATA_COMPONENTS.registerComponentType(
            "updraft_target", builder -> builder.persistent(GlobalPos.CODEC).networkSynchronized(GlobalPos.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FoldAnchorData>> FOLD_ANCHOR = DATA_COMPONENTS.registerComponentType(
            "fold_anchor", builder -> builder.persistent(FoldAnchorData.CODEC).networkSynchronized(FoldAnchorData.STREAM_CODEC));
}
