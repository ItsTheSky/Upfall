package net.itsthesky.upfall.gameplay;

import net.itsthesky.upfall.registry.ModSounds;
import net.itsthesky.upfall.registry.ModTags;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class ArmorSetBonuses {
    private ArmorSetBonuses() {}

    public static final float NIMBUSITE_FALL_DAMAGE_MULTIPLIER = 0.25F;
    public static final double DOUBLE_JUMP_VELOCITY = 0.95;
    public static final double DOUBLE_JUMP_FORWARD_BOOST = 0.75;
    public static final double DOUBLE_JUMP_MAX_HORIZONTAL_SPEED = 0.9;
    public static final double GLIDE_FALL_SPEED = -0.12;
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final Map<UUID, Long> LAST_DOUBLE_JUMP = new WeakHashMap<>();

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ArmorSetBonuses::onFall);
    }

    public static boolean hasFullSet(LivingEntity entity, TagKey<Item> armorTag) {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (!entity.getItemBySlot(slot).is(armorTag)) return false;
        }
        return true;
    }

    public static boolean hasLevititeSet(LivingEntity entity) {
        return hasFullSet(entity, ModTags.Items.LEVITITE_ARMOR);
    }

    private static void onFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();
        if (hasLevititeSet(entity)) {
            event.setDistance(0.0F);
            event.setCanceled(true);
        } else if (hasFullSet(entity, ModTags.Items.NIMBUSITE_ARMOR)) {
            event.setDamageMultiplier(event.getDamageMultiplier() * NIMBUSITE_FALL_DAMAGE_MULTIPLIER);
        }
    }

    public static Vec3 doubleJumpMotion(Vec3 motion, float yRot, float forwardInput, boolean inverted) {
        double boost = DOUBLE_JUMP_FORWARD_BOOST * Mth.clamp(forwardInput, 0.0F, 1.0F);
        float yaw = yRot * Mth.DEG_TO_RAD;
        double x = motion.x * 1.15 - Mth.sin(yaw) * boost;
        double z = motion.z * 1.15 + Mth.cos(yaw) * boost;
        double horizontal = Math.sqrt(x * x + z * z);
        if (horizontal > DOUBLE_JUMP_MAX_HORIZONTAL_SPEED) {
            x *= DOUBLE_JUMP_MAX_HORIZONTAL_SPEED / horizontal;
            z *= DOUBLE_JUMP_MAX_HORIZONTAL_SPEED / horizontal;
        }
        return new Vec3(x, inverted ? -DOUBLE_JUMP_VELOCITY : DOUBLE_JUMP_VELOCITY, z);
    }

    public static void onDoubleJump(ServerPlayer player) {
        if (!hasLevititeSet(player) || player.onGround() || player.isSpectator()) return;
        long now = player.level().getGameTime();
        Long last = LAST_DOUBLE_JUMP.get(player.getUUID());
        if (last != null && now - last < 8) return;
        LAST_DOUBLE_JUMP.put(player.getUUID(), now);
        player.resetFallDistance();
        ServerLevel level = player.serverLevel();
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.DOUBLE_JUMP.get(), SoundSource.PLAYERS, 0.7F, 1.0F);
        level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY(), player.getZ(), 8, 0.3, 0.05, 0.3, 0.02);
    }
}
