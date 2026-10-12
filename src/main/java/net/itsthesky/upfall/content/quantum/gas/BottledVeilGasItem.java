package net.itsthesky.upfall.content.quantum.gas;

import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.itsthesky.upfall.content.quantum.QuantumItems;
import net.itsthesky.upfall.content.quantum.QuantumSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

public class BottledVeilGasItem extends Item {
    public BottledVeilGasItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!level.getBlockState(pos).canBeReplaced()) {
            pos = pos.relative(context.getClickedFace());
        }
        if (!level.getBlockState(pos).isAir()) return InteractionResult.FAIL;
        if (!level.isClientSide) {
            level.setBlock(pos, QuantumBlocks.VEIL_GAS.get().defaultBlockState(), Block.UPDATE_ALL);
            level.playSound(null, pos, QuantumSounds.GAS_HISS.get(), SoundSource.BLOCKS, 0.8F, 1.0F);
            level.gameEvent(context.getPlayer(), GameEvent.FLUID_PLACE, pos);
            Player player = context.getPlayer();
            if (player != null && !player.getAbilities().instabuild) {
                player.setItemInHand(context.getHand(), ItemUtils.createFilledResult(context.getItemInHand(), player, new ItemStack(Items.GLASS_BOTTLE)));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.upfall.bottled_veil_gas.desc").withStyle(ChatFormatting.GRAY));
    }

    public static void onUseItem(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(Items.GLASS_BOTTLE)) return;
        Player player = event.getEntity();
        Level level = player.level();
        BlockPos gas = findGas(player);
        if (gas == null) return;
        if (!level.isClientSide) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), QuantumSounds.BOTTLE_FILL.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            player.setItemInHand(event.getHand(), ItemUtils.createFilledResult(stack, player, new ItemStack(QuantumItems.BOTTLED_VEIL_GAS.get())));
            level.gameEvent(player, GameEvent.FLUID_PICKUP, gas);
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
    }

    private static BlockPos findGas(Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        double reach = Math.min(player.blockInteractionRange(), 4.5);
        for (double d = 0.0; d <= reach; d += 0.25) {
            BlockPos pos = BlockPos.containing(eye.add(look.scale(d)));
            var state = player.level().getBlockState(pos);
            if (state.is(QuantumBlocks.VEIL_GAS.get())) return pos;
            if (!state.isAir() && !state.getCollisionShape(player.level(), pos).isEmpty()) return null;
        }
        return null;
    }
}
