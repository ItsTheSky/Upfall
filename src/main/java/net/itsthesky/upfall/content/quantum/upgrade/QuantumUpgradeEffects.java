package net.itsthesky.upfall.content.quantum.upgrade;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.UpfallConfig;
import net.itsthesky.upfall.compat.mc.McEntities;
import net.itsthesky.upfall.compat.mc.McLevel;
import net.itsthesky.upfall.compat.mc.McNbt;
import net.itsthesky.upfall.compat.mc.McPlayers;
import net.itsthesky.upfall.compat.mc.UseResults;
import net.itsthesky.upfall.content.chronospire.item.HourglassTracker;
import net.itsthesky.upfall.content.chronospire.item.RewindingHourglassItem;
import net.itsthesky.upfall.content.hamlet.block.AbstractRopeAnchorBlock;
import net.itsthesky.upfall.content.hamlet.block.RopeBlock;
import net.itsthesky.upfall.content.hamlet.block.ZiplineAnchorBlock;
import net.itsthesky.upfall.content.hamlet.entity.ZiplinePulley;
import net.itsthesky.upfall.content.quantum.QuantumDamageTypes;
import net.itsthesky.upfall.content.quantum.QuantumParticles;
import net.itsthesky.upfall.content.quantum.QuantumSounds;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
//? if >=26.1 {
/*import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
*///?} else {
import net.neoforged.neoforge.event.level.BlockEvent;
//?}

public final class QuantumUpgradeEffects {
    private QuantumUpgradeEffects() {}

    public static final String REWIND_READY_KEY = "upfall_rewind_ready";
    private static final ResourceLocation MOSSWALKER_MODIFIER = Upfall.id("quantum.mosswalker");

    private record Mark(UUID target, long expires) {}

    private static final Map<UUID, Mark> PENDING = new HashMap<>();
    private static final Map<UUID, Mark> LINKS = new HashMap<>();
    private static boolean sharing;
    private static boolean superposing;

    public static void register() {
        NeoForge.EVENT_BUS.addListener(QuantumUpgradeEffects::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(QuantumUpgradeEffects::onDamaged);
        NeoForge.EVENT_BUS.addListener(QuantumUpgradeEffects::onKnockBack);
        NeoForge.EVENT_BUS.addListener(QuantumUpgradeEffects::onDeath);
        NeoForge.EVENT_BUS.addListener(QuantumUpgradeEffects::onBreak);
        NeoForge.EVENT_BUS.addListener(QuantumUpgradeEffects::onPlayerTickPre);
        NeoForge.EVENT_BUS.addListener(QuantumUpgradeEffects::onPlayerTickPost);
        NeoForge.EVENT_BUS.addListener(QuantumUpgradeEffects::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(QuantumUpgradeEffects::onTooltip);
    }

    private static void onTooltip(ItemTooltipEvent event) {
        List<QuantumUpgrade> upgrades = QuantumUpgrades.get(event.getItemStack()).upgrades();
        if (upgrades.isEmpty()) return;
        List<Component> tooltip = event.getToolTip();
        int index = Math.min(1, tooltip.size());
        for (QuantumUpgrade upgrade : upgrades) {
            tooltip.add(index++, Component.translatable(upgrade.translationKey()).withStyle(upgrade.color()));
            tooltip.add(index++, Component.literal(" ").append(Component.translatable(upgrade.descriptionKey())).withStyle(ChatFormatting.GRAY));
        }
    }

    private static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        if (!(entity.level() instanceof ServerLevel level) || source.getEntity() == null || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        if (!QuantumUpgrades.equipped(entity, QuantumUpgrade.UNCERTAINTY)) return;
        if (entity.getRandom().nextDouble() >= UpfallConfig.getDouble(UpfallConfig.UNCERTAINTY_CHANCE)) return;
        event.setCanceled(true);
        level.sendParticles(QuantumParticles.VEIL_MOTE.get(), entity.getX(), entity.getY(0.5), entity.getZ(), 14, 0.35, 0.6, 0.35, 0.02);
        level.sendParticles(QuantumParticles.QUANTUM_SPARK.get(), entity.getX(), entity.getY(0.5), entity.getZ(), 10, 0.4, 0.6, 0.4, 0.05);
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), QuantumSounds.UNCERTAIN_DODGE.get(), SoundSource.PLAYERS, 0.8F,
                0.9F + entity.getRandom().nextFloat() * 0.3F);
    }

    private static void onKnockBack(LivingKnockBackEvent event) {
        if (QuantumUpgrades.equipped(event.getEntity(), QuantumUpgrade.ANCHORED)) event.setCanceled(true);
    }

