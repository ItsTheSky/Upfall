package net.itsthesky.upfall.item;

import net.itsthesky.upfall.portal.HaloPortalShape;
import net.itsthesky.upfall.registry.ModSounds;
import net.itsthesky.upfall.registry.ModTriggers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class SkywardKeyItem extends Item {
    public static final int MIN_PORTAL_Y = 200;

    public SkywardKeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        Player player = context.getPlayer();
        Optional<HaloPortalShape> shape = HaloPortalShape.findEmptyPortalShape(level, pos, context.getHorizontalDirection().getClockWise().getAxis());
        if (shape.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (level.dimension() != Level.OVERWORLD || shape.get().bottomLeft().getY() <= MIN_PORTAL_Y) {
            if (player != null && !level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.upfall.key_too_low", MIN_PORTAL_Y).withStyle(ChatFormatting.LIGHT_PURPLE), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide) {
            shape.get().createPortalBlocks();
            level.playSound(null, pos, ModSounds.HALO_PORTAL_IGNITE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            if (player instanceof ServerPlayer serverPlayer) {
                ModTriggers.PORTAL_LIT.get().trigger(serverPlayer);
            }
            if (player == null || !player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.upfall.skyward_key.desc", MIN_PORTAL_Y).withStyle(ChatFormatting.GRAY));
    }
}
