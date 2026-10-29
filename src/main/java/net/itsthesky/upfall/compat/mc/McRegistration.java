package net.itsthesky.upfall.compat.mc;

import net.itsthesky.upfall.Upfall;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;
//? if <26.1 {
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
//?}

public final class McRegistration {
    private McRegistration() {}

    public static <B extends Block> DeferredBlock<B> block(DeferredRegister.Blocks reg, String name, Function<BlockBehaviour.Properties, ? extends B> factory, BlockBehaviour.Properties properties) {
        //? if >=26.1 {
        /*return reg.registerBlock(name, factory, () -> properties);
        *///?} else {
        return reg.registerBlock(name, factory, properties);
        //?}
    }

    public static <B extends Block> DeferredBlock<B> block(DeferredRegister.Blocks reg, String name, Function<BlockBehaviour.Properties, ? extends B> factory, Supplier<BlockBehaviour.Properties> properties) {
        //? if >=26.1 {
        /*return reg.registerBlock(name, factory, properties);
        *///?} else {
        return reg.register(name, () -> factory.apply(properties.get()));
        //?}
    }

    public static DeferredBlock<Block> simpleBlock(DeferredRegister.Blocks reg, String name, BlockBehaviour.Properties properties) {
        //? if >=26.1 {
        /*return reg.registerSimpleBlock(name, () -> properties);
        *///?} else {
        return reg.registerSimpleBlock(name, properties);
        //?}
    }

    public static DeferredBlock<Block> simpleBlock(DeferredRegister.Blocks reg, String name, Supplier<BlockBehaviour.Properties> properties) {
        //? if >=26.1 {
        /*return reg.registerSimpleBlock(name, properties);
        *///?} else {
        return reg.register(name, () -> new Block(properties.get()));
        //?}
    }

    public static <I extends Item> DeferredItem<I> item(DeferredRegister.Items reg, String name, Function<Item.Properties, ? extends I> factory, Item.Properties properties) {
        //? if >=26.1 {
        /*return reg.registerItem(name, factory, () -> properties);
        *///?} else {
        return reg.registerItem(name, factory, properties);
        //?}
    }

    public static <I extends Item> DeferredItem<I> item(DeferredRegister.Items reg, String name, Function<Item.Properties, ? extends I> factory, Supplier<Item.Properties> properties) {
        //? if >=26.1 {
        /*return reg.registerItem(name, factory, properties);
        *///?} else {
        return reg.register(name, () -> factory.apply(properties.get()));
        //?}
    }

    public static DeferredItem<Item> simpleItem(DeferredRegister.Items reg, String name, Item.Properties properties) {
        //? if >=26.1 {
        /*return reg.registerSimpleItem(name, () -> properties);
        *///?} else {
        return reg.registerSimpleItem(name, properties);
        //?}
    }

    public static DeferredItem<Item> simpleItem(DeferredRegister.Items reg, String name, Supplier<Item.Properties> properties) {
        //? if >=26.1 {
        /*return reg.registerSimpleItem(name, properties);
        *///?} else {
        return reg.register(name, () -> new Item(properties.get()));
        //?}
    }

    public static <I extends Item> DeferredItem<I> blockItem(DeferredRegister.Items reg, String name, Function<Item.Properties, ? extends I> factory, Item.Properties properties) {
        //? if >=26.1 {
        /*return reg.registerItem(name, factory, () -> properties.useBlockDescriptionPrefix());
        *///?} else {
        return reg.registerItem(name, factory, properties);
        //?}
    }

    public static <I extends Item> DeferredItem<I> blockItem(DeferredRegister.Items reg, String name, Function<Item.Properties, ? extends I> factory, Supplier<Item.Properties> properties) {
        //? if >=26.1 {
        /*return reg.registerItem(name, factory, () -> properties.get().useBlockDescriptionPrefix());
        *///?} else {
        return reg.register(name, () -> factory.apply(properties.get()));
        //?}
    }

    public static <I extends Item> DeferredItem<I> blockItem(DeferredRegister.Items reg, String name, Function<Item.Properties, ? extends I> factory) {
        return blockItem(reg, name, factory, new Item.Properties());
    }

    public static <T extends BlockEntity> BlockEntityType<T> blockEntityType(BlockEntityType.BlockEntitySupplier<? extends T> factory, Block... blocks) {
        //? if >=26.1 {
        /*return new BlockEntityType<>(factory, blocks);
        *///?} else {
        return BlockEntityType.Builder.<T>of(factory, blocks).build(null);
        //?}
    }

    public static <T extends net.minecraft.world.entity.Entity> EntityType<T> entityType(EntityType.Builder<T> builder, String name) {
        //? if >=26.1 {
        /*return builder.build(ResourceKey.create(Registries.ENTITY_TYPE, Upfall.id(name)));
        *///?} else {
        return builder.build(Upfall.id(name).toString());
        //?}
    }

    // 26.1.2 spawn eggs use per-item textures, background and highlight only tint the 1.21.1 egg
    public static DeferredItem<Item> spawnEgg(DeferredRegister.Items reg, String name, Supplier<? extends EntityType<? extends Mob>> type, int background, int highlight) {
        //? if >=26.1 {
        /*return reg.registerItem(name, net.minecraft.world.item.SpawnEggItem::new, () -> new Item.Properties().spawnEgg(type.get()));
        *///?} else {
        return reg.register(name, () -> new DeferredSpawnEggItem(type, background, highlight, new Item.Properties()));
        //?}
    }
}