    private static void onDamaged(LivingDamageEvent.Post event) {
        LivingEntity target = event.getEntity();
        DamageSource source = event.getSource();
        if (sharing || source.is(QuantumDamageTypes.ENTANGLEMENT) || !(target.level() instanceof ServerLevel level)) return;
        long now = level.getGameTime();
        LivingEntity partner = partner(level, target, now);
        //? if >=26.1 {
        /*float dealt = event.getHealthDamage();
        *///?} else {
        float dealt = event.getNewDamage();
        //?}
        if (partner != null && dealt > 0.0F) {
            float shared = (float) (dealt * UpfallConfig.getDouble(UpfallConfig.ENTANGLEMENT_SHARE));
            int invulnerable = partner.invulnerableTime;
            sharing = true;
            try {
                partner.invulnerableTime = 0;
                McEntities.hurt(partner, level.damageSources().source(QuantumDamageTypes.ENTANGLEMENT, source.getEntity()), shared);
            } finally {
                partner.invulnerableTime = Math.max(partner.invulnerableTime, invulnerable);
                sharing = false;
            }
            level.sendParticles(QuantumParticles.QUANTUM_SPARK.get(), partner.getX(), partner.getY(0.5), partner.getZ(), 6, 0.3, 0.4, 0.3, 0.02);
        }
        if (!(source.getEntity() instanceof Player player) || source.getDirectEntity() != player || target == player) return;
        if (!QuantumUpgrades.equipped(player, QuantumUpgrade.ENTANGLEMENT) || partner != null) return;
        Mark pending = PENDING.get(player.getUUID());
        Entity first = pending == null || pending.expires() < now ? null : level.getEntity(pending.target());
        if (!(first instanceof LivingEntity linked) || !linked.isAlive() || linked == target || partner(level, linked, now) != null) {
            PENDING.put(player.getUUID(), new Mark(target.getUUID(), now + UpfallConfig.getInt(UpfallConfig.ENTANGLEMENT_TICKS)));
            level.sendParticles(QuantumParticles.QUANTUM_SPARK.get(), target.getX(), target.getY(0.5), target.getZ(), 8, 0.3, 0.4, 0.3, 0.02);
            return;
        }
        PENDING.remove(player.getUUID());
        link(linked, target, now + UpfallConfig.getInt(UpfallConfig.ENTANGLEMENT_TICKS));
        Vec3 a = linked.position().add(0.0, linked.getBbHeight() * 0.5, 0.0);
        Vec3 b = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0);
        for (int i = 0; i <= 12; i++) {
            Vec3 p = a.lerp(b, i / 12.0);
            level.sendParticles(QuantumParticles.QUANTUM_SPARK.get(), p.x, p.y, p.z, 1, 0.05, 0.05, 0.05, 0.0);
        }
        level.playSound(null, target.getX(), target.getY(), target.getZ(), QuantumSounds.ENTANGLE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static void link(LivingEntity a, LivingEntity b, long expires) {
        if (LINKS.size() > 512) LINKS.values().removeIf(mark -> mark.expires() < a.level().getGameTime());
        LINKS.put(a.getUUID(), new Mark(b.getUUID(), expires));
        LINKS.put(b.getUUID(), new Mark(a.getUUID(), expires));
    }

    public static @Nullable LivingEntity partner(ServerLevel level, LivingEntity entity, long now) {
        Mark mark = LINKS.get(entity.getUUID());
        if (mark == null) return null;
        if (mark.expires() < now) {
            LINKS.remove(entity.getUUID());
            return null;
        }
        return level.getEntity(mark.target()) instanceof LivingEntity living && living.isAlive() ? living : null;
    }

    private static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getSource().is(DamageTypes.GENERIC_KILL)) return;
        if (!QuantumUpgrades.equipped(player, QuantumUpgrade.REWIND)) return;
        long clock = McLevel.server(player).overworld().getGameTime();
        if (McNbt.getLong(player.getPersistentData(), REWIND_READY_KEY) > clock) return;
        ServerLevel level = McLevel.serverLevel(player);
        HourglassTracker.Footing footing = HourglassTracker.rewind(player.getUUID(), level.dimension(), level.getGameTime(), RewindingHourglassItem.MIN_AGE);
        if (footing == null) return;
        event.setCanceled(true);
        player.getPersistentData().putLong(REWIND_READY_KEY, clock + UpfallConfig.getInt(UpfallConfig.REWIND_COOLDOWN));
        player.setHealth((float) (player.getMaxHealth() * UpfallConfig.getDouble(UpfallConfig.REWIND_HEALTH)));
        player.clearFire();
        Vec3 from = player.position();
        level.sendParticles(QuantumParticles.QUANTUM_SPARK.get(), from.x, from.y + 1.0, from.z, 30, 0.3, 0.6, 0.3, 0.05);
        player.teleportTo(footing.pos().x, footing.pos().y, footing.pos().z);
        player.resetFallDistance();
        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;
        player.invulnerableTime = 40;
        level.sendParticles(QuantumParticles.QUANTUM_SPARK.get(), footing.pos().x, footing.pos().y + 1.0, footing.pos().z, 30, 0.3, 0.6, 0.3, 0.05);
        level.playSound(null, footing.pos().x, footing.pos().y, footing.pos().z, QuantumSounds.REWIND.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        McPlayers.overlay(player, Component.translatable("message.upfall.rewind.triggered"));
        HourglassTracker.clear(player.getUUID());
    }

