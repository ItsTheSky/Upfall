package net.itsthesky.upfall.content.machines;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.compat.mc.McItemHandlers;
import net.itsthesky.upfall.compat.mc.McRegistration;
import net.itsthesky.upfall.content.machines.block.entity.HasteningGearBlockEntity;
import net.itsthesky.upfall.content.machines.block.entity.RewinderBlockEntity;
import net.itsthesky.upfall.content.machines.block.entity.VoidTetherBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MachineBlockEntities {
    private MachineBlockEntities() {}

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Upfall.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VoidTetherBlockEntity>> VOID_TETHER = BLOCK_ENTITIES.register(
            "void_tether", () -> McRegistration.blockEntityType(VoidTetherBlockEntity::new, MachinesBlocks.VOID_TETHER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RewinderBlockEntity>> REWINDER = BLOCK_ENTITIES.register(
            "rewinder", () -> McRegistration.blockEntityType(RewinderBlockEntity::new, MachinesBlocks.REWINDER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HasteningGearBlockEntity>> HASTENING_GEAR = BLOCK_ENTITIES.register(
            "hastening_gear", () -> McRegistration.blockEntityType(HasteningGearBlockEntity::new, MachinesBlocks.HASTENING_GEAR.get()));

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        McItemHandlers.registerSink(event, HASTENING_GEAR.get(), HasteningGearBlockEntity::handler);
    }
}
