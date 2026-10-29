package net.itsthesky.upfall.item;

import net.itsthesky.upfall.registry.ModDataComponents;
import net.itsthesky.upfall.registry.ModPoiTypes;
import net.itsthesky.upfall.worldgen.ModDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class UpdraftCompassItem extends Item {
    public static final int SEARCH_RADIUS = 160;
    private static final int REFRESH_TICKS = 20;

    public UpdraftCompassItem(Properties properties) {
        super(properties);
    }

    //? if >=26.1 {
    /*@Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, net.minecraft.world.entity.EquipmentSlot slot) {
        refresh(stack, level, entity);
    }
    *///?} else {
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level instanceof ServerLevel serverLevel) refresh(stack, serverLevel, entity);
    }
    //?}

    private void refresh(ItemStack stack, ServerLevel level, Entity entity) {
        if (level.getGameTime() % REFRESH_TICKS != 0) return;
        GlobalPos target = null;
        if (level.dimension() == ModDimensions.UPFALL) {
            target = findNearestVent(level, entity.blockPosition())
                    .map(pos -> GlobalPos.of(level.dimension(), pos))
                    .orElse(null);
        }
        if (!Objects.equals(stack.get(ModDataComponents.UPDRAFT_TARGET.get()), target)) {
            if (target == null) {
                stack.remove(ModDataComponents.UPDRAFT_TARGET.get());
            } else {
                stack.set(ModDataComponents.UPDRAFT_TARGET.get(), target);
            }
        }
    }

    public static Optional<BlockPos> findNearestVent(ServerLevel level, BlockPos from) {
        return level.getPoiManager().findClosest(type -> type.is(ModPoiTypes.UPDRAFT_VENT.getKey()), from, SEARCH_RADIUS, PoiManager.Occupancy.ANY);
    }

    //? if >=26.1 {
    /*@Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        hoverText(stack, context, tooltip, flag);
    }
    *///?} else {
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        hoverText(stack, context, tooltip::add, flag);
    }
    //?}

    private void hoverText(ItemStack stack, TooltipContext context, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.upfall.updraft_compass.desc").withStyle(ChatFormatting.GRAY));
    }
}
