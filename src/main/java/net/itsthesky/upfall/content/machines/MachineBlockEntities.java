package net.itsthesky.upfall.content.machines;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.machines.block.entity.HasteningGearBlockEntity;
import net.itsthesky.upfall.content.machines.block.entity.RewinderBlockEntity;
import net.itsthesky.upfall.content.machines.block.entity.VoidTetherBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MachineBlockEntities {
    private MachineBlockEntities() {}

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Upfall.MODID);

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VoidTetherBlockEntity>> VOID_TETHER = BLOCK_ENTITIES.register(
            "void_tether", () -> BlockEntityType.Builder.of(VoidTetherBlockEntity::new, MachinesBlocks.VOID_TETHER.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RewinderBlockEntity>> REWINDER = BLOCK_ENTITIES.register(
            "rewinder", () -> BlockEntityType.Builder.of(RewinderBlockEntity::new, MachinesBlocks.REWINDER.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HasteningGearBlockEntity>> HASTENING_GEAR = BLOCK_ENTITIES.register(
            "hastening_gear", () -> BlockEntityType.Builder.of(HasteningGearBlockEntity::new, MachinesBlocks.HASTENING_GEAR.get()).build(null));

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, HASTENING_GEAR.get(), (gear, side) -> gear.handler());
    }
}
