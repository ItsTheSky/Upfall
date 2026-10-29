package net.itsthesky.upfall.content.machines.block;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.compat.mc.TooltipSource;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;

public class GearliftBlock extends Block implements TooltipSource {
    public static final MapCodec<GearliftBlock> CODEC = simpleCodec(GearliftBlock::new);
    public static final int RANGE = 64;
    private static final int COOLDOWN_TICKS = 8;
    private static final Map<ServerPlayer, Integer> LAST_RIDE = new WeakHashMap<>();

    public GearliftBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    public static BlockPos findTarget(Level level, BlockPos from, boolean up) {
        int step = up ? 1 : -1;
        for (int i = 1; i <= RANGE; i++) {
            BlockPos candidate = from.offset(0, step * i, 0);
            if (level.isOutsideBuildHeight(candidate)) return null;
            if (level.getBlockState(candidate).getBlock() instanceof GearliftBlock && hasRoom(level, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean hasRoom(Level level, BlockPos lift) {
        return level.getBlockState(lift.above()).getCollisionShape(level, lift.above()).isEmpty()
                && level.getBlockState(lift.above(2)).getCollisionShape(level, lift.above(2)).isEmpty();
    }

    public static BlockPos below(Level level, double x, double y, double z) {
        BlockPos below = BlockPos.containing(x, y - 0.2, z);
        return level.getBlockState(below).getBlock() instanceof GearliftBlock ? below : null;
    }

    public static boolean ride(ServerPlayer player, boolean up) {
        if (!(player.level() instanceof ServerLevel level) || player.isSpectator() || player.isPassenger()) return false;
        Integer last = LAST_RIDE.get(player);
        if (last != null && player.tickCount - last < COOLDOWN_TICKS) return false;
        BlockPos from = below(level, player.getX(), player.getY(), player.getZ());
        if (from == null) from = below(level, player.getX(), player.getY() - 1.0, player.getZ());
        if (from == null) return false;
        BlockPos to = findTarget(level, from, up);
        if (to == null) return false;
        LAST_RIDE.put(player, player.tickCount);
        double y = to.getY() + 1.0;
        burst(level, player.getX(), player.getY(), player.getZ(), up ? 1.0F : 0.8F);
        player.connection.teleport(player.getX(), y, player.getZ(), player.getYRot(), player.getXRot());
        player.setDeltaMovement(0, 0, 0);
        player.resetFallDistance();
        burst(level, player.getX(), y, player.getZ(), up ? 1.3F : 1.1F);
        return true;
    }

    private static void burst(ServerLevel level, double x, double y, double z, float pitch) {
        level.sendParticles(ParticleTypes.CLOUD, x, y + 0.1, z, 8, 0.25, 0.05, 0.25, 0.02);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y + 0.5, z, 6, 0.3, 0.4, 0.3, 0.05);
        level.playSound(null, x, y, z, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.5F, pitch);
        level.playSound(null, x, y, z, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 0.8F, pitch);
    }

    @Override
    public void hoverText(ItemStack stack, Item.TooltipContext context, Consumer<Component> out, TooltipFlag flag) {
        out.accept(Component.translatable("block.upfall.gearlift.desc").withStyle(ChatFormatting.GRAY));
    }

    //? if <26.1 {
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        hoverText(stack, context, tooltip::add, flag);
    }
    //?}
}
