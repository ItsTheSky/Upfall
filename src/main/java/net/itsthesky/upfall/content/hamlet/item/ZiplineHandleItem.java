package net.itsthesky.upfall.content.hamlet.item;

import net.itsthesky.upfall.content.hamlet.entity.ZiplinePulley;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class ZiplineHandleItem extends Item {
    public ZiplineHandleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isPassenger()) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide) {
            ZiplinePulley.Result result = ZiplinePulley.latchNearest(player);
            if (result != ZiplinePulley.Result.STARTED) {
                player.displayClientMessage(Component.translatable(result.message()), true);
                return InteractionResultHolder.fail(stack);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.upfall.zipline_handle.desc").withStyle(ChatFormatting.GRAY));
    }
}