    //? if >=26.1 {
    /*private static void onBreak(BreakBlockEvent event) {
    *///?} else {
    private static void onBreak(BlockEvent.BreakEvent event) {
    //?}
        Player player = event.getPlayer();
        BlockState state = event.getState();
        if (superposing || !(player instanceof ServerPlayer serverPlayer) || !(event.getLevel() instanceof ServerLevel level)) return;
        if (!state.is(Tags.Blocks.ORES) || !QuantumUpgrades.equipped(player, QuantumUpgrade.SUPERPOSITION)) return;
        for (Direction direction : Direction.values()) {
            BlockPos other = event.getPos().relative(direction);
            if (!level.getBlockState(other).is(state.getBlock())) continue;
            superposing = true;
            try {
                if (serverPlayer.gameMode.destroyBlock(other)) {
                    level.sendParticles(QuantumParticles.QUANTUM_SPARK.get(), other.getX() + 0.5, other.getY() + 0.5, other.getZ() + 0.5, 12, 0.3, 0.3, 0.3, 0.02);
                }
            } finally {
                superposing = false;
            }
            return;
        }
    }

    public static boolean ignoresMist(Entity entity) {
        return entity instanceof Player player && !player.isShiftKeyDown() && QuantumUpgrades.equipped(player, QuantumUpgrade.SOLIDIFIER);
    }

    private static boolean isMist(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(ModBlocks.MIST.get());
    }

    private static void onPlayerTickPre(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (!ignoresMist(player)) return;
        Level level = player.level();
        if (!isMist(level, player.blockPosition()) && !isMist(level, BlockPos.containing(player.getX(), player.getY() - 0.2, player.getZ()))) return;
        player.resetFallDistance();
        if (!level.isClientSide() || !player.isLocalPlayer()) return;
        Vec3 motion = player.getDeltaMovement();
        if (motion.y >= 0.0) return;
        double bounce = Math.max(UpfallConfig.getDouble(UpfallConfig.SOLIDIFIER_MIN_VELOCITY), -motion.y * UpfallConfig.getDouble(UpfallConfig.SOLIDIFIER_FACTOR));
        player.setDeltaMovement(motion.x, bounce, motion.z);
    }

    private static void onPlayerTickPost(PlayerTickEvent.Post event) {
        if (!event.getEntity().level().isClientSide()) updateMosswalker(event.getEntity());
    }

    public static void updateMosswalker(Player player) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;
        boolean onMoss = QuantumUpgrades.equipped(player, QuantumUpgrade.MOSSWALKER) && player.onGround()
                && (player.level().getBlockState(player.blockPosition()).is(ModTags.Blocks.MOSSWALKER_BLOCKS)
                || player.level().getBlockState(player.getOnPos()).is(ModTags.Blocks.MOSSWALKER_BLOCKS));
        if (!onMoss) {
            if (speed.hasModifier(MOSSWALKER_MODIFIER)) speed.removeModifier(MOSSWALKER_MODIFIER);
            return;
        }
        double amount = UpfallConfig.getDouble(UpfallConfig.MOSSWALKER_SPEED);
        AttributeModifier current = speed.getModifier(MOSSWALKER_MODIFIER);
        if (current == null || current.amount() != amount) {
            speed.addOrUpdateTransientModifier(new AttributeModifier(MOSSWALKER_MODIFIER, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (event.getHand() != InteractionHand.MAIN_HAND || !player.getMainHandItem().isEmpty() || player.isPassenger()) return;
        Block block = event.getLevel().getBlockState(event.getPos()).getBlock();
        boolean anchor = block instanceof ZiplineAnchorBlock;
        if (!anchor && !(block instanceof RopeBlock) && !(block instanceof AbstractRopeAnchorBlock)) return;
        if (!QuantumUpgrades.equipped(player, QuantumUpgrade.ZIPLINE)) return;
        event.setCanceled(true);
        event.setCancellationResult(UseResults.sided(event.getLevel()));
        if (event.getLevel().isClientSide()) return;
        ZiplinePulley.Result result = anchor ? ZiplinePulley.rideFromAnchor(player, event.getPos()) : ZiplinePulley.latchNearest(player);
        if (result != ZiplinePulley.Result.STARTED) McPlayers.overlay(player, Component.translatable(result.message()));
    }

    public static void latchFromAir(ServerPlayer player) {
        if (player.isPassenger() || !player.getMainHandItem().isEmpty() || !QuantumUpgrades.equipped(player, QuantumUpgrade.ZIPLINE)) return;
        ZiplinePulley.Result result = ZiplinePulley.latchNearest(player);
        if (result != ZiplinePulley.Result.STARTED && result != ZiplinePulley.Result.NO_LINE) {
            McPlayers.overlay(player, Component.translatable(result.message()));
        }
    }
}
